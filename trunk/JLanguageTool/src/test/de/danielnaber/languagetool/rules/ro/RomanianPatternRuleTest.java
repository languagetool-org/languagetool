package de.danielnaber.languagetool.rules.ro;

import junit.framework.TestCase;
import de.danielnaber.languagetool.rules.patterns.PatternRuleTest;

/**
 * 
 * @author Ionuț Păduraru
 * @since 07.05.2009 21:07:12
 * 
 * This testcase is not for actual rules but for PatternRuleTest to ensure proper 
 * xml cleaning: there is a romanian rule where "<<" is used; we want 
 * "X<marker><<</marker>Y" to be tranformed into "X<<Y", not into "XY" 
 * (see rule id GHILIMELE_DUBLE_INTERIOR_INCEPUT in ro/grammar.xml).  
 *  
 */
public class RomanianPatternRuleTest extends TestCase {

	private PatternRuleTestWrapper patternRuleTestWrapper = new PatternRuleTestWrapper();

	/**
	 * wrapper on PatternRuleTestWrapper to expose cleanXML method
	 * 
	 * @author Ionuț Păduraru
	 * @since 07.05.2009 21:11:01
	 */
	private static class PatternRuleTestWrapper extends PatternRuleTest {
		@Override
		public String cleanXML(String str) {
			return super.cleanXML(str);
		}
	}

	public String cleanXML(String str) {
		return patternRuleTestWrapper.cleanXML(str);
	}

	/**
	 * Ensure proper xml cleanining in PatternRuleTest
	 * 
	 * @author Ionuț Păduraru
	 * @since 07.05.2009 21:11:30
	 * @throws Exception
	 */
	public void testCleanXML() throws Exception {
		assertEquals(cleanXML("1<mark>2"), "12");
		assertEquals(cleanXML("1</mark>2"), "12");
		assertEquals(cleanXML("1<</mark>2"), "1<2");
		assertEquals(cleanXML("<</mark>2"), "<2");
		assertEquals(cleanXML("></mark>2"), ">2");
		assertEquals(cleanXML("1<mark>abc</mark>2"), "1abc2");
		assertEquals(cleanXML("1<mark><<</mark>2"), "1<<2");
		assertEquals(cleanXML("1<mark>>></mark>2"), "1>>2");
	}
}
