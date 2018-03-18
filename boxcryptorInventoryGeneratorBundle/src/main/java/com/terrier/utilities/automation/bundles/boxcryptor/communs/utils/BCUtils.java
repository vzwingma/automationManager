package com.terrier.utilities.automation.bundles.boxcryptor.communs.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.terrier.utilities.automation.bundles.boxcryptor.communs.exceptions.InventoryNotFoundException;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.AbstractBCInventaireStructure;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireRepertoire;

/**
 * Utils class
 * @author vzwingma
 *
 */
public class BCUtils {


	// Constructeur Utilitaires
	private BCUtils(){}
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
			boolean create = inventoryFile.createNewFile();
			LOGGER.info("Création du fichier {} : {}", inventoryFile.getName(), create);
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
	public static BCInventaireRepertoire loadYMLInventory(Yaml yml, String repertoire) throws InventoryNotFoundException{
		try{
			if(repertoire != null){
				// This will output the full path where the file will be written to...
				Path inventoryFile = FileSystems.getDefault().getPath(repertoire, BCUtils.INVENTORY_FILENAME);
				if(inventoryFile != null && inventoryFile.toFile().exists()){
					LOGGER.info("Chargement de l'inventaire depuis {}", inventoryFile.toAbsolutePath());
					String content = new String(Files.readAllBytes(inventoryFile));
					LOGGER.debug("Contenu : \n{}", content);
					return yml.loadAs(content, BCInventaireRepertoire.class);
				}
			}
		}
		catch(Exception e){
			LOGGER.error("Erreur lors du chargement de l'inventaire", e);
		}
		LOGGER.warn("Impossible de charger l'inventaire depuis {}.", repertoire);
		throw new InventoryNotFoundException();
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
				found &= inventoryItem.getNomFichierChiffre().toUpperCase().contains(search.toUpperCase())
						|| inventoryItem.getNomFichierClair().toUpperCase().contains(search.toUpperCase());
			}
			return found;
		}
	}


	/**
	 * Libellé date
	 * @param date
	 * @return libellé
	 */
	public static String getLibelleDateUTCFromMillis(Long date){

		if(date == null){
			return "---";
		}
		else{
			Calendar c = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			c.setTimeInMillis(date);
			return new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.FRENCH).format(c.getTime());
		}
	}
}
