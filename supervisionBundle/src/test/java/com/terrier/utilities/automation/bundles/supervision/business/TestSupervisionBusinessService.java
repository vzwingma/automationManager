package com.terrier.utilities.automation.bundles.supervision.business;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.junit.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.Version;
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
        Bundle mockBundle = mock(Bundle.class);
        when(mockBundle.getSymbolicName()).thenReturn("MockBundle");
        when(mockBundle.getVersion()).thenReturn(new Version(1, 1, 1));
        
        Hashtable<String, String> mapHeaders= new Hashtable<String, String>();
        mapHeaders.put("Bundle-Name", "[Automation] MockBundle");
        when(mockBundle.getHeaders()).thenReturn(mapHeaders);
        StatutBundleTopicObject statusBundle = new StatutBundleTopicObject(mockBundle);
        statusBundle.getProperties().add(new StatutPropertyBundleObject("Activité Supervision ThreadPool", true, StatutPropertyBundleEnum.OK));
        properties.put(StatutPropertyNameEnum.STATUS, statusBundle);
        properties.put(StatutPropertyNameEnum.TIME, System.currentTimeMillis());
        
        Event event = AutomationEventPublisher.createEvent(EventsTopicNameEnum.SUPERVISION_EVENTS, properties);
        assertNotNull(event);
        
        BundleSupervisionBusinessService hander = spy(new BundleSupervisionBusinessService());
        hander.handleEvent(event);
        
        verify(hander, times(1)).logStatut(any(StatutBundleTopicObject.class));
	}
}
