package io.unthrottled.doki.theme.assets;

import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.preferences.PreferenceConstants;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalStorageService {

  private static final ILog logger = Platform.getLog(Activator.getDefault().getBundle());

  private static LocalStorageService instance;

  public static LocalStorageService getInstance() {
    if (instance == null) {
      instance = new LocalStorageService();
    }

    return instance;
  }

  private LocalStorageService() {
  }

  public Path getAssetPath() {
    return Paths.get(
        Activator.getDefault().getPreferenceStore().getString(
            PreferenceConstants.ASSET_PATH_PREFERENCE
        )
    );
  }

  public void createDirectoriesIfNeeded(Path assetsDirectory) {
    if (!Files.exists(assetsDirectory)) {
      try {
        Files.createDirectories(assetsDirectory);
      } catch (IOException e) {
        logger.error("Unable to create asset directories!", e);
      }
    }
  }
}
