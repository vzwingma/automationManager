package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;
import com.terrier.utilities.automation.bundles.communs.utils.files.FileUtils;
import com.terrier.utilities.automation.bundles.emails.worker.business.HubicClient;

/**
 * Travail sur les mails HUBIC
 * @author PVZN02821
 *
 */
public class HubicEmailsWorkerRunnable extends AbstractEmailWorkerRunnable {

	public HubicEmailsWorkerRunnable(int index, Gmail gmailService) {
		super(index, gmailService);
	}

	protected static final String HUBIC_SENDER = "no-reply@hubic.com";

	private HubicClient client = new HubicClient();

	protected String repertoire = "src/test/resources";

	@Override
	public void executeRule() {
		// Liste des messages
		List<Message> messagesInbox = getMailsInbox();

		List<String> messagesHubic = getMessagesHubic(messagesInbox);
		// Traitement des messages
		LOGGER.info("Traitement des {} mails HUBIC parmi {}" , messagesHubic.size(), messagesInbox.size() );
		if(!messagesHubic.isEmpty()){
			messagesHubic
			.parallelStream()
			.forEach(hubic -> downloadFacture(hubic));
		}

	}

	/**
	 * @param messagesInbox
	 * @return liste des messages body pour le sender HUBIC_SENDER
	 */
	private List<String> getMessagesHubic(List<Message> messagesInbox){
		// Chargement des body
		List<String> messagesHubic = messagesInbox
				.parallelStream()
				.filter(m -> getSender(m.getId()).equalsIgnoreCase(HUBIC_SENDER))
				.map(m -> getBody(m.getId()))
				.collect(Collectors.toList());
		return messagesHubic;
	}


	/**
	 * Téléchargement de la facture
	 * @param messageHubic
	 */
	protected void downloadFacture(String messageHubic){
		String getURL = getURLFromBody(messageHubic);
		String reference =  getReference(getURL);
		LOGGER.info("Téléchargement de la facture [{}] : [{}]", reference, getURL);

		try {
			InputStream streamPdf = client.telechargementFichier(getURL);
			FileUtils.saveStreamToFile(streamPdf, repertoire+"/Facture_" + reference+".pdf");
			LOGGER.info("Fichier téléchargé [{}]", new File(repertoire).getAbsolutePath());
		} catch (Exception e) {
			LOGGER.error("Erreur lors du téléchargement de la facture {}", e);
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
