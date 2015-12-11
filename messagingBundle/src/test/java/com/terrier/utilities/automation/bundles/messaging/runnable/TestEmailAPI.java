/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.MediaType;

import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;

/**
 * @author vzwingma
 *
 */
public class TestEmailAPI {

	private Client mockClient = mock(Client.class);
	
	/**
	 * Test d'envoi
	 */
	@Test
	public void testEnvoiMail(){
		// Préparation
		Map<String, List<String>> queue = new ConcurrentHashMap<>();
		addToQueue(queue, "test", "message de test1");
		addToQueue(queue, "test", "message de test2");
		addToQueue(queue, "test2", "message de test3");


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

		// Préparation
		Map<String, List<String>> queue = new ConcurrentHashMap<>();
		addToQueue(queue, "test", "message de test1");
		addToQueue(queue, "test", "message de test2");
	
		SendEmailTaskRunnable runnable = new SendEmailTaskRunnable(
				"key-", 
				"https://api.mailgun.net/v3/sandboxc3830b67ded34305912ad73326e9af2f.mailgun.org/messages", 
				"sandboxc.mailgun.org", 
				"vincent.zwingmann@gmail.com", 
				queue);
		runnable.run();
	}

	
	/**
	 * Ajout dans la queue d'envoi
	 * @param messagesSendingQueue
	 * @param titre
	 * @param message
	 */
	private void addToQueue(Map<String, List<String>> messagesSendingQueue, String titre, String message){
		List<String> messagesToSend = messagesSendingQueue.getOrDefault(titre, new ArrayList<String>());
		messagesToSend.add(message);
		messagesSendingQueue.put(titre, messagesToSend);
	}
}
