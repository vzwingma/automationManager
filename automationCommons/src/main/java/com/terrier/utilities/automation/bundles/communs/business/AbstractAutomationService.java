/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.business;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PreDestroy;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.MessagePropertyNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.MessageTypeEnum;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyNameEnum;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.communs.model.StatutBundleTopicObject;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;

/**
 * Classe d'un service
 * @author vzwingma
 *
 */
public abstract class AbstractAutomationService extends AutomationEventPublisher<MessagePropertyNameEnum> implements ManagedService, Runnable {


	private final Logger LOGGER = LoggerFactory.getLogger( this.getClass() );

	// Dictionnaire
	private Dictionary<String, String> dictionnaire;

	private String configPID;

	/**
	 * Liste des tâches schedulées
	 */

	private ScheduledExecutorService scheduledThreadPool = Executors.newSingleThreadScheduledExecutor();

	private AutomationEventPublisher<MessagePropertyNameEnum> messagePublisher = new AutomationEventPublisher<>();

	private AutomationEventPublisher<StatutPropertyNameEnum> statusPublisher = new AutomationEventPublisher<>();

	/**
	 * Init de la supervision
	 */
	public AbstractAutomationService(){
		scheduledThreadPool.scheduleAtFixedRate(this, 0, 1, TimeUnit.MINUTES);
	}

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
			sendNotificationMessage(MessageTypeEnum.SMS, "Configuration", "Mise à jour du fichier de configuration /etc/"+ this.configPID +".cfg");
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
		// MessageProperties
		Map<StatutPropertyNameEnum, Object> properties = new HashMap<>();


		// Status
		StatutBundleTopicObject statutBundle = new StatutBundleTopicObject(context.getBundle());


		StatutPropertyBundleObject statutThread = new StatutPropertyBundleObject(
				"Activité du pool de threads de supervision",
				!this.scheduledThreadPool.isShutdown() && !this.scheduledThreadPool.isTerminated(),
				!this.scheduledThreadPool.isShutdown() && !this.scheduledThreadPool.isTerminated() ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.WARNING
				);
		statutBundle.getProperties().add(statutThread);
		updateSupervisionEvents(statutBundle.getProperties());

		properties.put(StatutPropertyNameEnum.STATUS, statutBundle);
		properties.put(StatutPropertyNameEnum.TIME, System.currentTimeMillis());
		// Publication
		statusPublisher.publishToTopic(EventsTopicNameEnum.SUPERVISION_EVENTS, properties);
	}



	/**
	 * Ajout des informations du bundle à superviser
	 * @param supervisionEvents événements de supervision, sous la forme titre->Données
	 */
	public abstract void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents);


	/**
	 * Envoi d'un message pour publication
	 * @param message message à envoyer
	 */
	public void sendNotificationMessage(MessageTypeEnum typeMessage, String titreMessage, String message)
	{
		HashMap<MessagePropertyNameEnum, Object> propertiesMessages = new HashMap<MessagePropertyNameEnum, Object>();
		propertiesMessages.put(MessagePropertyNameEnum.TITRE_MESSAGE, titreMessage);
		propertiesMessages.put(MessagePropertyNameEnum.MESSAGE, message);
		propertiesMessages.put(MessagePropertyNameEnum.TIME, System.currentTimeMillis());
		propertiesMessages.put(MessagePropertyNameEnum.TYPE_MESSAGE, typeMessage);
		messagePublisher.publishToTopic(EventsTopicNameEnum.NOTIFIFY_MESSAGE, propertiesMessages);
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

	/**
	 * Arrêt de la surveillance
	 */
	@PreDestroy
	public void stopSupervision(){
		LOGGER.warn("Arrêt de la supervision");
		this.scheduledThreadPool.shutdown();
		arretTasks();
	}


	public void arretTasks(){
		LOGGER.debug("Arrêt des tâches lors du @PreDestroy");
	}
}
