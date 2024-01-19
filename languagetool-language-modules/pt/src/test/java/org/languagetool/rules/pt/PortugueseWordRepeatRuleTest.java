/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.pt;

import org.junit.Test;
import org.languagetool.*;

import java.io.IOException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PortugueseWordRepeatRuleTest {

  @Test
  public void testIgnore() throws IOException {
    Language lang = Languages.getLanguageForShortCode("pt");
    JLanguageTool lt = new JLanguageTool(lang);
    PortugueseWordRepeatRule rule = new PortugueseWordRepeatRule(TestTools.getEnglishMessages(), lang);
    assertFalse(ignore("no repetition", lt, rule, 2));
    assertTrue(ignore("blá blá", lt, rule, 2));
    assertTrue(ignore("Aaptos aaptos", lt, rule, 2));
    assertTrue(ignore("Logo logo vamos ao mercado", lt, rule, 2));
    assertTrue(ignore("Coloquem-na na sala.", lt, rule, 4)); // the hyphen is a token
  }

  private boolean ignore(String input, JLanguageTool lt, PortugueseWordRepeatRule rule, int position) throws IOException {
    AnalyzedSentence sentence = lt.getAnalyzedSentence(input);
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    return rule.ignore(tokens, position);
  }
}
