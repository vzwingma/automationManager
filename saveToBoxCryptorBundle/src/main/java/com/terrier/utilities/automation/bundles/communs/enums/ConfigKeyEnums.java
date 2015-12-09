/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.enums;

/**
 * @author vzwingma
 *
 */
public enum ConfigKeyEnums {

	
	DOWNLOAD("automation.bundle.boxcryptor.save.repertoire.download"),
	BC_DIR("automation.bundle.boxcryptor.save.repertoire.boxcryptor"),
	FILES_NUMBER("automation.bundle.boxcryptor.save.nombre.pattern"),
	FILES_PATTERN_IN("automation.bundle.boxcryptor.save.pattern.file.in"),
	FILES_PATTERN_OUT("automation.bundle.boxcryptor.save.pattern.file.out"),
	;
	
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
