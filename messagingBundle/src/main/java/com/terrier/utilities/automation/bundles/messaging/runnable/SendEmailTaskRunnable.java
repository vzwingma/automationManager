/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

/**
 * Tâche d'envoi des mails
 * @author vzwingma
 *
 */
public class SendEmailTaskRunnable implements Runnable {



	private static final Logger LOGGER = LoggerFactory.getLogger( SendEmailTaskRunnable.class );

	/**
	 * Liste 
	 */
	private Map<String, List<String>> messagesSendingQueue = new ConcurrentHashMap<String, List<String>>();

	private String apiKey;
	private String apiURL;
	private String apiDomain;
	private String listeDestinataires;

	/**
	 * Constructeur de la tâche d'envoi
	 * @param messagesSendingQueue
	 */
	public SendEmailTaskRunnable(final String apiKey, final String apiURL, final String apiDomain, final String listeDestinataires, final Map<String, List<String>> messagesSendingQueue ) {
		this.apiKey = apiKey;
		this.apiURL = apiURL;
		this.apiDomain = apiDomain;
		this.listeDestinataires = listeDestinataires;
		this.messagesSendingQueue = messagesSendingQueue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Envoi des emails");
		boolean resultat = sendAllMessages();
		LOGGER.info("> Résulat des envois : {}", resultat);
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
		for (Iterator<Entry<String, List<String>>> gmIterator = messagesSendingQueue.entrySet().iterator(); gmIterator.hasNext();) {
			
			Entry<String, List<String>> groupeMessages = gmIterator.next();
			if(groupeMessages.getValue() != null && !groupeMessages.getValue().isEmpty()){
				MultivaluedMapImpl formData = getFormData(groupeMessages.getKey(), groupeMessages.getValue());
				ClientResponse response = webResource.post(ClientResponse.class, formData);
				LOGGER.info("> Resultat : " + response);
				boolean resultat = response != null && response.getStatus() == 200;
				if(resultat){
					LOGGER.debug("Suppression des messages de [{}] de la liste d'envoi", groupeMessages.getKey());
					gmIterator.remove();
				}
				else{
					LOGGER.error("Erreur lors de l'envoi, les messages de [{}] sont reprogrammés pour la prochaine échéance", groupeMessages.getKey());
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
	private MultivaluedMapImpl getFormData(String titre, List<String> messages) {
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
