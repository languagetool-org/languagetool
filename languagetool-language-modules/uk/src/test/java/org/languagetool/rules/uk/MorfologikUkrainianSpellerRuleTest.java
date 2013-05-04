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
package org.languagetool.rules.uk;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class MorfologikUkrainianSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    final MorfologikUkrainianSpellerRule rule = new MorfologikUkrainianSpellerRule (TestTools.getMessages("Ukrainian"), new Ukrainian());

    final JLanguageTool langTool = new JLanguageTool(new Ukrainian());

    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("До вас прийде заввідділу!")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("До нас приїде The Beatles!")).length);

    //incorrect sentences:

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("атакуючий"));
    // check match positions:
    assertEquals(1, matches.length);

    matches = rule.match(langTool.getAnalyzedSentence("шкляний"));

    assertEquals(1, matches.length);
    assertEquals("скляний", matches[0].getSuggestedReplacements().get(0));

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("а")).length);
    
    // mix alphabets
    matches = rule.match(langTool.getAnalyzedSentence("прийдешнiй"));   // latin 'i'

    assertEquals(1, matches.length);
    assertEquals("прийдешній", matches[0].getSuggestedReplacements().get(0));

  }

}
