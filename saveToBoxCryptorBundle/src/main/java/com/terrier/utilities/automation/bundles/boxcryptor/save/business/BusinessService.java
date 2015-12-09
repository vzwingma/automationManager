package com.terrier.utilities.automation.bundles.boxcryptor.save.business;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.terrier.utilities.automation.bundles.boxcryptor.save.Activator;
import com.terrier.utilities.automation.bundles.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.communs.utils.AutomationUtils;

/**
 * Service métier
 * @author vzwingma
 *
 */
@Singleton
public class BusinessService implements Runnable {

	private static final Logger LOGGER = Logger.getLogger( BusinessService.class );

	private ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);


	/**
	 * Démarrage du service
	 */
	@PostConstruct
	public void startService(){
		if(validateConfig()){
			Long periode = Long.parseLong(getKey(ConfigKeyEnums.PERIOD_SCAN));
			LOGGER.info("Démarrage du scheduler : " + periode + " minutes");
			scheduledThreadPool.scheduleAtFixedRate(this, 0L, periode, TimeUnit.MINUTES);	
		}
	}


	@PreDestroy
	public void stopService(){
		LOGGER.info("Arrêt du service");
		scheduledThreadPool.shutdownNow();
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		scan();
	}


	/**
	 * @return validation de la configuration
	 */
	protected boolean validateConfig(){

		LOGGER.info("** Configuration **");
		int nbPatterns = Integer.parseInt(getKey(ConfigKeyEnums.FILES_NUMBER));
		LOGGER.info(" > Nombre de pattern : " + nbPatterns);
		LOGGER.info(" > Période de scan : " + getKey(ConfigKeyEnums.PERIOD_SCAN) + " minutes");
		if(getKey(ConfigKeyEnums.PERIOD_SCAN) != null && nbPatterns > 0){
			LOGGER.info("** **");
			return true;
		}
		else{
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
		}
		LOGGER.info("** **");
		return false;
	}



	/**
	 * Scan du download
	 * @param scanDir répertoire à scanner
	 */
	protected void scan(){

		int nbPatterns = Integer.parseInt(getKey(ConfigKeyEnums.FILES_NUMBER));
		for (int i = 0; i < nbPatterns; i++) {
			String scanDir = getKey(ConfigKeyEnums.FILES_DIRECTORY_IN, i);
			LOGGER.info("Scan du répertoire  : " + scanDir);
			if(Files.isDirectory(FileSystems.getDefault().getPath(scanDir))){
				try{
					DirectoryStream<Path> downloadDirectoryPath = Files.newDirectoryStream(FileSystems.getDefault().getPath(scanDir));
					String regExMatch = getKey(ConfigKeyEnums.FILES_PATTERN_IN, i);
					LOGGER.debug(" > Matcher : " + regExMatch);
					if(regExMatch != null){

						for (Path fichier : downloadDirectoryPath) {
							LOGGER.info(" Traitement du fichier : " + fichier.getFileName().toString());
							if(fichier.getFileName().toString().matches(regExMatch)){
								LOGGER.trace(" > Match avec " + regExMatch);
								String outputPattern = getKey(ConfigKeyEnums.FILES_PATTERN_OUT, i);
								if(outputPattern == null || outputPattern.isEmpty()){
									outputPattern = fichier.getFileName().toString();
								}
								boolean resultat = copyToBoxcryptor(fichier, 
										AutomationUtils.replacePatterns(outputPattern), 
										getKey(ConfigKeyEnums.FILES_DIRECTORY_OUT, i));		
								if(resultat){
									LOGGER.info("Copie réalisée vers BoxCrytor");
									Files.delete(fichier);
								}
								else{
									LOGGER.error("Erreur lors de la copie vers BoxCrytor");
								}
							}
						}
					}
					else{
						LOGGER.warn("La clé ["+ConfigKeyEnums.FILES_PATTERN_IN.getCodeKey()+"."+i+"] n'existe pas dans le fichier de conf");
					}
				} catch (IOException e) {
					LOGGER.error("Erreur lors du scan de " + FileSystems.getDefault().getPath(scanDir).toAbsolutePath().toString(), e);
				}
			}
			else{
				LOGGER.error("Erreur lors du scan de " + FileSystems.getDefault().getPath(scanDir).toAbsolutePath() + ". Ce n'est pas un répertoire");
			}
		}
	}


	/**
	 * 
	 * @param fichierSource chemin vers le fichier source
	 * @param outFileName pattern de sortie
	 * @param directoryCible répertoire cible
	 */
	private boolean copyToBoxcryptor(Path fichierSource, String outFileName, String directoryCible){
		try {
			if(outFileName == null || outFileName.isEmpty()){
				outFileName = fichierSource.getFileName().toString();
			}
			Path fichierCible = FileSystems.getDefault().getPath(directoryCible + "/" +outFileName);
			LOGGER.debug(" > Copie "+fichierSource+" vers : " + fichierCible + " :" );

			if(!Files.exists(FileSystems.getDefault().getPath(directoryCible))){
				LOGGER.info("Création du répertoire");
				Files.createDirectories(FileSystems.getDefault().getPath(directoryCible));
			}
			if (!Files.exists( fichierCible)) {
				LOGGER.debug("Création du fichier");
				Files.createFile( fichierCible);
			}
			CopyOption[] options = new CopyOption[]{
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES
			}; 
			Files.copy(fichierSource, fichierCible, options);
			return true;
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}


	/**
	 * @param key clé
	 * @return valeur dans la config correspondante
	 * @throws KeyNotFoundException
	 */
	protected String getKey(ConfigKeyEnums key){
		return getKey(key, -1);
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
				return Activator.getConfig(keyValue);
			}
			return null;
		} catch (KeyNotFoundException e) {
			return null;
		}
	}
}
