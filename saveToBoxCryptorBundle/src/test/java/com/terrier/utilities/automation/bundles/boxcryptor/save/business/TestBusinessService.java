/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.save.business;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

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
	
	@Before
	public void initDictionnary() throws KeyNotFoundException{
		service = Mockito.spy(new BusinessService());
		when(service.getKey(any(ConfigKeyEnums.class))).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				switch (((ConfigKeyEnums)invocation.getArguments()[0]).getCodeKey()) {
				case "automation.bundle.boxcryptor.save.repertoire.download":
					return "src/test/resources/download";
				default:
					break;
				}
				return null;
			}
			
		});
	}
	
	@Test
	public void testConstruct(){
		assertNotNull(service);
		service.initService();
	}
}
