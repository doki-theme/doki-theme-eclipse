package io.unthrottled.doki.theme.parts;

import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.themes.StickerService;
import io.unthrottled.doki.theme.themes.ThemeManager;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import javax.annotation.PostConstruct;

import static io.unthrottled.doki.theme.preferences.PreferenceConstants.AUTO_SET_THEME;
import static io.unthrottled.doki.theme.preferences.PreferenceConstants.CURRENT_THEME_PREFERENCE;
import static io.unthrottled.doki.theme.preferences.PreferenceConstants.STICKER_TYPE_PREFERENCE;

public class StickerView {
  private Label stickerDisplayLabel;

  @PostConstruct
  public void createPartControl(Composite parent, IThemeEngine themeEngine) {
    var preferenceStore = Activator.getDefault().getPreferenceStore();
    preferenceStore.addPropertyChangeListener(changeEvent -> {
      boolean themeChanged = CURRENT_THEME_PREFERENCE.equals(changeEvent.getProperty());
      if (themeChanged ||
        STICKER_TYPE_PREFERENCE.equals(changeEvent.getProperty())) {
        var imageUrl = StickerService.getInstance().getCurrentStickerUrl();
        stickerDisplayLabel.setImage(new Image(parent.getDisplay(), imageUrl));
      }

      if ((themeChanged && Activator.getDefault().getPreferenceStore().getBoolean(AUTO_SET_THEME))) {
        ThemeManager.getInstance().getTheme((String) changeEvent.getNewValue())
          .ifPresent(dokiTheme -> themeEngine.setTheme(dokiTheme.getCSSId(), true));
      } else if (AUTO_SET_THEME.equals(changeEvent.getProperty()) && Boolean.TRUE.equals(changeEvent.getNewValue())) {
        ThemeManager.getInstance().getTheme(preferenceStore.getString(CURRENT_THEME_PREFERENCE))
          .ifPresent(dokiTheme -> themeEngine.setTheme(dokiTheme.getCSSId(), true));
      }
    });

    parent.setLayout(buildLayout());
    Image image = new Image(parent.getDisplay(),
    StickerService.getInstance().getCurrentStickerUrl());
    stickerDisplayLabel = new Label(parent, SWT.LEFT | SWT.TOP);
    stickerDisplayLabel.setImage(image);
  }

  private RowLayout buildLayout() {
    var rowLayout = new RowLayout();
    rowLayout.wrap = true;
    rowLayout.pack = true;
    rowLayout.justify = false;
    rowLayout.type = SWT.HORIZONTAL;
    rowLayout.fill = false;
    return rowLayout;
  }
}
