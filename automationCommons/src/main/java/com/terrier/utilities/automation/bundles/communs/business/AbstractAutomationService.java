/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.business;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventPropertyNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.TypeMessagingEnum;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;

/**
 * Classe d'un service
 * @author vzwingma
 *
 */
public abstract class AbstractAutomationService implements ManagedService, Runnable {


	private final Logger LOGGER = LoggerFactory.getLogger( this.getClass() );

	// Dictionnaire
	private Dictionary<String, String> dictionnaire;
	
	private String configPID;
	/**
	 * Démarrage du service
	 */

	/**
	 * Enregistrement aux modifications du fichier de configuration
	 * @param configPID nom du fichier de configuration
	 */
	public void registerToConfig(String configPID){
		LOGGER.info("Enregistrement de la surveillance du fichier de configuration : {}", configPID);
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		this.configPID = configPID;
		properties.put(Constants.SERVICE_PID, this.configPID);
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(ManagedService.class.getName(), this , properties);
		LOGGER.info("Chargement du fichier de configuration /etc/{}.cfg", this.configPID);
	}



	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if(properties != null){
			LOGGER.info("Mise à jour du fichier de configuration {}", this.configPID);
			this.dictionnaire = (Dictionary<String, String>)properties;
			notifyUpdateDictionary();
			sendNotificationMessage(TypeMessagingEnum.SMS, "Configuration", "Mise à jour du fichier de configuration /etc/"+ this.configPID +".cfg");
		}
		else{
			LOGGER.error("Impossible de trouver le fichier de configuration");
		}
	}


	/**
	 * Notification de Mise à jour du dictionnaire
	 * Pour être notifié, il est nécessaire d'appeler la méthode registerToConfig(String configPID)
	 */
	public abstract void notifyUpdateDictionary(); 
	

	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<EventAdmin> ref = context.getServiceReference(EventAdmin.class);
        if (ref != null)  {
            EventAdmin eventAdmin = context.getService(ref);
            LOGGER.debug("BundleContext {} / ServiceReference {} / EventAdmin {}", context, ref, eventAdmin);
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            
            Map<String, Object> statusBundle = new HashMap<>();
            updateSupervisionEvents(statusBundle);
            properties.put("STATUS", statusBundle);
            properties.put(EventPropertyNameEnum.TIME.name(), System.currentTimeMillis());
            Event reportGeneratedEvent = new Event(EventsTopicNameEnum.SUPERVISION_EVENTS.getTopicName(), properties);
            LOGGER.debug("Envoi des status sur le topic [{}]", EventsTopicNameEnum.SUPERVISION_EVENTS.getTopicName());
            eventAdmin.sendEvent(reportGeneratedEvent);
            LOGGER.trace("Message envoyé sur le topic");
        }
        else{
        	LOGGER.error("Erreur lors de la recherche de l'EventAdmin dans le bundleContext");
        }
	}



	/**
	 * Envoi d'un message pour publication
	 * @param message message à envoyer
	 */
	public abstract void updateSupervisionEvents(Map<String, Object> supervisionEvents);
	
	
	/**
	 * Envoi d'un message pour publication
	 * @param message message à envoyer
	 */
	public void sendNotificationMessage(TypeMessagingEnum typeMessage, String titreMessage, String message)
    {
		BundleContext context = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
        ServiceReference<EventAdmin> ref = context.getServiceReference(EventAdmin.class);
        if (ref != null)  {
            EventAdmin eventAdmin = context.getService(ref);
            LOGGER.debug("BundleContext {} / ServiceReference {} / EventAdmin {}", context, ref, eventAdmin);
            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            properties.put(EventPropertyNameEnum.TITRE_MESSAGE.name(), titreMessage);
            properties.put(EventPropertyNameEnum.MESSAGE.name(), message);
            properties.put(EventPropertyNameEnum.TIME.name(), System.currentTimeMillis());
            properties.put(EventPropertyNameEnum.TYPE_MESSAGE.name(), typeMessage);
            Event reportGeneratedEvent = new Event(EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName(), properties);
            LOGGER.debug("Envoi du message [{}] sur le topic [{}]", message, EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName());
            eventAdmin.sendEvent(reportGeneratedEvent);
            LOGGER.trace("Message envoyé sur le topic");
        }
        else{
        	LOGGER.error("Erreur lors de la recherche de l'EventAdmin dans le bundleContext");
        }
    }
	
	/**
	 * @param key clé à charger du fichier
	 * @return valeur de la clé dans la configuration
	 */
	public String getConfig(String key) throws KeyNotFoundException{
		if(this.dictionnaire != null && key != null){
			return this.dictionnaire.get(key);
		}
		else{
			throw new KeyNotFoundException(key);
		}
	}
}
