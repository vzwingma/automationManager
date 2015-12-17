package com.terrier.utilities.automation.bundles.save.to.business;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.TypeMessagingEnum;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.save.to.business.enums.CommandeEnum;
import com.terrier.utilities.automation.bundles.save.to.business.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.save.to.business.runnable.SaveToTaskRunnable;

/**
 * Service métier de Save to BC
 * @author vzwingma
 *
 */
@Singleton
public class SaveToBusinessService extends AbstractAutomationService {

	private static final Logger LOGGER = LoggerFactory.getLogger( SaveToBusinessService.class );

	/**
	 * Liste des tâches schedulées
	 */
	private List<ScheduledFuture<?>> listeScheduled = new ArrayList<ScheduledFuture<?>>();
	/**
	 * Threads pool
	 */
	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(50);

	// Nombre de patterns écrits
	protected int nbPatterns = 0;

	public static final String CONFIG_PID = "com.terrier.utilities.automation.bundles.save.to";

	
	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#startService()
	 */
	@PostConstruct
	public void startService() {
		// Register config
		super.registerToConfig(CONFIG_PID);
	}



	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#notifyUpdateDictionnary()
	 */
	@Override
	public void notifyUpdateDictionary() {

		LOGGER.info("** Configuration **");
		int nbPatterns = 0;
		while(getKey(ConfigKeyEnums.FILES_DIRECTORY_IN, nbPatterns) != null){
			nbPatterns++;
		}
		LOGGER.info(" > Nombre de pattern : {}", nbPatterns);
		this.nbPatterns = nbPatterns;

		if(this.nbPatterns > 0){
			// arrêt des tâches schedulées
			for (Iterator<ScheduledFuture<?>> iterator = listeScheduled.iterator(); iterator.hasNext();) {
				ScheduledFuture<?> scheduledFuture = (ScheduledFuture<?>) iterator.next();
				scheduledFuture.cancel(true);
				iterator.remove();
			}

			// Démarrage des treatments reprogrammées
			for (int p = 0; p < nbPatterns; p++) {
				if(validateConfig(p)){
					startTreatment(p);				
				}
			}
		}
		else{
			LOGGER.warn("Aucune configuration détectée. Pas de modification de la configuration");
		}
		LOGGER.info("** **");
	}


	/**
	 * Démarrage du traitement
	 * @param p
	 */
	protected void startTreatment(int p){
		Long periode = Long.parseLong(getKey(ConfigKeyEnums.PERIOD_SCAN, p));
		SaveToTaskRunnable copyRunnable = new SaveToTaskRunnable(
				p,
				CommandeEnum.valueOf(getKey(ConfigKeyEnums.COMMANDE, p)),
				getKey(ConfigKeyEnums.FILES_DIRECTORY_IN, p),
				getKey(ConfigKeyEnums.FILES_PATTERN_IN, p),
				getKey(ConfigKeyEnums.FILES_DIRECTORY_OUT, p),
				getKey(ConfigKeyEnums.FILES_PATTERN_OUT, p));
		LOGGER.info("[{}] Démarrage du scheduler : {} minutes", p ,periode);
		this.listeScheduled.add(scheduledThreadPool.scheduleAtFixedRate(copyRunnable, 0L, periode, TimeUnit.MINUTES));	
	}


	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfig(int p){

		boolean configValid = false;

		LOGGER.info("** [{}] **", p);
		LOGGER.info("[{}] > Commande : {}", p, getKey(ConfigKeyEnums.COMMANDE, p));
		LOGGER.info("[{}] > Période de scan : {} minutes", p, getKey(ConfigKeyEnums.PERIOD_SCAN, p));
		LOGGER.info("[{}] > Répertoire d'entrée : {}", p, getKey(ConfigKeyEnums.FILES_DIRECTORY_IN, p));
		LOGGER.info("[{}] > Répertoire de sortie : {}", p, getKey(ConfigKeyEnums.FILES_DIRECTORY_OUT, p));
		Long period = null;
		try{
			period = Long.parseLong(getKey(ConfigKeyEnums.PERIOD_SCAN, p));
		}
		catch(NumberFormatException e){
			LOGGER.error("[{}] > Erreur dans le format de la période {}", p, getKey(ConfigKeyEnums.PERIOD_SCAN, p));
		}
		configValid = period != null
				&& getKey(ConfigKeyEnums.COMMANDE, p) != null
				&& CommandeEnum.valueOf(getKey(ConfigKeyEnums.COMMANDE, p)) != null
				&& getKey(ConfigKeyEnums.FILES_DIRECTORY_IN, p) != null
				&& getKey(ConfigKeyEnums.FILES_DIRECTORY_OUT, p) != null;

		if(!configValid){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
			sendNotificationMessage(TypeMessagingEnum.SMS, "Erreur de configuration", "La configuration de "+CONFIG_PID+" est incorrecte");
		}
		else{

		}
		return configValid;
	}




	/**
	 * @return the nbPatterns
	 */
	public int getNbPatterns() {
		return nbPatterns;
	}


	/**
	 * @param key clé
	 * @return valeur dans la config correspondante
	 */
	protected String getKey(ConfigKeyEnums key){
		try {
			if(key != null){
				return super.getConfig(key.getCodeKey());
			}
		} catch (KeyNotFoundException e) {
			LOGGER.error("La clé {} est introuvable", key);
		}
		return null;
	}

	/**
	 * @param key clé
	 * @return valeur dans la config correspondante
	 * @throws KeyNotFoundException
	 */
	protected String getKey(final ConfigKeyEnums key, int indice){
		try {
			String keyValue = key != null ? key.getCodeKey() : null;

			if(keyValue != null){
				if(indice >= 0){
					keyValue += "." + indice;
				}
				return super.getConfig(keyValue);
			}
			return null;
		} catch (KeyNotFoundException e) {
			return null;
		}
	}



	/**
	 * Arrêt du service
	 */
	@PreDestroy
	public void stopService() {
		scheduledThreadPool.shutdownNow();
	}
}
