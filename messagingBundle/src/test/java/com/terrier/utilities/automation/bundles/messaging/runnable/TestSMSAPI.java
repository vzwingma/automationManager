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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.ws.rs.core.MediaType;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
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
		doCallRealMethod().when(mockClient).addFilter(any());
		WebResource mockWebResource = mock(WebResource.class);
		when(mockClient.resource(anyString())).thenReturn(mockWebResource);
		WebResource.Builder mockWebResourceBuilder = mock(WebResource.Builder.class);
		when(mockWebResource.type(eq(MediaType.APPLICATION_FORM_URLENCODED))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel
		when(mockWebResourceBuilder.get(eq(ClientResponse.class))).thenReturn(
				new ClientResponse(500, null, null, null), 
				new ClientResponse(200, null, null, null));

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockClient, times(1)).resource(eq("https://smsapi.free-mobile.fr/sendmsg?user=1&pass=2&msg=-+message+de+test1%0A-+message+de+test2%0A-+message+de+test3%0A"));

		assertEquals(3, service.getSmsSendingQueue().size());
		// Envoi d'un mail indiquant l'erreur d'envoi
		assertEquals(0, service.getEmailsSendingQueue().size());

		
		// Run
		runnable.run();
		// Cette fois tout est passé
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
		doCallRealMethod().when(mockClient).addFilter(any());
		WebResource mockWebResource = mock(WebResource.class);
		when(mockClient.resource(anyString())).thenReturn(mockWebResource);
		WebResource.Builder mockWebResourceBuilder = mock(WebResource.Builder.class);
		when(mockWebResource.type(eq(MediaType.APPLICATION_FORM_URLENCODED))).thenReturn(mockWebResourceBuilder);
		// Erreur lors du premier appel
		when(mockWebResourceBuilder.get(eq(ClientResponse.class))).thenThrow(new UniformInterfaceException("Erreur lors de l'envoi", new ClientResponse(500, null, new InputStream() {
			
			@Override
			public int read() throws IOException {
				return -1;
			}
		}, null)));

		// Run
		runnable.run();

		//verify : Création du client 1 fois
		verify(mockClient, times(1)).resource(eq("https://smsapi.free-mobile.fr/sendmsg?user=1&pass=2&msg=-+message+de+test1%0A"));

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


