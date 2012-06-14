/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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

import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;

import org.languagetool.rules.spelling.hunspell.*;

public class HunspellRuleTest {

  @Test
  public void testRule() throws UnsatisfiedLinkError, UnsupportedOperationException, IOException {

    HunspellRule rule =
            new HunspellRule(TestTools.getMessages("Polish"), Language.POLISH);

    RuleMatch[] matches;
    JLanguageTool langTool = new JLanguageTool(Language.POLISH);


    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("To jest test bez jakiegokolwiek błędu.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Żółw na starość wydziela dziwną woń.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);

    //incorrect sentences:

    matches = rule.match(langTool.getAnalyzedSentence("Zolw"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(4, matches[0].getToPos());
    assertEquals("Żółw", matches[0].getSuggestedReplacements().get(0));

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);

    // Catalan
    HunspellRule catRule =
    		new HunspellRule(TestTools.getMessages("Catalan"), Language.CATALAN);
    JLanguageTool catTool = new JLanguageTool(Language.CATALAN);

    // correct sentences:
    assertEquals(0, catRule.match(catTool.getAnalyzedSentence("Allò que més l'interessa.")).length);
    // checks that "WORDCHARS ·-'" is added to Hunspell .aff file
    assertEquals(0, catRule.match(catTool.getAnalyzedSentence("Porta'n quatre al col·legi.")).length);
    assertEquals(0, catRule.match(catTool.getAnalyzedSentence("Has de portar-me'n moltes.")).length);
    assertEquals(0, catRule.match(catTool.getAnalyzedSentence(",")).length);

    //incorrect sentences:
    matches = catRule.match(catTool.getAnalyzedSentence("Pecra"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(5, matches[0].getToPos());
    assertEquals("Pera", matches[0].getSuggestedReplacements().get(0));
  }

}
