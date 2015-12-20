/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.listeners;

import javax.inject.Singleton;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.supervision.communs.OSGIStatusUtils;

/**
 * Logger des événements sur les bundles et les services
 * @author vzwingma
 *
 */
@Singleton
public class AutomationBundlesListener implements BundleListener, ServiceListener {


	private static final Logger LOGGER = LoggerFactory.getLogger( BundlesEventsHandler.class );

	/* (non-Javadoc)
	 * @see org.osgi.framework.ServiceListener#serviceChanged(org.osgi.framework.ServiceEvent)
	 */
	@Override
	public void serviceChanged(ServiceEvent event) {
		LOGGER.debug("ServiceEvent :: {}", OSGIStatusUtils.getServiceStatusLibelle(event.getType()));
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleListener#bundleChanged(org.osgi.framework.BundleEvent)
	 */
	@Override
	public void bundleChanged(BundleEvent event) {
		LOGGER.info("BundleEvent :: {} {}", event.getOrigin(), OSGIStatusUtils.getBundleStatusLibelle(event.getType()));
	}

}
