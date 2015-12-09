package com.terrier.utilities.automation.bundles.boxcryptor.save.business;

import java.io.File;
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
			scheduledThreadPool.scheduleAtFixedRate(this, 0L, 30L, TimeUnit.SECONDS);	
		}
	}



	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		scan(getKey(ConfigKeyEnums.DOWNLOAD));
	}


	/**
	 * @return validation de la configuration
	 */
	public boolean validateConfig(){


		LOGGER.info("Recherche des fichiers à sauvegarder dans " + getKey(ConfigKeyEnums.DOWNLOAD) );
		LOGGER.info("Copie des fichiers à sauvegarder dans " + getKey(ConfigKeyEnums.BC_DIR) );
		int nbPatterns = Integer.parseInt(getKey(ConfigKeyEnums.FILES_NUMBER));
		LOGGER.info("Nombre de pattern : " + nbPatterns);
		if(getKey(ConfigKeyEnums.DOWNLOAD) != null && getKey(ConfigKeyEnums.BC_DIR) != null){

			File source = new File(getKey(ConfigKeyEnums.DOWNLOAD));
			File cible = new File(getKey(ConfigKeyEnums.BC_DIR));

			if(source.exists() && cible.exists() && nbPatterns > 0){
				return true;
			}
			else{
				LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
			}
		}
		else{
			LOGGER.error("La configuration est incorrecte. Veuillez vérifier le fichier de configuration");
		}
		return false;
	}



	/**
	 * Scan du download
	 * @param scanDir répertoire à scanner
	 */
	public void scan(String scanDir){

		int nbPatterns = Integer.parseInt(getKey(ConfigKeyEnums.FILES_NUMBER));

		try {
			DirectoryStream<Path> downloadDirectoryPath = Files.newDirectoryStream(FileSystems.getDefault().getPath(scanDir));
			for (Path fichier : downloadDirectoryPath) {
				LOGGER.info("Traitement du fichier : " + fichier.getFileName().toString());

				for (int i = 0; i < nbPatterns; i++) {
					String regExMatch = getKey(ConfigKeyEnums.FILES_PATTERN_IN, i);
					LOGGER.trace(" > Matcher : " + regExMatch);
					if(regExMatch != null){
						if(fichier.getFileName().toString().matches(regExMatch)){
							LOGGER.debug(" > Match : " + regExMatch);
							String outputPattern = getKey(ConfigKeyEnums.FILES_PATTERN_OUT, i);
							if(outputPattern == null || outputPattern.isEmpty()){
								outputPattern = fichier.getFileName().toString();
							}
							boolean resultat = copyToBoxcryptor(fichier, 
									AutomationUtils.replacePatterns(outputPattern), 
									getKey(ConfigKeyEnums.BC_DIR) + "\\" + getKey(ConfigKeyEnums.FILES_DIRECTORY_OUT, i));		
							if(resultat){
								LOGGER.info("Copie réalisée vers BoxCrytor");
								Files.delete(fichier);
							}
							else{
								LOGGER.error("Erreur lors de la copie vers BoxCrytor");
							}
						}
					}
					else{
						LOGGER.warn("La clé ["+ConfigKeyEnums.FILES_PATTERN_IN.getCodeKey()+"."+i+"] n'existe pas dans le fichier de conf");
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("Erreur lors du scan de " + scanDir, e);
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
			Path fichierCible = FileSystems.getDefault().getPath(directoryCible + "\\" +outFileName);
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
	 * @param key
	 * @return valeur dans la config correspondante
	 * @throws KeyNotFoundException
	 */
	protected String getKey(ConfigKeyEnums key){
		return getKey(key, -1);
	}

	/**
	 * @param key
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
