package io.unthrottled.doki.theme.themes;

import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.assets.AssetManager;
import io.unthrottled.doki.theme.definitions.Sticker;

import static io.unthrottled.doki.theme.definitions.ThemeConstants.Stickers.SECONDARY_STICKER;
import static io.unthrottled.doki.theme.preferences.PreferenceConstants.STICKER_TYPE_PREFERENCE;

public class StickerService {

  private static StickerService instance;

  private StickerService() {
  }

  public static StickerService getInstance() {
    if (instance == null) {
      instance = new StickerService();
    }

    return instance;
  }

  public String getCurrentStickerUrl() {
    var preferenceStore = Activator.getDefault().getPreferenceStore();
    var themeStickers = ThemeManager.getInstance().getCurrentTheme().getStickers();
    var preferredStickerType = preferenceStore.getString(STICKER_TYPE_PREFERENCE);
    if (SECONDARY_STICKER.equals(preferredStickerType) &&
      themeStickers.getSecondary() != null) {
      return resolveStickerUrl(themeStickers.getSecondary());
    }

    return resolveStickerUrl(themeStickers.getPrimary());
  }

  private String resolveStickerUrl(Sticker sticker) {
    return AssetManager.getInstance().resolveAssetUrl(
      "/stickers/vscode" + sticker.getPath()
    );
  }
}
