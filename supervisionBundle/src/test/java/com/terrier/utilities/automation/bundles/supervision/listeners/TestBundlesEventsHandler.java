package com.terrier.utilities.automation.bundles.supervision.listeners;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.osgi.service.event.Event;

import com.terrier.utilities.automation.bundles.communs.business.AutomationEventPublisher;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.StatusPropertyNameEnum;

/**
 * Test de réception des messages
 * @author vzwingma
 *
 */
public class TestBundlesEventsHandler {

	
	/**
	 * Test de réception de statut
	 */
	@SuppressWarnings("unchecked")
	@Test
	public void testReceptionBundleEvents(){
		// Préparation du message
		// MessageProperties
        Map<StatusPropertyNameEnum, Object> properties = new HashMap<>();
        // Status
        Map<String, Object> statusBundle = new HashMap<>();
        statusBundle.put("Activité Supervision ThreadPool", true);
        properties.put(StatusPropertyNameEnum.STATUS, statusBundle);
        properties.put(StatusPropertyNameEnum.BUNDLE, "[153] TestBundle");
        properties.put(StatusPropertyNameEnum.TIME, System.currentTimeMillis());
        
        Event event = AutomationEventPublisher.createEvent(EventsTopicNameEnum.SUPERVISION_EVENTS, properties);
        assertNotNull(event);
        
        BundlesEventsHandler hander = spy(new BundlesEventsHandler());
        hander.handleEvent(event);
        
        verify(hander, times(1)).logStatut(anyMap());
	}
}
