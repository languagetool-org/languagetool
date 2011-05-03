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
package de.danielnaber.languagetool.rules.patterns;

import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import junit.framework.TestCase;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * @author Daniel Naber
 */
public class FalseFriendRuleTest extends TestCase {

  public void testHintsForGermanSpeakers() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH, Language.GERMAN);
    langTool.activateDefaultFalseFriendRules();
    final List<RuleMatch> matches = assertErrors(1, "We will berate you.", langTool);
    assertEquals(matches.get(0).getSuggestedReplacements().toString(), "[to provide advice, to give advice]");
    assertErrors(0, "We will give you advice.", langTool);
    assertErrors(1, "I go to high school in Foocity.", langTool);
    final List<RuleMatch> matches2 = assertErrors(1, "The chef", langTool);
    assertEquals("[boss, chief]", matches2.get(0).getSuggestedReplacements().toString());
  }

  public void testHintsForEnglishSpeakers() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN, Language.ENGLISH);
    langTool.activateDefaultFalseFriendRules();
    assertErrors(1, "Man sollte ihn nicht so beraten.", langTool);
    assertErrors(0, "Man sollte ihn nicht so beschimpfen.", langTool);
    assertErrors(1, "Ich gehe in Blubbstadt zur Hochschule.", langTool);
  }

  public void testHintsForPolishSpeakers() throws IOException, ParserConfigurationException, SAXException {
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH, Language.POLISH);
    langTool.activateDefaultFalseFriendRules();
    assertErrors(1, "This is an absurd.", langTool);
    assertErrors(0, "This is absurdity.", langTool);
    assertSuggestions(0, "This is absurdity.", langTool);
    assertErrors(1, "I have to speak to my advocate.", langTool);
    assertSuggestions(3, "My brother is politic.", langTool);
  }
  
  private List<RuleMatch> assertErrors(int errorCount, String s, JLanguageTool langTool) throws IOException {
    List<RuleMatch> matches = langTool.check(s);
    //System.err.println(matches);
    assertEquals(errorCount, matches.size());
    return matches;
  }
  
  private void assertSuggestions(final int suggestionCount, final String s, final JLanguageTool langTool) throws IOException {
    final List<RuleMatch> matches = langTool.check(s);
    int suggFound = 0;
    for (final RuleMatch match : matches) {
      int pos = 0;
      while (pos != -1) {
        pos = match.getMessage().indexOf("<suggestion>", pos + 1);
        suggFound ++;
      }       
    }
    if (suggFound > 0) {
      suggFound--;
    }
    assertEquals(suggestionCount, suggFound);
  }
  
}
