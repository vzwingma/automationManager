/**
 * 
 */
package com.terrier.utilities.automation.bundles.emails.worker.business;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;

/**
 * Service m�tier du worker
 * @author PVZN02821
 *
 */
@Singleton
public class EmailsWorkerBusinessService extends AbstractAutomationService {

	private static final Logger LOGGER = LoggerFactory.getLogger( EmailsWorkerBusinessService.class );
	
	public static final String CONFIG_PID = "com.terrier.utilities.automation.bundles.emails.worker";

	
	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#startService()
	 */
	@PostConstruct
	public void startService() {
		// Register config
		super.registerToConfig(CONFIG_PID);
		
		
		
	}
	
	@Override
	public void notifyUpdateDictionary() {

		LOGGER.info("** Configuration **");
		
		LOGGER.info("** **");
		
	}

	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {
		// TODO Auto-generated method stub
		
	}

}
