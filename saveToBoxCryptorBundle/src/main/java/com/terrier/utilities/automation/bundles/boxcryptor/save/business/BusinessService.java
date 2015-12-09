/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.save.business;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.apache.log4j.Logger;

import com.terrier.utilities.automation.bundles.boxcryptor.save.Activator;
import com.terrier.utilities.automation.bundles.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;

/**
 * Service métier
 * @author vzwingma
 *
 */
@Singleton
public class BusinessService  {

	private static final Logger LOGGER = Logger.getLogger( BusinessService.class );


	/**
	 * Démarrage du service
	 */
	@PostConstruct
	public void startService(){
		if(validateConfig()){
			scan(getKey(ConfigKeyEnums.DOWNLOAD));
		}
	}
	

	/**
	 * @return validation de la configuration
	 */
	public boolean validateConfig(){


		LOGGER.info("Recherche des fichiers à sauvegarder dans " + getKey(ConfigKeyEnums.DOWNLOAD) );
		LOGGER.info("Copie des fichiers à sauvegarder dans " + getKey(ConfigKeyEnums.BC_DIR) );
		if(getKey(ConfigKeyEnums.DOWNLOAD) != null && getKey(ConfigKeyEnums.BC_DIR) != null){

			File source = new File(getKey(ConfigKeyEnums.DOWNLOAD));
			File cible = new File(getKey(ConfigKeyEnums.BC_DIR));

			if(source.exists() && cible.exists()){
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
	 * @param downloadDir
	 */
	public void scan(String downloadDir){
		
		try {
			DirectoryStream<Path> downloadDirectoryPath = Files.newDirectoryStream(FileSystems.getDefault().getPath(downloadDir));
			for (Path fichier : downloadDirectoryPath) {
				LOGGER.info("" + fichier.getFileName().toString());
			}
		} catch (IOException e) {
			LOGGER.error("Erreur lors du scan de " + downloadDir, e);
		}
		
	}
	

	/**
	 * @param key
	 * @return valeur dans la config correspondante
	 * @throws KeyNotFoundException
	 */
	protected String getKey(ConfigKeyEnums key){
		try {
			return Activator.getConfig(key);
		} catch (KeyNotFoundException e) {
			return null;
		}
	}
}
