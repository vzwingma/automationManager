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
public abstract class AbstractEmailRunnable extends GMailService implements Runnable {

	private final EmailsWorkerBusinessService service;
	
	
	private final int index;
	private final String nomFournisseur;
	
	
	public AbstractEmailRunnable(int index, String nomFournisseur, Gmail gmailAPI, EmailsWorkerBusinessService service) {
		super(gmailAPI);
		this.index = index;
		this.nomFournisseur = nomFournisseur;
		this.service = service;
		logger.info("[{}][{}] Worker {}", this.index, this.nomFournisseur, this.getClass().getSimpleName());
	}
	
	
	@Override
	public void run() {
		logger.info("[{}][{}] Exécution de la règle", this.index, this.nomFournisseur);
		try{
			long nbres = executeRule();
			logger.debug("[{}][{}] {} éléments traités", this.index, this.nomFournisseur, nbres);
		}
		catch(Exception e){
			logger.error("[{}][{}] Erreur lors de l'exécution de la règle", this.index, this.nomFournisseur, e);
		}
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
