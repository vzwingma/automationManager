/**
 * 
 */
package com.terrier.utilities.automation.bundles.save.to.business;

import static org.junit.Assert.assertFalse;
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

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.exceptions.KeyNotFoundException;
import com.terrier.utilities.automation.bundles.communs.utils.files.visitors.DeleteAllDirVisitor;
import com.terrier.utilities.automation.bundles.save.to.business.enums.ConfigKeyEnums;

/**
 * Test du business service
 * @author vzwingma
 *
 */
public class TestSendToBusinessService {

	
	private SaveToBusinessService service;

	private static final Logger LOGGER = LoggerFactory.getLogger( TestSendToBusinessService.class );
	/**
	 * Mock dictionnary
	 * @throws KeyNotFoundException
	 */
	@Before
	public void mockDictionnary() throws KeyNotFoundException{
		service = Mockito.spy(new SaveToBusinessService());
		Properties properties = new Properties();
		try {
		    properties.load(new FileInputStream(new File("src/test/resources/test.bundles.save.to.cfg")));
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
		service.startDelay = 0L;
	}
	

	
	/**
	 * @throws IOException
	 */
	@BeforeClass
	public static void initFiles() throws IOException{
		LOGGER.info("Création des fichiers init dans " + FileSystems.getDefault().getPath("src/test/resources/download/").toAbsolutePath() + " : " + Files.isDirectory(FileSystems.getDefault().getPath("src/test/resources/download/")));
		if(!Files.exists(FileSystems.getDefault().getPath("src/test/resources/download/_HUBICEU257005.pdf"))){
			Files.createFile(FileSystems.getDefault().getPath("src/test/resources/download/_HUBICEU257005.pdf"));
		}
		if(!Files.exists(FileSystems.getDefault().getPath("src/test/resources/download/Facture_Free_201512_2375646_593050686.pdf"))){
			Files.createFile(FileSystems.getDefault().getPath("src/test/resources/download/Facture_Free_201512_2375646_593050686.pdf"));
		}
	}
	
	
	
	/**
	 * Test copie
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testCopieCloud() throws IOException, InterruptedException{
		assertNotNull(service);
		service.nbPatterns = 3;
		// Scan
		assertTrue(service.validateConfig(1));
		service.startTreatment(1);
		Thread.sleep(400);
		// Verify
		Calendar c = Calendar.getInstance();
		int m = c.get(Calendar.MONTH)+1;
		String cl = "_HUBIC_" +c.get(Calendar.YEAR) + ( m<10 ? "0"+m : m)+ ".pdf";
		Path fichier1 = FileSystems.getDefault().getPath("src/test/resources/bc/Cloud/" + cl);
		assertTrue(Files.exists(fichier1));
		Files.delete(fichier1);
		Files.delete(FileSystems.getDefault().getPath("src/test/resources/bc/Cloud/"));
		
		assertTrue(Files.exists(FileSystems.getDefault().getPath("src/test/resources/download/_HUBICEU257005.pdf")));

	}
		
	/**
	 * Test copie
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testCopieFree() throws IOException, InterruptedException{
		
		assertTrue(service.validateConfig(0));
		service.startTreatment(0);
		Thread.sleep(1000);
		
		assertFalse(Files.exists(FileSystems.getDefault().getPath("src/test/resources/download/Facture_Free_201512_2375646_593050686.pdf")));
		
		Path fichier2 = FileSystems.getDefault().getPath("src/test/resources/bc/Free/Facture_Free_201512_2375646_593050686.pdf");
		assertTrue(Files.exists(fichier2));
		Files.delete(fichier2);
		Files.delete(FileSystems.getDefault().getPath("src/test/resources/bc/Free/"));
	}
	
	/**
	 * Test copie
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testCopieDirectory() throws IOException, InterruptedException{
		
		assertTrue(service.validateConfig(2));
		service.startTreatment(2);
		Thread.sleep(400);
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
		assertTrue(service.validateConfig(0));
		assertTrue(service.validateConfig(1));
		assertTrue(service.validateConfig(2));
	}
	
}
