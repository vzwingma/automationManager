/**
 * 
 */
package com.terrier.utilities.automation.bundles.supervision.business;

import javax.inject.Singleton;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Event Message Handler
 * @author vzwingma
 *
 */
@Singleton
public class BundlesEventsHandler implements EventHandler {


	private static final Logger LOGGER = LoggerFactory.getLogger( BundlesEventsHandler.class );


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
		LOGGER.debug("Topic [{}] Réception du message [{}]", event.getTopic(), sb.toString());
	}
}
