package com.terrier.utilities.automation.bundles.emails.worker.business;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import com.google.api.services.gmail.Gmail;
import com.terrier.utilities.automation.bundles.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;

/**
 * Test du service Emails Worker
 * @author PVZN02821
 *
 */
@RunWith(MockitoJUnitRunner.class)
public class TestEmailsBusinessService {

	@Spy
	private EmailsWorkerBusinessService service;

	/**
	 * Mock dictionnary
	 * @throws KeyNotFoundException
	 */
	@Before
	public void mockDictionnary() throws KeyNotFoundException{

		Properties properties = new Properties();
		try {
		    properties.load(new FileInputStream(new File("src/test/resources/test.bundles.emails.worker.cfg")));
		} catch (IOException e) { e.printStackTrace();}
		
		when(service.getKey(any(ConfigKeyEnums.class))).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return properties.getProperty(((ConfigKeyEnums)invocation.getArguments()[0]).getCodeKey());
			}
			
		});
		
		when(service.getKey(any(ConfigKeyEnums.class), anyInt())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return properties.getProperty(((ConfigKeyEnums)invocation.getArguments()[0]).getCodeKey() + "." + invocation.getArguments()[1]);
			}
		});
		
		Gmail mockGMail = mock(Gmail.class);
		service.setScope(null);
		when(service.getGMailService()).thenReturn(mockGMail);
		
		service.startDelay = 0L;
	}
	
	

	/**
	 * Test de validation du service
	 */
	@Test
	public void testValidateService(){
		assertNotNull(service);
		assertTrue(service.validateConfig(0));
		assertTrue(service.validateConfig(1));
	}
	
	@Test
	public void testStartService(){
		assertNotNull(service);
		service.notifyUpdateDictionary();
		assertEquals(2, service.listeScheduled.size());
	}
}
