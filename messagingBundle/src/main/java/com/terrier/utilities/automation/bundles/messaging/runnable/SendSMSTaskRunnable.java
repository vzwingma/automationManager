/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;

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
	public void HTTPClientRun() {
		LOGGER.info("Envoi des SMS : {} messages en attente", getService().getSmsSendingQueue().size());
		if(getService().getSmsSendingQueue().size() > 0){
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
		if(getService().getSmsSendingQueue().size() > 0){

			List<String> messages = new ArrayList<>();
			while(getService().getSmsSendingQueue().size() > 0){
				messages.add(getService().getSmsSendingQueue().poll());
			}
			LOGGER.debug("Envoi des {} messages par SMS", messages.size());
			resultat = callHTTPGet(getClient(), this.apiURL, "user=" + this.user + "&pass=" + this.password + "&msg=",  getFormData(messages));

			if(resultat){
				LOGGER.debug("Suppression des messages SMS de la liste d'envoi");
			}
			else{
				for (String msg : messages) {
					getService().sendNotificationSMS(msg);
				}
				LOGGER.error("Erreur lors de l'envoi, les messages sont reprogrammés pour la prochaine échéance");
				getService().sendNotificationEmail("Erreur envoi de SMS", "Erreur lors de l'envoi du SMS ");
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
		// TODO Auto-generated method stub
		
	}

}
