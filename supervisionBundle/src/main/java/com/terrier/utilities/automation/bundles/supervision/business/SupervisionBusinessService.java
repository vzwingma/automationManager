/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.business;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.StatusPropertyNameEnum;
import com.terrier.utilities.automation.bundles.supervision.listeners.AutomationBundlesListener;

/**
 * Supervision Service
 * @author vzwingma
 *
 */
@Singleton
public class SupervisionBusinessService extends AbstractAutomationService implements EventHandler {


	private static final Logger LOGGER = LoggerFactory.getLogger( SupervisionBusinessService.class );
	// Message Handler
	@Inject private AutomationBundlesListener automationBundlesListener;


	private static final Map<String, Map<StatusPropertyNameEnum, Object>> MAP_SUPERVISION_BUNDLE = new HashMap<>();

	/**
	 * Initialisation
	 */
	@PostConstruct
	public void initService(){

		Dictionary<String, String[]> props = new Hashtable<String, String[]>();
		String[] listeTopics = new String[]{EventsTopicNameEnum.SUPERVISION_EVENTS.getTopicName()};
		props.put(EventConstants.EVENT_TOPIC, listeTopics);
		LOGGER.info("Enregistrement de l'eventHandler {} sur le topic : {}", this, EventsTopicNameEnum.SUPERVISION_EVENTS.getTopicName());
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(EventHandler.class.getName(), this , props);
		
		// Supervision des bundles et des services
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().addBundleListener(automationBundlesListener);
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().addServiceListener(automationBundlesListener);
	}

	
	@Override
	public void notifyUpdateDictionary() {
		// Rien car il n'y a pas de fichier de configuration associé		
	}


	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#updateSupervisionEvents(java.util.Map)
	 */
	@Override
	public void updateSupervisionEvents(Map<String, Object> supervisionEvents) { }


	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	@Override
	public void handleEvent(Event event) {
		LOGGER.debug("Topic [{}] Réception du message [{}]", event.getTopic(), event);

		Map<StatusPropertyNameEnum, Object> statut = new HashMap<>();

		for (String propertyName : event.getPropertyNames()) {
			StatusPropertyNameEnum statutKey = StatusPropertyNameEnum.getEnumFromName(propertyName);
			statut.put(statutKey, event.getProperty(propertyName));
		}
		MAP_SUPERVISION_BUNDLE.put((String)statut.get(StatusPropertyNameEnum.BUNDLE), statut);
		LOGGER.info(logStatut(statut));
	}

	/**
	 * Affichage du statut
	 * @param statutBundle statut du bundle reçu
	 */
	protected String logStatut(Map<StatusPropertyNameEnum, Object> statutBundle){

		StringBuilder log = new StringBuilder("\n> Statut de ").append(statutBundle.get(StatusPropertyNameEnum.BUNDLE)).append("\n");
		if(statutBundle.get(StatusPropertyNameEnum.STATUS) != null){
			@SuppressWarnings("unchecked")
			Map<String, Object> statutMap = (Map<String, Object>) statutBundle.get(StatusPropertyNameEnum.STATUS);
			for (Entry<String, Object> statutEntry : statutMap.entrySet()) {
				log.append("     ").append(statutEntry.getKey()).append(" :: ").append(statutEntry.getValue()).append("\n");	
			}

		}
		return log.toString();
	}
	
	/**
	 * @return le statut des bundles
	 */
	public static final Map<String, Map<StatusPropertyNameEnum, Object>> getStatutBundles(){
		return MAP_SUPERVISION_BUNDLE;
	}
}
