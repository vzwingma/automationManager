package com.terrier.utilities.automation.bundles.communs.business;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.IAutomationTopicPropertyNamesEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;



/**
 * Publisher vers un topic
 * @author vzwingma
 *
 * @param <T> nom du topic
 * @param <T> liste des propriétés du message
 */
public class AutomationEventPublisher<T extends IAutomationTopicPropertyNamesEnum> {



	private final Logger logger = LoggerFactory.getLogger( this.getClass() );

	private static final Logger LOG_COMMON = LoggerFactory.getLogger( AutomationEventPublisher.class );

	/**
	 * Publication vers un topic
	 * @param topic enum du nom du topic
	 * @param propertiesMessages 
	 */
	public void publishToTopic(EventsTopicNameEnum topic, Map<T, Object> propertiesMessages){

		EventAdmin eventAdmin = getEventAdmin();
		if (eventAdmin != null)  {
			Event toTopicEvent = createEvent(topic, propertiesMessages);
			if(toTopicEvent != null){
				logger.debug("Envoi du message sur le topic [{}]", topic.getTopicName());
				try{
					eventAdmin.sendEvent(toTopicEvent);
					logger.trace("Message envoyé sur le topic");
				}
				catch(Exception e){
					logger.error("Erreur lors de l'envoi des status", e);
				}
			}
			else{
				logger.warn("Le message ne contient aucune propriété. Annulation de l'envoi sur le topic {}", topic.getTopicName());
			}
		}
	}

	/**
	 * Création d'un événement
	 * @param topic topic 
	 * @param propertiesMessages propriétés du message
	 * @return event event à envoyer. Null si erreur ou données incorrectes
	 */
	public static Event createEvent(EventsTopicNameEnum topic, Map<?  extends IAutomationTopicPropertyNamesEnum, Object> propertiesMessages){
		Dictionary<String, Object> properties = new Hashtable<>();
		// Transformation des properties messages en propertiestotopic
		if(propertiesMessages != null && !propertiesMessages.isEmpty()){
			for (Entry<?  extends IAutomationTopicPropertyNamesEnum, Object> property : propertiesMessages.entrySet()) {
				if(property.getKey().getClass().equals(topic.getEnumPropertyName())){
					properties.put(property.getKey().getName(), property.getValue());	
				}
				else{
					LOG_COMMON.warn("L'enum de la clé {} ne correspond pas au type de l'enum {} du topic {}. Cette propriété est ignorée", property.getKey().getName(), topic.getEnumPropertyName(), topic.getTopicName());
				}
			}
		}
		// Envoi réellement s'il y a  des propriétés
		if(!properties.isEmpty()){
			return new Event(topic.getTopicName(), properties);
		}
		else{
			LOG_COMMON.warn("Le message ne contient aucune propriété.");
			return null;
		}
	}


	/**
	 * @return l'eventAdmin utilisé pour faire des envois
	 */
	protected EventAdmin getEventAdmin(){

		Bundle bundle = FrameworkUtil.getBundle(this.getClass());
		if(bundle != null){
			try{
				BundleContext context = bundle.getBundleContext();
				ServiceReference<EventAdmin> ref = context.getServiceReference(EventAdmin.class);
				EventAdmin eventAdmin = context.getService(ref);
				logger.debug("BundleContext {} / ServiceReference {} / EventAdmin {}", context, ref, eventAdmin);
				return eventAdmin;
			}
			catch(Exception e){
				logger.error("Erreur lors de la recherche de l'EventAdmin dans le bundleContext [{}]", bundle.getBundleContext(), e);
			}
		}
		else{
			logger.warn("Le bundle est introuvable. Dans le cadre de TU, cette méthode doit être mockée.");
		}
		return null;
	}
}
