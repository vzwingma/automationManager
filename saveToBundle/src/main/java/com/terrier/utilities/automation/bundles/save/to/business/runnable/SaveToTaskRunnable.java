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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService;
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



	private static final Logger LOGGER = LoggerFactory.getLogger( SaveToTaskRunnable.class );

	// Paramètres
	private int index;
	private CommandeEnum commande;
	private String repertoireSource; 
	private String patternEntree; 
	private String repertoireDestinataire; 
	private String patternSortie;

	private Calendar dateDernierScan = null;

	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

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
		LOGGER.info("[{}] Scan du répertoire  : {}. Date de dernier scan : {}", index, scanDir, this.dateDernierScan != null ? sdf.format(this.dateDernierScan.getTime()) : "jamais");
		if(Files.isDirectory(FileSystems.getDefault().getPath(scanDir))){

			String regExMatch = patternEntree;
			LOGGER.debug("[{}] > Matcher : {}", index, regExMatch);
			if(regExMatch != null && !regExMatch.isEmpty()){

				traitementFichiersSaveTo(scanDir, regExMatch, dateDernierScan);
			}
			else{
				// Save To d'un répertoire
				traitementRepertoireSaveTo(scanDir, this.dateDernierScan);
			}

		}
		else{
			LOGGER.error("[{}] Erreur lors du scan de {}. Ce n'est pas un répertoire", index, FileSystems.getDefault().getPath(scanDir).toAbsolutePath());
		}
		// Mise à jour
		this.dateDernierScan = Calendar.getInstance();
	}


	/**
	 * Traitement d'un répertoire. Copie ou move de fichiers
	 * @param scanDir répertoire à scanner
	 * @param regExMatch regex des fichiers
	 * @param dateDernierScan date du dernier scann
	 */
	private void traitementFichiersSaveTo(String scanDir, String regExMatch, Calendar dateDernierScan){

		try{
			DirectoryStream<Path> downloadDirectoryPath = Files.newDirectoryStream(FileSystems.getDefault().getPath(scanDir));
			for (Path fichier : downloadDirectoryPath) {
				LOGGER.debug("[{}] Traitement du fichier : {}", index, fichier.getFileName().toString());
				if(fichier.getFileName().toString().matches(regExMatch)){
					LOGGER.trace("{} > match avec {}", fichier.getFileName().toString(), regExMatch);
					// Vérification vis à vis de la date de modification

					if(dateDernierScan == null || Files.getLastModifiedTime(fichier).toMillis() > dateDernierScan.getTimeInMillis()){
						String outputPattern = patternSortie;
						if(patternSortie == null || patternSortie.isEmpty()){
							outputPattern = fichier.getFileName().toString();
						}
						boolean resultat = copyFichierTo(fichier, 
								AutomationUtils.replaceDatePatterns(outputPattern), 
								repertoireDestinataire);		
						if(resultat){
							LOGGER.info("[{}] Copie réalisée vers {}", index, repertoireDestinataire);
							if(CommandeEnum.MOVE.equals(commande)){
								// Suppression du fichier source
								Files.delete(fichier);
								// Et notification du déplacement
								sendNotificationMessage("Déplacement de ",fichier.getFileName().toString(), " vers ", repertoireDestinataire);
							}
							else{
								sendNotificationMessage("Copie de ",fichier.getFileName().toString(), " vers ", repertoireDestinataire);
							}
						}
						else{
							LOGGER.error("[{}] Erreur lors de la copie vers {}", index, repertoireDestinataire);
							// Et notification de l'erreur
							sendNotificationMessage("Erreur lors de la copie de ",fichier.getFileName().toString(), " vers ", repertoireDestinataire);
						}
					}
					else{
						LOGGER.debug("[{}] Le fichier n'a pas été modifié", index);
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("[{}] Erreur lors du scan de {}", index, FileSystems.getDefault().getPath(scanDir).toAbsolutePath().toString(), e);
		}
	}

	/**
	 * Copie d'un répertoire complet, s'il a changé
	 * @param scanDir scan dir
	 * @param dateDernierScan date du dernier scan
	 */
	private void traitementRepertoireSaveTo(String scanDir, Calendar dateDernierScan){
		LOGGER.warn("[{}] Copie du répertoire complet", index);
		int nbFichiersCopies = copyDirTo(FileSystems.getDefault().getPath(scanDir), repertoireDestinataire).get();
		
		if(nbFichiersCopies > 0){
			LOGGER.info("[{}] Copie réalisée vers BoxCrytor : {} fichiers copiés", index, nbFichiersCopies);
			sendNotificationMessage("Copie du répertoire ", scanDir, " vers BoxCryptor : ", ""+nbFichiersCopies, " fichiers copiés");
		}
		else if(nbFichiersCopies < 0){
			LOGGER.error("[{}] Erreur lors de la copie de [{}] vers BoxCrytor [{}]", index, scanDir, repertoireDestinataire);
			// Et notification de l'erreur
			sendNotificationMessage("Erreur lors de la copie du répertoire ", scanDir, " vers ", repertoireDestinataire);
		}
		else{
			LOGGER.info("[{}] Aucun fichier copié", index);
		}
	}


	/**
	 * Envoi d'un email de notification
	 * @param message
	 */
	private void sendNotificationMessage(String... message){
		if(message != null){
			StringBuilder msg = new StringBuilder();
			for (String part : message) {
				msg.append(part);
			}
			LOGGER.debug("Envoi du message Copie vers BoxCryptor");
			sendNotificationMessage(TypeMessagingEnum.EMAIL, "Copie vers BoxCryptor", msg.toString());
		}
	}



	/**
	 * 
	 * @param fichierSource chemin vers le fichier source
	 * @param outFileName pattern de sortie
	 * @param directoryCible répertoire cible
	 */
	protected AtomicInteger copyDirTo(Path fichierSource, String directoryCible){
		
		AtomicInteger nbFichiersCopies = new AtomicInteger(0);
		try {
			Path fichierCible = FileSystems.getDefault().getPath(directoryCible);
			if(fichierCible != null && fichierSource != null){
				LOGGER.debug("[{}]  > Copie du répertoire {} vers : {}", index, fichierSource, fichierCible);
				
				Files.walkFileTree(fichierSource, new CopyDirVisitor(fichierSource, fichierCible, nbFichiersCopies, this.dateDernierScan));
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			nbFichiersCopies = new AtomicInteger(-1);
		}
		return nbFichiersCopies;
	}



	/**
	 * Copie du fichier
	 * @param fichierSource chemin vers le fichier source
	 * @param outFileName pattern de sortie
	 * @param directoryCible répertoire cible
	 */
	protected boolean copyFichierTo(Path fichierSource, String outFileName, String directoryCible){
		try {
			if((outFileName == null || outFileName.isEmpty()) && fichierSource != null){
				outFileName = fichierSource.getFileName().toString();
			}
			Path fichierCible = FileSystems.getDefault().getPath(directoryCible + "/" +outFileName);
			LOGGER.debug("[{}]  > Copie {} vers {}",  index, fichierSource, fichierCible);

			if(!Files.exists(FileSystems.getDefault().getPath(directoryCible))){
				LOGGER.info("[{}] Création du répertoire", index);
				Files.createDirectories(FileSystems.getDefault().getPath(directoryCible));
			}
			if (fichierCible != null && !fichierCible.toString().contains("null") && !Files.exists(fichierCible)) {
				LOGGER.debug("[{}] Création du fichier {}", index, fichierCible);
				Files.createFile(fichierCible);
			}
			CopyOption[] options = new CopyOption[]{
					StandardCopyOption.REPLACE_EXISTING,
					StandardCopyOption.COPY_ATTRIBUTES
			}; 
			if(fichierSource != null && fichierCible != null){
				Files.copy(fichierSource, fichierCible, options);
				return true;
			}
			else{
				return false;
			}
		} catch (IOException e) {
			LOGGER.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void notifyUpdateDictionnary() {
		// Rien
	}


	protected Calendar getDateDernierScan(){
		return this.dateDernierScan;
	}

}
