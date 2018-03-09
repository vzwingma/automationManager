/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;
import com.terrier.utilities.automation.bundles.messaging.http.client.AbstractHTTPClientRunnable;

/**
 * Tâche d'envoi des mails
 * @author vzwingma
 *
 */
public class SendSMSTaskRunnable extends AbstractHTTPClientRunnable {



	private static final Logger LOGGER = LoggerFactory.getLogger( SendSMSTaskRunnable.class );

	/**
	 * Liste 
	 */
	private String user;
	private String password;
	private String apiURL;

	private List<String> sentMessages = new ArrayList<>();
	
	/**
	 * Constructeur de la tâche d'envoi
	 * @param messagesSendingQueue
	 */
	public SendSMSTaskRunnable(final String apiURL, final String user, final String password, final MessagingBusinessService service) {
		this.user = user;
		this.password = password;
		this.apiURL = apiURL;
		super.setService(service);
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void httpClientRun() {
		LOGGER.info("Envoi des SMS : {} messages en attente", getService().getSmsSendingQueue().size());
		if(!getService().getSmsSendingQueue().isEmpty()){
			boolean resultat = sendAllMessages();
			LOGGER.info("> Résulat des envois : {}", resultat);
		}
	}


	/**
	 * @return résultat de l'envoi des messages
	 */
	public boolean sendAllMessages(){

		boolean resultat = false;
		
		// Envoi de tous les mails, groupé par titre :
		if(!getService().getSmsSendingQueue().isEmpty()){

			List<String> messages = new ArrayList<>();
			while(!getService().getSmsSendingQueue().isEmpty() && messages.size() < 5){
				messages.add(getService().getSmsSendingQueue().poll());
			}
			String messageSMS =  getFormData(messages);
			
			LOGGER.debug("Envoi des {} messages par SMS", messages.size());
			if(sentMessages.contains(messageSMS)){
				LOGGER.warn("Le message a déjà été envoyé. Pas d'envoi");
				resultat = true;
			}
			else{
				StringBuilder urlComplete = new StringBuilder(this.apiURL);
				urlComplete.append("user=").append(this.user).append("&pass=").append(this.password).append("&msg=").append(messageSMS);
				
				Invocation.Builder invocation = getInvocation(getClient(), urlComplete.toString(), null, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
				resultat = callHTTPGet(invocation);
				if(resultat){
					LOGGER.debug("Suppression des messages SMS de la liste d'envoi");
					sentMessages.add(messageSMS);
				}
				else{
					LOGGER.error("Erreur lors de l'envoi, les messages sont reprogrammés pour la prochaine échéance");
					for (String msg : messages) {
						getService().sendNotificationSMS(msg);
					}
					getService().sendNotificationEmail("Erreur envoi de SMS", "Erreur lors de l'envoi du SMS ");
				}
			}
		}
		return resultat;
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
	
	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.messaging.runnable.AbstractHTTPClientRunnable#updateSupervisionEvents(java.util.List)
	 */
	@Override
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Nombre de SMS en attente", 
						this.getService().getSmsSendingQueue().size(),
						StatutPropertyBundleEnum.OK));

		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Dernier d'appel du service " + this.apiURL, 
						this.getLastResponseCode() == 0 ? "?" : this.getLastResponseCode(),
						getCode(this.getLastResponseCode())));
	}

}
