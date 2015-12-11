package com.terrier.utilities.automation.bundles.messaging;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

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
	
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
	
	/**
	 * Liste des tâches schedulées
	 */
	private ScheduledFuture<?> sendEmailScheduled;
	
	
	// Message Handler
	@Inject private MessageEventHandler eventMessages;
	
	/**
	 * Flag de validation
	 */
	private boolean configValid;
	
	// Période d'envoi
	private Long periodeEnvoiMail;
	
	/**
	 * Liste 
	 */
	private Map<String, List<String>> messagesSendingQueue = new ConcurrentHashMap<String, List<String>>();
	
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
		// Validation de la config
		configValid = validateConfig();
		// Si correct, reprogrammation de la tâche d'envoi
		if(configValid){
			// arrêt des tâches schedulées
			sendEmailScheduled.cancel(true);
			String apiURL = getConfig(MessagingConfigKeyEnums.EMAIL_URL) + getConfig(MessagingConfigKeyEnums.EMAIL_DOMAIN) + getConfig(MessagingConfigKeyEnums.EMAIL_SERVICE);
			sendEmailScheduled = scheduledThreadPool.scheduleAtFixedRate(
					new SendEmailTaskRunnable(
							getConfig(MessagingConfigKeyEnums.EMAIL_KEY),
							apiURL,
							getConfig(MessagingConfigKeyEnums.EMAIL_DOMAIN),
							getConfig(MessagingConfigKeyEnums.EMAIL_DESTINATAIRES),
							this.messagesSendingQueue
					), 0L, periodeEnvoiMail, TimeUnit.MINUTES);
		}
		else{
			LOGGER.error("Impossible d'envoyer les emails à cause d'une erreur de configuration");
		}
	}
	

	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfig(){

		LOGGER.info("**  **");
		LOGGER.info(" > URL du service	: " + getConfig(MessagingConfigKeyEnums.EMAIL_URL));
		LOGGER.info(" > Domaine du service	: " + getConfig(MessagingConfigKeyEnums.EMAIL_DOMAIN));
		LOGGER.info(" > Service	du service : " + getConfig(MessagingConfigKeyEnums.EMAIL_SERVICE));
		LOGGER.info(" > Clé du service : " + (getConfig(MessagingConfigKeyEnums.EMAIL_KEY) != null ? "**********" : null));
		LOGGER.info(" > Destinataires	: " + getConfig(MessagingConfigKeyEnums.EMAIL_DESTINATAIRES));

		boolean configValid = true;
		try{
			Long periodeEnvoiMail = Long.parseLong(getConfig(MessagingConfigKeyEnums.EMAIL_PERIODE_ENVOI));
			LOGGER.info(" > Période d'envoi	: " + periodeEnvoiMail + " minutes");
			if(periodeEnvoiMail > 0){
				this.periodeEnvoiMail = periodeEnvoiMail;
			}
			else{
				configValid = false;
				LOGGER.error("Erreur lors de la mise à jour de la période d'envoi : " + periodeEnvoiMail);
			}
			
		}
		catch(NumberFormatException e){
			LOGGER.error("Erreur lors de la mise à jour de la période d'envoi : " + getConfig(MessagingConfigKeyEnums.EMAIL_PERIODE_ENVOI));
			configValid = false;
		}
		
		
		
		for (MessagingConfigKeyEnums configKey : MessagingConfigKeyEnums.values()) {
			configValid &= getConfig(configKey) != null;	
		}
		if(!configValid){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
		}
		else{
			LOGGER.info("La configuration est correcte.");
		}
		return configValid;
	}

	
	
	
	/**
	 * 
	 * @param titre titre du mail
	 * @param message message du mail
	 * @return le résulat de l'envoi
	 */
	public void sendNotificationEmail(String titre, String message){
		LOGGER.info("Ajout du message [" +message+ "] dans la liste des envois");
		List<String> messagesToSend = messagesSendingQueue.getOrDefault(titre, new ArrayList<String>());
		messagesToSend.add(message);
		messagesSendingQueue.put(titre, messagesToSend);
	}
	

	

	/**
	 * @return the messagesSendingQueue
	 */
	protected Map<String, List<String>> getMessagesSendingQueue() {
		return messagesSendingQueue;
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
