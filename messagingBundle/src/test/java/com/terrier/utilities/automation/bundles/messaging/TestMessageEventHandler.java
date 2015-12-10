/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging;

import static org.junit.Assert.assertNotNull;

import java.util.Dictionary;
import java.util.Hashtable;

import org.junit.Test;
import org.osgi.service.event.Event;

import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;

/**
 * Test du message EventHandler
 * @author vzwingma
 *
 */
public class TestMessageEventHandler {


	@Test
	public void testReceiveMessage(){
		MessageEventHandler handler = new MessageEventHandler();
		assertNotNull(handler);

		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("message", "test message");
		properties.put("time", System.currentTimeMillis());

		Event reportGeneratedEvent = new Event(EventsTopicNameEnum.NOTIFIFY_MESSAGE.getTopicName(), properties);
		// Test
		handler.handleEvent(reportGeneratedEvent);
	}
}
