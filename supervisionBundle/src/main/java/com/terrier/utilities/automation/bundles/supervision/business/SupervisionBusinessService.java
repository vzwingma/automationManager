/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.business;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;

/**
 * Supervision Service
 * @author vzwingma
 *
 */
@Singleton
public class SupervisionBusinessService extends AbstractAutomationService {


	private static final Logger LOGGER = LoggerFactory.getLogger( SupervisionBusinessService.class );
	// Message Handler
	@Inject private BundlesEventsHandler eventsBundlesHander;
	/**
	 * Initialisation
	 */
	@PostConstruct
	public void initService(){

		Dictionary<String, String[]> props = new Hashtable<String, String[]>();
		String[] listeTopics = new String[]{
				EventsTopicNameEnum.BUNDLE_EVENTS.getTopicName(),
				EventsTopicNameEnum.SERVICE_EVENTS.getTopicName()
				};
		props.put(EventConstants.EVENT_TOPIC, listeTopics);
		LOGGER.info("Enregistrement de l'eventHandler {} sur les topics : {}", eventsBundlesHander, Arrays.asList(listeTopics));
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(EventHandler.class.getName(), eventsBundlesHander , props);
	}

	
	@Override
	public void notifyUpdateDictionary() {
		// Rien car il n'y a pas de fichier de configuration associé		
	}

}
