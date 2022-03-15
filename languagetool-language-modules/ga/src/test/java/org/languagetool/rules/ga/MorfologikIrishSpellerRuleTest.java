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
package org.languagetool.rules.ga;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Irish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

public class MorfologikIrishSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    MorfologikIrishSpellerRule rule =
      new MorfologikIrishSpellerRule (TestTools.getMessages("ga"), new Irish(), null, Collections.emptyList());

    JLanguageTool lt = new JLanguageTool(new Irish());

    //incorrect sentences:
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("botun"));
    // check match positions:
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(0, matches[0].getFromPos());
    Assertions.assertEquals(5, matches[0].getToPos());
    Assertions.assertEquals("botún", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("amaċ"));
    // check match positions:
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(0, matches[0].getFromPos());
    Assertions.assertEquals(4, matches[0].getToPos());
    Assertions.assertEquals("amach", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("😂 botun"));
    // check match positions:
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(3, matches[0].getFromPos());
    Assertions.assertEquals(8, matches[0].getToPos());
    Assertions.assertEquals("botún", matches[0].getSuggestedReplacements().get(0));
  }

}
