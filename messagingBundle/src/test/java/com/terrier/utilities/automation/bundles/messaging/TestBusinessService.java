/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.messaging.enums.MessagingConfigKeyEnums;

/**
 * Test du service métier
 * @author vzwingma
 *
 */
public class TestBusinessService {


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

		Map<String, List<String>> queue = service.getEmailsSendingQueue();
		assertEquals(2, queue.keySet().size());
		assertEquals(2, queue.get("test").size());
		assertEquals(1, queue.get("test2").size());

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
}
