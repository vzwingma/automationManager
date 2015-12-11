/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.messaging.enums.MessagingConfigKeyEnums;

/**
 * @author vzwingma
 *
 */
public class TestEmailAPI {


	private MessagingBusinessService service;

	private Client mockClient = mock(Client.class);

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



	@Test
	public void testAjoutEmailsToQueue(){
		// Send email
		service.sendNotificationEmail("test", "message de test1");
		service.sendNotificationEmail("test", "message de test2");
		service.sendNotificationEmail("test2", "message de test3");

		Map<String, List<String>> queue = service.getMessagesSendingQueue();
		assertEquals(2, queue.keySet().size());
		assertEquals(2, queue.get("test").size());
		assertEquals(1, queue.get("test2").size());

	}
	/**
	 * Test d'envoi
	 */
	@Test
	public void testEnvoiMail(){
		assertNotNull(service);
		// Préparation
		service.sendNotificationEmail("test", "message de test1");
		service.sendNotificationEmail("test", "message de test2");
		service.sendNotificationEmail("test2", "message de test3");

		Map<String, List<String>> queue = service.getMessagesSendingQueue();

		SendEmailTaskRunnable runnable = spy(
				new SendEmailTaskRunnable(
						"123", 
						"https://api.mailgun.net/v3/sandboxc.mailgun.org/messages", 
						"sandboxc.mailgun.org", 
						"toto@world.com", 
						queue));

		when(runnable.getClient()).thenReturn(mockClient);
		doCallRealMethod().when(mockClient).addFilter(any());
		WebResource mockWebResource = mock(WebResource.class);
		when(mockClient.resource(anyString())).thenReturn(mockWebResource);
		WebResource.Builder mockWebResourceBuilder = mock(WebResource.Builder.class);
		when(mockWebResource.type(eq(MediaType.APPLICATION_FORM_URLENCODED))).thenReturn(mockWebResourceBuilder);
		when(mockWebResourceBuilder.post(eq(ClientResponse.class), any())).thenReturn(
				new ClientResponse(500, null, null, null), 
				new ClientResponse(200, null, null, null),
				new ClientResponse(200, null, null, null));

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockClient, times(1)).addFilter(any(ClientFilter.class));
		verify(mockClient, times(1)).resource(eq("https://api.mailgun.net/v3/sandboxc.mailgun.org/messages"));
		// Mais envoi de test par contre, test2 reste
		assertEquals(1, queue.keySet().size());
		assertEquals(1, queue.get("test2").size());
		assertNull(queue.get("test"));

		// Run
		runnable.run();
		// Cette fois tout est passé
		assertNull(queue.get("test2"));
		assertNull(queue.get("test"));
	}

	/**
	 * Test pour appeler l'API réelle
	 */
	@Ignore
	public void testRealAPI(){

		assertNotNull(service);
		// Préparation
		service.sendNotificationEmail("test", "message de test1");
		service.sendNotificationEmail("test", "message de test2");
	
		Map<String, List<String>> queue = service.getMessagesSendingQueue();

		SendEmailTaskRunnable runnable = new SendEmailTaskRunnable(
				"key-", 
				"https://api.mailgun.net/v3/sandboxc3830b67ded34305912ad73326e9af2f.mailgun.org/messages", 
				"sandboxc.mailgun.org", 
				"vincent.zwingmann@gmail.com", 
				queue);
		runnable.run();
	}

}
