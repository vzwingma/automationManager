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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;

/**
 * Tâche d'envoi de notification sur Slack
 * @author vzwingma
 *
 */
public class SendSlackNotifsTaskRunnable extends AbstractSendTaskRunnable  {



	private static final Logger LOGGER = LoggerFactory.getLogger( SendSlackNotifsTaskRunnable.class );

	/**
	 * Config 
	 */
	private String apiKey;
	private String apiURL;

	
	/**
	 * Constructeur de la tâche d'envoi
	 * @param messagesSendingQueue
	 */
	public SendSlackNotifsTaskRunnable(final String apiURL, final String apiKey, final MessagingBusinessService service ) {
		this.apiKey = apiKey;
		this.apiURL = apiURL;
		super.setService(service);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void executeMessagesTask() {
		LOGGER.info("Envoi des notifications Slack : {} messages en attente", getService().getNotifsSendingQueue().size());
		if(! getService().getNotifsSendingQueue().isEmpty()){
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
		for (Iterator<Entry<String, ConcurrentLinkedQueue<String>>> gmIterator = getService().getNotifsSendingQueue().entrySet().iterator(); gmIterator.hasNext();) {

			Entry<String, ConcurrentLinkedQueue<String>> groupeMessages = gmIterator.next();
			if(groupeMessages.getValue() != null && !groupeMessages.getValue().isEmpty()){
				String formData = getFormData(groupeMessages.getKey(), groupeMessages.getValue());
				LOGGER.debug("Envoi de la notification : {}", formData);
				
				if(sentMessages.contains(formData)){
					LOGGER.warn("Le message a déjà été envoyé. Pas d'envoi");
					gmIterator.remove();
				}
				else{
					Invocation.Builder invocation = getInvocation(getClient(), this.apiURL, this.apiKey, MediaType.APPLICATION_JSON_TYPE);
					boolean resultat = callHTTPPost(invocation , Entity.json(formData));
					if(resultat){
						LOGGER.debug("Suppression des messages de [{}] de la liste d'envoi", groupeMessages.getKey());
						sentMessages.add(formData);
						gmIterator.remove();
					}
					else{
						// si erreur, on utilise l'autre canal pour envoyer le message d'erreur
						getService().sendNotificationSMS("Erreur lors de l'envoi de la notification Slack, les messages de ["+groupeMessages.getKey()+"] n'ont pas été envoyé.");
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
	private String getFormData(String titre, ConcurrentLinkedQueue<String> messages) {
		
		StringBuilder messageAEnvoyer = new StringBuilder().append("{\"text\": \"");
		messageAEnvoyer.append("*").append(titre).append("*")
		.append("\n");
		for (String msg : messages) {
			messageAEnvoyer.append(msg).append("\n");
		}
		messageAEnvoyer.append("\"");
		messageAEnvoyer.append(" }");
		return messageAEnvoyer.toString();
	}

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.messaging.runnable.AbstractHTTPClientRunnable#updateSupervisionEvents(java.util.List)
	 */
	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Nombre de notifications en attente", 
						this.getService().getNotifsSendingQueue().size(),
						StatutPropertyBundleEnum.OK));
		
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Dernier d'appel du service " + this.apiURL, 
						this.getLastResponseCode() == 0 ? "?" : this.getLastResponseCode(), getCode(this.getLastResponseCode())));
	}
}
