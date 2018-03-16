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
import com.google.api.services.gmail.GmailScopes;
import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.emails.worker.business.api.GoogleAuthHelper;
import com.terrier.utilities.automation.bundles.emails.worker.business.enums.EmailRuleEnum;
import com.terrier.utilities.automation.bundles.emails.worker.business.runnable.AbstractEmailRunnable;
import com.terrier.utilities.automation.bundles.emails.worker.business.runnable.ArchiveEmailsRunnable;
import com.terrier.utilities.automation.bundles.emails.worker.business.runnable.AttachementsRunnable;
import com.terrier.utilities.automation.bundles.emails.worker.business.runnable.HubicDLRunnable;

/**
 * Service métier du worker
 * @author PVZN02821
 *
 */
@Singleton
public class EmailsWorkerBusinessService extends AbstractAutomationService {

	private static final Logger LOGGER = LoggerFactory.getLogger( EmailsWorkerBusinessService.class );

	public static final String CONFIG_PID = "com.terrier.utilities.automation.bundles.emails.worker";

	private Gmail gmailService;
	private String scope = GmailScopes.MAIL_GOOGLE_COM;

	public static final String NOTIF_HEADER = "Emails Worker";

	// Nombre de patterns écrits
	protected int nombrePatterns = 0;
	private String destinationDirectory = null;
	/**
	 * Threads pool
	 */
	private ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(50);
	/**
	 * Liste des tâches schedulées
	 */
	protected List<AbstractEmailRunnable> listeScheduled = new ArrayList<>();

	// Durée d'attente avec le démarrage réel
	protected Long startDelay = 10L;
	private Long periode = 60L;
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

		periode = Long.parseLong(getKey(ConfigKeyEnums.EMAIL_WORKER_PERIOD));
		LOGGER.info(" > Démarrage de l'exécution des règles dans {} minutes puis toutes les {} minutes", startDelay, periode);

		int nbPatterns = 0;
		while(getKey(ConfigKeyEnums.EMAIL_WORKER_RULE, nbPatterns) != null){
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

			if(getGMailService() != null && validateConfig()){
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
	 * Démarrage du traitement
	 * @param p
	 */
	protected void startTreatment(int index){
		EmailRuleEnum rule = EmailRuleEnum.valueOf(getKey(ConfigKeyEnums.EMAIL_WORKER_RULE, index).toUpperCase());

		AbstractEmailRunnable workerRunnable = null;
		switch (rule) {
		case HUBIC:
			workerRunnable = new HubicDLRunnable(index, "Hubic", gmailService, this);
			break;
		case AUTOLIB:
			workerRunnable = new ArchiveEmailsRunnable(index, "Autolib'", gmailService, this);
			break;
		case FREE_MOBILE:
			workerRunnable = new AttachementsRunnable(index, "FreeMobile", gmailService, this);
			break;
		case AWS:
			workerRunnable = new AttachementsRunnable(index, "AWS", gmailService, this);
			break;			
		default:
			break;
		}
		if(workerRunnable != null){
			LOGGER.info("[{}] Démarrage de la règle : {} : {}", index, rule, workerRunnable);
			this.listeScheduled.add(workerRunnable);
			scheduledThreadPool.scheduleWithFixedDelay(workerRunnable, startDelay, periode, TimeUnit.MINUTES);
		}
	}


	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfig(){

		boolean configValid = true;

		LOGGER.info("Période de scan : {} minutes", getKey(ConfigKeyEnums.EMAIL_WORKER_PERIOD));
		Long period = null;
		try{
			period = Long.parseLong(getKey(ConfigKeyEnums.EMAIL_WORKER_PERIOD));
		}
		catch(NumberFormatException e){
			LOGGER.error("Erreur dans le format de la période {}", getKey(ConfigKeyEnums.EMAIL_WORKER_PERIOD));
		}
		configValid &= period != null;
		
		destinationDirectory = getKey(ConfigKeyEnums.EMAIL_WORKER_DESTDIR);
		LOGGER.info("Répertoire de destination {}", destinationDirectory);
		configValid &= destinationDirectory != null;
		
		if(!configValid){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
			sendNotificationMessage("Erreur de configuration", "La configuration de "+CONFIG_PID+" est incorrecte");
		}
		return configValid;
	}

	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfig(int p){

		boolean configValid = true;

		LOGGER.info("** [{}] **", p);
		String rule = getKey(ConfigKeyEnums.EMAIL_WORKER_RULE, p);
		LOGGER.info("[{}] > Règle : {}", p, rule);
		
		EmailRuleEnum ruleName = null;
		try{
			ruleName = EmailRuleEnum.valueOf(rule.toUpperCase());
		}
		catch(IllegalArgumentException e){
			LOGGER.error("Erreur : [{}] n'est pas une règle existante", rule);
		}
		
		configValid &= rule != null && ruleName != null;
		
		if(!configValid){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
			sendNotificationMessage("Erreur de configuration", "La configuration de "+CONFIG_PID+" est incorrecte");
		}
		return configValid;
	}


	/**
	 * @return GMail Service
	 */
	protected Gmail getGMailService(){

		if(gmailService == null && scope != null){
			LOGGER.info("Initialisation du Gmail Service");
			try {
				gmailService = GoogleAuthHelper.getGmailService(scope);
			} catch (IOException e) {
				LOGGER.error("Erreur lors de l'initialisation du service GMail", e);
			}
		}
		return gmailService;
	}


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
	}

	/**
	 * @param scope the scope to set
	 */
	protected final void setScope(String scope) {
		this.scope = scope;
	}

	/**
	 * @return the destinationDirectory
	 */
	public String getDestinationDirectory() {
		return destinationDirectory;
	}
}
