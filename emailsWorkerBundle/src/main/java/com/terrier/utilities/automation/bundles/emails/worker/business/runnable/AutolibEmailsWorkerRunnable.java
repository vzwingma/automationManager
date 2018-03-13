package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import java.util.List;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.terrier.utilities.automation.bundles.emails.worker.business.EmailsWorkerBusinessService;

/**
 * Travail sur les mails Autolib
 * @author PVZN02821
 *
 */
public class AutolibEmailsWorkerRunnable extends AbstractEmailWorkerRunnable {

	/**
	 * @param index
	 * @param gmailService
	 * @param service
	 */
	public AutolibEmailsWorkerRunnable(int index, Gmail gmailService, EmailsWorkerBusinessService service) {
		super(index, gmailService, service);
	}

	protected static final String AUTOLIB_SENDER = "no-reply@autolib.eu";


	@Override
	public long executeRule() {
		
		long nbMessagesTraites = 0L;
		// Liste des messages
		List<Message> messagesInbox = getMailsInbox();
		// Traitement des messages
		logger.info("Traitement des mails Autolib parmi {}" , messagesInbox.size() );
		if(!messagesInbox.isEmpty()){
			nbMessagesTraites = messagesInbox
			.stream()
			.filter(m -> AUTOLIB_SENDER.equalsIgnoreCase(getSender(m.getId())))
			.filter(m -> archiveMessage(m.getId()))
			.count();
			getBusinessService().sendNotificationMessage(EmailsWorkerBusinessService.NOTIF_HEADER, "Archivage de " + nbMessagesTraites + " mails Autolib parmi " + messagesInbox.size());
		}
		return nbMessagesTraites;
	}
}
