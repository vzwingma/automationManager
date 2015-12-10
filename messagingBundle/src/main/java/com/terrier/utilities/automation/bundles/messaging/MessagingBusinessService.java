package com.terrier.utilities.automation.bundles.messaging;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;

import org.apache.log4j.Logger;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.messaging.enums.MessagingConfigKeyEnums;

/**
 * Classe de service de messaging
 * @author vzwingma
 *
 */
@Singleton
public class MessagingBusinessService extends AbstractAutomationService {



	private static final Logger LOGGER = Logger.getLogger( MessagingBusinessService.class );
	
	
	@Inject private MessageEventHandler eventMessages;
	
	private boolean configValid;
	
	/**
	 * Initialisation
	 */
	@PostConstruct
	public void initService(){
		registerToConfig("com.terrier.utilities.automation.bundles.messaging");
		
		LOGGER.info("Enregistrement de l'eventHandler " + eventMessages + " sur le topic : " + EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName());
		Dictionary<String, String[]> props = new Hashtable<String, String[]>();
        props.put(EventConstants.EVENT_TOPIC, new String[]{EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName()});
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(EventHandler.class.getName(), eventMessages , props);
	}
	
	
	
	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#notifyUpdateDictionnary()
	 */
	@Override
	public void notifyUpdateDictionnary() {
		configValid = validateConfig();
	}
	

	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfig(){

		LOGGER.info("**  **");
		LOGGER.info(" > URL du service	: " + getConfig(MessagingConfigKeyEnums.EMAIL_URL));
		LOGGER.info(" > Domaine du service	: " + getConfig(MessagingConfigKeyEnums.EMAIL_DOMAIN));
		LOGGER.info(" > Service	du service : " + getConfig(MessagingConfigKeyEnums.EMAIL_SERVICE));
		LOGGER.info(" > Destinataires	: " + getConfig(MessagingConfigKeyEnums.EMAIL_DESTINATAIRES));

		boolean configValid = true;
		for (MessagingConfigKeyEnums configKey : MessagingConfigKeyEnums.values()) {
			configValid &= getConfig(configKey) != null;	
		}
		if(!configValid){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
		}
		return configValid;
	}

	
	
	
	/**
	 * 
	 * @param titre titre du mail
	 * @param message message du mail
	 * @return le résulat de l'envoi
	 */
	public boolean sendNotificationEmail(String titre, String message){
		if(configValid){
		    Client client = getClient();
		    client.addFilter(new HTTPBasicAuthFilter("api", getConfig(MessagingConfigKeyEnums.EMAIL_KEY)));
		    WebResource webResource =
		        client.resource(getConfig(MessagingConfigKeyEnums.EMAIL_URL) + getConfig(MessagingConfigKeyEnums.EMAIL_DOMAIN) + getConfig(MessagingConfigKeyEnums.EMAIL_SERVICE));
		    MultivaluedMapImpl formData = getFormData(titre, message);
		    Builder b = webResource.type(MediaType.APPLICATION_FORM_URLENCODED);
		    System.out.println(b);
			ClientResponse response = b.post(ClientResponse.class, formData);
			LOGGER.info("Resultat : " + response);
			return response != null && response.getStatus() == 200;
		}
		else{
			LOGGER.error("Impossible d'envoyer l'email ["+titre+"]["+message+"] à cause d'une erreur de configuration");
			return false;
		}
	}
	
	
	/**
	 * Création d'un client HTTP à part pour être mocké
	 * @return HTTPClient
	 */
	protected Client getClient(){
		return Client.create();
	}
	
	/**
	 * Prépare les données
	 * @param titre
	 * @param message
	 * @return formData pour l'email
	 */
	private MultivaluedMapImpl getFormData(String titre, String message) {
	    MultivaluedMapImpl formData = new MultivaluedMapImpl();
	    formData.add("from", "Automation Messaging Service <postmaster@"+getConfig(MessagingConfigKeyEnums.EMAIL_DOMAIN)+">");
	    formData.add("to", getConfig(MessagingConfigKeyEnums.EMAIL_DESTINATAIRES));
	    formData.add("subject", titre);
	    formData.add("text", message);
	    return formData;
	}
	/**
	 * @param key clé
	 * @return valeur dans la config correspondante
	 */
	protected String getConfig(MessagingConfigKeyEnums key){
		try {
			if(key != null){
				return super.getConfig(key.getCodeKey());
			}
		} catch (KeyNotFoundException e) {
			LOGGER.error("La clé "+key+" est introuvable");
		}
		return null;
	}
}
