package com.terrier.utilities.automation.bundles.emails.worker.business.runnable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
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
 * Test du worker Autolib
 * @author PVZN02821
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestAutolibEmailsWorkerRunnable {

	@Mock
	private EmailsWorkerBusinessService service;
	
	private AutolibEmailsWorkerRunnable runnable ;
	
	@Before
	public void mockRunnable() throws Exception{
		Message m = new Message();
		m.setId("11111");
		m.set("From", AutolibEmailsWorkerRunnable.AUTOLIB_SENDER);

		Message m2 = new Message();
		m2.setId("22222");
		m2.set("From", "test");

		runnable = spy(new AutolibEmailsWorkerRunnable(0, null, service));
		
		when(runnable.getMailsInbox()).thenReturn(Arrays.asList(m, m2));
		when(runnable.getSender(anyString())).thenReturn(AutolibEmailsWorkerRunnable.AUTOLIB_SENDER, "test");
		when(runnable.archiveMessage(anyString())).thenReturn(true);
	}

	//Before
	public void init() throws IOException{
		runnable = new AutolibEmailsWorkerRunnable(0, GoogleAuthHelper.getGmailService(GmailScopes.MAIL_GOOGLE_COM), service);
	}

	/**
	 * Filtre des messages
	 */
	@Test
	public void testRuleFilter(){
		assertEquals(1, runnable.executeRule());
	}
}
