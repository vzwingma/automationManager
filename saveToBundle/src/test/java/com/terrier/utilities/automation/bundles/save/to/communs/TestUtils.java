package com.terrier.utilities.automation.bundles.save.to.communs;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

import com.terrier.utilities.automation.bundles.communs.utils.AutomationUtils;

/**
 * Test utilitaires
 * @author vzwingma
 *
 */
public class TestUtils {

	
	/**
	 * Remplacement du pattern date
	 */
	@Test
	public void testReplace(){
		
		Calendar c = Calendar.getInstance();
		String cl = "_HUBIC_" +c.get(Calendar.YEAR) + (c.get(Calendar.MONTH)+1)+ ".pdf";
		
		assertEquals(cl, AutomationUtils.replacePatterns("_HUBIC_{{yyyyMM}}.pdf"));
	}
}
