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
		int m = c.get(Calendar.MONTH)+1;
		String cl = "_HUBIC_" +c.get(Calendar.YEAR) + ( m<10 ? "0"+m : m)+ ".pdf";
		
		assertEquals(cl, AutomationUtils.replaceDatePatterns("_HUBIC_{{yyyyMM}}.pdf"));
	}
}
