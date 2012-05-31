package org.languagetool;

import static org.junit.Assert.*;

import org.junit.Test;

public class LanguageTest {

	@Test
	public void testgetLanguageForShortName() {
		assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForShortName("en-US"));
		assertEquals(Language.GERMAN, Language.getLanguageForShortName("de"));
	}
	
	@Test
	public void testgetLanguageForName() {
		assertEquals(Language.AMERICAN_ENGLISH, Language.getLanguageForName("American English"));
		assertEquals(Language.GERMAN, Language.getLanguageForName("German"));
	}
	
}
