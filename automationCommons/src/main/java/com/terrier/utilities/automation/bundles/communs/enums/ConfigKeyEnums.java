/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.enums;

/**
 * @author vzwingma
 *
 */
public enum ConfigKeyEnums  {

	/** BOX CRYPTOR **/
	// Période scannée
	BXCPTR_PERIOD_SCAN("automation.bundles.boxcryptor.scan.period"),
	// Répertoire download
	SOURCE_DIRECTORY("automation.bundles.boxcryptor.source.repertoire"),
	// Sous répertoire BC
	CRYPTED_DIRECTORY("automation.bundles.boxcryptor.chiffre.repertoire"),
	
	/** SAVE_TO **/
	// Période scannée
	SAVE_TO_PERIOD_SCAN("automation.bundle.boxcryptor.save.scan.period"),
	// Commande : COPY ou MOVE
	COMMANDE("automation.bundle.boxcryptor.save.commande"),
	// Répertoire download
	FILES_DIRECTORY_IN("automation.bundle.boxcryptor.save.in.repertoire"),
	// Pattern fichier en entrée
	FILES_PATTERN_IN("automation.bundle.boxcryptor.save.in.pattern.file"),
	// Pattern fichier en sortie
	FILES_PATTERN_OUT("automation.bundle.boxcryptor.save.out.pattern.file"),
	// Sous répertoire BC
	FILES_DIRECTORY_OUT("automation.bundle.boxcryptor.save.out.repertoire"),
	
	/** EMAIL_WORKER **/
	// Période scannée
	EMAIL_WORKER_PERIOD("automation.bundle.emails.worker.period"),
	EMAIL_WORKER_RULE("automation.bundle.emails.worker.rule"),
	EMAIL_WORKER_DESTDIR("automation.bundle.emails.worker.destination.directory")
	;
	
	
	// Code de la clé dans le fichier de configuration
	private String codeKey;
	
	
	/**
	 * Valeur de l'enum
	 * @param value
	 */
	private ConfigKeyEnums(String value){
		this.codeKey = value;
	}


	/**
	 * @return the codeKey
	 */
	public String getCodeKey() {
		return codeKey;
	}
}
