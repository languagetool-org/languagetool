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
package org.languagetool.rules.en;

import junit.framework.TestCase;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.English;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.WordRepeatRule;

import java.io.IOException;

public class WordRepeatRuleTest extends TestCase {

  public void testRule() throws IOException {
    final English english = new English();
    final WordRepeatRule rule = new WordRepeatRule(TestTools.getEnglishMessages(), english);
    RuleMatch[] matches;
    final JLanguageTool langTool = new JLanguageTool(english);
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

}
