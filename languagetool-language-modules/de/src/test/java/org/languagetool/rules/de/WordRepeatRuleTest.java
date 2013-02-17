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
package org.languagetool.rules.de;

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.WordRepeatRule;

import java.io.IOException;

public class WordRepeatRuleTest extends TestCase {

  public void testRuleGerman() throws IOException {
    final German german = new German();
    final WordRepeatRule rule = new GermanWordRepeatRule(TestTools.getEnglishMessages(), german);
    RuleMatch[] matches;
    final JLanguageTool langTool = new JLanguageTool(german);
    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Das sind die Sätze, die die testen sollen."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Sätze, die die testen."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Das Haus, auf das das Mädchen zeigt."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Warum fragen Sie sie nicht selbst?"));
    assertEquals(0, matches.length);
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("Die die Sätze zum testen."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Und die die Sätze zum testen."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Auf der der Fensterbank steht eine Blume."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Das Buch, in in dem es steht."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Das Haus, auf auf das Mädchen zurennen."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("Sie sie gehen nach Hause."));
    assertEquals(1, matches.length);
  }
  
}
