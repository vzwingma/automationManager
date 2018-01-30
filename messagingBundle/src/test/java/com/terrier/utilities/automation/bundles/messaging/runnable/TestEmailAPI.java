/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

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

		Invocation.Builder mockWebResourceBuilder = mock(Invocation.Builder.class);
		when(runnable.getInvocation(any(Client.class), anyString(), anyString(), any(MediaType.class))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel (envoi de test2)
		when(mockWebResourceBuilder.post(any())).thenReturn(
				Response.serverError().build(), 
				Response.ok().build(), 
				Response.ok().build());
		assertEquals(0, service.getSmsSendingQueue().size());

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockWebResourceBuilder, times(2)).post(any(Entity.class));
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

		Invocation.Builder mockWebResourceBuilder = mock(Invocation.Builder.class);
		when(runnable.getInvocation(any(Client.class), anyString(), anyString(), any(MediaType.class))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel (envoi de test2)
		when(mockWebResourceBuilder.post(any())).thenReturn(
				Response.ok().build());
		assertEquals(0, service.getSmsSendingQueue().size());

		// Run
		runnable.run();

		verify(mockWebResourceBuilder, times(2)).post(any(Entity.class));
		//  envoi de test 
		assertEquals(0, service.getEmailsSendingQueue().keySet().size());
		assertNull(service.getEmailsSendingQueue().get("test"));

		// Retour des mêmes messages
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test1");
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test2");
		addToQueue(service.getEmailsSendingQueue(), "test2", "message de test3");
		
		runnable.run();
		// Pas de nouvel envoi
		verify(mockWebResourceBuilder, times(2)).post(any(Entity.class));
		// Et la liste est vide
		assertNull(service.getEmailsSendingQueue().get("test"));

		
		
		// Retour des mêmes messages
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test1");
		addToQueue(service.getEmailsSendingQueue(), "test", "message de test3");
		addToQueue(service.getEmailsSendingQueue(), "test2", "message de test4");
		
		runnable.run();
		// nouvel envoi
		verify(mockWebResourceBuilder, times(4)).post(any(Entity.class));
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

		Invocation.Builder mockWebResourceBuilder = mock(Invocation.Builder.class);
		when(runnable.getInvocation(any(Client.class), anyString(), anyString(), any(MediaType.class))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel (envoi de test2)
		when(mockWebResourceBuilder.post(any())).thenThrow(new ResponseProcessingException(Response.serverError().build(), "Erreur lors de l'envoi"));
		assertEquals(0, service.getSmsSendingQueue().size());

		// Run
		runnable.run();

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
