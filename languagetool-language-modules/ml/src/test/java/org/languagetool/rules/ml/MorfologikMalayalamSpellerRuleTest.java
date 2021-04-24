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
package org.languagetool.rules.ml;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Malayalam;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MorfologikMalayalamSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    final Malayalam language = new Malayalam();
    final MorfologikMalayalamSpellerRule rule =
            new MorfologikMalayalamSpellerRule (TestTools.getMessages("ml"), language, null, Collections.emptyList());

    RuleMatch[] matches;
    JLanguageTool lt = new JLanguageTool(language);

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("എന്തുകൊണ്ട്‌ അംഗത്വം")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("എങ്ങനെ അംഗമാകാം?")).length);
    //test for "LanguageTool":
    assertEquals(0, rule.match(lt.getAnalyzedSentence("LanguageTool")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);

    //incorrect sentences:

    matches = rule.match(lt.getAnalyzedSentence("Aagohw"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(6, matches[0].getToPos());
    assertTrue(matches[0].getSuggestedReplacements().isEmpty());

    matches = rule.match(lt.getAnalyzedSentence("എaങ്ങനെ"));
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(7, matches[0].getToPos());
    assertEquals(matches[0].getSuggestedReplacements().get(0), "എങ്ങനെ");

    assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);
    assertEquals(1, rule.match(lt.getAnalyzedSentence("a")).length);
  }

}
