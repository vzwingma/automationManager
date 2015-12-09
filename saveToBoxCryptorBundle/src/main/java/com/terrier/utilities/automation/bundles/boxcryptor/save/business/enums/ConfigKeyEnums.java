/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.save.business.enums;

/**
 * @author vzwingma
 *
 */
public enum ConfigKeyEnums {

	PERIOD_SCAN("automation.bundle.boxcryptor.save.scan.period"),
	// Nombre de type de fichier
	FILES_NUMBER("automation.bundle.boxcryptor.save.nombre.pattern"),
	// Répertoire download
	FILES_DIRECTORY_IN("automation.bundle.boxcryptor.save.in.repertoire"),
	// Pattern fichier en entrée
	FILES_PATTERN_IN("automation.bundle.boxcryptor.save.in.pattern.file"),
	// Pattern fichier en sortie
	FILES_PATTERN_OUT("automation.bundle.boxcryptor.save.out.pattern.file"),
	// Sous répertoire BC
	FILES_DIRECTORY_OUT("automation.bundle.boxcryptor.save.out.repertoire"),
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
