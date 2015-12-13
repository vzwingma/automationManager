package com.terrier.utilities.automation.bundles.boxcryptor.business.runnables;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.boxcryptor.communs.utils.BCUtils;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireRepertoire;

/**
 * Main class of BoxCryptor Inventory Generator
 * @author vzwingma
 *
 */
public class BCInventoryGeneratorRunnable implements Runnable {



	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BCInventoryGeneratorRunnable.class);

	private int index;
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
	public BCInventoryGeneratorRunnable(final int index, final String cheminRepertoireChiffre, final String cheminRepertoireNonChiffre){
		this.index = index;
		this.repertoireChiffre = new File(cheminRepertoireChiffre);
		this.repertoireNonChiffre = new File(cheminRepertoireNonChiffre);
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try{
			LOGGER.info("[{}] Début de la génération de l'inventaire [{}]", index, this.repertoireNonChiffre);
			// Lecture de l'inventaire
			BCInventaireRepertoire inventaire = loadFileInventory();

			BCUtils.printDelayFromBeginning(this.index, "Read file Inventory", this.startTraitement);

			// Création de l'inventaire
			ExecutorService threadsPool = Executors.newFixedThreadPool(100);
			DirectoryInventoryStreamGeneratorCallable inventory = new DirectoryInventoryStreamGeneratorCallable(
					threadsPool,
					this.repertoireNonChiffre.getName(),
					inventaire,
					this.repertoireChiffre.getAbsolutePath(), this.repertoireNonChiffre.getAbsolutePath());
			BCInventaireRepertoire inventaireNew = inventory.call();
			threadsPool.shutdown();
			BCUtils.printDelayFromBeginning(this.index, "Generate Inventory", this.startTraitement);


			// Ecriture de l'inventaire
			BCUtils.dumpYMLInventory(this.repertoireNonChiffre, inventaireNew);
			BCUtils.printDelayFromBeginning(this.index, "Dump Inventory", this.startTraitement);
		}
		catch(Exception e){
			LOGGER.error("[{}] Erreur lors de la génération de l'inventaire",this.index, e);
			
		}
	}

	/**
	 * Lecture de l'inventaire existant pour mise à jour
	 * @throws IOException
	 */
	protected BCInventaireRepertoire loadFileInventory() throws IOException{
		// This will output the full path where the file will be written to...
		File inventoryFile = new File(repertoireNonChiffre, BCUtils.INVENTORY_FILENAME);
		BCInventaireRepertoire repertoire;
		if(inventoryFile.exists()){
			LOGGER.info("[{}] Enregistrement de la liste dans {}", this.index, inventoryFile.getCanonicalPath());
			repertoire = BCUtils.loadYMLInventory(repertoireNonChiffre.getAbsolutePath());
		}
		else{
			LOGGER.warn("[{}] Le fichier {} n'existe pas. Création du fichier", this.index, inventoryFile.getAbsolutePath());
			repertoire  = new BCInventaireRepertoire(repertoireChiffre.getName(), repertoireNonChiffre.getName());
		}
		return repertoire;
	}
}
