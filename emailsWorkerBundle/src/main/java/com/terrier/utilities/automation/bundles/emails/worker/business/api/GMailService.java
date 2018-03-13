package com.terrier.utilities.automation.bundles.emails.worker.business.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.Message;

/**
 * Service des API Gmail
 * @author PVZN02821
 *
 */
public class GMailService{


	protected final Logger LOGGER = LoggerFactory.getLogger( GMailService.class );

	// API Gmails
	private Gmail gmailAPI;

	public GMailService(Gmail gmailAPI) {
		this.gmailAPI = gmailAPI;
	}

	private static final String USER_ME = "me";


	/**
	 * @return liste des mails Inbox
	 */
	public List<Message> getMailsInbox(){
		try {
			if(gmailAPI != null){
				return gmailAPI.users().messages().list(USER_ME).setLabelIds(Arrays.asList("INBOX")).execute().getMessages();
			}
		} catch (Exception e) {
			LOGGER.error("Erreur lors de la recherche des emails", e);
		}
		return new ArrayList<>();
	}

	/**
	 * @param idMessage
	 * @return message
	 */
	public Message getMessage(String idMessage){
		if(gmailAPI != null){
			try {
				return gmailAPI.users().messages().get(USER_ME, idMessage).execute();
			} catch (Exception e) {
				LOGGER.error("Erreur lors du chargement du mail [{}]", idMessage, e);
			}
		}
		return null;
	}


	/**
	 * @param idMessage
	 * @return contenu du message
	 */
	public String getBody(String idMessage){
		if(getMessage(idMessage) != null){
			return new String(getMessage(idMessage).getPayload().getBody().decodeData());
		}
		return null;
	}

	/**
	 * @param idMessage
	 * @return sender du message
	 */
	public String getSender(String idMessage){
		return getHeader(idMessage, "From");
	}

	/**
	 * @param idMessage
	 * @param headerName
	 * @return entête
	 */
	private String getHeader(String idMessage, String headerName){
		Message message = getMessage(idMessage);
		if(message != null){
			Optional<String> sender = message.getPayload().getHeaders()
					.stream()
					.filter(header -> header.getName().equals(headerName))
					.map(header -> header.getValue()).findFirst();
			if(sender.isPresent()){
				return sender.get();
			}
		}
		return null;
	}
}
