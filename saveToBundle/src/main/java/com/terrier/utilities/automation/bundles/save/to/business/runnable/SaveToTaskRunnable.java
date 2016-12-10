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
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.enums.messaging.MessageTypeEnum;
import com.terrier.utilities.automation.bundles.communs.enums.statut.StatutPropertyBundleEnum;
import com.terrier.utilities.automation.bundles.communs.model.StatutPropertyBundleObject;
import com.terrier.utilities.automation.bundles.communs.utils.AutomationUtils;
import com.terrier.utilities.automation.bundles.communs.utils.files.visitors.CopyDirVisitor;
import com.terrier.utilities.automation.bundles.save.to.business.SaveToBusinessService;
import com.terrier.utilities.automation.bundles.save.to.business.enums.CommandeEnum;

/**
 * Traitement d'une copie ou d'un déplacement
 * @author vzwingma
 *
 */
public class SaveToTaskRunnable implements Runnable {



	private static final Logger LOGGER = LoggerFactory.getLogger( SaveToTaskRunnable.class );

	// Paramètres
	private int index;
	private CommandeEnum commande;
	private String repertoireSource; 
	private String patternEntree; 
	private String repertoireDestinataire; 
	private String patternSortie;

	private Calendar dateDernierScan = null;

	private boolean dernierResultat = true;
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	// Service
	private SaveToBusinessService service;
	
	/**
	 * @param repertoireSource répertoire source
	 * @param patternEntree pattern d'entrée (si null : copie du répertoire)
	 * @param repertoireDestinataire répertoire destinataire (X: de boxcryptor)
	 * @param patternSortie pattern de sortie (si modification)
	 */
	public SaveToTaskRunnable(final int index, final CommandeEnum commande, final String repertoireSource, final String patternEntree, final String repertoireDestinataire, final String patternSortie, final SaveToBusinessService service){
		this.index = index;
		this.commande = commande;
		this.repertoireSource = repertoireSource;
		this.patternEntree = patternEntree;
		this.repertoireDestinataire = repertoireDestinataire;
		this.patternSortie = patternSortie;
		this.service = service;
	}

	/**
	 * Traitement d'un répertoire
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		String scanDir = repertoireSource;

		// Si la date du dernier scan est null
		if(this.dateDernierScan == null){
			this.dateDernierScan = getDateInitScan();
		}

		LOGGER.info("[{}] Scan du répertoire  : {}. Date de dernier scan : {}", index, scanDir, this.dateDernierScan != null ? sdf.format(this.dateDernierScan.getTime()) : "jamais");
		if(Files.isDirectory(FileSystems.getDefault().getPath(scanDir))){

			String regExMatch = patternEntree;
			LOGGER.debug("[{}] > Matcher : {}", index, regExMatch);
			if(regExMatch != null && !regExMatch.isEmpty()){

				this.dernierResultat = traitementFichiersSaveTo(scanDir, regExMatch, dateDernierScan);
			}
			else{
				// Save To d'un répertoire
				this.dernierResultat = traitementRepertoireSaveTo(scanDir, this.dateDernierScan);
			}

		}
		else{
			LOGGER.error("[{}] Erreur lors du scan de {}. Ce n'est pas un répertoire", index, FileSystems.getDefault().getPath(scanDir).toAbsolutePath());
			this.dernierResultat = false;
		}
		// Mise à jour
		this.dateDernierScan = Calendar.getInstance();
	}


	/**
	 * Si le bundle démarre, la date de dernier scan est la date de modif du répertoire
	 */
	protected Calendar getDateInitScan(){
		// Vérification du répertoire de destination :
		Path pathRepertoireDest = FileSystems.getDefault().getPath(this.repertoireDestinataire);
		if(Files.exists(pathRepertoireDest)){
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(Files.getLastModifiedTime(pathRepertoireDest).toMillis());
				LOGGER.debug("[{}] Date de {} : {}", this.repertoireDestinataire, cal.getTime());
				return cal;
			} catch (IOException e) {
				LOGGER.error("[{}] Erreur lors de la recherche de la date du dernier scan initial", index);
			}
		}
		else{
			LOGGER.warn("[{}] Le répertoire de destination n'existe pas", index, this.repertoireSource);	
		}
		return null;
	}

	/**
	 * Traitement d'un répertoire. Copie ou move de fichiers
	 * @param scanDir répertoire à scanner
	 * @param regExMatch regex des fichiers
	 * @param dateDernierScan date du dernier scann
	 */
	private boolean traitementFichiersSaveTo(String scanDir, String regExMatch, Calendar dateDernierScan){

		boolean resultatGlobal = true;
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
								AutomationUtils.replacePatterns(fichier.getFileName().toString(), outputPattern), 
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
						resultatGlobal &= resultat;
					}
					else{
						LOGGER.debug("[{}] Le fichier {} n'a pas été modifié", index, fichier.getFileName().toString());
					}
				}
			}
		} catch (IOException e) {
			LOGGER.error("[{}] Erreur lors du scan de {}", index, FileSystems.getDefault().getPath(scanDir).toAbsolutePath().toString(), e);
			resultatGlobal = false;
		}
		return resultatGlobal;
	}

	/**
	 * Copie d'un répertoire complet, s'il a changé
	 * @param scanDir scan dir
	 * @param dateDernierScan date du dernier scan
	 */
	private boolean traitementRepertoireSaveTo(String scanDir, Calendar dateDernierScan){
		LOGGER.debug("[{}] Copie du répertoire complet", index);
		int nbFichiersCopies = copyDirTo(FileSystems.getDefault().getPath(scanDir), repertoireDestinataire).get();
		boolean resultat = true;
		if(nbFichiersCopies > 0){
			LOGGER.info("[{}] Copie réalisée vers BoxCrytor : {} fichiers copiés", index, nbFichiersCopies);
			sendNotificationMessage("Copie du répertoire ", scanDir, " vers BoxCryptor : ", ""+nbFichiersCopies, " fichiers copiés");
		}
		else if(nbFichiersCopies < 0){
			LOGGER.error("[{}] Erreur lors de la copie de [{}] vers BoxCrytor [{}]", index, scanDir, repertoireDestinataire);
			// Et notification de l'erreur
			sendNotificationMessage("Erreur lors de la copie du répertoire ", scanDir, " vers ", repertoireDestinataire);
			resultat = false;
		}
		else{
			LOGGER.debug("[{}] Aucun fichier copié", index);
		}
		return resultat;
	}


	/**
	 * Envoi d'un email de notification
	 * @param message
	 */
	protected void sendNotificationMessage(String... message){
		if(message != null && service != null){
			StringBuilder msg = new StringBuilder();
			for (String part : message) {
				msg.append(part);
			}
			LOGGER.debug("Envoi du message Copie vers BoxCryptor");
			service.sendNotificationMessage(MessageTypeEnum.EMAIL, "Copie vers BoxCryptor", msg.toString());
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


	/**
	 * @return date du dernier scann
	 */
	protected Calendar getDateDernierScan(){
		return this.dateDernierScan;
	}

	

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.business.AbstractAutomationService#updateSupervisionEvents(java.util.List)
	 */
	public void updateSupervisionEvents(List<StatutPropertyBundleObject> supervisionEvents) {
		
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");
		
		supervisionEvents.add(
				new StatutPropertyBundleObject(
						"Traitement n°"+this.index + " au " + (this.dateDernierScan != null ? sdf.format(this.dateDernierScan.getTime()) : "N/A"), 
						this.dernierResultat,
						this.dernierResultat ? StatutPropertyBundleEnum.OK : StatutPropertyBundleEnum.WARNING ));

	}
}
