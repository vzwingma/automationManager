/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;

/**
 * Tâche d'envoi des mails
 * @author vzwingma
 *
 */
public class SendEmailTaskRunnable implements Runnable {



	private static final Logger LOGGER = LoggerFactory.getLogger( SendEmailTaskRunnable.class );

	/**
	 * Config 
	 */
	private String apiKey;
	private String apiURL;
	private String apiDomain;
	private String listeDestinataires;
	// Service métier
	private MessagingBusinessService service;
	
	/**
	 * Constructeur de la tâche d'envoi
	 * @param messagesSendingQueue
	 */
	public SendEmailTaskRunnable(final String apiKey, final String apiURL, final String apiDomain, final String listeDestinataires, final MessagingBusinessService service ) {
		this.apiKey = apiKey;
		this.apiURL = apiURL;
		this.apiDomain = apiDomain;
		this.listeDestinataires = listeDestinataires;
		this.service = service;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Envoi des emails : {} messages en attente", this.service.getEmailsSendingQueue().size());
		if(this.service.getEmailsSendingQueue().size() > 0){
			boolean resultat = sendAllMessages();
			LOGGER.info("> Résulat des envois : {}", resultat);
		}
	}


	/**
	 * @return résultat de l'envoi des messages
	 */
	public boolean sendAllMessages(){

		Client client = getClient();
		client.addFilter(new HTTPBasicAuthFilter("api", this.apiKey));
		WebResource.Builder webResource =
				client.resource(this.apiURL).type(MediaType.APPLICATION_FORM_URLENCODED);

		boolean allResponses = true;

		// Envoi de tous les mails, groupé par titre :
		for (Iterator<Entry<String, ConcurrentLinkedQueue<String>>> gmIterator = service.getEmailsSendingQueue().entrySet().iterator(); gmIterator.hasNext();) {

			Entry<String, ConcurrentLinkedQueue<String>> groupeMessages = gmIterator.next();
			if(groupeMessages.getValue() != null && !groupeMessages.getValue().isEmpty()){
				MultivaluedMapImpl formData = getFormData(groupeMessages.getKey(), groupeMessages.getValue());
				LOGGER.debug("Envoi du mail : {}", formData.get("subject"));
				boolean resultat = false;
				try{
					ClientResponse response = webResource.post(ClientResponse.class, formData);
					LOGGER.debug("> Resultat : {}", response);
					resultat = response != null && response.getStatus() == 200;
				}
				catch(Exception e){
					LOGGER.error("> Resultat : Erreur lors de l'envoi du mail", e);
					this.service.sendNotificationSMS("Erreur lors de l'envoi du mail, les messages de ["+groupeMessages.getKey()+"] n'ont pas été envoyé :" + e.getMessage());
					resultat = false;
				}
				if(resultat){
					LOGGER.debug("Suppression des messages de [{}] de la liste d'envoi", groupeMessages.getKey());
					gmIterator.remove();
				}
				else{
					LOGGER.error("Erreur lors de l'envoi, les messages de [{}] sont reprogrammés pour la prochaine échéance", groupeMessages.getKey());
					// si erreur, on utilise l'autre canal pour envoyer le message d'erreur
					
				}
				allResponses &= resultat;
			}

		}
		return allResponses;
	}


	/**
	 * Créé un client HTTP 
	 * (dans une méthode séparée pour pouvoir être mocké facilement)
	 * @return client HTTP
	 */
	protected Client getClient(){
		return Client.create();
	}

	/**
	 * Prépare les données
	 * @param titre
	 * @param message
	 * @return formData pour l'email
	 */
	private MultivaluedMapImpl getFormData(String titre, ConcurrentLinkedQueue<String> messages) {
		MultivaluedMapImpl formData = new MultivaluedMapImpl();
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

}
