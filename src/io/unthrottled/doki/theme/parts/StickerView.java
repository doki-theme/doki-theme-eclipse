package io.unthrottled.doki.theme.parts;

import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.themes.StickerService;
import io.unthrottled.doki.theme.themes.ThemeManager;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import javax.annotation.PostConstruct;

import static io.unthrottled.doki.theme.preferences.PreferenceConstants.*;

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
    var rowLayout = new RowLayout();
    rowLayout.wrap = true;
    rowLayout.pack = true;
    rowLayout.justify = false;
    rowLayout.type = SWT.HORIZONTAL;
    parent.setLayout(rowLayout);
    var parent1 = parent.getParent();
    Image image = new Image(parent.getDisplay(),
      StickerService.getInstance().getCurrentStickerUrl());
    stickerDisplayLabel = new Label(parent, SWT.COLOR_INFO_BACKGROUND);

    var p = new Composite(parent, SWT.COLOR_INFO_BACKGROUND);
    stickerDisplayLabel.setText("Shit");
    parent1.addPaintListener(pe -> {
      p.setLayoutData(new RowData(0,0));
      p.setBounds(0,0,0,0);
      p.setSize(0,0);
      stickerDisplayLabel.setBackground(p.getBackground());
      parent1.setBackground(p.getBackground());
    });
    stickerDisplayLabel.setImage(image);
  }
}
