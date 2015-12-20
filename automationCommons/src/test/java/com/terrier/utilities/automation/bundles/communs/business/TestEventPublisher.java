package com.terrier.utilities.automation.bundles.communs.business;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.Test;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.MessagePropertyNameEnum;

public class TestEventPublisher {

	
	/**
	 * TU du publisher
	 */
	@Test
	public void testPublisher(){
		AutomationEventPublisher<MessagePropertyNameEnum> publishMessages = spy(new AutomationEventPublisher<>());
		EventAdmin mockEventAdmin = mock(EventAdmin.class);
		when(publishMessages.getEventAdmin()).thenReturn(mockEventAdmin);
		
		
		Map<MessagePropertyNameEnum, Object> properties = new HashMap<>();
		properties.put(MessagePropertyNameEnum.TITRE_MESSAGE, "Titre");
		
		
		// Publication
		publishMessages.publishToTopic(EventsTopicNameEnum.NOTIFIFY_MESSAGE, properties);
		
		// Vérification
		verify(mockEventAdmin).sendEvent(argThat(new BaseMatcher<Event>() {

			@Override
			public boolean matches(Object item) {
				if(item instanceof Event){
					Event eventSent = (Event)item;
					return eventSent.getProperty(MessagePropertyNameEnum.TITRE_MESSAGE.getName()).equals("Titre");
				}
				return false;
			}

			@Override
			public void describeTo(Description description) { }

			
		}));

	}
	

	/**
	 * TU du publisher avec une incohérence entre le topic et les properties
	 * Si aucune ne correspond pas d'envoi de message
	 */
	@Test
	public void testPublisherWithBadProperties(){
		AutomationEventPublisher<MessagePropertyNameEnum> publishMessages = spy(new AutomationEventPublisher<>());
		EventAdmin mockEventAdmin = mock(EventAdmin.class);
		when(publishMessages.getEventAdmin()).thenReturn(mockEventAdmin);
		
		
		Map<MessagePropertyNameEnum, Object> properties = new HashMap<>();
		properties.put(MessagePropertyNameEnum.TITRE_MESSAGE, "Titre");
		
		
		// Publication
		publishMessages.publishToTopic(EventsTopicNameEnum.SUPERVISION_EVENTS, properties);
		
		// Vérification
		verify(mockEventAdmin, never()).sendEvent(any(Event.class));

	}
}
