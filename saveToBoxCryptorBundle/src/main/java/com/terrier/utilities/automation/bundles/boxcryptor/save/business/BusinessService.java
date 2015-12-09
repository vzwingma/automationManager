/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.save.business;

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

	
	
	@PostConstruct
	public void initService(){

			LOGGER.info("Recherche des fichiers à sauvegarder dans " + getKey(ConfigKeyEnums.DOWNLOAD) );
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
