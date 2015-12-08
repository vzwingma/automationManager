package com.terrier.utilities.automation.bundles.boxcryptor.save.config;

import java.util.Dictionary;

import org.apache.log4j.Logger;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * Configuration updater
 * @author vzwingma
 *
 */
public class ConfigUpdater implements ManagedService {



	private static final Logger LOGGER = Logger.getLogger( ConfigUpdater.class );
	
	
	@Override
	public void updated(@SuppressWarnings("rawtypes") Dictionary properties) throws ConfigurationException {
		
		if(properties != null){
			LOGGER.info("Mise à jour du fichier de configuration");
		}
		else{
			LOGGER.error("Impossible de trouver le fichier de configuration");
		}
	}

}
