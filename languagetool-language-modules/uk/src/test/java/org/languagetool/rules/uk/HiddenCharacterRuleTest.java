/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Andriy Rysin
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
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class HiddenCharacterRuleTest {

  @Test
  public void testRule() throws IOException {
    HiddenCharacterRule rule = new HiddenCharacterRule(TestTools.getMessages("uk"));
    JLanguageTool lt = new JLanguageTool(new Ukrainian());

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("сміття")).length);

    //incorrect sentences:

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("смі\u00ADття"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("сміття"), matches[0].getSuggestedReplacements());
  }

}
