/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.internal.util.collection.MultivaluedStringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;

/**
 * Tâche d'envoi des mails
 * @author vzwingma
 *
 */
public class SendEmailTaskRunnable extends AbstractSendTaskRunnable  {



	private static final Logger LOGGER = LoggerFactory.getLogger( SendEmailTaskRunnable.class );

	/**
	 * Config 
	 */
	private String apiKey;
	private String apiURL;
	private String apiDomain;
	private String apiService;
	private String listeDestinataires;
	

	
	/**
	 * Constructeur de la tâche d'envoi
	 * @param messagesSendingQueue
	 */
	public SendEmailTaskRunnable(final String apiKey, final String apiURL, final String apiDomain, final String apiService, final String listeDestinataires, final MessagingBusinessService service ) {
		this.apiKey = apiKey;
		this.apiURL = apiURL;
		this.apiDomain = apiDomain;
		this.apiService = apiService;
		this.listeDestinataires = listeDestinataires;
		super.setService(service);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void executeMessagesTask() {
		LOGGER.info("Envoi des emails : {} messages en attente", getService().getEmailsSendingQueue().size());
		if(! getService().getEmailsSendingQueue().isEmpty()){
			boolean resultat = sendAllMessages();
			LOGGER.info("> Résulat des envois : {}", resultat);
		}
	}


	/**
	 * @return résultat de l'envoi des messages
	 */
	private boolean sendAllMessages(){

		boolean allResponses = true;

		// Envoi de tous les mails, groupé par titre :
		for (Iterator<Entry<String, ConcurrentLinkedQueue<String>>> gmIterator = getService().getEmailsSendingQueue().entrySet().iterator(); gmIterator.hasNext();) {

			Entry<String, ConcurrentLinkedQueue<String>> groupeMessages = gmIterator.next();
			if(groupeMessages.getValue() != null && !groupeMessages.getValue().isEmpty()){
				MultivaluedMap<String, String> formData = getFormData(groupeMessages.getKey(), groupeMessages.getValue());
				LOGGER.debug("Envoi du mail : {}", formData.get("subject"));
				
				if(super.sentMessages.contains(formData.get("html"))){
					LOGGER.warn("Le message a déjà été envoyé. Pas d'envoi");
					gmIterator.remove();
				}
				else{
					HttpAuthenticationFeature feature = HttpAuthenticationFeature.basicBuilder()
						    .nonPreemptive()
						    .credentials("api", this.apiKey)
						    .build();
					Invocation.Builder invocation = getInvocation(getClient(feature), this.apiURL + this.apiDomain, this.apiService, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
					boolean resultat = callHTTPPost(invocation , Entity.form(formData));
					if(resultat){
						LOGGER.debug("Suppression des messages de [{}] de la liste d'envoi", groupeMessages.getKey());
						sentMessages.add(formData.get("html"));
						gmIterator.remove();
					}
					else{
						// si erreur, on utilise l'autre canal pour envoyer le message d'erreur
						getService().sendNotificationSMS("Erreur lors de l'envoi du mail, les messages de ["+groupeMessages.getKey()+"] n'ont pas été envoyé.");
						LOGGER.error("Erreur lors de l'envoi, les messages de [{}] sont reprogrammés pour la prochaine échéance", groupeMessages.getKey());
					}
					allResponses &= resultat;
				}
			}
		}
		return allResponses;
	}


	/**
	 * Prépare les données
	 * @param titre
	 * @param message
	 * @return formData pour l'email
	 */
	private MultivaluedMap<String, String> getFormData(String titre, ConcurrentLinkedQueue<String> messages) {
		MultivaluedMap<String, String> formData = new MultivaluedStringMap();
		formData.add("from", "Automation Messaging Service <postmaster@"+this.apiDomain+">");
		formData.add("to", this.listeDestinataires);
		formData.add("subject", titre);

		StringBuilder messageAEnvoyer = new StringBuilder("<h3>Liste des messages</h3><ul>");
		for (String msg : messages) {
			messageAEnvoyer.append("<li>").append(msg).append("</li>");
		}
		messageAEnvoyer.append("</ul>");
		formData.add("html", messageAEnvoyer.toString());
		return formData;
	}

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.messaging.runnable.AbstractHTTPClientRunnable#updateSupervisionEvents(java.util.List)
	 */
	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Nombre d'emails en attente", 
						this.getService().getEmailsSendingQueue().size(),
						StatutPropertyBundleEnum.OK));
		
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Dernier d'appel du service " + this.apiURL, 
						this.getLastResponseCode() == 0 ? "?" : this.getLastResponseCode(), getCode(this.getLastResponseCode())));
	}
}
