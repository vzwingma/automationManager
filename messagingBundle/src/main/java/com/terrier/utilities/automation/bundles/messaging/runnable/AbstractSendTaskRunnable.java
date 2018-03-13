/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import java.util.ArrayList;
import java.util.List;

import com.terrier.utilities.automation.bundles.communs.http.AbstractHTTPClient;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;

/**
 * Classe abstraite d'envoi
 * @author PVZN02821
 *
 */
public abstract class AbstractSendTaskRunnable extends AbstractHTTPClient implements Runnable {

	// Service Métier
	private MessagingBusinessService service;
	
	protected List<Object> sentMessages = new ArrayList<>();
	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		executeMessagesTask();
	}
	


	/**
	 * Méthode de traitement runnable
	 */
	public abstract void executeMessagesTask();
	
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
	
	/**
	 * Ajout des informations du bundle à superviser
	 * @param supervisionEvents événements de supervision, sous la forme titre->Données
	 */
	public abstract void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents);


}
