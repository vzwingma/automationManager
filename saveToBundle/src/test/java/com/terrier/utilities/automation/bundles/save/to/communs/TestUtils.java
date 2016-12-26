package com.terrier.utilities.automation.bundles.save.to.communs;

import static org.junit.Assert.assertEquals;

import java.util.Calendar;

import org.junit.Test;

import com.terrier.utilities.automation.bundles.communs.utils.AutomationUtils;
import com.terrier.utilities.automation.bundles.communs.utils.replace.ReplaceDatePattern;
import com.terrier.utilities.automation.bundles.communs.utils.replace.ReplaceStringPattern;

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
	public void testReplaceDate(){
		
		Calendar c = Calendar.getInstance();
		int m = c.get(Calendar.MONTH)+1;
		String cl = "_HUBIC_" +c.get(Calendar.YEAR) + ( m<10 ? "0"+m : m)+ ".pdf";
		
		assertEquals(cl, new ReplaceDatePattern().replace("_HUBIC_123123RUYUI.pdf", "_HUBIC_{{yyyyMM}}.pdf"));
	}
	
	
	/**
	 * Remplacement du pattern date
	 */
	@Test
	public void testReplaceString(){	
		assertEquals("UBER-BWOWJNKP-03-2016-0000362", new ReplaceStringPattern().replace("invoice-BWOWJNKP-03-2016-0000362", "{{invoice|UBER}}"));
	}
	
	
	/**
	 * Remplacement du pattern date
	 */
	@Test
	public void testReplaceAutomations(){
		
		Calendar c = Calendar.getInstance();
		int m = c.get(Calendar.MONTH)+1;
		String cl = "_HUBIC_" +c.get(Calendar.YEAR) + ( m<10 ? "0"+m : m)+ ".pdf";
		
		assertEquals(cl,AutomationUtils.replacePatterns("_HUBIC_123123RUYUI.pdf", "_HUBIC_{{yyyyMM}}.pdf"));
	}
}
