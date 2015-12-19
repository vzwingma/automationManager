/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.business;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.supervision.listeners.AutomationBundlesListener;

/**
 * Supervision Service
 * @author vzwingma
 *
 */
@Singleton
public class SupervisionBusinessService extends AbstractAutomationService {


	private static final Logger LOGGER = LoggerFactory.getLogger( SupervisionBusinessService.class );
	// Message Handler
	@Inject private AutomationBundlesListener automationBundlesListener;

	/**
	 * Initialisation
	 */
	@PostConstruct
	public void initService(){

		Dictionary<String, String[]> props = new Hashtable<String, String[]>();
		String[] listeTopics = new String[]{
				"org/osgi/framework/BundleEvent/",
				"org/osgi/framework/ServiceEvent/"
				};
		props.put(EventConstants.EVENT_TOPIC, listeTopics);
		//LOGGER.info("Enregistrement de l'eventHandler {} sur les topics : {}", eventsBundlesHander, Arrays.asList(listeTopics));
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().addBundleListener(automationBundlesListener);
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().addServiceListener(automationBundlesListener);
	}

	
	@Override
	public void notifyUpdateDictionary() {
		// Rien car il n'y a pas de fichier de configuration associé		
	}


	@Override
	public void updateSupervisionEvents(Map<String, Object> supervisionEvents) {
		// TODO Auto-generated method stub
		
	}

}
