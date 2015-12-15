/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.business.runnables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.terrier.utilities.automation.bundles.boxcryptor.communs.utils.BCUtils;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireRepertoire;

/**
 * @author vzwingma
 *
 */
public class TestBCInventoryGeneratorRunnable {


	private BCInventoryGeneratorRunnable runnable;

	@Before
	public void init() throws IOException, InterruptedException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		if(Files.exists(FileSystems.getDefault().getPath("src/test/resources/data/clear/liste_Fichiers_BoxCryptor.yml"))){
			Files.delete(FileSystems.getDefault().getPath("src/test/resources/data/clear/liste_Fichiers_BoxCryptor.yml"));
		}

		runnable = spy(new BCInventoryGeneratorRunnable(0, new Yaml(), "src/test/resources/data/clear/", "src/test/resources/data/bc/"));
		doNothing().when(runnable).sendNotificationMessage(any(), anyString(), anyString());

		// Encoding en UTF-8
		// Forcage en UTF-8 pour les caractères chinois utilisés par BC
		System.setProperty("file.encoding","UTF-8");
		Field charset = Charset.class.getDeclaredField("defaultCharset");
		charset.setAccessible(true);
		charset.set(null,null);

		// Rapprochement des fichiers
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

	/**
	 * Création inventaire
	 * @throws IOException
	 */
	@Test
	public void testRunTreatmentsInventaire() throws IOException {
		// Test
		runnable.run();
		//Vérification
		File inventoryFile = new File("src/test/resources/data/clear");
		assertTrue(Files.exists(FileSystems.getDefault().getPath(inventoryFile.getAbsolutePath() + "/liste_Fichiers_BoxCryptor.yml")));

		BCInventaireRepertoire inventaire = BCUtils.loadYMLInventory(new Yaml(), inventoryFile.getAbsolutePath());
		assertNotNull(inventaire);
		assertEquals("bc", inventaire.get_NomFichierChiffre());
		assertEquals("clear", inventaire.get_NomFichierClair());
		assertEquals(1, inventaire.getMapInventaireFichiers().size());
		assertEquals("d1.txt", inventaire.getMapInventaireFichiers().get("95a0bca6b6a307c7c59d23a1e997b652").get_NomFichierClair());
		assertEquals(1, inventaire.getMapInventaireSousRepertoires().size());
		assertEquals("subdir", inventaire.getMapInventaireSousRepertoires().get("86ae37b338459868804e9697025ba4c2").get_NomFichierClair());


		Long dateMiseAJour = inventaire.getDateModificationDernierInventaire();
		assertNotNull(dateMiseAJour);
		// Relance de l'inventaire. Pas de mise à jour
		runnable.run();
		BCInventaireRepertoire inventaire2 = BCUtils.loadYMLInventory(new Yaml(), inventoryFile.getAbsolutePath());
		assertNotNull(inventaire2);
		assertEquals(dateMiseAJour, inventaire2.getDateModificationDernierInventaire());
		
		Files.delete(FileSystems.getDefault().getPath("src/test/resources/data/clear/liste_Fichiers_BoxCryptor.yml"));
	}
}
