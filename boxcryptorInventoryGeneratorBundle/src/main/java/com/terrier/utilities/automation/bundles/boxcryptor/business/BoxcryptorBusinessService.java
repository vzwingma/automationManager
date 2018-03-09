/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.business;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;

import com.terrier.utilities.automation.bundles.boxcryptor.business.runnables.BCInventoryGeneratorRunnable;
import com.terrier.utilities.automation.bundles.boxcryptor.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.MessageTypeEnum;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;

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
	// Threads pool
	private ScheduledThreadPoolExecutor scheduledThreadPool = new ScheduledThreadPoolExecutor(50);

	// YAML
	private Yaml yaml;

	/**
	 * Liste des tÃ¢ches schedulées
	 */
	private List<ScheduledFuture<?>> listeScheduled = new ArrayList<>();


	/**
	 * Construction du service
	 */
	public BoxcryptorBusinessService(){
		initYAML();
	}

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#startService()
	 */
	@PostConstruct
	public void startService() throws NoSuchFieldException, IllegalAccessException {
		super.registerToConfig(CONFIG_PID);
		// Encoding en UTF-8
		// Forcage en UTF-8 pour les caractères chinois utilisés par BC
		System.setProperty("file.encoding","UTF-8");
		Field charset = Charset.class.getDeclaredField("defaultCharset");
		charset.setAccessible(true);
		charset.set(null,null);
	}


	/**
	 * Init YAML
	 */
	private void initYAML(){
		if(FrameworkUtil.getBundle(this.getClass()) != null){
			BundleContext bundleContext = FrameworkUtil.getBundle(this.getClass()).getBundleContext();
			LOGGER.warn("Chargement de YAML à partir du classloader du bundle [{}]", bundleContext);
			this.yaml = new Yaml(new CustomClassLoaderConstructor(bundleContext.getBundle().adapt(BundleWiring.class).getClassLoader()));
		}
		else{
			this.yaml = new Yaml();
		}
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

		// arret des taches schedulées
		for (Iterator<ScheduledFuture<?>> iterator = listeScheduled.iterator(); iterator.hasNext();) {
			ScheduledFuture<?> scheduledFuture = iterator.next();
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
	private void startTreatment(int p){
		Long periode = Long.parseLong(getKey(ConfigKeyEnums.PERIOD_SCAN, p));
		BCInventoryGeneratorRunnable generateInventoryRunnable = new BCInventoryGeneratorRunnable(
				p,
				this.yaml, 
				getKey(ConfigKeyEnums.SOURCE_DIRECTORY, p),
				getKey(ConfigKeyEnums.CRYPTED_DIRECTORY, p),
				this);
		LOGGER.info("[{}] Démarrage du scheduler : {} minutes", p ,periode);
		this.listeScheduled.add(scheduledThreadPool.scheduleWithFixedDelay(generateInventoryRunnable, 0L, periode, TimeUnit.MINUTES));	
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
			LOGGER.error("[{}] > Erreur : la période {} est incorrecte", p, getKey(ConfigKeyEnums.PERIOD_SCAN, p));
		}
		configValid = period != null
				&& getKey(ConfigKeyEnums.SOURCE_DIRECTORY, p) != null
				&& getKey(ConfigKeyEnums.CRYPTED_DIRECTORY, p) != null;

		if(!configValid){
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
			sendNotificationMessage(MessageTypeEnum.SMS, "Erreur de configuration", "La configuration de "+CONFIG_PID+" est incorrecte");
		}
		return configValid;
	}




	/**
	 * @return the nbPatterns 
	 */
	protected int getNbInventaires() {
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

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#updateSupervisionEvents(java.util.List)
	 */
	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {
		
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Traitements de génération", 
						this.nbInventaires,
						this.nbInventaires == this.listeScheduled.size() ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.ERROR ));
		
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Activité de traitements périodiques", 
						!this.scheduledThreadPool.isShutdown() && !this.scheduledThreadPool.isTerminated(),
						!this.scheduledThreadPool.isShutdown() && !this.scheduledThreadPool.isTerminated() ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.ERROR ));
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Traitements programmés", 
						this.listeScheduled.size(),
						!this.listeScheduled.isEmpty() ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.WARNING));
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Threads utilisés", 
						this.scheduledThreadPool.getQueue().size() + "/" + this.scheduledThreadPool.getPoolSize(),
						this.scheduledThreadPool.getQueue().size() <= this.scheduledThreadPool.getPoolSize() ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.WARNING));
	}
}
