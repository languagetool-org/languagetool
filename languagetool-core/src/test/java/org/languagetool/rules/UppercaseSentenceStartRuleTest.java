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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class UppercaseSentenceStartRuleTest {

  @Test
  public void testRule() throws IOException {
    UppercaseSentenceStartRule rule = new UppercaseSentenceStartRule(TestTools.getEnglishMessages(), TestTools.getDemoLanguage());
    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(TestTools.getDemoLanguage());

    // correct sentences:
    matches = rule.match(langTool.getAnalyzedSentence("a) This is a test sentence."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("iv. This is a test sentence..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("\"iv. This is a test sentence...\""));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("»iv. This is a test sentence..."));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("This is a test sentence"));
    assertEquals(0, matches.length);
    //the previous sentence does not end in [.?!…]
    matches = rule.match(langTool.getAnalyzedSentence("this is a sentence"));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence(""));
    assertEquals(0, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("http://www.languagetool.org"));
    assertEquals(0, matches.length);
    
    // incorrect sentences:
    matches = rule.match(langTool.getAnalyzedSentence("this is a test sentence."));
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(4, matches[0].getToPos());
    matches = rule.match(langTool.getAnalyzedSentence("this"));
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(4, matches[0].getToPos());
    matches = rule.match(langTool.getAnalyzedSentence("'this is a sentence'."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("\"this is a sentence.\""));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("„this is a sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("«this is a sentence."));
    assertEquals(1, matches.length);
    matches = rule.match(langTool.getAnalyzedSentence("‘this is a sentence."));
    assertEquals(1, matches.length);
  }
}
