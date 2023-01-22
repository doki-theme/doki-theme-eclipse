import fs from "fs";
import path from "path";
import FileType from "file-type";
import {
  assetDirectories, assetDirectory,
  BUCKET_NAME,
  buildS3Client,
  createChecksum,
  getSyncedAssets,
  rootDirectory,
  StringDictionary, syncedAssetsJson,
  walkDir
} from "./AssetTools";

const syncedAssets: StringDictionary<string> = getSyncedAssets();

function buildKey(filePath: string): string {
  return `doki-theme/${filePath.substr(assetDirectory.length + 1).replace('\\','/')}`;
}

const s3 = buildS3Client();

const uploadUnsyncedAssets = (
  workToBeDone: [string, string][]
): Promise<[string, string][]> => {
  const next = workToBeDone.pop();
  if (next) {
    const [filePath] = next;
    return FileType.fromFile(filePath)
      .then(fileType => {
        if (!fileType && filePath.endsWith(".xml")) {
          return { mime: "application/xml" };
        }

        if (!fileType && (filePath.endsWith(".map") || filePath.endsWith(".txt") )) {
          return { mime: "text/plain" };
        }

        if (!fileType && filePath.endsWith(".css")) {
          return { mime: "text/css" };
        }

        if (!fileType && filePath.endsWith(".html")) {
          return { mime: "text/html" };
        }

        if (!fileType && filePath.endsWith(".json")) {
          return { mime: "application/json" };
        }


        if (!fileType && filePath.endsWith(".js")) {
          return { mime: "application/javascript" };
        }

        if(!fileType){
          throw Error(`File ${filePath} does not have a type!!`);
        }

        return fileType;
      })
      .then(fileType => {
        return new Promise<boolean>(res => {
          const fileStream = fs.createReadStream(filePath);
          fileStream.on("error", err => {
            console.warn(
              `Unable to open stream for ${next} for raisins ${err}`
            );
            res(false);
          });

          const key = buildKey(filePath);
          console.info(`Uploading ${filePath} with key ${key}`);
          s3.upload(
            {
              Bucket: BUCKET_NAME,
              Key: key,
              Body: fileStream,
              ACL: "public-read",
              ContentType: fileType?.mime
            },
            (err: any) => {
              if (err) {
                console.warn(
                  `Unable to upload ${next} to s3 for raisins ${err}`
                );
                res(false);
              } else {
                console.log(`Uploaded ${next}`);
                res(true);
              }
            }
          );
        }).then(workResult =>
          uploadUnsyncedAssets(workToBeDone).then(others => {
            if (workResult) {
              others.push(next);
            }
            return others;
          })
        );
      });
  } else {
    return Promise.resolve([]);
  }
};

console.log("Starting asset upload.");

const getUpdatePluginXmls = (): Promise<string[]> =>
  Promise.resolve(
    fs
      .readdirSync(rootDirectory)
      .filter(patho => patho.startsWith("updatePlugins"))
      .map(p => path.resolve(".", p))
  );

const scanDirectories = () => {
  console.log("Scanning asset directories");
  return [
    ...assetDirectories.map(directory =>
      walkDir(path.join(rootDirectory, directory))
    ),
    getUpdatePluginXmls()
  ];
};

Promise.all(scanDirectories())
  .then(directories =>
    directories.reduce((accum, dirs) => accum.concat(dirs), [])
  )
  .then(allAssets => {
    console.log("Calculating differences");
    return Promise.all(
      allAssets.map(assetPath =>
        new Promise<Buffer>((res, rej) =>
          fs.readFile(assetPath, (err, dat) => {
            if (err) {
              rej(err);
            } else {
              res(dat);
            }
          })
        )
          .then(createChecksum)
          .then(checkSum => ({
            assetPath,
            checkSum
          }))
      )
    ).then(assetToCheckSums =>
      assetToCheckSums.reduce(
        (accum: StringDictionary<string>, assetToChecksum) => {
          accum[assetToChecksum.assetPath] = assetToChecksum.checkSum;
          return accum;
        },
        {}
      )
    );
  })
  .then(assetToCheckSum => {
    console.log("Calculating Deltas");
    return Object.keys(assetToCheckSum)
      .filter(assetPath => {
        const assetKey = buildKey(assetPath);
        return (
          !syncedAssets[assetKey] ||
          syncedAssets[assetKey] !== assetToCheckSum[assetPath]
        );
      })
      .map(changedAsset => ({
        key: changedAsset,
        value: assetToCheckSum[changedAsset]
      }))
      .reduce((accum: StringDictionary<string>, kv) => {
        console.log(`${kv.key} is new or changed`);
        accum[kv.key] = kv.value;
        return accum;
      }, {});
  })
  .then(allNewAssets => {
    console.log("Staring asset sync");
    return uploadUnsyncedAssets(Object.entries(allNewAssets))
      .then(syncedAssets =>
        syncedAssets
          .map(([key, value]) => [buildKey(key), value])
          .reduce((accum: StringDictionary<string>, kva) => {
            const [key, value] = kva;
            accum[key] = value;
            return accum;
          }, {})
      )
      .then(syncedAssetDictionary => {
        fs.writeFileSync(
          syncedAssetsJson,
          JSON.stringify(
            {
              ...syncedAssets,
              ...syncedAssetDictionary
            },
            null,
            2
          ),
          "utf8"
        );
        console.log("Asset sync complete!");
      });
  });

