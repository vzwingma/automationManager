/**
 * 
 */
package com.terrier.utilities.automation.bundles.communs.utils.replace;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author vzwingma
 *
 */
public class ReplaceDatePattern implements IReplacePattern {


	private static final Logger LOGGER = LoggerFactory.getLogger( ReplaceDatePattern.class );

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.utils.replace.IReplacePattern#replace(java.lang.String)
	 */
	@Override
	public String replace(String chaineSource, String pattern) {

		Pattern p = Pattern.compile(".*"+PATTERN_IN+"{2} *(.*) *"+PATTERN_OUT+"{2}.*");
		Matcher m = p.matcher(pattern);
		if(m.find()){
			String patternDate = m.group(1);
			try{
				SimpleDateFormat sdf = new SimpleDateFormat(patternDate);

				String date = sdf.format(Calendar.getInstance().getTime());
				chaineSource = pattern.replaceAll(PATTERN_IN + PATTERN_IN + patternDate + PATTERN_OUT + PATTERN_OUT, date);
			}
			catch(IllegalArgumentException e){
				LOGGER.warn("Le pattern {} n'est pas un format de date", m.group(1));
			}
		}
		return chaineSource;
	}

	/* (non-Javadoc)
	 * @see com.terrier.utilities.automation.bundles.communs.utils.replace.IReplacePattern#description()
	 */
	@Override
	public String description() {
		return "Le pattern est de la forme {{yyyyMMdd}} avec à l'intérieur un format de date {@link SimpleDateFormat}";
	}

}
