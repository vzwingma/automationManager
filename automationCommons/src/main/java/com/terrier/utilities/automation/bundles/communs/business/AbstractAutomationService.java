/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.business;

import java.util.Dictionary;
import java.util.Hashtable;

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
public abstract class AbstractAutomationService implements ManagedService {


	private static final Logger LOGGER = LoggerFactory.getLogger( AbstractAutomationService.class );

	// Dictionnaire
	private Dictionary<String, String> dictionnaire;
	/**
	 * Démarrage du service
	 */

	/**
	 * Enregistrement aux modifications du fichier de configuration
	 * @param configPID nom du fichier de configuration
	 */
	public void registerToConfig(String configPID){
		LOGGER.info("Enregistrement au fichier de configuration : {}", configPID);
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(Constants.SERVICE_PID, configPID);
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(ManagedService.class.getName(), this , properties);
		LOGGER.info("Chargement du fichier de configuration /etc/{}.cfg", configPID);
	}



	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if(properties != null){
			LOGGER.info("Mise à jour du fichier de configuration");
			this.dictionnaire = (Dictionary<String, String>)properties;
			notifyUpdateDictionnary();
		}
		else{
			LOGGER.error("Impossible de trouver le fichier de configuration");
		}
	}


	/**
	 * Notification de Mise à jour du dictionnaire
	 * Pour être notifié, il est nécessaire d'appeler la méthode registerToConfig(String configPID)
	 */
	public abstract void notifyUpdateDictionnary(); 
	

	
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

            Dictionary<String, Object> properties = new Hashtable<String, Object>();
            properties.put(EventPropertyNameEnum.TITRE_MESSAGE.name(), titreMessage);
            properties.put(EventPropertyNameEnum.MESSAGE.name(), message);
            properties.put(EventPropertyNameEnum.TIME.name(), System.currentTimeMillis());
            properties.put(EventPropertyNameEnum.TYPE_MESSAGE.name(), typeMessage);
            Event reportGeneratedEvent = new Event(EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName(), properties);
            LOGGER.debug("Envoi du message [{}] sur le topic [{}]", message, EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName());
            eventAdmin.sendEvent(reportGeneratedEvent);
        }
        else{
        	LOGGER.error("Erreur lors de la recherche de l'EventAdmin");
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
