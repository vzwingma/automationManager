/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.business;

import java.util.Dictionary;
import java.util.EnumMap;
import java.util.Hashtable;
import java.util.List;
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

import com.terrier.utilities.automation.bundles.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.MessagePropertyNameEnum;
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


	private final Logger logger = LoggerFactory.getLogger( this.getClass() );

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
		scheduledThreadPool.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
	}

	/**
	 * Démarrage du service
	 */

	/**
	 * Enregistrement aux modifications du fichier de configuration
	 * @param configPID nom du fichier de configuration
	 */
	public void registerToConfig(String configPID){
		logger.info("Enregistrement de la surveillance du fichier de configuration : {}", configPID);
		Hashtable<String, Object> properties = new Hashtable<>();
		this.configPID = configPID;
		properties.put(Constants.SERVICE_PID, this.configPID);
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(ManagedService.class.getName(), this , properties);
		logger.info("Chargement du fichier de configuration /etc/{}.cfg", this.configPID);
	}



	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if(properties != null){
			logger.info("Mise à jour du fichier de configuration {}", this.configPID);
			this.dictionnaire = (Dictionary<String, String>)properties;
			notifyUpdateDictionary();
			sendNotificationMessage("Configuration", "Mise à jour du fichier de configuration /etc/"+ this.configPID +".cfg");
		}
		else{
			logger.error("Impossible de trouver le fichier de configuration");
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
		EnumMap<StatutPropertyNameEnum, Object> properties = new EnumMap<>(StatutPropertyNameEnum.class);


		// Status
		StatutBundleTopicObject statutBundle = new StatutBundleTopicObject(context.getBundle());


		StatutPropertyBundleObject statutThread = new StatutPropertyBundleObject(
				"Activité du pool de threads de supervision",
				!this.scheduledThreadPool.isShutdown() && !this.scheduledThreadPool.isTerminated(),
				!this.scheduledThreadPool.isShutdown() && !this.scheduledThreadPool.isTerminated() ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.WARNING
				);
		statutBundle.getProperties().add(statutThread);
		updateSupervisionEvents(statutBundle.getProperties());

		logger.debug("Envoi des éléments de supervision : {}", statutBundle.getProperties());
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
	public void sendNotificationMessage(String titreMessage, String message)
	{
		EnumMap<MessagePropertyNameEnum, Object> propertiesMessages = new EnumMap<>(MessagePropertyNameEnum.class);
		propertiesMessages.put(MessagePropertyNameEnum.TITRE_MESSAGE, titreMessage);
		propertiesMessages.put(MessagePropertyNameEnum.MESSAGE, message);
		propertiesMessages.put(MessagePropertyNameEnum.TIME, System.currentTimeMillis());
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
	 * @param key clé
	 * @return valeur dans la config correspondante
	 */
	public String getKey(ConfigKeyEnums key){
		try {
			if(key != null){
				return getConfig(key.getCodeKey());
			}
		} catch (KeyNotFoundException e) {
			logger.error("La clé {} est introuvable", key);
		}
		return null;
	}

	/**
	 * @param key clé
	 * @return valeur dans la config correspondante
	 * @throws KeyNotFoundException
	 */
	public String getKey(final ConfigKeyEnums key, int indice){
		try {
			String keyValue = key != null ? key.getCodeKey() : null;

			if(keyValue != null){
				if(indice >= 0){
					keyValue += "." + indice;
				}
				return getConfig(keyValue);
			}
			return null;
		} catch (KeyNotFoundException e) {
			return null;
		}
	}
	
	/**
	 * Arrêt de la surveillance
	 */
	@PreDestroy
	public void stopSupervision(){
		logger.warn("Arrêt de la supervision");
		this.scheduledThreadPool.shutdown();
		arretTasks();
	}


	public void arretTasks(){
		logger.debug("Arrêt des tâches lors du @PreDestroy");
	}
}
