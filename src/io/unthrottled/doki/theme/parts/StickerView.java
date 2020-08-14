package io.unthrottled.doki.theme.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.IServiceConstants;
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
	private Label myLabelInView;

	@PostConstruct
	public void createPartControl(Composite parent) {
		Activator.getDefault().getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent changeEvent) {
				if (PreferenceConstants.CURRENT_THEME_PREFERENCE.equals(changeEvent.getProperty())) {
					var themeId = (String) changeEvent.getNewValue();
					var imageUrl = geStickerUrl(themeId);
					myLabelInView.setImage(new Image(parent.getDisplay(), imageUrl));
				}
			}
		});
		myLabelInView = new Label(parent, SWT.BORDER);
		Image image = new Image(parent.getDisplay(),
				"/C:/Users/birdm.DESKTOP-FO92PV5/AppData/Roaming/Hyper/.doki-theme-hyper-config/stickers/killLaKill/ryuko/ryuko.png");
		myLabelInView.setImage(image);
	}

	protected String geStickerUrl(String themeId) {
		switch(themeId) {
		case "3a78b13e-dbf2-410f-bb20-12b57bff7735": 
			return "/C:/Users/birdm.DESKTOP-FO92PV5/AppData/Roaming/Hyper/.doki-theme-hyper-config/stickers/killLaKill/satsuki/satsuki.png";
		  default:
			  return "/C:/Users/birdm.DESKTOP-FO92PV5/AppData/Roaming/Hyper/.doki-theme-hyper-config/stickers/killLaKill/ryuko/ryuko.png";
		}
	}

	@Focus
	public void setFocus() {

	}

	/**
	 * This method is kept for E3 compatiblity. You can remove it if you do not mix
	 * E3 and E4 code. <br/>
	 * With E4 code you will set directly the selection in ESelectionService and you
	 * do not receive a ISelection
	 * 
	 * @param s the selection received from JFace (E3 mode)
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) ISelection s) {
		if (s == null || s.isEmpty())
			return;

		if (s instanceof IStructuredSelection) {
			IStructuredSelection iss = (IStructuredSelection) s;
			if (iss.size() == 1)
				setSelection(iss.getFirstElement());
			else
				setSelection(iss.toArray());
		}
	}

	/**
	 * This method manages the selection of your current object. In this example we
	 * listen to a single Object (even the ISelection already captured in E3 mode).
	 * <br/>
	 * You should change the parameter type of your received Object to manage your
	 * specific selection
	 * 
	 * @param o : the current object received
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object o) {
		// Remove the 2 following lines in pure E4 mode, keep them in mixed mode
		if (o instanceof ISelection) // Already captured
			return;

	}

	/**
	 * This method manages the multiple selection of your current objects. <br/>
	 * You should change the parameter type of your array of Objects to manage your
	 * specific selection
	 * 
	 * @param o : the current array of objects received in case of multiple
	 *          selection
	 */
	@Inject
	@Optional
	public void setSelection(@Named(IServiceConstants.ACTIVE_SELECTION) Object[] selectedObjects) {

	}
}
