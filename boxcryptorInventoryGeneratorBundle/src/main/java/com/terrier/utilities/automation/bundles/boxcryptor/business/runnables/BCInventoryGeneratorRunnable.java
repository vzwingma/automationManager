package com.terrier.utilities.automation.bundles.boxcryptor.business.runnables;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.terrier.utilities.automation.bundles.boxcryptor.communs.exceptions.InventoryNotFoundException;
import com.terrier.utilities.automation.bundles.boxcryptor.communs.utils.BCUtils;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireRepertoire;
import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.TypeMessagingEnum;

/**
 * Main class of BoxCryptor Inventory Generator
 * @author vzwingma
 *
 */
public class BCInventoryGeneratorRunnable extends AbstractAutomationService implements Runnable {



	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BCInventoryGeneratorRunnable.class);

	private int index;
	// Répertoire chiffré
	private File repertoireChiffre;
	// Répertoire non chiffré
	private File repertoireNonChiffre;

	private Long dateDernierTraitement;
	
	private Yaml yml;
	/**
	 * Start inventory
	 * @param args directories parameters
	 * @throws Exception error during generation
	 */
	public BCInventoryGeneratorRunnable(final int index, final Yaml yml, final String cheminRepertoireNonChiffre, final String cheminRepertoireChiffre){
		this.index = index;
		this.yml = yml;
		this.repertoireChiffre = new File(cheminRepertoireChiffre);
		this.repertoireNonChiffre = new File(cheminRepertoireNonChiffre);
	}


	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		try{
			Calendar startTraitement = Calendar.getInstance();
			LOGGER.info("[{}] Début de la génération de l'inventaire [{}]", index, this.repertoireNonChiffre);
			// Lecture de l'inventaire
			BCInventaireRepertoire inventaire = loadFileInventory();

			this.dateDernierTraitement = inventaire.getDateModificationDernierInventaire();

			if(this.dateDernierTraitement != null && this.dateDernierTraitement > 0){
				LOGGER.info("[{}] Date du dernier inventaire [{}]", index, BCUtils.getLibelleDateUTCFromMillis(this.dateDernierTraitement));
			}
			else{
				LOGGER.warn("[{}] Date du dernier inventaire [Jamais]", index);
			}

			BCUtils.printDelayFrom(this.index, "Read file Inventory", startTraitement);

			// Création de l'inventaire
			ExecutorService threadsPool = Executors.newFixedThreadPool(100);
			DirectoryInventoryStreamGeneratorCallable inventory = new DirectoryInventoryStreamGeneratorCallable(
					this.index,
					threadsPool,
					this.repertoireNonChiffre.getName(),
					inventaire,
					this.repertoireChiffre.getAbsolutePath(), this.repertoireNonChiffre.getAbsolutePath());
			BCInventaireRepertoire inventaireNew = inventory.call();
			threadsPool.shutdown();
			BCUtils.printDelayFrom(this.index, "Generate Inventory", startTraitement);


			// Ecriture de l'inventaire ssi il a changé
			if(this.dateDernierTraitement == null || inventaireNew.getDateModificationDernierInventaire() > this.dateDernierTraitement){
				LOGGER.debug("[{}] Date DernierTraitement {} / Date modification dernier inventaire : {}", 
						BCUtils.getLibelleDateUTCFromMillis(this.dateDernierTraitement), 
						BCUtils.getLibelleDateUTCFromMillis(inventaireNew.getDateModificationDernierInventaire()));
				
				BCUtils.dumpYMLInventory(this.yml, this.repertoireNonChiffre, inventaireNew);
				BCUtils.printDelayFrom(this.index, "Dump Inventory", startTraitement);
				LOGGER.info("[{}] Inventaire de {} généré", this.index, this.repertoireNonChiffre.getName());
				sendMessage("Génération de l'inventaire de " + this.repertoireNonChiffre.getName());
			}
			else{
				LOGGER.info("[{}] L'inventaire n'a pas changé depuis. Pas de mise à jour du fichier", this.index);
			}
		}
		catch(Exception e){
			LOGGER.error("[{}] Erreur lors de la génération de l'inventaire",this.index, e);
			sendMessage("Erreur lors de la génération de l'inventaire de [" + this.repertoireNonChiffre.getName() +"]");

		}
	}

	
	
	/**
	 * Lecture de l'inventaire existant pour mise à jour
	 * @throws IOException
	 */
	protected BCInventaireRepertoire loadFileInventory() throws InventoryNotFoundException, IOException{
		// This will output the full path where the file will be written to...
		File inventoryFile = new File(repertoireNonChiffre, BCUtils.INVENTORY_FILENAME);
		BCInventaireRepertoire repertoire;
		if(inventoryFile.exists()){
			LOGGER.info("[{}] Mise à jour de l'inventaire de {}", this.index, inventoryFile.getCanonicalPath());
			repertoire = BCUtils.loadYMLInventory(this.yml, repertoireNonChiffre.getAbsolutePath());
			if(repertoire != null){			
				LOGGER.debug("[{}] Mise à jour de l'inventaire à partir de {} ", this.index, BCUtils.getLibelleDateUTCFromMillis(repertoire.getDateModificationDernierInventaire()));
			}
		}
		else{
			LOGGER.warn("[{}] Le fichier {} n'existe pas. Création du fichier", this.index, inventoryFile.getAbsolutePath());
			repertoire  = new BCInventaireRepertoire(repertoireChiffre.getName(), repertoireNonChiffre.getName());
		}
		return repertoire;
	}


	@Override
	public void notifyUpdateDictionary() {
		// Rien
	}


	/**
	 * Envoi d'un message de notification par mail
	 * @param message message à envoyer
	 */
	private void sendMessage(String message){
		String date = BCUtils.getLibelleDateUTCFromMillis(Calendar.getInstance().getTimeInMillis());
		sendNotificationMessage(TypeMessagingEnum.EMAIL, "Génération inventaire BoxCryptor", date + " :: " + message);
	}
}
