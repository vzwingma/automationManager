/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.messaging.enums.MessagingConfigKeyEnums;

/**
 * Test du service métier
 * @author vzwingma
 *
 */
public class TestBusinessService {


	private static final Logger LOGGER = LoggerFactory.getLogger( TestBusinessService.class );
	private MessagingBusinessService service;


	/**
	 * Mock dictionnary
	 * @throws KeyNotFoundException
	 */
	@Before
	public void mockDictionnary() throws KeyNotFoundException{
		service = Mockito.spy(new MessagingBusinessService());
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File("src/test/resources/com.terrier.utilities.automation.bundles.messaging.cfg")));
		} catch (IOException e) { e.printStackTrace();}

		when(service.getConfig(any(MessagingConfigKeyEnums.class))).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return properties.getProperty(((MessagingConfigKeyEnums)invocation.getArguments()[0]).getCodeKey());
			}
		});
	}
	/**
	 * Validation de la configuration
	 */
	@Test
	public void testConfig(){
		assertTrue(service.validateConfig());
	}



	/**
	 * Test d'ajout d'emails
	 */
	@Test
	public void testAjoutEmailsToQueue(){
		// Send email
		service.sendNotificationEmail("test", "message de test1");
		service.sendNotificationEmail("test", "message de test2");
		service.sendNotificationEmail("test2", "message de test3");

		Map<String, ConcurrentLinkedQueue<String>> queue = service.getEmailsSendingQueue();
		assertEquals(2, queue.keySet().size());
		assertEquals(2, queue.get("test").size());
		assertEquals(1, queue.get("test2").size());

	}


	@Test
	public void testSupervisionEventsEmail(){
		
		service.notifyUpdateDictionary();
		// Send email
		service.sendNotificationEmail("test", "message de test1");
		service.sendNotificationEmail("test", "message de test2");
		service.sendNotificationEmail("test2", "message de test3");

		
		List<StatutPropertyBundleObject> statuts = new ArrayList<StatutPropertyBundleObject>();
		service.updateSupervisionEvents(statuts);
		
		assertNotNull(statuts);
		assertTrue(statuts.size() > 0);
		assertTrue((Boolean)statuts.get(0).getValue());
		assertEquals(2, statuts.get(1).getValue());
	}


	/**
	 * Test d'ajout d'emails
	 */
	@Test
	public void testAjoutSMSToQueue(){
		// Send email
		service.sendNotificationSMS("message de test1");
		service.sendNotificationSMS("message de test2");
		service.sendNotificationSMS("message de test3");

		ConcurrentLinkedQueue<String> queue = service.getSmsSendingQueue();
		assertEquals(3, queue.size());
	}


	@Test
	public void testSupervisionEventsSMS(){
		
		service.notifyUpdateDictionary();
		// Send email
		service.sendNotificationSMS("message de test1");
		service.sendNotificationSMS("message de test2");
		service.sendNotificationSMS("message de test3");

		
		List<StatutPropertyBundleObject> statuts = new ArrayList<StatutPropertyBundleObject>();
		service.updateSupervisionEvents(statuts);
		LOGGER.info("{}", statuts);
		
		assertNotNull(statuts);
		assertTrue(statuts.size() > 0);
		assertTrue((Boolean)statuts.get(3).getValue());
		assertEquals(3, statuts.get(4).getValue());
	}
	

	@Test
	public void testSupervisionEventsInit(){
		
		service.notifyUpdateDictionary();
		List<StatutPropertyBundleObject> statuts = new ArrayList<StatutPropertyBundleObject>();
		service.updateSupervisionEvents(statuts);
		
		LOGGER.info("{}", statuts);
		
		assertNotNull(statuts);
		assertTrue(statuts.size() > 0);
		// Statut des threads
		assertTrue((Boolean)statuts.get(3).getValue());
		assertTrue((Boolean)statuts.get(0).getValue());
	}
}
