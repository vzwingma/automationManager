/**
 * 
 */
package com.terrier.utilities.automation.bundles.emails.worker.business;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.gmail.Gmail;
import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.emails.worker.business.api.GoogleAuthHelper;
import com.terrier.utilities.automation.bundles.emails.worker.business.runnable.EmailWorkerRunnable;

/**
 * Service m�tier du worker
 * @author PVZN02821
 *
 */
@Singleton
public class EmailsWorkerBusinessService extends AbstractAutomationService {

	private static final Logger LOGGER = LoggerFactory.getLogger( EmailsWorkerBusinessService.class );

	public static final String CONFIG_PID = "com.terrier.utilities.automation.bundles.emails.worker";

	private Gmail gmailService;

	// Nombre de patterns écrits
	protected int nombrePatterns = 0;

	/**
	 * Threads pool
	 */
	private ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(50);
	/**
	 * Liste des tâches schedulées
	 */
	protected List<EmailWorkerRunnable> listeScheduled = new ArrayList<>();

	// Durée d'attente avec le démarrage réel
	protected Long startDelay = 10L;

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#startService()
	 */
	@PostConstruct
	public void startService() {
		// Register config
		super.registerToConfig(CONFIG_PID);

	}

	@Override
	public void notifyUpdateDictionary() {

		LOGGER.info("** Configuration **");
		int nbPatterns = 0;
		while(getKey(ConfigKeyEnums.EMAIL_WORKER_PERIOD, nbPatterns) != null){
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

			if(getGMailService() != null){
				// Démarrage des treatments reprogrammées
				for (int p = 0; p < nbPatterns; p++) {
					if(validateConfig(p)){
						startTreatment(p);				
					}
				}
			}
		}
		else{
			LOGGER.warn("Aucune configuration détectée. Pas de modification de la configuration");
		}

	}


	/**
	 * @return GMail Service
	 */
	protected Gmail getGMailService(){
		
		if(gmailService == null){
			LOGGER.info("Initialisation du Gmail Service");
			try {
				gmailService = GoogleAuthHelper.getGmailService();
			} catch (IOException e) {
				LOGGER.error("Erreur lors de l'initialisation du service GMail", e);
			}
		}
		return gmailService;
	}

	/**
	 * Démarrage du traitement
	 * @param p
	 */
	protected void startTreatment(int p){
		Long periode = Long.parseLong(getKey(ConfigKeyEnums.EMAIL_WORKER_PERIOD, p));
		EmailWorkerRunnable workerRunnable = new EmailWorkerRunnable(
				p, 
				getGMailService());
		LOGGER.info("[{}] Démarrage du scheduler dans {} minutes puis toutes les {} minutes", p, startDelay, periode);
		this.listeScheduled.add(workerRunnable);
		scheduledThreadPool.scheduleWithFixedDelay(workerRunnable, startDelay, periode, TimeUnit.MINUTES);
	}


	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfig(int p){

		boolean configValid = false;

		LOGGER.info("** [{}] **", p);
		LOGGER.info("[{}] > Période de scan : {} minutes", p, getKey(ConfigKeyEnums.EMAIL_WORKER_PERIOD, p));
		Long period = null;
		try{
			period = Long.parseLong(getKey(ConfigKeyEnums.EMAIL_WORKER_PERIOD, p));
		}
		catch(NumberFormatException e){
			LOGGER.error("[{}] > Erreur dans le format de la période {}", p, getKey(ConfigKeyEnums.EMAIL_WORKER_PERIOD, p));
		}
		configValid = period != null;
		if(!configValid){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
			sendNotificationMessage("Erreur de configuration", "La configuration de "+CONFIG_PID+" est incorrecte");
		}
		return configValid;
	}


	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {
		// TODO Auto-generated method stub

	}

}
