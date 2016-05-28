/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.business;

import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
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
import com.terrier.utilities.automation.bundles.communs.enums.messaging.StatusPropertyNameEnum;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;

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
	
	private AutomationEventPublisher<StatusPropertyNameEnum> statusPublisher = new AutomationEventPublisher<>();
	
	/**
	 * Init de la supervision
	 */
	public AbstractAutomationService(){
		scheduledThreadPool.scheduleAtFixedRate(this, 1, 10, TimeUnit.MINUTES);
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
        Map<StatusPropertyNameEnum, Object> properties = new HashMap<>();
        // Status
        Map<String, Object> statusBundle = new HashMap<>();
        statusBundle.put("Activité du ThreadPool de Supervision", !this.scheduledThreadPool.isShutdown() && !this.scheduledThreadPool.isTerminated());
		updateSupervisionEvents(statusBundle);
		
        properties.put(StatusPropertyNameEnum.STATUS, statusBundle);
        properties.put(StatusPropertyNameEnum.BUNDLE, "[" + context.getBundle().getBundleId() + "]" + context.getBundle().getSymbolicName());
        properties.put(StatusPropertyNameEnum.TIME, System.currentTimeMillis());
        // Publication
		statusPublisher.publishToTopic(EventsTopicNameEnum.SUPERVISION_EVENTS, properties);
	}



	/**
	 * Ajout des informations du bundle à superviser
	 * @param supervisionEvents événements de supervision, sous la forme titre->Données
	 */
	public abstract void updateSupervisionEvents(Map<String, Object> supervisionEvents);
	
	
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
