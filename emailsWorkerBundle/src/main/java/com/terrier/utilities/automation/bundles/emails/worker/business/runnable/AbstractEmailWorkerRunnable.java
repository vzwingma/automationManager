/**
 * 
 */
package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import com.google.api.services.gmail.Gmail;
import com.terrier.utilities.automation.bundles.emails.worker.business.EmailsWorkerBusinessService;
import com.terrier.utilities.automation.bundles.emails.worker.business.api.GMailService;

/**
 * Travail sur un pattern d'email
 * @author PVZN02821
 *
 */
public abstract class AbstractEmailWorkerRunnable extends GMailService implements Runnable {

	private final EmailsWorkerBusinessService service;
	
	
	private final int index;
	
	public AbstractEmailWorkerRunnable(int index, Gmail gmailAPI, EmailsWorkerBusinessService service) {
		super(gmailAPI);
		this.index = index;
		this.service = service;
		logger.info("[{}] Worker {}", index, this.getClass().getSimpleName());
	}
	
	
	@Override
	public void run() {
		executeRule();
	}

	public abstract long executeRule();

	/**
	 * @return the index
	 */
	public final int getIndex() {
		return index;
	}


	/**
	 * @return the service
	 */
	public final EmailsWorkerBusinessService getBusinessService() {
		return service;
	}
}
