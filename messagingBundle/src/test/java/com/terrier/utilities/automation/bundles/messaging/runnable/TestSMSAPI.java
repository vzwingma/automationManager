/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.core.MediaType;

import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

/**
 * @author vzwingma
 *
 */
public class TestSMSAPI {

	private Client mockClient = mock(Client.class);
	
	/**
	 * Test d'envoi
	 */
	@Test
	public void testEnvoiMail(){
		// Préparation
		ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
		queue.add("message de test1");
		queue.add("message de test2");
		queue.add("message de test3");


		SendSMSTaskRunnable runnable = spy(
				new SendSMSTaskRunnable(
						"1", 
						"2",
						"https://smsapi.free-mobile.fr/sendmsg?",
						queue));

		when(runnable.getClient()).thenReturn(mockClient);
		doCallRealMethod().when(mockClient).addFilter(any());
		WebResource mockWebResource = mock(WebResource.class);
		when(mockClient.resource(anyString())).thenReturn(mockWebResource);
		WebResource.Builder mockWebResourceBuilder = mock(WebResource.Builder.class);
		when(mockWebResource.type(eq(MediaType.APPLICATION_FORM_URLENCODED))).thenReturn(mockWebResourceBuilder);
		when(mockWebResourceBuilder.get(eq(ClientResponse.class))).thenReturn(
				new ClientResponse(500, null, null, null), 
				new ClientResponse(200, null, null, null));

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockClient, times(1)).resource(eq("https://smsapi.free-mobile.fr/sendmsg?user=1&pass=2&msg=-+message+de+test1%0A-+message+de+test2%0A-+message+de+test3%0A"));

		assertEquals(3, queue.size());

		// Run
		runnable.run();
		// Cette fois tout est passé
		assertEquals(0, queue.size());
	}

	/**
	 * Test pour appeler l'API réelle
	 */
	@Ignore
	public void testRealAPI(){

		// Préparation
		ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
		queue.add("message de test1");
		queue.add("message de test2");
		queue.add("message de test3");
	
		SendSMSTaskRunnable runnable = new SendSMSTaskRunnable(
				"1", 
				"A", 
				"https://smsapi.free-mobile.fr/sendmsg?", 
				queue);
		runnable.run();
	}
}


