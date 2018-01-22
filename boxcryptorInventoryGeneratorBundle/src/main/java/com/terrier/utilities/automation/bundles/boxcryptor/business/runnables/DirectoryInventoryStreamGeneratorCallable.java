/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.business.runnables;

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
import com.terrier.utilities.automation.bundles.boxcryptor.communs.utils.BCUtils;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireFichier;
import com.terrier.utilities.automation.bundles.boxcryptor.objects.BCInventaireRepertoire;

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

	// Index
	private int index;
	// Inventaire du répertoire
	private BCInventaireRepertoire inventaireR;
	// Répertoire chiffré
	private String absRepertoireChiffre;
	// Répertoire non chiffré
	private String absRepertoireNonChiffre;


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
	public DirectoryInventoryStreamGeneratorCallable(final int index, final ExecutorService executorPool, final String nomTraitementParent, final BCInventaireRepertoire inventaireExistant, final String absRepertoireChiffre, final String absRepertoireNonChiffre){
		this.index = index;
		this.absRepertoireChiffre = absRepertoireChiffre;
		this.absRepertoireNonChiffre = absRepertoireNonChiffre;
		if(inventaireExistant != null){
			this.inventaireR = inventaireExistant;
		}
		else{
			this.inventaireR = new BCInventaireRepertoire(
					FileSystems.getDefault().getPath(this.absRepertoireChiffre).getFileName().toString(), 
					FileSystems.getDefault().getPath(this.absRepertoireNonChiffre).getFileName().toString());
			this.inventaireR.setDateModificationDernierInventaire(0L);
		}
		this.executorPool = executorPool;
		this.nomTraitementParent = nomTraitementParent;
	}


	/* (non-Javadoc)
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public BCInventaireRepertoire call() throws Exception {

		Calendar startTraitement = Calendar.getInstance();
		LOGGER.debug("[{}] - INIT THREAD [{}] date=[{}] > {} ms", 
				this.index, 
				BCUtils.getLibelleDateUTCFromMillis(inventaireR.getDateModificationDernierInventaire()),
				this.nomTraitementParent, Calendar.getInstance().getTimeInMillis() - startTraitement.getTimeInMillis());


		// Premier parcours pour trouver les sous répertoires et lancer les tâches correspondantes
		List<Future<BCInventaireRepertoire>> listeExecSousRepertoires = new ArrayList<Future<BCInventaireRepertoire>>();

		// Parcours du répertoire non chiffré
		try (DirectoryStream<Path> dsNonChiffre = Files.newDirectoryStream(FileSystems.getDefault().getPath(absRepertoireNonChiffre), new DirectoryFilter());) {
			for (Path sousRepertoireNonChiffre : dsNonChiffre) {
				try(DirectoryStream<Path> dsChiffre = Files.newDirectoryStream(FileSystems.getDefault().getPath(absRepertoireChiffre), new DirectoryFilter());){

					for (Path sousRepertoireChiffre : dsChiffre) {

						if(Files.getLastModifiedTime(sousRepertoireChiffre).toMillis() == Files.getLastModifiedTime(sousRepertoireNonChiffre).toMillis()){
							listeExecSousRepertoires.add(
									this.executorPool.submit(
											new DirectoryInventoryStreamGeneratorCallable(
													this.index,
													this.executorPool,
													this.nomTraitementParent + "|" + sousRepertoireNonChiffre.getFileName().toString(), 
													this.inventaireR.getBCInventaireSousRepertoire(sousRepertoireChiffre, sousRepertoireNonChiffre),
													sousRepertoireChiffre.toFile().getAbsolutePath(), sousRepertoireNonChiffre.toFile().getAbsolutePath()))
									);						
						}
					}
				}
				catch(Exception e){
					LOGGER.warn("[{}] - THREAD [{}] Le répertoire {} est introuvable. Passage au répertoire suivant", index, this.nomTraitementParent, sousRepertoireNonChiffre);
				}
			}


			try(DirectoryStream<Path> dsfNonChiffre = Files.newDirectoryStream(FileSystems.getDefault().getPath(absRepertoireNonChiffre), new FileFilter());){

				for (Path fichierNonChiffre : dsfNonChiffre) {
					try(DirectoryStream<Path> dsfChiffre = Files.newDirectoryStream(FileSystems.getDefault().getPath(absRepertoireChiffre), new FileFilter());){

						for (Path fichierChiffre : dsfChiffre) {

							if(fichierChiffre.toFile().exists() && Files.getLastModifiedTime(fichierChiffre).toMillis() == Files.getLastModifiedTime(fichierNonChiffre).toMillis()){
								inventaireR.addFichier(new BCInventaireFichier(fichierChiffre.getFileName().toString(), fichierNonChiffre.getFileName().toString()));
								LOGGER.trace("[{}] - THREAD [{}] date=[{}] > fichier [{}]", 
										index, 
										this.nomTraitementParent,
										BCUtils.getLibelleDateUTCFromMillis(Files.getLastModifiedTime(fichierChiffre).toMillis()),
										fichierNonChiffre.getFileName().toString()); 
								// Mise à jour de la date, ssi différent du fichier d'inventaire
								if(!fichierNonChiffre.getFileName().toString().equals(BCUtils.INVENTORY_FILENAME) 
										&& 
										(inventaireR.getDateModificationDernierInventaire() == null
										|| Files.getLastModifiedTime(fichierChiffre).toMillis() > inventaireR.getDateModificationDernierInventaire())){
									LOGGER.debug("[{}] - THREAD [{}] date mise à jour =[{}]", 
											index, 
											this.nomTraitementParent, 
											BCUtils.getLibelleDateUTCFromMillis(Files.getLastModifiedTime(fichierChiffre).toMillis()));
									inventaireR.setDateModificationDernierInventaire(Files.getLastModifiedTime(fichierChiffre).toMillis());
								}
							}
						}
					}
					catch(Exception e){
						LOGGER.warn("[{}] - THREAD [{}] Le fichier {} est introuvable. Passage au fichier suivant", index, this.nomTraitementParent, fichierNonChiffre.getFileName());
					}
				}
			}
			catch (Exception e) {
				LOGGER.info("Erreur lors du traitement du répertoire [{}]", absRepertoireNonChiffre, e);
			}

		} catch (Exception e) {
			LOGGER.info("Erreur lors du traitement du répertoire [{}]", absRepertoireNonChiffre, e);
		}

		// Récupération des résultats des sous répertoires
		for (Future<BCInventaireRepertoire> resultatSousRepertoire : listeExecSousRepertoires) {
			BCInventaireRepertoire ssRepertoire = resultatSousRepertoire.get();
			LOGGER.trace("[{}] - THREAD [{}] date=[{}] > sous répertoire [{}]", 
					index, 
					this.nomTraitementParent,
					BCUtils.getLibelleDateUTCFromMillis(ssRepertoire.getDateModificationDernierInventaire()),
					ssRepertoire.get_NomFichierClair()); 
			// Mise à jour de la date
			if(inventaireR.getDateModificationDernierInventaire() == null
					|| (ssRepertoire.getDateModificationDernierInventaire() != null 
					&& ssRepertoire.getDateModificationDernierInventaire() > inventaireR.getDateModificationDernierInventaire())){
				LOGGER.debug("[{}] - THREAD [{}] date mise à jour =[{}]", index, this.nomTraitementParent, BCUtils.getLibelleDateUTCFromMillis(ssRepertoire.getDateModificationDernierInventaire()));
				inventaireR.setDateModificationDernierInventaire(ssRepertoire.getDateModificationDernierInventaire());
			}
			inventaireR.addSSRepertoire(ssRepertoire);
		}
		/*
		 * Log du traitement
		 */
		LOGGER.debug("[{}] - THREAD [{}] date=[{}] > {} ms", 
				this.index, 
				this.nomTraitementParent, 
				BCUtils.getLibelleDateUTCFromMillis(inventaireR.getDateModificationDernierInventaire()),
				Calendar.getInstance().getTimeInMillis() - startTraitement.getTimeInMillis());

		return inventaireR;
	}
}
