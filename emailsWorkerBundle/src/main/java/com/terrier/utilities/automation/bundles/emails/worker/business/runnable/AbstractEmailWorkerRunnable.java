/**
 * 
 */
package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.gmail.Gmail;
import com.terrier.utilities.automation.bundles.emails.worker.business.api.GMailService;

/**
 * Travail sur un pattern d'email
 * @author PVZN02821
 *
 */
public abstract class AbstractEmailWorkerRunnable extends GMailService implements Runnable {


	
	private final int index;
	
	public AbstractEmailWorkerRunnable(int index, Gmail gmailAPI) {
		super(gmailAPI);
		this.index = index;
		LOGGER.info("[{}] Worker {}", index, this.getClass().getSimpleName());
	}
	
	
	@Override
	public void run() {
		executeRule();
	}

	public abstract void executeRule();

	/**
	 * @return the index
	 */
	public final int getIndex() {
		return index;
	}
}
