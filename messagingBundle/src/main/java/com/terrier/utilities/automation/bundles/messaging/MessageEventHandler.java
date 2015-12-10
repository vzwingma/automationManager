/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging;

import javax.inject.Singleton;

import org.apache.log4j.Logger;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;

/**
 * Event Message
 * @author vzwingma
 *
 */
@Singleton
public class MessageEventHandler implements EventHandler {


	private static final Logger LOGGER = Logger.getLogger( MessagingBusinessService.class );
	
	
	@Override
	public void handleEvent(Event event) {
		
		StringBuilder sb = new StringBuilder("{");
		for (String propertyName : event.getPropertyNames()) {
			sb.append(propertyName)
				.append("=")
				.append(event.getProperty(propertyName))
				.append("; ");
		}
		sb.append("}");
		LOGGER.info("Topic [" + event.getTopic() + "] Réception du message [" + sb.toString() + "]");
	}

}
