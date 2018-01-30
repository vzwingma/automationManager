/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.utils.replace;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Remplacement d'une chaine de caractère
 * @author vzwingma
 *
 */
public class ReplaceStringPattern implements IReplacePattern {

	
	
	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.utils.replace.IReplacePattern#replace(java.lang.String)
	 */
	@Override
	public String replace(String chaineSource, String pattern) {

		Pattern p = Pattern.compile(ReplacePatternBlockEnum.IN.getValue()+"{2}(.[^|]*)\\|(.[^|]*)"+ReplacePatternBlockEnum.OUT.getValue()+"{2}");
        Matcher m = p.matcher(pattern);
        if(m.find()){
        	String patternARemplacer = m.group(1);
        	String patternRemplacee = m.group(2);
        	chaineSource = chaineSource.replaceAll(patternARemplacer, patternRemplacee);
        }
		return chaineSource;
	}

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.utils.replace.IReplacePattern#description()
	 */
	@Override
	public String toString() {
		return "Le pattern est de la forme {{chaine_a_trouver|chaine_remplacee}} ";
	}

}
