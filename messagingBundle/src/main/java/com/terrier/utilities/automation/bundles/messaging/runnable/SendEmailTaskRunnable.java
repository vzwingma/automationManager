/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;

/**
 * Tâche d'envoi des mails
 * @author vzwingma
 *
 */
public class SendEmailTaskRunnable extends AbstractHTTPClientRunnable  {



	private static final Logger LOGGER = LoggerFactory.getLogger( SendEmailTaskRunnable.class );

	/**
	 * Config 
	 */
	private String apiKey;
	private String apiURL;
	private String apiDomain;
	private String listeDestinataires;
	
	
	/**
	 * Constructeur de la tâche d'envoi
	 * @param messagesSendingQueue
	 */
	public SendEmailTaskRunnable(final String apiKey, final String apiURL, final String apiDomain, final String listeDestinataires, final MessagingBusinessService service ) {
		this.apiKey = apiKey;
		this.apiURL = apiURL;
		this.apiDomain = apiDomain;
		this.listeDestinataires = listeDestinataires;
		super.setService(service);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void HTTPClientRun() {
		LOGGER.info("Envoi des emails : {} messages en attente", getService().getEmailsSendingQueue().size());
		if(getService().getEmailsSendingQueue().size() > 0){
			boolean resultat = sendAllMessages();
			LOGGER.info("> Résulat des envois : {}", resultat);
		}
	}


	/**
	 * @return résultat de l'envoi des messages
	 */
	public boolean sendAllMessages(){

		boolean allResponses = true;

		// Envoi de tous les mails, groupé par titre :
		for (Iterator<Entry<String, ConcurrentLinkedQueue<String>>> gmIterator = getService().getEmailsSendingQueue().entrySet().iterator(); gmIterator.hasNext();) {

			Entry<String, ConcurrentLinkedQueue<String>> groupeMessages = gmIterator.next();
			if(groupeMessages.getValue() != null && !groupeMessages.getValue().isEmpty()){
				MultivaluedMapImpl formData = getFormData(groupeMessages.getKey(), groupeMessages.getValue());
				LOGGER.debug("Envoi du mail : {}", formData.get("subject"));
				boolean resultat = callHTTPPost(getClient(new HTTPBasicAuthFilter("api", this.apiKey)), this.apiURL, formData);
				if(resultat){
					LOGGER.debug("Suppression des messages de [{}] de la liste d'envoi", groupeMessages.getKey());
					gmIterator.remove();
				}
				else{
					getService().sendNotificationSMS("Erreur lors de l'envoi du mail, les messages de ["+groupeMessages.getKey()+"] n'ont pas été envoyé.");
					LOGGER.error("Erreur lors de l'envoi, les messages de [{}] sont reprogrammés pour la prochaine échéance", groupeMessages.getKey());
					// si erreur, on utilise l'autre canal pour envoyer le message d'erreur
					
				}
				allResponses &= resultat;
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

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.messaging.runnable.AbstractHTTPClientRunnable#updateSupervisionEvents(java.util.List)
	 */
	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {
		// TODO Auto-generated method stub
		
	}
	
	
}
