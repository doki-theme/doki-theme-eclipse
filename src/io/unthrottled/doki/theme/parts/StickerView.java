package io.unthrottled.doki.theme.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import io.unthrottled.doki.theme.Activator;
import io.unthrottled.doki.theme.preferences.PreferenceConstants;

public class StickerView {
	private Label stickerDisplayLabel;

	@PostConstruct
	public void createPartControl(Composite parent) {
		var preferenceStore = Activator.getDefault().getPreferenceStore();
		preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent changeEvent) {
				if (PreferenceConstants.CURRENT_THEME_PREFERENCE.equals(changeEvent.getProperty())) {
					var themeId = (String) changeEvent.getNewValue();
					var imageUrl = getStickerUrl(themeId);
					stickerDisplayLabel.setImage(new Image(parent.getDisplay(), imageUrl));
				}
			}
		});
		stickerDisplayLabel = new Label(parent, SWT.BORDER);
		Image image = new Image(parent.getDisplay(),
				getStickerUrl(preferenceStore.getString(PreferenceConstants.CURRENT_THEME_PREFERENCE)));
		stickerDisplayLabel.setImage(image);
	}

	protected String getStickerUrl(String themeId) {
		switch (themeId) {
		case "3a78b13e-dbf2-410f-bb20-12b57bff7735":
			return "/C:/Users/birdm.DESKTOP-FO92PV5/AppData/Roaming/Hyper/.doki-theme-hyper-config/stickers/killLaKill/satsuki/satsuki.png";
		default:
			return "/C:/Users/birdm.DESKTOP-FO92PV5/AppData/Roaming/Hyper/.doki-theme-hyper-config/stickers/killLaKill/ryuko/ryuko.png";
		}
	}
}
