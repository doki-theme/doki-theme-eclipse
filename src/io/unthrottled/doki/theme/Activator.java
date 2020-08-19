package io.unthrottled.doki.theme;

import static io.unthrottled.doki.theme.preferences.PreferenceConstants.CURRENT_THEME_PREFERENCE;
import static io.unthrottled.doki.theme.preferences.PreferenceConstants.STICKER_TYPE_PREFERENCE;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import io.unthrottled.doki.theme.themes.StickerService;
import io.unthrottled.doki.theme.themes.ThemeManager;

public class Activator extends AbstractUIPlugin implements BundleActivator {

	private static BundleContext context;
	private static Activator activator;

	static BundleContext getContext() {
		return context;
	}

	public Activator() {
		super();
		activator = this;
	}

	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		
		var preferenceStore = getPreferenceStore();
    preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
      @Override
      public void propertyChange(PropertyChangeEvent changeEvent) {
        if (CURRENT_THEME_PREFERENCE.equals(changeEvent.getProperty())) {
        	ThemeManager.getInstance().getTheme((String)changeEvent.getNewValue())
        	.ifPresent(dokiTheme -> {        		
        		PlatformUI.getWorkbench().getThemeManager().setCurrentTheme(
        				dokiTheme.getCSSId());
        	});;
        }
      }
    });
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	
	public static Activator getDefault() {
		return activator;
	}
}
