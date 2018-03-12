/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;
import com.terrier.utilities.automation.bundles.messaging.http.client.AbstractHTTPClient;

/**
 * Classe abstraite d'envoi
 * @author PVZN02821
 *
 */
public abstract class AbstractSendTaskRunnable extends AbstractHTTPClient implements Runnable {

	// Service Métier
	private MessagingBusinessService service;
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		executeMessagesTask();
	}
	

	/**
	 * @param service the service to set
	 */
	public void setService(MessagingBusinessService service) {
		this.service = service;
	}

	/**
	 * @return the service
	 */
	public MessagingBusinessService getService() {
		return service;
	}
}
