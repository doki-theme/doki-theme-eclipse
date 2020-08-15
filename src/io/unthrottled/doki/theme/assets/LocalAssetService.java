package io.unthrottled.doki.theme.assets;

import com.google.common.hash.Hashing;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.unthrottled.doki.theme.Activator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

enum AssetChangedStatus {
  SAME, DIFFERENT, LUL_DUNNO
}

public class LocalAssetService {
  private static final ILog logger = Platform.getLog(Activator.getDefault().getBundle());
  private static LocalAssetService instance;

  public static LocalAssetService getInstance() {
    if (instance == null) {
      instance = new LocalAssetService();
    }

    return instance;
  }

  private LocalAssetService() {
  }

  private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
  private final Map<String, Instant> assetChecks = readPreviousAssetChecks();

  public boolean hasAssetChanged(
      Path localInstallPath,
      String remoteAssetUrl
  ) {
    return !Files.exists(localInstallPath) ||
        (!hasBeenCheckedToday(localInstallPath) &&
            isLocalDifferentFromRemote(localInstallPath, remoteAssetUrl) == AssetChangedStatus.DIFFERENT);
  }

  private String getOnDiskCheckSum(Path localAssetPath) {
    try {
      return computeCheckSum(Files.readAllBytes(localAssetPath));
    } catch (Throwable e) {
      logger.warn("Unable to calculate local checksum for "+localAssetPath.toAbsolutePath(), e);
      return "lol not going to match";
    }
  }

  @SuppressWarnings("deprecation")
  private String computeCheckSum(byte[] byteArray) {
    return Hashing.md5().hashBytes(byteArray).toString();
  }

  private Optional<String> getRemoteAssetChecksum(String remoteAssetUrl) {
    return RestClient.getInstance().performGet(remoteAssetUrl + ".checksum.txt");
  }

  private AssetChangedStatus isLocalDifferentFromRemote(
      Path localInstallPath,
      String remoteAssetUrl
  ) {
    return getRemoteAssetChecksum(remoteAssetUrl)
        .map(remoteChecksum -> {
              writeCheckedDate(localInstallPath);
              var onDiskCheckSum = getOnDiskCheckSum(localInstallPath);
              if (remoteChecksum.equals(onDiskCheckSum)) {
                return AssetChangedStatus.SAME;
              } else {
	              logger.warn("\n" +
	                "Local asset: " +localInstallPath +
	                "\nis different from remote asset " +remoteAssetUrl +
		              "\nLocal Checksum: " + onDiskCheckSum +
		              "\nRemote Checksum: " + remoteChecksum);
                return AssetChangedStatus.DIFFERENT;
              }
            }
        ).orElse(AssetChangedStatus.LUL_DUNNO);
  }

  private boolean hasBeenCheckedToday(Path localInstallPath) {
    Instant dateChecked = assetChecks.get(getAssetCheckKey(localInstallPath));
		return dateChecked != null && dateChecked.truncatedTo(ChronoUnit.DAYS).equals(
				Instant.now().truncatedTo(ChronoUnit.DAYS)
				);
  }

  private void writeCheckedDate(Path localInstallPath) {
    assetChecks.put(getAssetCheckKey(localInstallPath), Instant.now());
    getAssetChecksFile()
        .ifPresent(checksFile -> {
              LocalStorageService.getInstance().createDirectoriesIfNeeded(checksFile.getParent());
              try (var writer = Files.newBufferedWriter(
                  checksFile, Charset.defaultCharset(),
                  StandardOpenOption.CREATE,
                  StandardOpenOption.TRUNCATE_EXISTING
              )) {
                writer.write(gson.toJson(assetChecks));
              } catch (Throwable e) {
              	logger.warn("Unable to write checked date for asset " + localInstallPath.toAbsolutePath(), e);
              }
            }
        );
  }

  private Map<String, Instant> readPreviousAssetChecks() {
    try {
      return getAssetChecksFile()
          .filter(Files::exists)
          .flatMap(it -> {
            try (var reader = Files.newBufferedReader(it)) {
              return Optional.ofNullable(gson.fromJson(reader,
                  new TypeToken<Map<String, Instant>>() {}.getType()
              ));
            } catch (Throwable e) {
            	logger.warn("Unable to read previous asset checks for raisins.", e);
              return Optional.<Map<String, Instant>>empty();
            }
          }).orElseGet(HashMap::new);
    } catch (Throwable e) {
      logger.warn("Unable to get local asset checks for raisins", e);
      return new HashMap<>();
    }
  }

  private Optional<Path> getAssetChecksFile() {
    return Optional.of(Paths.get(
        LocalStorageService.getInstance().getAssetPath().toAbsolutePath().toString(),
        "assetChecks.json"
    ));
  }

  private String getAssetCheckKey(Path localInstallPath) {
    return localInstallPath.toAbsolutePath().toString();
  }
}

