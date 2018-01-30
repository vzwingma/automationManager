package com.terrier.utilities.automation.bundles.communs.utils.replace;

/**
 * Debut et fin du pattern Replace
 * @author PVZN02821
 *
 */
public enum ReplacePatternBlockEnum {
	
	IN("\\{"), OUT("\\}");
	
	private String value = "";
	
	private ReplacePatternBlockEnum(String value){
		this.value = value;
	}

	/**
	 * @return the value
	 */
	public final String getValue() {
		return value;
	}
	
}
