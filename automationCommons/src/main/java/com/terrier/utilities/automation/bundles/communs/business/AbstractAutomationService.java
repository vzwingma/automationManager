/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.business;

import java.util.Dictionary;
import java.util.Hashtable;

import org.apache.log4j.Logger;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;

import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;

/**
 * Classe d'un service
 * @author vzwingma
 *
 */
public abstract class AbstractAutomationService implements ManagedService {


	private static final Logger LOGGER = Logger.getLogger( AbstractAutomationService.class );

	// Dictionnaire
	private Dictionary<String, String> dictionnaire;
	/**
	 * Démarrage du service
	 */

	/**
	 * Enregistrement aux modifications du fichier de configuration
	 * @param configPID nom du fichier de configuration
	 */
	public void registerToConfig(String configPID){
		LOGGER.info("Enregistrement au fichier de configuration : " + configPID);
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.put(Constants.SERVICE_PID, configPID);
		FrameworkUtil.getBundle(this.getClass()).getBundleContext().registerService(ManagedService.class.getName(), this , properties);
		LOGGER.info("Chargement du fichier de configuration /etc/" + configPID + ".cfg");
	}



	/* (non-Javadoc)
	 * @see org.osgi.service.cm.ManagedService#updated(java.util.Dictionary)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void updated(Dictionary<String, ?> properties) throws ConfigurationException {
		if(properties != null){
			LOGGER.info("Mise à jour du fichier de configuration");
			this.dictionnaire = (Dictionary<String, String>)properties;
			notifyUpdateDictionnary();
		}
		else{
			LOGGER.error("Impossible de trouver le fichier de configuration");
		}
	}


	/**
	 * Mise à jour du dictionnaire
	 * @param dictionary dictionnaire
	 */
	public abstract void notifyUpdateDictionnary(); 
	
	
	/**
	 * @param key clé à charger du fichier
	 * @return valeur de la clé dans la configuration
	 */
	public String getConfig(String key) throws KeyNotFoundException{
		if(this.dictionnaire != null && key != null){
			return this.dictionnaire.get(key);
		}
		else{
			throw new KeyNotFoundException(key);
		}
	}
}
