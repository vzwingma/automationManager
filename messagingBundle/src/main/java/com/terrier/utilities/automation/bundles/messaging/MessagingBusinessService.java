package com.terrier.utilities.automation.bundles.messaging;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;

/**
 * Classe de service de messaging
 * @author vzwingma
 *
 */
@Singleton
public class MessagingBusinessService extends AbstractAutomationService {

	
	
	/**
	 * Initialisation
	 */
	@PostConstruct
	public void initService(){
		registerToConfig("com.terrier.utilities.automation.bundles.messaging");
	}
	
	
	
	@Override
	public void notifyUpdateDictionnary() {
		// TODO Auto-generated method stub
		
	}

}
