package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Response;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.terrier.utilities.automation.bundles.emails.worker.business.EmailsWorkerBusinessService;
import com.terrier.utilities.automation.bundles.emails.worker.business.HubicClient;
import com.terrier.utilities.automation.bundles.emails.worker.business.api.GoogleAuthHelper;

/**
 * Test du worker Hubic
 * @author PVZN02821
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestHubicDLRunnable {

	@Mock
	private EmailsWorkerBusinessService service;
	
	@Mock
	private HubicClient mockClient;
	
	private HubicDLRunnable runnable ;
	
	private static final String BODY_MAIL_URL = "Le PDF de la facture HUBICEU631909 est consultable sur https://www.ovh.com/cgi-bin/order/bill.pdf?reference=HUBICEU631909&passwd=aphn \r\n Suite du mail";

	@Before
	public void mockRunnable() throws Exception{
		Message m = new Message();
		m.setId("11111");
		m.set("From", HubicDLRunnable.HUBIC_SENDER);

		Message m2 = new Message();
		m2.setId("22222");
		m2.set("From", "test");

		runnable = spy(new HubicDLRunnable(0, "Hubic", null, service));
		
		when(runnable.getMailsInbox()).thenReturn(Arrays.asList(m, m2));
		when(runnable.getSender(any(Message.class))).thenReturn(HubicDLRunnable.HUBIC_SENDER, "test");
		when(runnable.getBody(any(Message.class))).thenReturn(BODY_MAIL_URL);
		
		
		runnable.setClient(mockClient);
		when(mockClient.callHTTPGetData(any(Invocation.Builder.class))).thenReturn(Response.ok().build());
		when(runnable.archiveMessage(any(Message.class))).thenReturn(true);
	}

	//Before
	public void init() throws IOException{
		runnable = new HubicDLRunnable(0, "Hubic", GoogleAuthHelper.getGmailService(GmailScopes.MAIL_GOOGLE_COM), service);
	}

	/**
	 * Filtre des messages
	 */
	@Test
	public void testRuleFilter(){
		assertEquals(1, runnable.executeRule());
	}


	
	
	@Test 
	public void testGetURL(){
		assertEquals("https://www.ovh.com/cgi-bin/order/bill.pdf?reference=HUBICEU631909&passwd=aphn", runnable.getURLFromBody(BODY_MAIL_URL));
	}
	
	@Test 
	public void testGetReference(){
		assertEquals("HUBICEU631909", runnable.getReference(runnable.getURLFromBody(BODY_MAIL_URL)));
	}
	
	
	@Test 
	public void testDownload() throws Exception{
		runnable.downloadFacture(BODY_MAIL_URL);
		verify(mockClient, times(1)).telechargementFichier(anyString());
	}
}
