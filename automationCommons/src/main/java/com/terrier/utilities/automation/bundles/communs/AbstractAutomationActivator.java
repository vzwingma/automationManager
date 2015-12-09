/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

/**
 * Activator de l'Automation Manager
 * @author vzwingma
 *
 */
public abstract class AbstractAutomationActivator implements BundleActivator, ManagedService {


	 private static final Logger LOGGER = Logger.getLogger( AbstractAutomationActivator.class );
	 
	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		LOGGER.info("Demarrage du bundle");
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(Constants.SERVICE_PID, getConfigurationPID());
		context.registerService(ManagedService.class.getName(), this , properties);
		LOGGER.info("Chargement du fichier de configuration /etc/" + getConfigurationPID() + ".cfg");
	}
	

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		LOGGER.info("Arrêt du bundle");
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updated(@SuppressWarnings("rawtypes") Dictionary properties) throws ConfigurationException {

		if(properties != null){
			LOGGER.info("Mise à jour du fichier de configuration");
			Dictionary<String, String> dictionnaire = (Dictionary<String, String>)properties;
			updateDictionnary(dictionnaire);
		}
		else{
			LOGGER.error("Impossible de trouver le fichier de configuration");
		}
	}
	
	
	/**
	 * Mise à jour du dictionnaire
	 * @param dictionary dictionnaire
	 */
	public abstract void updateDictionnary(Dictionary<String, String> dictionary); 
	
	/**
	 * @return le PID du fichier de configuration
	 */
	public abstract String getConfigurationPID();
}
