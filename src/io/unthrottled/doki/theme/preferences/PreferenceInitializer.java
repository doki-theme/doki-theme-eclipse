package io.unthrottled.doki.theme.preferences;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import io.unthrottled.doki.theme.assets.LocalStorageService;
import io.unthrottled.doki.theme.definitions.ThemeConstants;
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
		LocalStorageService.getInstance().createDirectoriesIfNeeded(assetsDirectory);
		store.setDefault(PreferenceConstants.ASSET_PATH_PREFERENCE, assetsDirectory.toString());
		store.setDefault(PreferenceConstants.STICKER_TYPE_PREFERENCE, ThemeConstants.Stickers.PRIMARY_STICKER);
		store.setDefault(PreferenceConstants.CURRENT_THEME_PREFERENCE, ThemeConstants.Themes.DEFAULT_THEME_ID);
	}


}
