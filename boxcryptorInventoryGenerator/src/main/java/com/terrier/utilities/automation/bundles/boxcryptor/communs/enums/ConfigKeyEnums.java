/**
 * 
 */
package com.terrier.utilities.automation.bundles.boxcryptor.communs.enums;

/**
 * @author vzwingma
 *
 */
public enum ConfigKeyEnums  {

	// Période scannée
	PERIOD_SCAN("automation.bundles.boxcryptor.scan.period"),
	// Répertoire download
	SOURCE_DIRECTORY("automation.bundles.boxcryptor.source.repertoire"),
	// Sous répertoire BC
	CRYPTED_DIRECTORY("automation.bundles.boxcryptor.chiffre.repertoire"),
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
