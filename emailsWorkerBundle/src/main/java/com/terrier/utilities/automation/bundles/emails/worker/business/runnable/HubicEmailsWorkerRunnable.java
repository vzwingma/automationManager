package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import com.google.api.services.gmail.Gmail;

/**
 * Travail sur les mails HUBIC
 * @author PVZN02821
 *
 */
public class HubicEmailsWorkerRunnable extends AbstractEmailWorkerRunnable {

	public HubicEmailsWorkerRunnable(int index, Gmail gmailService) {
		super(index, gmailService);
	}

	@Override
	public void executeRule() {
		// TODO Auto-generated method stub
		
	}

}
