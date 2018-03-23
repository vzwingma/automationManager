package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.google.api.services.gmail.GmailScopes;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartBody;
import com.terrier.utilities.automation.bundles.emails.worker.business.EmailsWorkerBusinessService;
import com.terrier.utilities.automation.bundles.emails.worker.business.api.GMailService;
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

		when(service.getDestinationDirectory()).thenReturn("src/test/resources");
		runnable = spy(new AttachementsRunnable(0, "Attachement", null, service));

		Message m = new Message();
		m.setId("11111");
		m.set("From", "Test@send");
		m.set("Subject", "Test d'envoi");
		
		Message m2 = new Message();
		m2.setId("22222");
		m2.set("From", "Test@send");
		m2.set("Subject", "Test d'envoi");
		
		when(runnable.getMailsInbox()).thenReturn(Arrays.asList(m, m2));
		
		MessagePartBody mpb1 = new MessagePartBody();
		mpb1.setData("Y2VjaSBlc3QgbGUgYm9uIHRleHRl");
		mpb1.set(AttachementsRunnable.PART_FILENAME, "message1.txt");
		mpb1.set(GMailService.HEADER_FROM, m.get(GMailService.HEADER_FROM));
		mpb1.set(GMailService.HEADER_SUBJECT, m.get(GMailService.HEADER_SUBJECT));
		// Message 1 avec un pdf. Message 2 sans
		when(runnable.getAttachements(any(Message.class), eq(AttachementsRunnable.MIME_PDF)))
			.thenReturn(Arrays.asList(mpb1), new ArrayList<>());
		
		when(runnable.getSender(any(Message.class))).thenReturn(
				(String) m.get(GMailService.HEADER_FROM));
		when(runnable.getObject(any(Message.class))).thenReturn(
				(String) m.get(GMailService.HEADER_SUBJECT));
	//	when(runnable.archiveMessage(anyString())).thenReturn(true);
	}

	@Ignore
	public void testRealAPI() throws IOException{
		runnable = new AttachementsRunnable(0, "Attachement", GoogleAuthHelper.getGmailService(GmailScopes.MAIL_GOOGLE_COM), service);
		runnable.executeRule();
	}

	/**
	 * Filtre des messages
	 * Seuls le premier doivent fonctionner
	 */
	@Test
	public void testRuleFilter(){
		assertEquals(1, runnable.executeRule());
		assertTrue(Files.exists(Paths.get("src/test/resources/message1.txt")));
	}
}
