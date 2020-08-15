package io.unthrottled.doki.theme.preferences;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.assets.LocalStorageService;
import io.unthrottled.doki.theme.definitions.ThemeConstants;

public class PreferenceInitializer extends AbstractPreferenceInitializer {
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
