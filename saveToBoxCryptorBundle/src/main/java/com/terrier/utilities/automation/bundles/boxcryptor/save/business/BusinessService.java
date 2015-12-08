/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.save.business;

import java.util.Hashtable;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ManagedService;

import com.terrier.utilities.automation.bundles.boxcryptor.save.config.ConfigUpdater;

/**
 * @author vzwingma
 *
 */
@Singleton
public class BusinessService {

	private static final Logger LOGGER = Logger.getLogger( BusinessService.class );


	private static final String CONFIG_PID = "com.terrier.utilities.automation.bundles.boxcryptor.save";

	public BusinessService(){
		LOGGER.info("Businessservice");
	}


	public void initConfig(BundleContext context){
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(Constants.SERVICE_PID, CONFIG_PID);
		context.registerService(ManagedService.class.getName(), new ConfigUpdater() , properties);
		LOGGER.info("Chargement du fichier de configuration /etc/" + CONFIG_PID + ".cfg");
	}
}
