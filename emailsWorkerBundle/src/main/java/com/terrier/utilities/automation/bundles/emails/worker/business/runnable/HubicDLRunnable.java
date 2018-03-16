package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.terrier.utilities.automation.bundles.communs.utils.files.FileUtils;
import com.terrier.utilities.automation.bundles.emails.worker.business.EmailsWorkerBusinessService;
import com.terrier.utilities.automation.bundles.emails.worker.business.HubicClient;

/**
 * Travail sur les mails qui nécessitent de télécharger depuis un lien (ex: HUBIC)
 * @author PVZN02821
 *
 */
public class HubicDLRunnable extends AbstractEmailRunnable {

	/**
	 * @param index de la configuration
	 * @param gmailService service email
	 * @param service
	 */
	public HubicDLRunnable(int index, String nomFournisseur, Gmail gmailService, EmailsWorkerBusinessService service) {
		super(index, nomFournisseur, gmailService, service);
	}

	protected static final String HUBIC_SENDER = "no-reply@hubic.com";

	private static final String NOTIF_HEADER = "Emails Worker";
	
	private HubicClient client = new HubicClient();

	protected String repertoire = "src/test/resources";

	@Override
	public long executeRule() {
		// Liste des messages
		List<Message> messagesInbox = getMailsInbox();
		long nbMessagesTraites = 0L;
		// Traitement des messages
		logger.info("Traitement des mails HUBIC parmi {}" , messagesInbox.size() );
		if(!messagesInbox.isEmpty()){
			nbMessagesTraites = messagesInbox
			.parallelStream()
			.filter(m -> HUBIC_SENDER.equalsIgnoreCase(getSender(m.getId())))
			.filter(m -> {
				String body = getBody(m.getId()); 
				return downloadFacture(body);	
			})
			.filter(m -> archiveMessage(m.getId()))
			.count();
			
			getBusinessService().sendNotificationMessage(NOTIF_HEADER, "Traitement de " + nbMessagesTraites + " parmi " + messagesInbox.size());
		}
		return nbMessagesTraites;
	}



	/**
	 * Téléchargement de la facture
	 * @param messageHubic
	 */
	protected boolean downloadFacture(String messageHubic){
		String getURL = getURLFromBody(messageHubic);
		String reference =  getReference(getURL);
		logger.info("Téléchargement de la facture [{}] : [{}]", reference, getURL);

		try {
			InputStream streamPdf = client.telechargementFichier(getURL);
			FileUtils.saveStreamToFile(streamPdf, repertoire+"/Facture_" + reference+".pdf");
			logger.info("Fichier téléchargé [{}]", new File(repertoire).getAbsolutePath());
			getBusinessService().sendNotificationMessage(NOTIF_HEADER, "La facture " + reference + " a été téléchargée");
			return true;
		} catch (Exception e) {
			logger.error("Erreur lors du téléchargement de la facture {}", e);
			getBusinessService().sendNotificationMessage(NOTIF_HEADER, "Erreur lors du traitement de la facture " + reference);
			return false;
		}
	}


	protected String getReference(String urlHubic){
		Matcher matcher = Pattern.compile("reference=(.*)&").matcher(urlHubic);
		if (matcher.find())
		{
			return matcher.group(1).trim();
		}
		return null;
	}


	protected String getURLFromBody(String messageHubic){
		Matcher matcher = Pattern.compile("(https:\\/\\/www.ovh.com.*)").matcher(messageHubic);
		if (matcher.find())
		{
			return matcher.group(1).trim();
		}
		return null;
	}

	/**
	 * @param client the client to set
	 */
	public final void setClient(HubicClient client) {
		this.client = client;
	}
}
