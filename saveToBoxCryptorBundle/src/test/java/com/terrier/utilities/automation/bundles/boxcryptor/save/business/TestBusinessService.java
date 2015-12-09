/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.save.business;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.terrier.utilities.automation.bundles.communs.enums.ConfigKeyEnums;
import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;

/**
 * Test du business service
 * @author vzwingma
 *
 */
public class TestBusinessService {

	
	private BusinessService service;
	
	/**
	 * Mock dictionnary
	 * @throws KeyNotFoundException
	 */
	@Before
	public void mockDictionnary() throws KeyNotFoundException{
		service = Mockito.spy(new BusinessService());
		Properties properties = new Properties();
		try {
		    properties.load(new FileInputStream(new File("src/test/resources/test.bundles.boxcryptor.save.cfg")));
		} catch (IOException e) { e.printStackTrace();}
		
		when(service.getKey(any(ConfigKeyEnums.class))).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return properties.getProperty(((ConfigKeyEnums)invocation.getArguments()[0]).getCodeKey());
			}
			
		});
	}
	
	/**
	 * Test de validation du service
	 */
	@Test
	public void testValidateService(){
		assertNotNull(service);
		assertTrue(service.validateConfig());
	}
	
	/**
	 * Test copie
	 */
	@Test
	public void testCopie(){
		assertNotNull(service);
		service.scan(service.getKey(ConfigKeyEnums.DOWNLOAD));
		
	}
}
