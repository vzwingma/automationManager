/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilitaires de l'automation manager
 * @author vzwingma
 *
 */
public class AutomationUtils {

	
	public static final String PATTERN_IN = "\\{";
	public static final String PATTERN_OUT = "\\}";
	
	public static String replacePatterns(String source){
		if(source == null){
			return null;
		}
		
		Pattern p = Pattern.compile(".*"+PATTERN_IN+"{2} *(.*) *"+PATTERN_OUT+"{2}.*");
        Matcher m = p.matcher(source);
        if(m.find()){
        	String patternDate = m.group(1);
        	SimpleDateFormat sdf = new SimpleDateFormat(patternDate);
        	String date = sdf.format(Calendar.getInstance().getTime());
        	source = source.replaceAll(PATTERN_IN + PATTERN_IN + patternDate + PATTERN_OUT + PATTERN_OUT, date);
        }
		return source;
	}
}
