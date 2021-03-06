package com.terrier.utilities.automation.bundles.save.to.business;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.save.to.business.enums.CommandeEnum;
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
	private List<SaveToTaskRunnable> listeScheduled = new ArrayList<>();
	/**
	 * Threads pool
	 */
	private ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(50);

	// Nombre de patterns écrits
	protected int nombrePatterns = 0;

	// Durée d'attente avec le démarrage réel
	protected Long startDelay = 10L;
	
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
		this.nombrePatterns = nbPatterns;

		if(this.nombrePatterns > 0){
			// arrêt des tâches schedulées
			for (Iterator<Runnable> iterator = scheduledThreadPool.getQueue().iterator(); iterator.hasNext();) {
				Runnable scheduledFuture = iterator.next();
				scheduledThreadPool.remove(scheduledFuture);
			}
			this.listeScheduled.clear();

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
		Long periode = Long.parseLong(getKey(ConfigKeyEnums.SAVE_TO_PERIOD_SCAN, p));
		SaveToTaskRunnable copyRunnable = new SaveToTaskRunnable(
				p,
				CommandeEnum.valueOf(getKey(ConfigKeyEnums.COMMANDE, p)),
				getKey(ConfigKeyEnums.FILES_DIRECTORY_IN, p),
				getKey(ConfigKeyEnums.FILES_PATTERN_IN, p),
				getKey(ConfigKeyEnums.FILES_DIRECTORY_OUT, p),
				getKey(ConfigKeyEnums.FILES_PATTERN_OUT, p),
				this);
		LOGGER.info("[{}] Démarrage du scheduler dans {} minutes puis toutes les {} minutes", p, startDelay, periode);
		this.listeScheduled.add(copyRunnable);
		scheduledThreadPool.scheduleWithFixedDelay(copyRunnable, startDelay, periode, TimeUnit.MINUTES);
	}


	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfig(int p){

		boolean configValid = false;

		LOGGER.info("** [{}] **", p);
		LOGGER.info("[{}] > Commande : {}", p, getKey(ConfigKeyEnums.COMMANDE, p));
		LOGGER.info("[{}] > Période de scan : {} minutes", p, getKey(ConfigKeyEnums.SAVE_TO_PERIOD_SCAN, p));
		LOGGER.info("[{}] > Répertoire d'entrée : {}", p, getKey(ConfigKeyEnums.FILES_DIRECTORY_IN, p));
		LOGGER.info("[{}] > Répertoire de sortie : {}", p, getKey(ConfigKeyEnums.FILES_DIRECTORY_OUT, p));
		Long period = null;
		try{
			period = Long.parseLong(getKey(ConfigKeyEnums.SAVE_TO_PERIOD_SCAN, p));
		}
		catch(NumberFormatException e){
			LOGGER.error("[{}] > Erreur dans le format de la période {}", p, getKey(ConfigKeyEnums.SAVE_TO_PERIOD_SCAN, p));
		}
		configValid = period != null
				&& getKey(ConfigKeyEnums.COMMANDE, p) != null
				&& CommandeEnum.valueOf(getKey(ConfigKeyEnums.COMMANDE, p)) != null
				&& getKey(ConfigKeyEnums.FILES_DIRECTORY_IN, p) != null
				&& getKey(ConfigKeyEnums.FILES_DIRECTORY_OUT, p) != null;

		if(!configValid){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
			sendNotificationMessage("Erreur de configuration", "La configuration de "+CONFIG_PID+" est incorrecte");
		}
		return configValid;
	}




	/**
	 * @return the nbPatterns
	 */
	public int getNbPatterns() {
		return nombrePatterns;
	}


	/**
	 * Arrêt du service
	 */
	@Override
	public void arretTasks() {
		scheduledThreadPool.shutdownNow();
	}



	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#updateSupervisionEvents(java.util.List)
	 */
	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {
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
		
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Nombre de patterns de copie", 
						this.nombrePatterns,
						this.nombrePatterns > 0 ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.WARNING));
		
		for (SaveToTaskRunnable copie : this.listeScheduled) {
			copie.updateSupervisionEvents(supervisionEvents);
		}
	}
}
