package com.terrier.utilities.automation.bundles.boxcryptor.generate;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.boxcryptor.communs.utils.BCUtils;
import com.terrier.utilities.automation.bundles.boxcryptor.generate.objects.BCInventaireRepertoire;

/**
 * Main class of BoxCryptor Inventory Generator
 * @author vzwingma
 *
 */
public class BCInventoryGenerator {



	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BCInventoryGenerator.class);

	
	// Répertoire chiffré
	private File repertoireChiffre;
	// Répertoire non chiffré
	private File repertoireNonChiffre;

	private Calendar startTraitement = Calendar.getInstance();




	/**
	 * Start inventory
	 * @param args directories parameters
	 * @throws Exception error during generation
	 */
	public void startInventory(String cheminRepertoireChiffre, String cheminRepertoireNonChiffre) throws Exception{
		LOGGER.info("Début de la génération de l'inventaire");
		repertoireChiffre = new File(cheminRepertoireChiffre);
		repertoireNonChiffre = new File(cheminRepertoireNonChiffre);
		generateInventory();
	}



	/**
	 * Inventory generator
	 * @throws Exception  error during generation
	 */
	private void generateInventory() throws Exception{

		// Lecture de l'inventaire
		BCInventaireRepertoire inventaire = loadFileInventory();

		BCUtils.printDelayFromBeginning("Read file Inventory", this.startTraitement);

		// Création de l'inventaire
		ExecutorService threadsPool = Executors.newFixedThreadPool(100);
		DirectoryInventoryStreamGeneratorCallable inventory = new DirectoryInventoryStreamGeneratorCallable(
				threadsPool,
				this.repertoireNonChiffre.getName(),
				inventaire,
				this.repertoireChiffre.getAbsolutePath(), this.repertoireNonChiffre.getAbsolutePath());
		BCInventaireRepertoire inventaireNew = inventory.call();
		threadsPool.shutdown();
		BCUtils.printDelayFromBeginning("Generate Inventory", this.startTraitement);


		// Ecriture de l'inventaire
		BCUtils.dumpYMLInventory(this.repertoireNonChiffre, inventaireNew);
		BCUtils.printDelayFromBeginning("Dump Inventory", this.startTraitement);
	}

	/**
	 * Lecture de l'inventaire existant pour mise à jour
	 * @throws IOException
	 */
	public BCInventaireRepertoire loadFileInventory() throws IOException{
		// This will output the full path where the file will be written to...
		File inventoryFile = new File(repertoireNonChiffre, BCUtils.INVENTORY_FILENAME);
		BCInventaireRepertoire repertoire;
		if(inventoryFile.exists()){
			LOGGER.info("Enregistrement de la liste dans {}", inventoryFile.getCanonicalPath());
			repertoire = BCUtils.loadYMLInventory(repertoireNonChiffre.getAbsolutePath());
		}
		else{
			LOGGER.warn("Le fichier {} n'existe pas. Création du fichier", inventoryFile.getAbsolutePath());
			repertoire  = new BCInventaireRepertoire(repertoireChiffre.getName(), repertoireNonChiffre.getName());
		}
		return repertoire;
	}
}
