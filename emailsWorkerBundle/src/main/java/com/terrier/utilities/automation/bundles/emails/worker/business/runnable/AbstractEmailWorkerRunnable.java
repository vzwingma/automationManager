/**
 * 
 */
package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.gmail.Gmail;

/**
 * Travail sur un pattern d'email
 * @author PVZN02821
 *
 */
public abstract class AbstractEmailWorkerRunnable implements Runnable {


	private static final Logger LOGGER = LoggerFactory.getLogger( AbstractEmailWorkerRunnable.class );
	
	private final int index;
	
	private final Gmail gmailService;
	
	public AbstractEmailWorkerRunnable(int index, final Gmail gmailService){
		this.index = index;
		this.gmailService = gmailService;
		LOGGER.info("[{}] Worker", index);
	}
	
	@Override
	public void run() {
		executeRule();
	}

	public abstract void executeRule(); 
}
