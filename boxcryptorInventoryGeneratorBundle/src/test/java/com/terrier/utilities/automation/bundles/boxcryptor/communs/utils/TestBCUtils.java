/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.communs.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import com.terrier.utilities.automation.bundles.boxcryptor.communs.exceptions.InventoryNotFoundException;
import com.terrier.utilities.automation.bundles.boxcryptor.communs.utils.BCUtils;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireFichier;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireRepertoire;

/**
 * Test class for utils methods
 * @author vzwingma
 *
 */
public class TestBCUtils {

	@BeforeClass
	public static void init(){
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
	}
	/**
	 * Test of multiple split
	 */
	@Test
	public void testMulitpleSplit(){
		String valueToSplit = "recherche de tous_ces_termes";
		String[] valuesSplitted = valueToSplit.split(BCUtils.SPLIT_REGEX);
		assertEquals(5, valuesSplitted.length);
	}
	
	@Test
	public void testLibelleCalendar(){
		
		String date = BCUtils.getLibelleDateUTCFromMillis(1450271887815L);
		assertTrue(date.startsWith("16/12/2015"));
	}
	/**
	 * Test dump and load YML file of inventory
	 * @throws IOException error
	 * @throws SecurityException error 
	 * @throws NoSuchFieldException  error
	 * @throws IllegalAccessException  error
	 * @throws IllegalArgumentException  error
	 * @throws InventoryNotFoundException error
	 */
	@Test
	public void testDumpYaml() throws IOException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, InventoryNotFoundException{
		System.setProperty("file.encoding","UTF-8");
		Field charset = Charset.class.getDeclaredField("defaultCharset");
		charset.setAccessible(true);
		charset.set(null,null);
		
		
		File testDir = new File("src/test/resources");
		File inventoryFile = new File(testDir, BCUtils.INVENTORY_FILENAME);
		// Prepare
		if(inventoryFile.exists()){
			inventoryFile.delete();
		}
		assertFalse(inventoryFile.exists());
		// Dump
		BCInventaireRepertoire testInventory = new BCInventaireRepertoire("倐徎婢冈呹忪僣庝左勓嗊宽坻墒受䀊", "nom en clair");
		testInventory.addFichier(new BCInventaireFichier("倐徎婢冈呹忪僣庝左勓嗊宽坻墒受䀊", "nom en clair"));
		BCUtils.dumpYMLInventory(new Yaml(), new File("src/test/resources"), testInventory);
		
		assertTrue(inventoryFile.exists());
		
		// Load
		BCInventaireRepertoire loadedInventory = BCUtils.loadYMLInventory(new Yaml(), testDir.getPath());
		assertNotNull(loadedInventory);
		assertEquals(testInventory.get_NomFichierChiffre(), loadedInventory.get_NomFichierChiffre());
		assertEquals(testInventory.get_NomFichierClair(), loadedInventory.get_NomFichierClair());
		assertEquals(testInventory.getMapInventaireFichiers().values().iterator().next().get_NomFichierChiffre(), loadedInventory.getMapInventaireFichiers().values().iterator().next().get_NomFichierChiffre());
		
	}
	
	
	@Test
	public void testSearchTermsInInventoryDir(){
		BCInventaireRepertoire testInventory = new BCInventaireRepertoire("倐徎婢冈呹忪僣庝左勓嗊宽坻墒受䀊", "nom en clair_et_attaché");
		
		// Test null
		assertTrue(BCUtils.searchTermsInInventory(testInventory, null));
		assertTrue(BCUtils.searchTermsInInventory(testInventory, ""));
		
		// Test false search
		assertFalse(BCUtils.searchTermsInInventory(testInventory, "clari"));
		assertFalse(BCUtils.searchTermsInInventory(testInventory, "倐彈删媘娺咴囦坂圤婷"));
		
		// Test one word
		assertTrue(BCUtils.searchTermsInInventory(testInventory, "clair"));
		// Test multiple word
		assertTrue(BCUtils.searchTermsInInventory(testInventory, "clair attaché"));

		// Test false multiple word 
		assertFalse(BCUtils.searchTermsInInventory(testInventory, "clair non attaché"));
	}
}
