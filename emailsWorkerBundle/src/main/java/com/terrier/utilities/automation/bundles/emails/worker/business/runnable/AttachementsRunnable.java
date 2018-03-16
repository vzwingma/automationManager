package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import java.util.Arrays;
import java.util.List;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.terrier.utilities.automation.bundles.emails.worker.business.EmailsWorkerBusinessService;

/**
 * Travail sur les mails avec PDF joints
 * @author PVZN02821
 *
 */
public class AttachementsRunnable extends AbstractEmailRunnable {

	/**
	 * @param index
	 * @param gmailService
	 * @param service
	 */
	public AttachementsRunnable(int index, String nomFournisseur, Gmail gmailService, EmailsWorkerBusinessService service) {
		super(index, nomFournisseur, gmailService, service);
	}

	protected static final String AUTOLIB_SENDER = "no-reply@autolib.eu";
	protected static final List<String> AUTOLIB_OBJECTS = Arrays.asList("Autolib' - Facturation", "Autolib' : Ticket de débit");


	@Override
	public long executeRule() {
		
		long nbMessagesTraites = 0L;
		// Liste des messages
		List<Message> messagesInbox = getMailsInbox();
		// Traitement des messages
		logger.info("Traitement des mails avec pièces jointes parmi {}" , messagesInbox.size() );
		if(!messagesInbox.isEmpty()){
			nbMessagesTraites = messagesInbox
			.stream()
			
			.parallel()
			.filter(m -> AUTOLIB_OBJECTS.contains(getObject(m.getId())))
			.filter(m -> archiveMessage(m.getId()))
			.count();
			
			getBusinessService().sendNotificationMessage(EmailsWorkerBusinessService.NOTIF_HEADER, "Archivage de " + nbMessagesTraites + " mails Autolib parmi " + messagesInbox.size());
		}
		return nbMessagesTraites;
	}
}
