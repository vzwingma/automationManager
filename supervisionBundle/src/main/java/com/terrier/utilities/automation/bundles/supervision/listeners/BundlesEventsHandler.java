/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.listeners;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Singleton;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.messaging.StatusPropertyNameEnum;

/**
 * Event Message Handler
 * @author vzwingma
 *
 */
@Singleton
public class BundlesEventsHandler implements EventHandler {


	private static final Logger LOGGER = LoggerFactory.getLogger( BundlesEventsHandler.class );


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
}
