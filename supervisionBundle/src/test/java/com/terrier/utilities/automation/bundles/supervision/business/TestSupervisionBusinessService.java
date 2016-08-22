package com.terrier.utilities.automation.bundles.supervision.business;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.service.event.Event;

import com.terrier.utilities.automation.bundles.communs.business.AutomationEventPublisher;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyNameEnum;
import com.terrier.utilities.automation.bundles.communs.model.StatutBundleTopicObject;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;

/**
 * Test de réception des messages
 * @author vzwingma
 *
 */
public class TestSupervisionBusinessService {

	
	/**
	 * Test de réception de statut
	 */
	@Test
	public void testReceptionBundleEvents(){
		// Préparation du message
		// MessageProperties
        Map<StatutPropertyNameEnum, Object> properties = new HashMap<>();
        // Status
        StatutBundleTopicObject statusBundle = new StatutBundleTopicObject(mock(Bundle.class));
        statusBundle.getProperties().add(new StatutPropertyBundleObject("Activité Supervision ThreadPool", true, StatutPropertyBundleEnum.OK));
        properties.put(StatutPropertyNameEnum.STATUS, statusBundle);
        properties.put(StatutPropertyNameEnum.TIME, System.currentTimeMillis());
        
        Event event = AutomationEventPublisher.createEvent(EventsTopicNameEnum.SUPERVISION_EVENTS, properties);
        assertNotNull(event);
        
        SupervisionBusinessService hander = spy(new SupervisionBusinessService());
        hander.handleEvent(event);
        
        verify(hander, times(1)).logStatut(any(StatutBundleTopicObject.class));
	}
}
