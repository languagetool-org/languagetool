/* LanguageTool, a natural language style checker
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

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.RuleMatch;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class ArabicHunspellSpellerRuleTest {

  @Test
  public void testRuleWithArabic() throws Exception {
    ArabicHunspellSpellerRule rule = new ArabicHunspellSpellerRule(TestTools.getMessages("ar"));
    JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortCode("ar"));
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("السلام عليكم.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("والبلاد")).length);
    // ignore URLs:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("تصفح http://foo.org/bar.")).length);

    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("السلام عليييكم."));
    assertThat(matches.length, is(1));
    assertTrue(matches[0].getSuggestedReplacements().contains("عليميكم"));

    matches = rule.match(langTool.getAnalyzedSentence("هذه العباره فيها أغلاط."));
    assertThat(matches.length, is(1));
    assertTrue(matches[0].getSuggestedReplacements().contains("العبارة"));
    assertThat(matches[0].getFromPos(), is(4));
    assertThat(matches[0].getToPos(), is(11));
  }

}
