/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
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
		LOGGER.info("Envoi des SMS : {} messages en attente", this.messagesSendingQueue.size());
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

			List<String> messages = new ArrayList<>();
			while(messagesSendingQueue.size() > 0){
				messages.add(messagesSendingQueue.poll());
			}
			LOGGER.debug("Envoi des {} messages par SMS", messages.size());
			boolean resultat = false;
			try{
				WebResource.Builder webResource = client.resource(prepareAPIURL + getFormData(messages)).type(MediaType.APPLICATION_FORM_URLENCODED);
				ClientResponse response = webResource.get(ClientResponse.class);
				LOGGER.debug("> Resultat : {}", response);
				resultat = response != null && response.getStatus() == 200;
			}
			catch(Exception e){
				LOGGER.error("> Resultat : Erreur lors de l'envoi du SMS", e);
			}
			if(resultat){
				LOGGER.debug("Suppression des messages SMS de la liste d'envoi");
			}
			else{
				for (String msg : messages) {
					messagesSendingQueue.add(msg);
				}
				LOGGER.error("Erreur lors de l'envoi, les messages sont reprogrammés pour la prochaine échéance");
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
	private String getFormData(List<String> messages) {
		try {
			StringBuilder messageAEnvoyer = new StringBuilder();
			for (String msg : messages) {
				messageAEnvoyer.append("- ").append(msg).append("\n");
			}
			return URLEncoder.encode(messageAEnvoyer.toString(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			LOGGER.error("Erreur lors de l'encodage du message", e);
			return "Erreur%20encoding%20messages";
		}
	}

}
