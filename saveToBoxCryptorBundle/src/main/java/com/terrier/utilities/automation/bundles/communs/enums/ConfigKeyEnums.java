/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.enums;

/**
 * @author vzwingma
 *
 */
public enum ConfigKeyEnums {

	
	DOWNLOAD("automation.bundle.boxcryptor.save.repertoire.download");
	
	// Code de la clé dans le fichier de configuration
	private String codeKey;
	
	
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
