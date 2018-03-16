package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.terrier.utilities.automation.bundles.emails.worker.business.EmailsWorkerBusinessService;
import com.terrier.utilities.automation.bundles.emails.worker.business.api.GoogleAuthHelper;

/**
 * Test du worker avec PJ
 * @author PVZN02821
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAttachementsRunnable {

	@Mock
	private EmailsWorkerBusinessService service;
	
	private AttachementsRunnable runnable ;
	
	@Before
	public void mockRunnable() throws Exception{
		Message m = new Message();
		m.setId("11111");
		m.set("From", ArchiveEmailsRunnable.AUTOLIB_SENDER);
		m.set("Subject", ArchiveEmailsRunnable.AUTOLIB_OBJECTS.get(0));
		
		runnable = spy(new AttachementsRunnable(0, "Attachement", null, service));
		
		when(runnable.getMailsInbox()).thenReturn(Arrays.asList(m));
		when(runnable.getSender(any(Message.class))).thenReturn(
				(String) m.get("From"));
		when(runnable.getObject(any(Message.class))).thenReturn(
				(String) m.get("Subject"));
	//	when(runnable.archiveMessage(anyString())).thenReturn(true);
	}

	@Test
	public void testRealAPI() throws IOException{
		runnable = new AttachementsRunnable(0, "Attachement", GoogleAuthHelper.getGmailService(GmailScopes.MAIL_GOOGLE_COM), service);
		runnable.executeRule();
	}

	/**
	 * Filtre des messages
	 * Seuls les 2 premiers doivent fonctionner
	 */
	@Test
	public void testRuleFilter(){
		assertEquals(1, runnable.executeRule());
	}
}
