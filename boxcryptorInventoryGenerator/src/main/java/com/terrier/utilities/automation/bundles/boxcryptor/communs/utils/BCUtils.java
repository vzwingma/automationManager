package com.terrier.utilities.automation.bundles.boxcryptor.communs.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.terrier.utilities.automation.bundles.boxcryptor.objects.AbstractBCInventaireStructure;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireRepertoire;

/**
 * Utils class
 * @author vzwingma
 *
 */
public class BCUtils {


	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BCUtils.class);

	// Inventory filename
	public static final String INVENTORY_FILENAME = "liste_Fichiers_BoxCryptor.yml";

	// Regex to split search values
	protected static final String SPLIT_REGEX = "[ _-]";

	/**
	 * Print delay from startTraitementCal
	 * @param treatementName  name of treatment
	 * @param startTraitementCal start time of Treatment
	 */
	public static void printDelayFrom(int index, String treatementName, Calendar startTraitementCal){
		LOGGER.debug("[{}][{}] > {} ms", 
				index, 
				treatementName, 
				(Calendar.getInstance().getTimeInMillis() - startTraitementCal.getTimeInMillis()));
	}


	/**
	 * Ecriture de l'inventaire (dump)
	 * @param inventaireR inventaire
	 * @throws IOException
	 */
	public static void dumpYMLInventory(final Yaml yml, final File repertoire, final BCInventaireRepertoire inventaireR) throws IOException{

		File inventoryFile = new File(repertoire.getAbsolutePath(), BCUtils.INVENTORY_FILENAME);
		if(!inventoryFile.exists()){
			inventoryFile.createNewFile();
		}

		FileWriter inventoryWriter = new FileWriter(inventoryFile);
		yml.dump(inventaireR, inventoryWriter);
		inventoryWriter.flush();
		inventoryWriter.close();
	}
	


	/**
	 * @param yml : Yaml 
	 * @param repertoire répertoire
	 * @return inventaire
	 * @throws IOException error during loading
	 */
	public static BCInventaireRepertoire loadYMLInventory(Yaml yml, String repertoire) throws IOException{
		if(repertoire != null){
			// This will output the full path where the file will be written to...
			File inventoryFile = new File(repertoire, BCUtils.INVENTORY_FILENAME);
			if(inventoryFile.exists()){
				LOGGER.info("Chargement de l'inventaire depuis {}", inventoryFile.getCanonicalPath());
				FileInputStream fis = new FileInputStream(inventoryFile);
				BCInventaireRepertoire inventaire = yml.loadAs(fis, BCInventaireRepertoire.class);
				fis.close();
				return inventaire;
			}
		}
		return null;
	}



	/**
	 * Search method of termes in inventory
	 * @param inventoryItem inventory item (directory or file)
	 * @param searchValue search value : 
	 * 	if null return true
	 * 	if spaces are present, searchValue is splitted in multiple terms
	 * return true if all terms are presents
	 */
	public static boolean searchTermsInInventory(AbstractBCInventaireStructure inventoryItem, String searchValue){
		if(searchValue == null || searchValue.isEmpty()){
			return true;
		}
		else{ 
			String[] allSearchValues = searchValue.split(SPLIT_REGEX);

			boolean found = true;
			for (String search : allSearchValues) {
				found &= inventoryItem.get_NomFichierChiffre().toUpperCase().contains(search.toUpperCase())
						|| inventoryItem.get_NomFichierClair().toUpperCase().contains(search.toUpperCase());
			}
			return found;
		}
	}
	

	/**
	 * Libellé date
	 * @param date
	 * @return libellé
	 */
	public static String getLibelleDateFromMillis(Long date){
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(date);
		return c.getTime().toString();
	}
}
