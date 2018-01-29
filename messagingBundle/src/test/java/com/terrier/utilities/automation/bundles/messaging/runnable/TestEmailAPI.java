/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.core.MediaType;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.messaging.MessagingBusinessService;

/**
 * @author vzwingma
 *
 */
public class TestEmailAPI {

	private Client mockClient = mock(Client.class);

	private static Properties properties = new Properties();
	/**
	 * Charge les données privées issues de com.terrier.utilities.automation.private.messaging
	 * Ce fichier ne doit pas être commité
	 * @throws KeyNotFoundException
	 */
	@BeforeClass
	public static void loadPrivateData() throws KeyNotFoundException{
		
		try {
			properties.load(new FileInputStream(new File("src/test/resources/com.terrier.utilities.automation.private.messaging.cfg")));
		} catch (IOException e) { e.printStackTrace();}
	}
	
	
	/**
	 * Test d'envoi
	 */
	@Test
	public void testEnvoiMail(){
		// Préparation
		MessagingBusinessService service = new MessagingBusinessService();
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test1");
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test2");
		addToQueue(service.getEmailsSendingQueue(), "test2", "message de test3");


		SendEmailTaskRunnable runnable = spy(
				new SendEmailTaskRunnable(
						"123", 
						"https://api.mailgun.net/v3/", 
						"sandboxc.mailgun.org", 
						"/messages",
						"toto@world.com", 
						service));

		when(runnable.getClient()).thenReturn(mockClient);
		doCallRealMethod().when(mockClient).addFilter(any());
		WebResource mockWebResource = mock(WebResource.class);
		when(mockClient.resource(anyString())).thenReturn(mockWebResource);
		WebResource.Builder mockWebResourceBuilder = mock(WebResource.Builder.class);
		when(mockWebResource.type(eq(MediaType.APPLICATION_FORM_URLENCODED))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel (envoi de test2)
		when(mockWebResourceBuilder.post(eq(ClientResponse.class), any())).thenReturn(
				new ClientResponse(500, null, null, null), 
				new ClientResponse(200, null, null, null),
				new ClientResponse(200, null, null, null));
		assertEquals(0, service.getSmsSendingQueue().size());

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockClient, times(2)).addFilter(any(ClientFilter.class));
		verify(mockClient, times(2)).resource(eq("https://api.mailgun.net/v3/sandboxc.mailgun.org/messages"));
		// Mais envoi de test par contre, test2 reste
		assertEquals(1, service.getEmailsSendingQueue().keySet().size());
		assertEquals(1, service.getEmailsSendingQueue().get("test2").size());
		assertNull(service.getEmailsSendingQueue().get("test"));

		// Run
		runnable.run();
		// Cette fois tout est passé
		assertNull(service.getEmailsSendingQueue().get("test2"));
		assertNull(service.getEmailsSendingQueue().get("test"));
	}


	
	/**
	 * Test d'envoi
	 */
	@Test
	public void testEnvoiMailNoSpam(){
		// Préparation
		MessagingBusinessService service = new MessagingBusinessService();
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test1");
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test2");
		addToQueue(service.getEmailsSendingQueue(), "test2", "message de test3");


		SendEmailTaskRunnable runnable = spy(
				new SendEmailTaskRunnable(
						"123", 
						"https://api.mailgun.net/v3/", 
						"sandboxc.mailgun.org", 
						"/messages",
						"toto@world.com", 
						service));

		when(runnable.getClient()).thenReturn(mockClient);
		doCallRealMethod().when(mockClient).addFilter(any());
		WebResource mockWebResource = mock(WebResource.class);
		when(mockClient.resource(anyString())).thenReturn(mockWebResource);
		WebResource.Builder mockWebResourceBuilder = mock(WebResource.Builder.class);
		when(mockWebResource.type(eq(MediaType.APPLICATION_FORM_URLENCODED))).thenReturn(mockWebResourceBuilder);
		// Envoi OK
		when(mockWebResourceBuilder.post(eq(ClientResponse.class), any())).thenReturn(
				new ClientResponse(200, null, null, null));
		assertEquals(0, service.getSmsSendingQueue().size());

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockClient, times(2)).addFilter(any(ClientFilter.class));
		verify(mockClient, times(2)).resource(eq("https://api.mailgun.net/v3/sandboxc.mailgun.org/messages"));
		//  envoi de test 
		assertEquals(0, service.getEmailsSendingQueue().keySet().size());
		assertNull(service.getEmailsSendingQueue().get("test"));

		// Retour des mêmes messages
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test1");
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test2");
		addToQueue(service.getEmailsSendingQueue(), "test2", "message de test3");
		
		runnable.run();
		// Pas de nouvel envoi
		verify(mockClient, times(2)).addFilter(any(ClientFilter.class));
		verify(mockClient, times(2)).resource(eq("https://api.mailgun.net/v3/sandboxc.mailgun.org/messages"));
		// Et la liste est vide
		assertNull(service.getEmailsSendingQueue().get("test"));

		
		
		// Retour des mêmes messages
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test1");
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test3");
		addToQueue(service.getEmailsSendingQueue(), "test2", "message de test4");
		
		runnable.run();
		// nouvel envoi
		verify(mockClient, times(4)).addFilter(any(ClientFilter.class));
		verify(mockClient, times(4)).resource(eq("https://api.mailgun.net/v3/sandboxc.mailgun.org/messages"));
		// Et la liste est vide
		assertNull(service.getEmailsSendingQueue().get("test"));

	}


	/**
	 * Test d'envoi
	 */
	@Test
	public void testEnvoiMailException(){
		// Préparation
		MessagingBusinessService service = new MessagingBusinessService();
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test1");

		SendEmailTaskRunnable runnable = spy(
				new SendEmailTaskRunnable(
						"123", 
						"https://api.mailgun.net/v3/", 
						"sandboxc.mailgun.org", 
						"/messages",
						"toto@world.com", 
						service));

		when(runnable.getClient()).thenReturn(mockClient);
		doCallRealMethod().when(mockClient).addFilter(any());
		WebResource mockWebResource = mock(WebResource.class);
		when(mockClient.resource(anyString())).thenReturn(mockWebResource);
		WebResource.Builder mockWebResourceBuilder = mock(WebResource.Builder.class);
		when(mockWebResource.type(eq(MediaType.APPLICATION_FORM_URLENCODED))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel
		when(mockWebResourceBuilder.post(eq(ClientResponse.class), any(MultivaluedMapImpl.class))).thenThrow(new UniformInterfaceException("Erreur lors de l'envoi", new ClientResponse(500, null, new InputStream() {

			@Override
			public int read() throws IOException {
				return -1;
			}
		}, null)));
		assertEquals(0, service.getSmsSendingQueue().size());

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockClient, times(1)).addFilter(any(ClientFilter.class));
		verify(mockClient, times(1)).resource(eq("https://api.mailgun.net/v3/sandboxc.mailgun.org/messages"));
		// Mais envoi de test par contre, test2 reste
		assertEquals(1, service.getEmailsSendingQueue().keySet().size());
		assertNotNull(service.getEmailsSendingQueue().get("test"));
		// Envoi d'un SMS indiquant l'erreur d'envoi
		assertEquals(1, service.getSmsSendingQueue().size());

	}


	/**
	 * Test pour appeler l'API réelle
	 */
	@Ignore
	public void testRealAPI(){

		// Préparation
		MessagingBusinessService service = new MessagingBusinessService();
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test1");
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test2");

		
		assertNotNull(TestEmailAPI.properties.getProperty("automation.bundle.messaging.email.key"));
		
		SendEmailTaskRunnable runnable = new SendEmailTaskRunnable(
				TestEmailAPI.properties.getProperty("automation.bundle.messaging.email.key"), 
				TestEmailAPI.properties.getProperty("automation.bundle.messaging.email.mailgun.url"), 
				TestEmailAPI.properties.getProperty("automation.bundle.messaging.email.mailgun.domain"),
				TestEmailAPI.properties.getProperty("automation.bundle.messaging.email.mailgun.service"), 
				TestEmailAPI.properties.getProperty("automation.bundle.messaging.email.destinataires"), 
				service);
		runnable.run();
	}


	/**
	 * Ajout dans la queue d'envoi
	 * @param messagesSendingQueue
	 * @param titre
	 * @param message
	 */
	private void addToQueue(Map<String, ConcurrentLinkedQueue<String>> messagesSendingQueue, String titre, String message){
		ConcurrentLinkedQueue<String> messagesToSend = messagesSendingQueue.getOrDefault(titre, new ConcurrentLinkedQueue<String>());
		messagesToSend.add(message);
		messagesSendingQueue.put(titre, messagesToSend);
	}
}
