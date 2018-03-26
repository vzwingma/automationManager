package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartBody;
import com.terrier.utilities.automation.bundles.emails.worker.business.EmailsWorkerBusinessService;
import com.terrier.utilities.automation.bundles.emails.worker.business.api.GMailService;

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


	public static final String MIME_PDF = "application/pdf";


	@Override
	public long executeRule() {

		AtomicLong nbMessagesTraites = new AtomicLong(0);
		// Liste des messages
		List<Message> messagesInbox = getMailsInbox();
		// Traitement des messages
		logger.info("Traitement des mails avec pièces jointes parmi {}" , messagesInbox.size() );
		if(!messagesInbox.isEmpty()){
			messagesInbox
			.parallelStream()
			// Recherche des PJs
			.map(m -> getAttachements(m, MIME_PDF))
			.flatMap(List::stream)
			// Recherche des pieces jointes 
			.forEach(pj ->  {
				logger.info("Téléchargement de la pièce jointe de {} : {}", pj.get(GMailService.HEADER_FROM), pj.get(GMailService.HEADER_SUBJECT));
				getBusinessService().sendNotificationMessage(EmailsWorkerBusinessService.NOTIF_HEADER, "Téléchargement de la pièce jointe de " + pj.get(GMailService.HEADER_FROM) + "/" +  pj.get(GMailService.HEADER_SUBJECT));
				downloadPdfFromMulipart(pj);
				nbMessagesTraites.incrementAndGet();
			}); 
		}
		return nbMessagesTraites.get();
	}


	/**
	 * Téléchargement du fichier depuis la multipart base64
	 * @param p part
	 * @throws IOException 
	 */
	private void downloadPdfFromMulipart(MessagePartBody attachPart) {
		byte[] fileByteArray = Base64.decodeBase64(attachPart.getData());
		File fichier = new File(this.getBusinessService().getDestinationDirectory(), attachPart.get(GMailService.PART_FILENAME).toString());
		try(FileOutputStream fileOutFile =	new FileOutputStream(fichier)){
			fileOutFile.write(fileByteArray);
		}
		catch(Exception e){
			logger.error("Erreur lors du téléchargement de la pièce jointe {}", attachPart.get(GMailService.PART_FILENAME));
		}

	}
}
