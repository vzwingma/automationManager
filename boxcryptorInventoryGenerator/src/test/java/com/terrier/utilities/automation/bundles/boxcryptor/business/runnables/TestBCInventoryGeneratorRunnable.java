/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.business.runnables;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author vzwingma
 *
 */
public class TestBCInventoryGeneratorRunnable {

	/**
	 * @throws IOException
	 */
	@BeforeClass
	public static void initFiles() throws IOException{
		if(Files.exists(FileSystems.getDefault().getPath("src/test/resources/data/clear/liste_Fichiers_BoxCryptor.yml"))){
			Files.delete(FileSystems.getDefault().getPath("src/test/resources/data/clear/liste_Fichiers_BoxCryptor.yml"));
		}
	}
	
	
	@Test
	public void testRunTreatment(){
		BCInventoryGeneratorRunnable runnable = new BCInventoryGeneratorRunnable(0, "src/test/resources/data/clear/", "src/test/resources/data/bc/");
		runnable.run();
		
		assertTrue(Files.exists(FileSystems.getDefault().getPath("src/test/resources/data/clear/liste_Fichiers_BoxCryptor.yml")));
	}
}
