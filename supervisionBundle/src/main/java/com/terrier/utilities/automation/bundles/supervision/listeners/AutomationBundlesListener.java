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

import com.terrier.utilities.automation.bundles.communs.model.StatutBundleTopicObject;
import com.terrier.utilities.automation.bundles.supervision.business.BundleSupervisionBusinessService;
import com.terrier.utilities.automation.bundles.supervision.communs.OSGIStatusUtils;

/**
 * Logger des événements sur les bundles et les services
 * @author vzwingma
 *
 */
@Singleton
public class AutomationBundlesListener implements BundleListener, ServiceListener {


	private static final Logger LOGGER = LoggerFactory.getLogger( AutomationBundlesListener.class );

	
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
		LOGGER.info("BundleEvent :: {} {}", event.getOrigin(), OSGIStatusUtils.getBundleStatusEventLibelle(event.getType()));
		StatutBundleTopicObject obj = BundleSupervisionBusinessService.getStatutBundles().get(event.getOrigin().getBundleId());
		if(obj == null){
			LOGGER.warn("Création du statutBundleTopicObject pour {}", event.getOrigin().getBundleId());
			obj = new StatutBundleTopicObject(event.getBundle());
		}
		obj.setBundle(event.getOrigin());
		if(event.getOrigin().getHeaders().get("Bundle-Name").contains("Automation")){
			BundleSupervisionBusinessService.getStatutBundles().put(event.getOrigin().getBundleId(), obj);
		}
	}
}