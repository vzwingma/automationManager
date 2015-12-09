/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.exceptions;

/**
 * @author vzwingma
 *
 */
public class KeyNotFoundException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7695001263665284536L;
	
	public KeyNotFoundException(String key){
		super("La clé [" + key + "] n'a pas été trouvée dans le fichier de configuration");
	}

}
