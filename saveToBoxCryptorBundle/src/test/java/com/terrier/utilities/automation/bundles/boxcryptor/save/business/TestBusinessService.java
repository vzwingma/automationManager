/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.save.business;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Calendar;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
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

	private static final Logger LOGGER = Logger.getLogger( TestBusinessService.class );
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
		
		when(service.getKey(any(ConfigKeyEnums.class), anyInt())).thenAnswer(new Answer<String>() {

			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				return properties.getProperty(((ConfigKeyEnums)invocation.getArguments()[0]).getCodeKey() + "." + invocation.getArguments()[1]);
			}
			
		});
	}
	

	
	@BeforeClass
	public static void initFiles() throws IOException{
		LOGGER.info("Création des fichiers init dans " + FileSystems.getDefault().getPath("src/test/resources/download/").toAbsolutePath() + " : " + Files.isDirectory(FileSystems.getDefault().getPath("src/test/resources/download/")));
		Files.createFile(FileSystems.getDefault().getPath("src/test/resources/download/_HUBICEU257005.pdf"));
		Files.createFile(FileSystems.getDefault().getPath("src/test/resources/download/Facture_Free_201512_2375646_593050686.pdf"));
	}
	
	
	
	/**
	 * Test copie
	 * @throws IOException 
	 */
	@Test
	public void testCopie() throws IOException{
		assertNotNull(service);
		service.scan();
		String cl = "_HUBIC_" +Calendar.getInstance().get(Calendar.YEAR) + (Calendar.getInstance().get(Calendar.MONTH)+1)+ ".pdf";
		Path fichier1 = FileSystems.getDefault().getPath("src/test/resources/bc/Cloud/" + cl);
		assertTrue(Files.exists(fichier1));
		Files.delete(fichier1);
		Files.delete(FileSystems.getDefault().getPath("src/test/resources/bc/Cloud/"));
		Path fichier2 = FileSystems.getDefault().getPath("src/test/resources/bc/Free/Facture_Free_201512_2375646_593050686.pdf");
		assertTrue(Files.exists(fichier2));
		Files.delete(fichier2);
		Files.delete(FileSystems.getDefault().getPath("src/test/resources/bc/Free/"));
		
		
		Path dir1 = FileSystems.getDefault().getPath("src/test/resources/bc/directory/d1.txt");
		assertTrue(Files.exists(dir1));
		Path dir2 = FileSystems.getDefault().getPath("src/test/resources/bc/directory/subdirectory/d2.txt");
		assertTrue(Files.exists(dir2));
		
		Files.walkFileTree(FileSystems.getDefault().getPath("src/test/resources/bc/directory"), new DeleteAllDirVisitor());
	}
	
	

	/**
	 * Test de validation du service
	 */
	@Test
	public void testValidateService(){
		assertNotNull(service);
		assertTrue(service.validateConfig());
	}
	
}
