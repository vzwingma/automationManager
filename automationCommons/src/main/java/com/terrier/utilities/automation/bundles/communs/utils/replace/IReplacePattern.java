/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.utils.replace;

/**
 * Interface de patterns de remplacement
 * @author vzwingma
 *
 */
public interface IReplacePattern {

	
	/**
	 * @param chaineSource
	 * @return chaine transformée par le pattern
	 */
	String replace(String chaineSource, String pattern);
	
	
	/**
	 * @return la description du pattern
	 */
	String toString();
}
