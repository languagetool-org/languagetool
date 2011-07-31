package de.danielnaber.languagetool.tools;

import org.apache.tika.language.LanguageIdentifier;

import junit.framework.TestCase;

public class LanguageIdentifierToolsTest extends TestCase {

  public void testAddProfile() throws Exception {
    LanguageIdentifier.initProfiles();
    final int numLanguagesBefore = LanguageIdentifier.getSupportedLanguages().size();
    assertEquals(18, numLanguagesBefore);
    LanguageIdentifierTools.addLtProfiles();
    final int numLanguagesAfter = LanguageIdentifier.getSupportedLanguages().size();
    assertNotSame(numLanguagesAfter, numLanguagesBefore);
  }

}