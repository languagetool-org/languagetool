/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 LanguageTool contributors
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
package org.languagetool.rules.az;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Azerbaijani;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class AzerbaijaniSimpleReplaceRuleTest {

  private AzerbaijaniSimpleReplaceRule rule;
  private JLanguageTool lt;

  @Before
  public void setUp() throws IOException {
    rule = new AzerbaijaniSimpleReplaceRule(TestTools.getMessages("az"));
    lt = new JLanguageTool(new Azerbaijani());
  }

  @Test
  public void testRule() throws IOException {
    // ASCII spelling without diacritics should produce a match.
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("azerbaycan"));
    assertEquals(1, matches.length);
    assertEquals("azərbaycan", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("sehv"));
    assertEquals(1, matches.length);
    assertEquals("səhv", matches[0].getSuggestedReplacements().get(0));

    // Correctly spelled form should not match.
    matches = rule.match(lt.getAnalyzedSentence("azərbaycan"));
    assertEquals(0, matches.length);
  }

}
