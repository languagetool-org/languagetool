/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package de.danielnaber.languagetool;

import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import junit.framework.TestCase;

public class ValidateXMLTest extends TestCase {

  public void testPatternFile() throws IOException {
    testPatternFile(null, false);
  }

  public void testPatternFile(Set<Language> ignoredLanguages, boolean verbose) throws IOException {
    final XMLValidator validator = new XMLValidator();
    for (int i = 0; i < Language.LANGUAGES.length; i++) {
      final Language lang = Language.LANGUAGES[i];
      if (ignoredLanguages != null && ignoredLanguages.contains(lang)) {
        continue;
      }
      if (verbose) {
      	System.out.println("Running tests for " + lang.getName() + "...");
      }
      final String grammarFile = JLanguageTool.getDataBroker().getRulesDir() + "/" + lang.getShortName() + "/grammar.xml";
      validator.validate(grammarFile, JLanguageTool.getDataBroker().getRulesDir() + "/rules.xsd");
    }
  }
  
  public void testFalseFriendsXML() throws IOException {
    final XMLValidator validator = new XMLValidator();
    validator.validate(JLanguageTool.getDataBroker().getRulesDir() + "/false-friends.xml", 
    		JLanguageTool.getDataBroker().getRulesDir() + "/false-friends.dtd", "rules");
  }

  public void testDisambiguationRuleFile() throws IOException {
    final XMLValidator validator = new XMLValidator();
    int disambiguationChecks = 0;
    for (Language language : Language.LANGUAGES) {
      
      final String disambiguationFile = JLanguageTool.getDataBroker().getResourceDir() + "/" + language.getShortName() + "/disambiguation.xml";
      final InputStream stream = this.getClass().getResourceAsStream(disambiguationFile);
      try {
        if (stream != null) {
          validator.validate(disambiguationFile, JLanguageTool.getDataBroker().getResourceDir() + "/disambiguation.xsd");
          disambiguationChecks++;
        }
      } finally {
        if (stream != null) { stream.close(); }
      }
    }
    assertTrue(disambiguationChecks > 0);
  }

  /**
   * Validate XML files, as a help for people developing rules that are not programmers.
   */
  public static void main(final String[] args) throws IOException {
    final ValidateXMLTest prt = new ValidateXMLTest();
    System.out.println("Validating XML grammar files ...");
    if (args.length == 0) {
      prt.testPatternFile(null, true);
    } else {
      final Set<Language> ignoredLanguages = TestTools.getLanguagesExcept(args);
      prt.testPatternFile(ignoredLanguages, true);
    }
    prt.testFalseFriendsXML();
    prt.testDisambiguationRuleFile();
    System.out.println("Validation tests successful.");
  }
}
