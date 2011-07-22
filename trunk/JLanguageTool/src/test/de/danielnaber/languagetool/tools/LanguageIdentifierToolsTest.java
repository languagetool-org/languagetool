package de.danielnaber.languagetool.tools;

import org.apache.tika.language.LanguageIdentifier;

import junit.framework.TestCase;

public class LanguageIdentifierToolsTest extends TestCase {

	public void testAddProfile() throws Exception {
		LanguageIdentifier.initProfiles();
		int numLanguagesBefore = LanguageIdentifier.getSupportedLanguages().size();
		System.out.println(Integer.toString(numLanguagesBefore) + " languages before");
		LanguageIdentifierTools.addLtProfiles();
		int numLanguagesAfter = LanguageIdentifier.getSupportedLanguages().size();
		assertNotSame(numLanguagesAfter, numLanguagesBefore);		
	}
	
}