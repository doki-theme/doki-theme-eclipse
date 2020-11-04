const fs = require("fs");
const path = require("path");
const crypto = require("crypto");
const aws = require("aws-sdk");

export const rootDirectory =
  path.join(__dirname, '..', '..');

export const assetDirectory =
  path.resolve(rootDirectory, 'plugin-update-site');

export const assetDirectories = [
  'plugin-update-site'
];

export async function walkDir(dir: string): Promise<string[]> {
  const values: Promise<string[]>[] = fs.readdirSync(dir)
    .map((file: string) => {
      const dirPath: string = path.join(dir, file);
      const isDirectory = fs.statSync(dirPath).isDirectory();
      if (isDirectory) {
        return walkDir(dirPath);
      } else {
        return Promise.resolve([path.join(dir, file)]);
      }
    });
  const scannedDirectories = await Promise.all(values);
  return scannedDirectories.reduce((accum, files) => accum.concat(files), []);
}

export function createChecksum(data: Buffer | string): string {
  return crypto.createHash('md5')
    .update(data)
    .digest('hex');
}

export interface StringDictionary<T> {
  [key: string]: T
}

export const BUCKET_NAME = 'eclipse-plugin-repo';

export const buildS3Client = () => {
  aws.config.update({region: 'us-east-1'});
  return new aws.S3();
};

export const syncedAssetsJson = path.join(__dirname, "..", "syncedAssets.json")

export const getSyncedAssets = () => JSON.parse(
  fs.readFileSync(syncedAssetsJson, 'utf-8'));
