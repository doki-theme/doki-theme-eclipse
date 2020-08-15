package io.unthrottled.doki.theme.parts;

import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.themes.StickerService;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import javax.annotation.PostConstruct;

import static io.unthrottled.doki.theme.preferences.PreferenceConstants.CURRENT_THEME_PREFERENCE;
import static io.unthrottled.doki.theme.preferences.PreferenceConstants.STICKER_TYPE_PREFERENCE;

public class StickerView {
  private Label stickerDisplayLabel;

  @PostConstruct
  public void createPartControl(Composite parent) {
    var preferenceStore = Activator.getDefault().getPreferenceStore();
    preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent changeEvent) {
        if (CURRENT_THEME_PREFERENCE.equals(changeEvent.getProperty()) ||
            STICKER_TYPE_PREFERENCE.equals(changeEvent.getProperty())) {
          var imageUrl = StickerService.getInstance().getCurrentStickerUrl();
          stickerDisplayLabel.setImage(new Image(parent.getDisplay(), imageUrl));
        }
      }
    });
    stickerDisplayLabel = new Label(parent, SWT.BORDER);
    Image image = new Image(parent.getDisplay(),
        StickerService.getInstance().getCurrentStickerUrl());
    stickerDisplayLabel.setImage(image);
  }
}
