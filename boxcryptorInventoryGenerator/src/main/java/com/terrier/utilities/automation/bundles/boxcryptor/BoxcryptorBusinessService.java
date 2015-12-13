/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor;

import javax.inject.Singleton;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;

/**
 * @author vzwingma
 *
 */
@Singleton
public class BoxcryptorBusinessService extends AbstractAutomationService{


	private static final String CONFIG_PID = "com.terrier.utilities.automation.bundles.boxcryptor";
	
	/**
	 * Constructeur
	 */
	public BoxcryptorBusinessService() {
		super.registerToConfig(CONFIG_PID);
	}
	
	@Override
	public void notifyUpdateDictionary() {
		// TODO Auto-generated method stub
		
	}

}
