/**
 * 
 */
package com.terrier.utilities.automation.bundles.messaging.runnable;


import static org.junit.Assert.assertEquals;
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
import java.util.Properties;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.ResponseProcessingException;
import javax.ws.rs.client.WebTarget;
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
public class TestSMSAPI {

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
	public void testEnvoiSMS(){
		// Préparation
		MessagingBusinessService service = new MessagingBusinessService();
		service.getSmsSendingQueue().add("message de test1");
		service.getSmsSendingQueue().add("message de test2");
		service.getSmsSendingQueue().add("message de test3");


		SendSMSTaskRunnable runnable = spy(
				new SendSMSTaskRunnable(
						"https://smsapi.free-mobile.fr/sendmsg?", 
						"1", 
						"2",
						service));

		when(runnable.getClient()).thenReturn(mockClient);
		WebTarget target = mock(WebTarget.class);
		when(mockClient.target(anyString())).thenReturn(target);
		when(target.path(null)).thenReturn(target);
		Invocation.Builder mockWebResourceBuilder = mock(Invocation.Builder.class);
		when(target.request(any(MediaType.class))).thenReturn(mockWebResourceBuilder);

		when(runnable.getInvocation(any(Client.class), anyString(), anyString(), any(MediaType.class))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel (envoi de test2)
		when(mockWebResourceBuilder.get()).thenReturn(
				Response.serverError().build(), 
				Response.ok().build(), 
				Response.ok().build());

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockWebResourceBuilder, times(1)).get();

		assertEquals(3, service.getSmsSendingQueue().size());
		// Envoi d'un mail indiquant l'erreur d'envoi
		assertEquals(1, service.getEmailsSendingQueue().size());

		
		// Run
		runnable.run();
		// Cette fois tout est passé
		assertEquals(0, service.getSmsSendingQueue().size());
	}
	
	/**
	 * Test d'envoi No Span
	 */
	@Test
	public void testEnvoiSMSNoSpam(){
		// Préparation
		MessagingBusinessService service = new MessagingBusinessService();
		service.getSmsSendingQueue().add("message de test1");
		service.getSmsSendingQueue().add("message de test2");
		service.getSmsSendingQueue().add("message de test3");

		SendSMSTaskRunnable runnable = spy(
				new SendSMSTaskRunnable(
						"https://smsapi.free-mobile.fr/sendmsg?", 
						"1", 
						"2",
						service));

		when(runnable.getClient()).thenReturn(mockClient);
		WebTarget target = mock(WebTarget.class);
		when(mockClient.target(anyString())).thenReturn(target);
		when(target.path(null)).thenReturn(target);
		Invocation.Builder mockWebResourceBuilder = mock(Invocation.Builder.class);
		when(target.request(any(MediaType.class))).thenReturn(mockWebResourceBuilder);

		when(runnable.getInvocation(any(Client.class), anyString(), anyString(), any(MediaType.class))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel (envoi de test2)
		when(mockWebResourceBuilder.get()).thenReturn(
				Response.ok().build());

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockWebResourceBuilder, times(1)).get();

		assertEquals(0, service.getSmsSendingQueue().size());
		
		// Réinjection des mêmes messages
		service.getSmsSendingQueue().add("message de test1");
		service.getSmsSendingQueue().add("message de test2");
		service.getSmsSendingQueue().add("message de test3");
		// Run
		runnable.run();
		// Pas de nouvel envoi
		verify(mockWebResourceBuilder, times(1)).get();

		assertEquals(0, service.getSmsSendingQueue().size());
		
		// Réinjection des presque mêmes messages
		service.getSmsSendingQueue().add("message de test1");
		service.getSmsSendingQueue().add("message de test2");
		// Run
		runnable.run();
		// Cette fois tout est passé
		verify(mockWebResourceBuilder, times(2)).get();

		assertEquals(0, service.getSmsSendingQueue().size());

		
		
	}
	
	/**
	 * Test d'envoi
	 */
	@Test
	public void testEnvoiSMSException(){
		// Préparation
		MessagingBusinessService service = new MessagingBusinessService();
		service.getSmsSendingQueue().add("message de test1");

		SendSMSTaskRunnable runnable = spy(
				new SendSMSTaskRunnable(
						"https://smsapi.free-mobile.fr/sendmsg?", 
						"1", 
						"2",
						service));

		when(runnable.getClient()).thenReturn(mockClient);
		WebTarget target = mock(WebTarget.class);
		when(mockClient.target(anyString())).thenReturn(target);
		when(target.path(null)).thenReturn(target);
		Invocation.Builder mockWebResourceBuilder = mock(Invocation.Builder.class);
		when(target.request(any(MediaType.class))).thenReturn(mockWebResourceBuilder);

		when(runnable.getInvocation(any(Client.class), anyString(), anyString(), any(MediaType.class))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel (envoi de test2)
		when(mockWebResourceBuilder.get()).thenThrow(new ResponseProcessingException(Response.serverError().build(), "Erreur lors de l'envoi"));

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockWebResourceBuilder, times(1)).get();
		
		assertEquals(1, service.getSmsSendingQueue().size());
		// Envoi d'un mail indiquant l'erreur d'envoi
		assertEquals(1, service.getEmailsSendingQueue().size());

	}	

	/**
	 * Test pour appeler l'API réelle
	 */
	@Ignore
	public void testRealAPI(){

		// Préparation
		MessagingBusinessService service = new MessagingBusinessService();
		service.getSmsSendingQueue().add("message de test1");
		service.getSmsSendingQueue().add("message de test2");
		service.getSmsSendingQueue().add("message de test3");
	
		SendSMSTaskRunnable runnable = new SendSMSTaskRunnable(
				"https://smsapi.free-mobile.fr/sendmsg?", 
				properties.getProperty("automation.bundle.messaging.sms.user"), 
				properties.getProperty("automation.bundle.messaging.sms.pass"), 
				service);
		runnable.run();
	}
}


