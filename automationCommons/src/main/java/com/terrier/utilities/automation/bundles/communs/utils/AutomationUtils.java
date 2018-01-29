/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.utils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terrier.utilities.automation.bundles.communs.utils.replace.IReplacePattern;
import com.terrier.utilities.automation.bundles.communs.utils.replace.ReplaceDatePattern;

/**
 * Utilitaires de l'automation manager
 * @author vzwingma
 *
 */
public class AutomationUtils {

	// Constructeur priv� pour utilitaire
	private AutomationUtils(){}
	
	/**
	 * Liste des patterns de remplacement
	 */
	private static final List<IReplacePattern> listePatterns = Arrays.asList(new ReplaceDatePattern(), new ReplaceDatePattern());

	private static final Logger LOGGER = LoggerFactory.getLogger( AutomationUtils.class );
	
	
	/**
	 * @param source donnée source à remplacer
	 * Le pattern est de la forme {{yyyyMMdd}} avec à l'intérieur un format de date {@link SimpleDateFormat}
	 * @return données remplacée par le pattern
	 */
	public static String replacePatterns(String source, String pattern){
		if(source == null){
			return null;
		}
		
		for (IReplacePattern iReplacePattern : listePatterns) {
			LOGGER.info("Application du pattern {} : {}", iReplacePattern.getClass().getSimpleName(), iReplacePattern);
			source = iReplacePattern.replace(source, pattern);
		}
		return source;
	}
	
	
}
