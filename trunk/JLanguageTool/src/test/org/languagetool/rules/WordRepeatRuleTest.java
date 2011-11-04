/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules;

import java.io.IOException;

import junit.framework.TestCase;
import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.TestTools;
import de.danielnaber.languagetool.rules.de.GermanWordRepeatRule;

/**
 * 
 * @author Daniel Naber
 */
public class WordRepeatRuleTest extends TestCase {

  public void testRule() throws IOException {
    WordRepeatRule rule = new WordRepeatRule(TestTools.getEnglishMessages(), Language.ENGLISH);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.ENGLISH);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("This is a test sentence."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is a test sentence..."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("This this is a test sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is a test sentence sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is is a a test sentence sentence."));
    assertEquals(3, matches.length);
  }

  public void testRuleGerman() throws IOException {
    WordRepeatRule rule = new GermanWordRepeatRule(TestTools.getEnglishMessages(), Language.GERMAN);
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.GERMAN);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Das sind die Sätze, die die testen sollen."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Sätze, die die testen."));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Die die Sätze zum testen."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Und die die Sätze zum testen."));
    assertEquals(1, matches.length);
  }
  
  public void testRulePolish() throws IOException {
	    WordRepeatRule rule = new WordRepeatRule(TestTools.getEnglishMessages(), Language.POLISH);
	    RuleMatch[] matches;
	    JLanguageTool langTool = new JLanguageTool(Language.POLISH);
	    // correct sentences:
	    matches = rule.match(langTool.getAnalyzedSentence("To jest zdanie."));
	    assertEquals(0, matches.length);
	    // incorrect sentences:
	    matches = rule.match(langTool.getAnalyzedSentence("To jest jest zdanie."));
	    assertEquals(1, matches.length);
	  }
  
}
