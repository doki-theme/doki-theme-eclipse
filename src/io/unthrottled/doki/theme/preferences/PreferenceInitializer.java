package io.unthrottled.doki.theme.preferences;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import io.unthrottled.doki.theme.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
	private static final ILog logger = Platform.getLog(Activator.getDefault().getBundle());
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		Path assetsDirectory = Paths.get(System.getProperty("user.home"), ".eclipse", "dokiThemeAssets")
				.toAbsolutePath();
		createDirectoriesIfNeeded(assetsDirectory);
		store.setDefault(PreferenceConstants.ASSET_PATH_PREFERENCE, assetsDirectory.toString());
		store.setDefault(PreferenceConstants.CURRENT_THEME_PREFERENCE, "19b65ec8-133c-4655-a77b-13623d8e97d3");
	}

	private void createDirectoriesIfNeeded(Path assetsDirectory) {
		if (!Files.exists(assetsDirectory)) {
			try {
				Files.createDirectories(assetsDirectory);
			} catch (IOException e) {
				logger.error("Unable to create asset directories!", e);
			}
		}
	}
}
