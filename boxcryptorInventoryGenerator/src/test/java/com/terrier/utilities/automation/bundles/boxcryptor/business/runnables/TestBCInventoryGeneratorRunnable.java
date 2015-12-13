/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.business.runnables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireRepertoire;

/**
 * @author vzwingma
 *
 */
public class TestBCInventoryGeneratorRunnable {


	@Before
	public void init() throws IOException, InterruptedException{
		if(Files.exists(FileSystems.getDefault().getPath("src/test/resources/data/clear/liste_Fichiers_BoxCryptor.yml"))){
			Files.delete(FileSystems.getDefault().getPath("src/test/resources/data/clear/liste_Fichiers_BoxCryptor.yml"));
		}
		
		
		Calendar c = Calendar.getInstance();
		new File("src/test/resources/data/clear/d1.txt").setLastModified(c.getTimeInMillis());
		new File("src/test/resources/data/bc/倐弭呠做嘢叧呂咀䃛.bc").setLastModified(c.getTimeInMillis());
		Thread.sleep(400);
		Calendar c2 = Calendar.getInstance();
		new File("src/test/resources/data/clear/subdir").setLastModified(c2.getTimeInMillis());
		new File("src/test/resources/data/bc/倐忽剶傦婽哾希奃䂴").setLastModified(c2.getTimeInMillis());
		Thread.sleep(400);
		Calendar c3 = Calendar.getInstance();
		new File("src/test/resources/data/clear/subdir/d2.txt").setLastModified(c3.getTimeInMillis());
		new File("src/test/resources/data/bc/倐忽剶傦婽哾希奃䂴/倐徹尜傴岡崪幐値䂫.bc").setLastModified(c3.getTimeInMillis());
	}

	@Test
	public void testRunTreatmentInventaireNull() throws IOException {
		BCInventoryGeneratorRunnable runnable = new BCInventoryGeneratorRunnable(0, "src/test/resources/data/clear/", "src/test/resources/data/bc/");
		runnable.run();

		assertTrue(Files.exists(FileSystems.getDefault().getPath("src/test/resources/data/clear/liste_Fichiers_BoxCryptor.yml")));
		
		BCInventaireRepertoire inventaire = runnable.loadFileInventory();
		assertNotNull(inventaire);
		assertEquals("bc", inventaire.get_NomFichierChiffre());
		assertEquals("clear", inventaire.get_NomFichierClair());
	}


	/**
	 * Tests fichiers existants
	 * @throws IOException
	 */
	@Test 
	public void testFichierInventairesExistants() throws IOException{
		BCInventoryGeneratorRunnable runnable = new BCInventoryGeneratorRunnable(1, "X:/eBooks", "D:/Perso/eBooks");
		BCInventaireRepertoire inventaire = runnable.loadFileInventory();
		assertNotNull(inventaire);

		BCInventoryGeneratorRunnable runnable2 = new BCInventoryGeneratorRunnable(1, "X:/Films/Kino", "D:/Perso/Films/Kino");
		BCInventaireRepertoire inventaire2 = runnable2.loadFileInventory();
		assertNotNull(inventaire2);
	}
}
