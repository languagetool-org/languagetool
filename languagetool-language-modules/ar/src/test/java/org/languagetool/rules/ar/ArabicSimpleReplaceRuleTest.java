/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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

package org.languagetool.rules.ar;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.Arabic;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class ArabicSimpleReplaceRuleTest {

  private ArabicSimpleReplaceRule rule;
  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("ar"));

  @Before
  public void setUp() throws Exception {
    Language arabic = new Arabic();
    rule = new ArabicSimpleReplaceRule(TestTools.getMessages("ar"));
  }

  @Test
  public void testRule() throws IOException {
    
    assertEquals(0, rule.match(lt.getAnalyzedSentence("عبد الله")).length);

    // incorrect sentences:
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("عبدالله"));
    assertEquals(1, matches.length);
    assertEquals("عبد الله", matches[0].getSuggestedReplacements().get(0));

    assertEquals(1, rule.match(lt.getAnalyzedSentence("يافطة")).length);

    assertEquals(1, rule.match(lt.getAnalyzedSentence("المائة")).length);

    assertEquals(1, rule.match(lt.getAnalyzedSentence("الذى")).length);

  }
}
