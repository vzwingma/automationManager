/**
 * 
 */
package com.terrier.utilities.automation.bundles.save.to.business.runnable;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import org.apache.log4j.Logger;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.EventsTopicNameEnum;
import com.terrier.utilities.automation.bundles.communs.enums.messaging.TypeMessagingEnum;
import com.terrier.utilities.automation.bundles.communs.utils.AutomationUtils;
import com.terrier.utilities.automation.bundles.communs.utils.files.visitors.CopyDirVisitor;
import com.terrier.utilities.automation.bundles.save.to.business.enums.CommandeEnum;

/**
 * Traitement d'une copie ou d'un déplacement
 * @author vzwingma
 *
 */
public class SaveToTaskRunnable extends AbstractAutomationService implements Runnable {

	

	private static final Logger LOGGER = Logger.getLogger( SaveToTaskRunnable.class );
	
	// Paramètres
	private int index;
	private CommandeEnum commande;
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
	public SaveToTaskRunnable(int index, CommandeEnum commande, String repertoireSource, String patternEntree, String repertoireDestinataire, String patternSortie){
		this.index = index;
		this.commande = commande;
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
		LOGGER.info("[" + index + "] Scan du répertoire  : " + scanDir);
		if(Files.isDirectory(FileSystems.getDefault().getPath(scanDir))){
			try{
				DirectoryStream<Path> downloadDirectoryPath = Files.newDirectoryStream(FileSystems.getDefault().getPath(scanDir));
				String regExMatch = patternEntree;
				LOGGER.trace("[" + index + "] > Matcher : " + regExMatch);
				if(regExMatch != null && !regExMatch.isEmpty()){

					for (Path fichier : downloadDirectoryPath) {
						LOGGER.trace("[" + index + "] Traitement du fichier : " + fichier.getFileName().toString());
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
								LOGGER.info("[" + index + "] Copie réalisée vers " + repertoireDestinataire);
								if(CommandeEnum.MOVE.equals(commande)){
									// Suppression du fichier source
									Files.delete(fichier);
									// Et notification du déplacement
									sendNotificationMessage("Déplacement de " +fichier.getFileName().toString()+ " vers " + repertoireDestinataire);
								}
								else{
									sendNotificationMessage("Copie de " +fichier.getFileName().toString()+ " vers " + repertoireDestinataire);
								}
							}
							else{
								LOGGER.error("[" + index + "] Erreur lors de la copie vers BoxCrytor");
								// Et notification de l'erreur
								sendNotificationMessage("Erreur lors de la copie de " +fichier.getFileName().toString()+ " vers " + repertoireDestinataire);
							}
						}
					}
				}
				else{
					LOGGER.warn("[" + index + "] Copie du répertoire complet");
					if(copyDirToBoxcryptor(FileSystems.getDefault().getPath(scanDir), repertoireDestinataire)){
						LOGGER.info("[" + index + "] Copie réalisée vers BoxCrytor");
						sendNotificationMessage("Copie du répertoire " +scanDir+ " vers BoxCryptor");
					}
					else{
						LOGGER.error("[" + index + "] Erreur lors de la copie de ["+scanDir+"] vers BoxCrytor [" +repertoireDestinataire+"]");
						// Et notification de l'erreur
						sendNotificationMessage("Erreur lors de la copie du répertoire " +scanDir+ " vers " + repertoireDestinataire);
					}
				}
			} catch (IOException e) {
				LOGGER.error("[" + index + "] Erreur lors du scan de " + FileSystems.getDefault().getPath(scanDir).toAbsolutePath().toString(), e);
			}
		}
		else{
			LOGGER.error("[" + index + "] Erreur lors du scan de " + FileSystems.getDefault().getPath(scanDir).toAbsolutePath() + ". Ce n'est pas un répertoire");
		}
	}
	
	/**
	 * Envoi d'un email de notification
	 * @param message
	 */
	private void sendNotificationMessage(String message){
		sendNotificationMessage(TypeMessagingEnum.EMAIL, EventsTopicNameEnum.NOTIFIFY_MESSAGE, "Copie vers BoxCryptor", message);
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
			LOGGER.debug("[" + index + "]  > Copie du répertoire "+fichierSource+" vers : " + fichierCible);
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
			LOGGER.debug("[" + index + "]  > Copie "+fichierSource+" vers : " + fichierCible);

			if(!Files.exists(FileSystems.getDefault().getPath(directoryCible))){
				LOGGER.info("[" + index + "] Création du répertoire");
				Files.createDirectories(FileSystems.getDefault().getPath(directoryCible));
			}
			if (!Files.exists( fichierCible)) {
				LOGGER.debug("[" + index + "] Création du fichier");
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

	@Override
	public void notifyUpdateDictionnary() {
		// Rien
	}


}
