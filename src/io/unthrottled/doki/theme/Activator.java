package io.unthrottled.doki.theme;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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
	}

	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}
	
	public static Activator getDefault() {
		return activator;
	}
}
