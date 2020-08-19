package io.unthrottled.doki.theme.parts;

import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.themes.StickerService;
import io.unthrottled.doki.theme.themes.ThemeManager;

import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import javax.annotation.PostConstruct;

import static io.unthrottled.doki.theme.preferences.PreferenceConstants.CURRENT_THEME_PREFERENCE;
import static io.unthrottled.doki.theme.preferences.PreferenceConstants.STICKER_TYPE_PREFERENCE;

public class StickerView {
  private Label stickerDisplayLabel;

  @PostConstruct
  public void createPartControl(Composite parent, IThemeEngine themeEngine) {
    var preferenceStore = Activator.getDefault().getPreferenceStore();
    preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent changeEvent) {
        boolean themeChanged = CURRENT_THEME_PREFERENCE.equals(changeEvent.getProperty());
				if (themeChanged ||
            STICKER_TYPE_PREFERENCE.equals(changeEvent.getProperty())) {
          var imageUrl = StickerService.getInstance().getCurrentStickerUrl();
          stickerDisplayLabel.setImage(new Image(parent.getDisplay(), imageUrl));
        }
				
				if(themeChanged) {
					ThemeManager.getInstance().getTheme((String)changeEvent.getNewValue())
        	.ifPresent(dokiTheme -> {        
        		themeEngine.setTheme(dokiTheme.getCSSId(),true);
        	});
				}
      }
    });
    stickerDisplayLabel = new Label(parent, SWT.BORDER);
    Image image = new Image(parent.getDisplay(),
        StickerService.getInstance().getCurrentStickerUrl());
    stickerDisplayLabel.setImage(image);
  }
}
