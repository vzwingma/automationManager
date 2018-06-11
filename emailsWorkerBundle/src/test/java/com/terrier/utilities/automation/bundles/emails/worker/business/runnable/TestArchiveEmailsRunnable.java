package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.terrier.utilities.automation.bundles.emails.worker.business.EmailsWorkerBusinessService;
import com.terrier.utilities.automation.bundles.emails.worker.business.api.GoogleAuthHelper;

/**
 * Test du worker Autolib
 * @author PVZN02821
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestArchiveEmailsRunnable {

	@Mock
	private EmailsWorkerBusinessService service;
	
	private ArchiveEmailsRunnable runnable ;
	
	@Before
	public void mockRunnable() throws Exception{
		Message m = new Message();
		m.setId("11111");
		m.set("From", ArchiveEmailsRunnable.AUTOLIB_SENDER);
		m.set("Subject", ArchiveEmailsRunnable.AUTOLIB_OBJECTS.get(0));
		
		Message m2 = new Message();
		m2.setId("22222");
		m2.set("From", ArchiveEmailsRunnable.AUTOLIB_SENDER);
		m2.set("Subject", ArchiveEmailsRunnable.AUTOLIB_OBJECTS.get(1));
		
		Message m3 = new Message();
		m3.setId("33333");
		m3.set("From", ArchiveEmailsRunnable.AUTOLIB_SENDER);
		m3.set("Subject", "Autolib' : Ticket de débit - information sur votre paiement");
		
		Message m4 = new Message();
		m4.setId("44444");
		m4.set("From", "test");
		m4.set("Subject", ArchiveEmailsRunnable.AUTOLIB_OBJECTS.get(1));

		
		runnable = spy(new ArchiveEmailsRunnable(0, "Autolib'", null, service));
		
		when(runnable.getMailsInbox()).thenReturn(Arrays.asList(m, m2, m3, m4));
		when(runnable.getSender(any(Message.class))).thenReturn(
				(String) m.get("From"), 
				(String) m2.get("From"), 
				(String) m3.get("From"), 
				(String) m4.get("From"));
		
		when(runnable.getObject(any(Message.class))).thenReturn(
				(String) m.get("Subject"), 
				(String) m2.get("Subject"), 
				(String) m3.get("Subject"), 
				(String) m4.get("Subject"));
		when(runnable.archiveMessage(any(Message.class))).thenReturn(true);
	}

	@Test
	public void testRealAPI() throws IOException{
		runnable = new ArchiveEmailsRunnable(0, "Autolib'", GoogleAuthHelper.getGmailService(GmailScopes.MAIL_GOOGLE_COM), service);
		runnable.executeRule();
	}

	/**
	 * Filtre des messages
	 * Seuls les 2 premiers doivent fonctionner
	 */
	@Test
	public void testRuleFilter(){
		assertEquals(2, runnable.executeRule());
	}
}
