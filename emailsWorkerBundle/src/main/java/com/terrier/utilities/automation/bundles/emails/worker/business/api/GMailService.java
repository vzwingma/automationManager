package com.terrier.utilities.automation.bundles.emails.worker.business.api;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.Gmail.Users.Messages;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartBody;
import com.google.api.services.gmail.model.MessagePartHeader;
import com.google.api.services.gmail.model.ModifyMessageRequest;

/**
 * Service des API Gmail
 * @author PVZN02821
 *
 */
public class GMailService{


	protected final Logger logger = LoggerFactory.getLogger( this.getClass() );

	// API Gmails
	private Messages gmailAPI = null;

	public GMailService(Gmail gmailAPI) {
		if(gmailAPI != null){
			this.gmailAPI = gmailAPI.users().messages();
		}
	}

	private static final String GMAIL_USER 		= "me";
	private static final String GMAIL_INBOX 	= "INBOX";
	
	public static final String HEADER_FROM 		= "From";
	public static final String HEADER_SUBJECT 	= "Subject";

	public static final String PART_FILENAME 	= "FileName";
	public static final String PART_MIME	 	= "MIME";
	
	/**
	 * @return liste des mails Inbox
	 */
	public List<Message> getMailsInbox(){
		try {
			if(gmailAPI != null){
				return gmailAPI
						.list(GMAIL_USER)
						.setLabelIds(Arrays.asList(GMAIL_INBOX)).execute().getMessages()
						.stream()
						.map(m -> getMessage(m.getId()))
						.collect(Collectors.toList());
			}
		} catch (Exception e) {
			logger.error("Erreur lors de la recherche des emails", e);
		}
		return new ArrayList<>();
	}

	/**
	 * @param idMessage
	 * @return message
	 */
	private Message getMessage(String idMessage){
		if(gmailAPI != null){
			try {
				return gmailAPI.get(GMAIL_USER, idMessage).execute();
			} catch (Exception e) {
				logger.error("Erreur lors du chargement du mail [{}]", idMessage, e);
			}
		}
		return null;
	}

	/**
	 * @param idMessage
	 * @return message
	 */
	public List<MessagePartBody> getAttachements(Message message, String mime){
		if(gmailAPI != null && message != null && message.getPayload() != null && message.getPayload().getParts() != null){
			// Parcours des parts
			return message.getPayload().getParts()
					.stream()
					.filter(p -> p.getBody().getAttachmentId() != null
							&& (mime == null || mime.equals(p.getMimeType()))
							)
					// Map en attachement
					.map(
						p -> {
							MessagePartBody pj = null;
							try {
								// Recherche de l'attachement et complétion avec les métadonnées du message pour le mapping 
								pj = gmailAPI.attachments().get(GMAIL_USER, message.getId(), p.getBody().getAttachmentId()).execute();
								pj.set(HEADER_FROM, getSender(message));
								pj.set(HEADER_SUBJECT, getObject(message));
								pj.set(PART_FILENAME, p.getFilename());
							} catch (IOException e) {
								logger.error("Erreur lors de la recherche de la pièce jointe {}", getSender(message));
							}
							return pj;
						})
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	/**
	 * @param message
	 * @return message
	 */
	public boolean archiveMessage(Message message){
		if(gmailAPI != null){
			try {
				ModifyMessageRequest archive = new ModifyMessageRequest();
				archive.setRemoveLabelIds(Arrays.asList(GMAIL_INBOX));
				logger.info("Archivage de {}", message);
				return gmailAPI.modify(GMAIL_USER, message.getId(), archive).execute() != null;
			} catch (Exception e) {
				logger.error("Erreur lors de l'archivage du mail [{}]", message.getId(), e);
			}
		}
		return false;
	}

	/**
	 * @param idMessage
	 * @return contenu du message
	 */
	public String getBody(Message message){
		if(message != null){
			return new String(message.getPayload().getBody().decodeData());
		}
		return null;
	}

	/**
	 * @param idMessage
	 * @return sender du message
	 */
	public String getSender(Message message){
		return getHeader(message, HEADER_FROM);
	}

	/**
	 * @param idMessage
	 * @return objet du message
	 */
	public String getObject(Message message){
		return getHeader(message, HEADER_SUBJECT);
	}


	/**
	 * @param idMessage
	 * @param headerName
	 * @return entête correspondant au header
	 */
	private String getHeader(Message message, String headerName){
		if(message != null){
			Optional<String> sender = message.getPayload().getHeaders()
					.stream()
					.filter(header -> header.getName().equals(headerName))
					.map(MessagePartHeader::getValue).findFirst();
			if(sender.isPresent()){
				return sender.get();
			}
		}
		return null;
	}
}
