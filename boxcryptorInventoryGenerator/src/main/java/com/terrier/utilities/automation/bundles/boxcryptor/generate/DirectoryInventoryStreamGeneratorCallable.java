/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.generate;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.boxcryptor.communs.filters.DirectoryFilter;
import com.terrier.utilities.automation.bundles.boxcryptor.communs.filters.FileFilter;
import com.terrier.utilities.automation.bundles.boxcryptor.generate.objects.BCInventaireFichier;
import com.terrier.utilities.automation.bundles.boxcryptor.generate.objects.BCInventaireRepertoire;

/**
 * Callable d'un inventaire d'un répertoire
 * @author vzwingma
 *
 */
public class DirectoryInventoryStreamGeneratorCallable implements Callable<BCInventaireRepertoire> {


	/**
	 * Logger
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(DirectoryInventoryStreamGeneratorCallable.class);

	// Inventaire du répertoire
	private BCInventaireRepertoire inventaireR;
	// Répertoire chiffré
	private String absRepertoireChiffre;
	// Répertoire non chiffré
	private String absRepertoireNonChiffre;
	// Démarrage du traitement
	private Calendar startTraitement;

	// Pool de threads
	private ExecutorService executorPool;
	// Parent
	private String nomTraitementParent;

	/**
	 * Génération	
	 * @param parent callable parent
	 * @param repertoireChiffre 
	 * @param repertoireNonChiffre
	 */
	public DirectoryInventoryStreamGeneratorCallable(final ExecutorService executorPool, final String nomTraitementParent, final BCInventaireRepertoire inventaireExistant, final String absRepertoireChiffre, final String absRepertoireNonChiffre){
		this.absRepertoireChiffre = absRepertoireChiffre;
		this.absRepertoireNonChiffre = absRepertoireNonChiffre;
		if(inventaireExistant != null){
			this.inventaireR = inventaireExistant;
		}
		else{
			this.inventaireR = new BCInventaireRepertoire(
					FileSystems.getDefault().getPath(this.absRepertoireChiffre).getFileName().toString(), 
					FileSystems.getDefault().getPath(this.absRepertoireNonChiffre).getFileName().toString());
		}
		this.executorPool = executorPool;
		this.nomTraitementParent = nomTraitementParent;
	}


	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public BCInventaireRepertoire call() throws Exception {

		this.startTraitement = Calendar.getInstance();

		// Premier parcours pour trouver les sous répertoires et lancer les tâches correspondantes
		List<Future<BCInventaireRepertoire>> listeExecSousRepertoires = new ArrayList<Future<BCInventaireRepertoire>>();

		// Parcours du répertoire non chiffré
		try {
			DirectoryStream<Path> dsNonChiffre = Files.newDirectoryStream(FileSystems.getDefault().getPath(absRepertoireNonChiffre), new DirectoryFilter());
			DirectoryStream<Path> dsChiffre;
			for (Path sousRepertoireNonChiffre : dsNonChiffre) {
				dsChiffre = Files.newDirectoryStream(FileSystems.getDefault().getPath(absRepertoireChiffre), new DirectoryFilter());
				for (Path sousRepertoireChiffre : dsChiffre) {
					if(Files.getLastModifiedTime(sousRepertoireChiffre).toMillis() == Files.getLastModifiedTime(sousRepertoireNonChiffre).toMillis()){
						listeExecSousRepertoires.add(
								this.executorPool.submit(
										new DirectoryInventoryStreamGeneratorCallable(
												this.executorPool,
												this.nomTraitementParent + "|" + sousRepertoireNonChiffre.getFileName().toString(), 
												this.inventaireR.getBCInventaireSousRepertoire(sousRepertoireChiffre, sousRepertoireNonChiffre),
												sousRepertoireChiffre.toFile().getAbsolutePath(), sousRepertoireNonChiffre.toFile().getAbsolutePath()))
								);						
					}
				}
			}


			DirectoryStream<Path> dsfNonChiffre = Files.newDirectoryStream(FileSystems.getDefault().getPath(absRepertoireNonChiffre), new FileFilter());
			DirectoryStream<Path> dsfChiffre;
			for (Path fichierNonChiffre : dsfNonChiffre) {
				dsfChiffre = Files.newDirectoryStream(FileSystems.getDefault().getPath(absRepertoireChiffre), new FileFilter());
				for (Path fichierChiffre : dsfChiffre) {
					if(Files.getLastModifiedTime(fichierChiffre).toMillis() == Files.getLastModifiedTime(fichierNonChiffre).toMillis()){
						inventaireR.addFichier(new BCInventaireFichier(fichierChiffre.getFileName().toString(), fichierNonChiffre.getFileName().toString()));					
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Récupération des résultats des sous répertoires
		for (Future<BCInventaireRepertoire> resultatSousRepertoire : listeExecSousRepertoires) {
			BCInventaireRepertoire ssRepertoire = resultatSousRepertoire.get();
			inventaireR.addSSRepertoire(ssRepertoire);
		}
		printDelayTraitementFromBeginning();
		return inventaireR;
	}



	/**
	 * Print du temps de traitement
	 */
	private void printDelayTraitementFromBeginning(){
		LOGGER.info("THREAD [{}] > {} ms", this.nomTraitementParent, Calendar.getInstance().getTimeInMillis() - startTraitement.getTimeInMillis());
	}

}
