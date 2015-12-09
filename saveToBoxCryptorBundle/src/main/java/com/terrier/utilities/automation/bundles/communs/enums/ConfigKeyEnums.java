/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.enums;

/**
 * @author vzwingma
 *
 */
public enum ConfigKeyEnums {

	PERIOD_SCAN("automation.bundle.boxcryptor.save.scan.period"),
	// Répertoire download
	DOWNLOAD("automation.bundle.boxcryptor.save.repertoire.download"),
	// Répertoire BoxCryptor
	BC_DIR("automation.bundle.boxcryptor.save.repertoire.boxcryptor"),
	// Nombre de type de fichier
	FILES_NUMBER("automation.bundle.boxcryptor.save.nombre.pattern"),
	// Pattern fichier en entrée
	FILES_PATTERN_IN("automation.bundle.boxcryptor.save.pattern.file.in"),
	// Pattern fichier en sortie
	FILES_PATTERN_OUT("automation.bundle.boxcryptor.save.pattern.file.out"),
	// Sous répertoire BC
	FILES_DIRECTORY_OUT("automation.bundle.boxcryptor.save.pattern.directory.out"),
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
