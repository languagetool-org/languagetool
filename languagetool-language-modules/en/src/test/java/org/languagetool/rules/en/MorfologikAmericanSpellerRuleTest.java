/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski
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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MorfologikAmericanSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    final AmericanEnglish language = new AmericanEnglish();
    final MorfologikAmericanSpellerRule rule =
            new MorfologikAmericanSpellerRule (TestTools.getMessages("English"), language);

    final JLanguageTool langTool = new JLanguageTool(language);

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("This is an example: we get behavior as a dictionary word.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Why don't we speak today.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("An URL like http://sdaasdwe.com is no error.")).length);
    //with doesn't
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("He doesn't know what to do.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);

    //incorrect sentences:

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("behaviour"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(9, matches[0].getToPos());
    assertEquals("behavior", matches[0].getSuggestedReplacements().get(0));

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);
    
    //based on replacement pairs:
    
    matches = rule.match(langTool.getAnalyzedSentence("He teached us."));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(3, matches[0].getFromPos());
    assertEquals(10, matches[0].getToPos());
    assertEquals("taught", matches[0].getSuggestedReplacements().get(0));
    
    // hyphens - accept words if all their parts are okay:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("A web-based software.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("A wxeb-based software.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("A web-baxsed software.")).length);
    // yes, we also accept fantasy words:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("A web-feature-driven-car software.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("A web-feature-drivenx-car software.")).length);
  }

}
