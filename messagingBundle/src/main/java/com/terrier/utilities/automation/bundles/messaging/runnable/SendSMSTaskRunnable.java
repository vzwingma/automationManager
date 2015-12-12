/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * Tâche d'envoi des mails
 * @author vzwingma
 *
 */
public class SendSMSTaskRunnable implements Runnable {



	private static final Logger LOGGER = LoggerFactory.getLogger( SendSMSTaskRunnable.class );

	/**
	 * Liste 
	 */
	private ConcurrentLinkedQueue<String> messagesSendingQueue = new ConcurrentLinkedQueue<String>();
	private String user;
	private String password;
	private String apiURL;

	/**
	 * Constructeur de la tâche d'envoi
	 * @param messagesSendingQueue
	 */
	public SendSMSTaskRunnable(final String user, final String password, final String apiURL, final ConcurrentLinkedQueue<String> messagesSendingQueue) {
		this.user = user;
		this.password = password;
		this.apiURL = apiURL;
		this.messagesSendingQueue = messagesSendingQueue;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		LOGGER.info("Envoi des SMS");
		boolean resultat = sendAllMessages();
		LOGGER.info("> Résulat des envois : {}", resultat);
	}


	/**
	 * @return résultat de l'envoi des messages
	 */
	public boolean sendAllMessages(){

		Client client = getClient();

		boolean allResponses = true;

		String prepareAPIURL = this.apiURL + "user=" + this.user + "&pass=" + this.password + "&msg=";

		// Envoi de tous les mails, groupé par titre :

		if(this.messagesSendingQueue.size() > 0){
			String messageSMS = getFormData(this.messagesSendingQueue);
			WebResource.Builder webResource = client.resource(prepareAPIURL + messageSMS).type(MediaType.APPLICATION_FORM_URLENCODED);
			ClientResponse response = webResource.get(ClientResponse.class);
			LOGGER.info("> Resultat : " + response);
			boolean resultat = response != null && response.getStatus() == 200;
			if(resultat){
				LOGGER.debug("Suppression des messages de [{}] de la liste d'envoi");
			}
			else{
				LOGGER.error("Erreur lors de l'envoi, les messages de [{}] sont reprogrammés pour la prochaine échéance");
			}
			allResponses &= resultat;
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
	 * @param messages liste des messages
	 * @return données
	 */
	private String getFormData(ConcurrentLinkedQueue<String> messagesSendingQueue) {
		StringBuilder messageAEnvoyer = new StringBuilder();
		while(messagesSendingQueue.size() > 0){
			messageAEnvoyer.append("- ").append(messagesSendingQueue.poll()).append("\n");
		}
		return messageAEnvoyer.toString();
	}

}
