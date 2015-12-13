/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.business;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.boxcryptor.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.TypeMessagingEnum;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;

/**
 * @author vzwingma
 *
 */
@Singleton
public class BoxcryptorBusinessService extends AbstractAutomationService{


	private static final String CONFIG_PID = "com.terrier.utilities.automation.bundles.boxcryptor";

	private static final Logger LOGGER = LoggerFactory.getLogger( BoxcryptorBusinessService.class );

	// Nombre de répertoires configurés
	protected int nbInventaires = 0;
	

	/**
	 * Liste des tâches schedulées
	 */
	private List<ScheduledFuture<?>> listeScheduled = new ArrayList<ScheduledFuture<?>>();


	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#startService()
	 */
	@PostConstruct
	public void startService() {
		super.registerToConfig(CONFIG_PID);
	}
	
	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#notifyUpdateDictionary()
	 */
	@Override
	public void notifyUpdateDictionary() {

		LOGGER.info("** Configuration **");
		int nbPatterns = 0;
		while(getKey(ConfigKeyEnums.SOURCE_DIRECTORY, nbPatterns) != null){
			nbPatterns++;
		}
		LOGGER.info(" > Nombre de pattern : {}", nbPatterns);
		this.nbInventaires = nbPatterns;
		
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
		LOGGER.info("** **");
	}



	/**
	 * Démarrage du traitement
	 * @param p
	 */
	protected void startTreatment(int p){
		Long periode = Long.parseLong(getKey(ConfigKeyEnums.PERIOD_SCAN, p));
//		SaveToTaskRunnable copyRunnable = new SaveToTaskRunnable(
//				p,
//				CommandeEnum.valueOf(getKey(ConfigKeyEnums.COMMANDE, p)),
//				getKey(ConfigKeyEnums.FILES_DIRECTORY_IN, p),
//				getKey(ConfigKeyEnums.FILES_PATTERN_IN, p),
//				getKey(ConfigKeyEnums.FILES_DIRECTORY_OUT, p),
//				getKey(ConfigKeyEnums.FILES_PATTERN_OUT, p));
//		LOGGER.info("[{}] Démarrage du scheduler : {} minutes", p ,periode);
//		this.listeScheduled.add(scheduledThreadPool.scheduleAtFixedRate(copyRunnable, 0L, periode, TimeUnit.MINUTES));	
	}
	
	
	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfig(int p){

		boolean configValid = false;

		LOGGER.info("** [{}] **", p);
		LOGGER.info("[{}] > Période de scan : {} minutes", p, getKey(ConfigKeyEnums.PERIOD_SCAN, p));
		LOGGER.info("[{}] > Répertoire d'entrée : {}", p, getKey(ConfigKeyEnums.SOURCE_DIRECTORY, p));
		LOGGER.info("[{}] > Répertoire de sortie : {}", p, getKey(ConfigKeyEnums.CRYPTED_DIRECTORY, p));
		Long period = null;
		try{
			period = Long.parseLong(getKey(ConfigKeyEnums.PERIOD_SCAN, p));
		}
		catch(NumberFormatException e){
			
		}
		configValid = period != null
				&& getKey(ConfigKeyEnums.SOURCE_DIRECTORY, p) != null
				&& getKey(ConfigKeyEnums.CRYPTED_DIRECTORY, p) != null;

		if(!configValid){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
			sendNotificationMessage(TypeMessagingEnum.SMS, "Erreur de configuration", "La configuration de "+CONFIG_PID+" est incorrecte");
		}
		return configValid;
	}




	/**
	 * @return the nbPatterns
	 */
	public int getNbPatterns() {
		return nbInventaires;
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
}
