package com.terrier.utilities.automation.bundles.messaging;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.osgi.framework.FrameworkUtil;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.messaging.enums.MessagingConfigKeyEnums;
import com.terrier.utilities.automation.bundles.messaging.runnable.SendEmailTaskRunnable;
import com.terrier.utilities.automation.bundles.messaging.runnable.SendSMSTaskRunnable;
import com.terrier.utilities.automation.bundles.messaging.runnable.SendSlackNotifsTaskRunnable;

/**
 * Classe de service de messaging
 * @author vzwingma
 *
 */
@Singleton
public class MessagingBusinessService extends AbstractAutomationService {



	private static final Logger LOGGER = LoggerFactory.getLogger( MessagingBusinessService.class );

	private ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(5);

	/**
	 * Liste des tâches schedulées
	 */
	private SendEmailTaskRunnable sendEmailScheduled;
	private SendSlackNotifsTaskRunnable sendNotifsScheduled;
	private SendSMSTaskRunnable sendSMSScheduled;

	// Message Handler
	@Inject private MessageEventHandler eventMessages;

	// Période d'envoi
	private Long periodeEnvoiMessages;

	private static final String CONFIG_PID = "com.terrier.utilities.automation.bundles.messaging";

	private static final String ANONYMOUS = "**********";
	
	/**
	 * Liste de messages à envoyer
	 */
	private Map<String, ConcurrentLinkedQueue<String>> emailSendingQueue = new ConcurrentHashMap<>();
	private Map<String, ConcurrentLinkedQueue<String>> notifsSendingQueue = new ConcurrentHashMap<>();
	private ConcurrentLinkedQueue<String> smsSendingQueue = new ConcurrentLinkedQueue<>();

	/**
	 * Initialisation
	 */
	@PostConstruct
	public void initService(){
		registerToConfig(CONFIG_PID);

		LOGGER.info("Enregistrement de l'eventHandler {} sur le topic : {}", eventMessages, EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName());
		Dictionary<String, String[]> props = new Hashtable<>();
		props.put(EventConstants.EVENT_TOPIC, new String[]{EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName()});
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(EventHandler.class.getName(), eventMessages , props);
	}



	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#notifyUpdateDictionnary()
	 */
	@Override
	public void notifyUpdateDictionary() {

		// Si correct, reprogrammation de la tâche d'envoi
		if(validateConfigEmail()){
			scheduleSendingEmail();
		}
		else{
			LOGGER.info("La tâche d'envoi des mails est désactivée");
		}
		if(validateConfigSms()){
			scheduleSendingSMS();
		}
		else{
			LOGGER.info("La tâche d'envoi des SMS est désactivée");
		}
		if(validateConfigSlack()){
			scheduleSendingSlack();
		}
		else{
			LOGGER.info("La tâche d'envoi SLACK est désactivée");
		}
	}

	/**
	 * Envoi des emails
	 */
	private void scheduleSendingEmail(){
		// arrêt des tâches schedulées
		if(sendEmailScheduled != null){
			boolean cancel = scheduledThreadPool.getQueue().remove(sendEmailScheduled);
			this.sendEmailScheduled = null;
			LOGGER.warn("Arrêt de la tâche d'envoi des emails : {}", cancel);
		}
		sendEmailScheduled = new SendEmailTaskRunnable(
				getConfig(MessagingConfigKeyEnums.EMAIL_KEY),
				getConfig(MessagingConfigKeyEnums.EMAIL_URL),
				getConfig(MessagingConfigKeyEnums.EMAIL_DOMAIN),
				getConfig(MessagingConfigKeyEnums.EMAIL_SERVICE),
				getConfig(MessagingConfigKeyEnums.EMAIL_DESTINATAIRES),
				this
				);
		scheduledThreadPool.scheduleWithFixedDelay(sendEmailScheduled, 1L, periodeEnvoiMessages, TimeUnit.MINUTES);

		LOGGER.info("La tâche d'envoi des mails est programmée");
	}


	/**
	 * Envoi des emails
	 */
	private void scheduleSendingSlack(){
		// arrêt des tâches schedulées
		if(sendNotifsScheduled != null){
			boolean cancel = scheduledThreadPool.getQueue().remove(sendNotifsScheduled);
			this.sendNotifsScheduled = null;
			LOGGER.warn("Arrêt de la tâche d'envoi des notifs Slack : {}", cancel);
		}
		sendNotifsScheduled = new SendSlackNotifsTaskRunnable(
				getConfig(MessagingConfigKeyEnums.SLACK_URL),
				getConfig(MessagingConfigKeyEnums.SLACK_KEY),
				this
				);
		scheduledThreadPool.scheduleWithFixedDelay(sendNotifsScheduled, 1L, periodeEnvoiMessages, TimeUnit.MINUTES);

		LOGGER.info("La tâche d'envoi des notifications Slack est programmée");
	}
	/**
	 * Envoi des emails
	 */
	private void scheduleSendingSMS(){
		// arrêt des tâches schedulées
		if(sendSMSScheduled != null){
			boolean cancel = scheduledThreadPool.getQueue().remove(sendSMSScheduled);
			this.sendSMSScheduled = null;
			LOGGER.warn("Arrêt de la tâche d'envoi des SMS : {}", cancel);
		}
		sendSMSScheduled = 
				new SendSMSTaskRunnable(
						getConfig(MessagingConfigKeyEnums.SMS_URL),
						getConfig(MessagingConfigKeyEnums.SMS_USER),
						getConfig(MessagingConfigKeyEnums.SMS_PASS),
						this
						);
		scheduledThreadPool.scheduleAtFixedRate(sendSMSScheduled, 1L, periodeEnvoiMessages, TimeUnit.MINUTES);
		LOGGER.info("La tâche d'envoi des SMS est programmée");
	}



	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfigSlack(){

		LOGGER.info("**  **");
		LOGGER.info("[SLACK] > URL du service : {}", getConfig(MessagingConfigKeyEnums.SLACK_URL));
		LOGGER.info("[SLACK] > Clé du service : {}", getConfig(MessagingConfigKeyEnums.SLACK_KEY, true));
		boolean configValide = true;
		try{
			Long periodeEnvoiMail = Long.parseLong(getConfig(MessagingConfigKeyEnums.SEND_PERIODE_ENVOI));
			LOGGER.info(" > Période d'envoi	: {} minutes", periodeEnvoiMail);
			if(periodeEnvoiMail > 0){
				this.periodeEnvoiMessages = periodeEnvoiMail;
			}
			else{
				configValide = false;
				LOGGER.error("Erreur lors de la mise à jour de la période d'envoi : {}", periodeEnvoiMail);
			}

		}
		catch(NumberFormatException e){
			LOGGER.error("Erreur lors de la mise à jour de la période d'envoi : {}", getConfig(MessagingConfigKeyEnums.SEND_PERIODE_ENVOI));
			configValide = false;
		}


		for (MessagingConfigKeyEnums configKey : MessagingConfigKeyEnums.values("SLACK")) {
			configValide &= getConfig(configKey) != null;	
		}
		if(!configValide){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
		}
		else{
			LOGGER.info("La configuration est correcte.");
		}
		return configValide;
	}


	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfigEmail(){

		LOGGER.info("**  **");
		LOGGER.info("[EMAIL] > URL du service	: {}", getConfig(MessagingConfigKeyEnums.EMAIL_URL));
		LOGGER.info("[EMAIL] > Domaine du service : {}", getConfig(MessagingConfigKeyEnums.EMAIL_DOMAIN));
		LOGGER.info("[EMAIL] > Nom du service : {}", getConfig(MessagingConfigKeyEnums.EMAIL_SERVICE));
		LOGGER.info("[EMAIL] > Clé du service : {}", getConfig(MessagingConfigKeyEnums.EMAIL_KEY, true));
		LOGGER.info("[EMAIL] > Destinataires : {}", getConfig(MessagingConfigKeyEnums.EMAIL_DESTINATAIRES));
		boolean configValide = true;
		try{
			Long periodeEnvoiMail = Long.parseLong(getConfig(MessagingConfigKeyEnums.SEND_PERIODE_ENVOI));
			LOGGER.info(" > Période d'envoi	: {} minutes", periodeEnvoiMail);
			if(periodeEnvoiMail > 0){
				this.periodeEnvoiMessages = periodeEnvoiMail;
			}
			else{
				configValide = false;
				LOGGER.error("Erreur lors de la mise à jour de la période d'envoi : {}", periodeEnvoiMail);
			}
		}
		catch(NumberFormatException e){
			LOGGER.error("Erreur lors de la mise à jour de la période d'envoi : {}", getConfig(MessagingConfigKeyEnums.SEND_PERIODE_ENVOI));
			configValide = false;
		}


		for (MessagingConfigKeyEnums configKey : MessagingConfigKeyEnums.values("EMAIL")) {
			configValide &= getConfig(configKey) != null;	
		}
		if(!configValide){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
		}
		else{
			LOGGER.info("La configuration est correcte.");
		}
		return configValide;
	}


	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfigSms(){

		LOGGER.info("**  **");
		LOGGER.info("[ SMS ] > URL du service : {}", getConfig(MessagingConfigKeyEnums.SMS_URL));
		LOGGER.info("[ SMS ] > User du service : {}", getConfig(MessagingConfigKeyEnums.SMS_USER, true));
		LOGGER.info("[ SMS ] > Mot de passe du service : {}", getConfig(MessagingConfigKeyEnums.SMS_PASS, true));
		boolean configValide = true;
		try{
			Long periodeEnvoiMail = Long.parseLong(getConfig(MessagingConfigKeyEnums.SEND_PERIODE_ENVOI));
			LOGGER.info(" > Période d'envoi	: {} minutes", periodeEnvoiMail);
			if(periodeEnvoiMail > 0){
				this.periodeEnvoiMessages = periodeEnvoiMail;
			}
			else{
				configValide = false;
				LOGGER.error("Erreur lors de la mise à jour de la période d'envoi : {}", periodeEnvoiMail);
			}

		}
		catch(NumberFormatException e){
			LOGGER.error("Erreur lors de la mise à jour de la période d'envoi : {}", getConfig(MessagingConfigKeyEnums.SEND_PERIODE_ENVOI));
			configValide = false;
		}


		for (MessagingConfigKeyEnums configKey : MessagingConfigKeyEnums.values("SMS")) {
			configValide &= getConfig(configKey) != null;	
		}
		if(!configValide){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
		}
		else{
			LOGGER.info("La configuration est correcte.");
		}
		return configValide;
	}



	/**
	 * 
	 * @param titre titre du mail
	 * @param message message du mail
	 * @return le résulat de l'envoi
	 */
	public void sendNotificationEmail(String titre, String message){
		LOGGER.info("Ajout du message [{}] dans la liste [{}] des envois d'emails", message, titre);
		ConcurrentLinkedQueue<String> messagesToSend = emailSendingQueue.getOrDefault(titre, new ConcurrentLinkedQueue<String>());
		messagesToSend.add(message);
		emailSendingQueue.put(titre, messagesToSend);
	}
	/**
	 * 
	 * @param titre titre du mail
	 * @param message message du mail
	 * @return le résulat de l'envoi
	 */
	public void sendNotificationSlack(String titre, String message){
		LOGGER.info("Ajout du message [{}] dans la liste [{}] des notifs Slack", message, titre);
		ConcurrentLinkedQueue<String> messagesToSend = notifsSendingQueue.getOrDefault(titre, new ConcurrentLinkedQueue<String>());
		messagesToSend.add(message);
		notifsSendingQueue.put(titre, messagesToSend);
	}

	/**
	 * 
	 * @param message message du SMS
	 * @return le résulat de l'envoi
	 */
	public void sendNotificationSMS(String message){
		LOGGER.info("Ajout du message [{}] dans la liste des envois de SMS", message);
		if(!smsSendingQueue.contains(message)){
			smsSendingQueue.add(message);
		}
		else{
			LOGGER.info("Le message [{}] existe déjà dans la file. Pas d'ajout supplémentaire", message);
		}
	}



	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#arretTasks()
	 */
	@Override
	public void arretTasks(){
		LOGGER.warn("Arrêt des tâches d'envoi de mails et de SMS");
		this.scheduledThreadPool.shutdown();
	}


	/**
	 * @return the messagesSendingQueue
	 */
	public Map<String, ConcurrentLinkedQueue<String>> getEmailsSendingQueue() {
		return emailSendingQueue;
	}




	/**
	 * @return the smsSendingQueue
	 */
	public Queue<String> getSmsSendingQueue() {
		return smsSendingQueue;
	}


	/**
	 * @return the notifsSendingQueue
	 */
	public final Map<String, ConcurrentLinkedQueue<String>> getNotifsSendingQueue() {
		return notifsSendingQueue;
	}


	/**
	 * @param key clé
	 * @return valeur dans la config correspondante
	 */
	protected String getConfig(MessagingConfigKeyEnums key, boolean anonymous){
		String value = getConfig(key);
		if(value != null && anonymous){
			return ANONYMOUS;
		}
		else{
			return value;
		}
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
			LOGGER.error("La clé {} est introuvable", key);
		}
		return null;
	}



	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#updateSupervisionEvents(java.util.List)
	 */
	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {		

		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Statut de l'envoi d'emails", 
						this.sendEmailScheduled != null,
						this.sendEmailScheduled != null ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.ERROR ));

		// Statut des emails
		if(sendEmailScheduled != null){
			this.sendEmailScheduled.updateSupervisionEvents(supervisionEvents);
		}

		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Statut de l'envoi de SMS", 
						sendSMSScheduled != null,
						sendSMSScheduled != null ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.ERROR ));
		if(this.sendSMSScheduled != null){
			this.sendSMSScheduled.updateSupervisionEvents(supervisionEvents);
		}
		
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Statut de l'envoi de notification Slack", 
						this.sendNotifsScheduled != null,
						this.sendNotifsScheduled != null ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.ERROR ));

		// Statut des notifs
		if(sendNotifsScheduled != null){
			this.sendNotifsScheduled.updateSupervisionEvents(supervisionEvents);
		}

		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Activité de traitements périodiques", 
						!this.scheduledThreadPool.isShutdown() && !this.scheduledThreadPool.isTerminated(),
						!this.scheduledThreadPool.isShutdown() && !this.scheduledThreadPool.isTerminated() ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.ERROR ));
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Threads utilisés", 
						this.scheduledThreadPool.getQueue().size() + "/" + this.scheduledThreadPool.getPoolSize(),
						this.scheduledThreadPool.getQueue().size() <= this.scheduledThreadPool.getPoolSize() ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.WARNING));
	}


	/**
	 * Arrêt de la surveillance
	 */
	@PreDestroy
	@Override
	public void stopSupervision(){
		LOGGER.warn("Arrêt de la supervision");
		this.scheduledThreadPool.shutdown();
		arretTasks();
	}




}
