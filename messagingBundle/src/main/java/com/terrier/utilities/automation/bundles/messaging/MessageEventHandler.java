/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.messaging.MessagePropertyNameEnum;

/**
 * Event Message Handler
 * @author vzwingma
 *
 */
@Singleton
public class MessageEventHandler implements EventHandler {


	private static final Logger LOGGER = LoggerFactory.getLogger( MessageEventHandler.class );


	@Inject private MessagingBusinessService messagingService;

	/* (non-Javadoc)
	 * @see org.osgi.service.event.EventHandler#handleEvent(org.osgi.service.event.Event)
	 */
	@Override
	public void handleEvent(Event event) {

		StringBuilder sb = new StringBuilder("{");
		for (String propertyName : event.getPropertyNames()) {
			sb.append(propertyName)
			.append("=")
			.append(event.getProperty(propertyName))
			.append(";");
		}
		sb.append("}");

		LOGGER.debug("Topic [{}] Réception du message [{}]", event.getTopic(), sb);
		String titre = (String)event.getProperty(MessagePropertyNameEnum.TITRE_MESSAGE.name());
		String message = (String)event.getProperty(MessagePropertyNameEnum.MESSAGE.name());
		messagingService.sendNotificationSlack(titre, message);
	}
}
