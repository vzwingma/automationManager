/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.save.business;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;

import com.terrier.utilities.automation.bundles.communs.utils.AutomationUtils;
import com.terrier.utilities.automation.bundles.communs.utils.files.visitors.CopyDirVisitor;

/**
 * @author vzwingma
 *
 */
public class SaveToBoxCryptorCallable implements Runnable{

	

	private static final Logger LOGGER = Logger.getLogger( SaveToBoxCryptorCallable.class );
	
	// Paramètres
	private String repertoireSource; 
	private String patternEntree; 
	private String repertoireDestinataire; 
	private String patternSortie;
	
	/**
	 * @param repertoireSource répertoire source
	 * @param patternEntree pattern d'entrée (si null : copie du répertoire)
	 * @param repertoireDestinataire répertoire destinataire (X: de boxcryptor)
	 * @param patternSortie pattern de sortie (si modification)
	 */
	public SaveToBoxCryptorCallable(String repertoireSource, String patternEntree, String repertoireDestinataire, String patternSortie){
		this.repertoireSource = repertoireSource;
		this.patternEntree = patternEntree;
		this.repertoireDestinataire = repertoireDestinataire;
		this.patternSortie = patternSortie;
	}

	/**
	 * Traitement d'un répertoire
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		String scanDir = repertoireSource;
		LOGGER.info("Scan du répertoire  : " + scanDir);
		if(Files.isDirectory(FileSystems.getDefault().getPath(scanDir))){
			try{
				DirectoryStream<Path> downloadDirectoryPath = Files.newDirectoryStream(FileSystems.getDefault().getPath(scanDir));
				String regExMatch = patternEntree;
				LOGGER.trace(" > Matcher : " + regExMatch);
				if(regExMatch != null && !regExMatch.isEmpty()){

					for (Path fichier : downloadDirectoryPath) {
						LOGGER.trace(" Traitement du fichier : " + fichier.getFileName().toString());
						if(fichier.getFileName().toString().matches(regExMatch)){
							LOGGER.trace(fichier.getFileName().toString() + " > match avec " + regExMatch);
							String outputPattern = patternSortie;
							if(patternSortie == null || patternSortie.isEmpty()){
								outputPattern = fichier.getFileName().toString();
							}
							boolean resultat = copyToBoxcryptor(fichier, 
									AutomationUtils.replacePatterns(outputPattern), 
									repertoireDestinataire);		
							if(resultat){
								LOGGER.info("Copie réalisée vers BoxCrytor");
								Files.delete(fichier);
							}
							else{
								LOGGER.error("Erreur lors de la copie vers BoxCrytor");
							}
						}
					}
				}
				else{
					LOGGER.warn("Copie du répertoire complet");
					if(copyDirToBoxcryptor(FileSystems.getDefault().getPath(scanDir), repertoireDestinataire)){
						LOGGER.info("Copie réalisée vers BoxCrytor");
					}
					else{
						LOGGER.error("Erreur lors de la copie vers BoxCrytor [" +repertoireDestinataire+"]");
					}
				}
			} catch (IOException e) {
				LOGGER.error("Erreur lors du scan de " + FileSystems.getDefault().getPath(scanDir).toAbsolutePath().toString(), e);
			}
		}
		else{
			LOGGER.error("Erreur lors du scan de " + FileSystems.getDefault().getPath(scanDir).toAbsolutePath() + ". Ce n'est pas un répertoire");
		}
	}
	
	


	/**
	 * 
	 * @param fichierSource chemin vers le fichier source
	 * @param outFileName pattern de sortie
	 * @param directoryCible répertoire cible
	 */
	private boolean copyDirToBoxcryptor(Path fichierSource, String directoryCible){
		try {

			Path fichierCible = FileSystems.getDefault().getPath(directoryCible);
			LOGGER.debug(" > Copie du répertoire "+fichierSource+" vers : " + fichierCible);
			Files.walkFileTree(fichierSource, new CopyDirVisitor(fichierSource, fichierCible));
			return true;
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}



	/**
	 * 
	 * @param fichierSource chemin vers le fichier source
	 * @param outFileName pattern de sortie
	 * @param directoryCible répertoire cible
	 */
	private boolean copyToBoxcryptor(Path fichierSource, String outFileName, String directoryCible){
		try {
			if(outFileName == null || outFileName.isEmpty()){
				outFileName = fichierSource.getFileName().toString();
			}
			Path fichierCible = FileSystems.getDefault().getPath(directoryCible + "/" +outFileName);
			LOGGER.debug(" > Copie "+fichierSource+" vers : " + fichierCible);

			if(!Files.exists(FileSystems.getDefault().getPath(directoryCible))){
				LOGGER.info("Création du répertoire");
				Files.createDirectories(FileSystems.getDefault().getPath(directoryCible));
			}
			if (!Files.exists( fichierCible)) {
				LOGGER.debug("Création du fichier");
				Files.createFile( fichierCible);
			}
			CopyOption[] options = new CopyOption[]{
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES
			}; 
			Files.copy(fichierSource, fichierCible, options);
			return true;
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}


}