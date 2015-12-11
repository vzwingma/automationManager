/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventPropertyNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.TypeMessagingEnum;

/**
 * Event Message Handler
 * @author vzwingma
 *
 */
@Singleton
public class MessageEventHandler implements EventHandler {


	private static final Logger LOGGER = Logger.getLogger( MessagingBusinessService.class );


	@Inject private MessagingBusinessService messagingService;

	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	@Override
	public void handleEvent(Event event) {

		StringBuilder sb = new StringBuilder("\n{");
		for (String propertyName : event.getPropertyNames()) {
			sb.append(propertyName)
			.append("=")
			.append(event.getProperty(propertyName))
			.append("; \n ");
		}
		sb.append("}");
		LOGGER.debug("Topic [" + event.getTopic() + "] Réception du message [" + sb.toString() + "]");


		//Envoi d'un email
		if(event.getProperty(EventPropertyNameEnum.TYPE_MESSAGE.name()) != null 
				&& 
				TypeMessagingEnum.EMAIL.equals(event.getProperty(EventPropertyNameEnum.TYPE_MESSAGE.name()))){
           String titre = (String)event.getProperty(EventPropertyNameEnum.TITRE_MESSAGE.name());
           String message = (String)event.getProperty(EventPropertyNameEnum.MESSAGE.name());
           
           messagingService.sendNotificationEmail(titre, message);
		}
		else{
			LOGGER.warn("Aucune configuration pour ce message de type " + event.getProperty(EventPropertyNameEnum.TYPE_MESSAGE.name()));
		}
	}

}
