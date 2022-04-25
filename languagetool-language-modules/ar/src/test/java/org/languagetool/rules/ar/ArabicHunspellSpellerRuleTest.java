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
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("ar"));

    assertEquals(0, rule.match(lt.getAnalyzedSentence("السلام عليكم.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("والبلاد")).length);
    // ignore URLs:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("تصفح http://foo.org/bar.")).length);

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("السلام عليييكم."));
    assertThat(matches.length, is(1));
    //TODO fix flaky test
    //String exp = "عليميكم";
    //assertTrue("Expected '" + exp + "', got: " + matches[0].getSuggestedReplacements(), matches[0].getSuggestedReplacements().contains(exp));

    matches = rule.match(lt.getAnalyzedSentence("هذه العباره فيها أغلاط."));
    assertThat(matches.length, is(1));
    assertTrue(matches[0].getSuggestedReplacements().contains("العبارة"));
    assertThat(matches[0].getFromPos(), is(4));
    assertThat(matches[0].getToPos(), is(11));

    matches = rule.match(lt.getAnalyzedSentence("تظاعف"));
    assertTrue(matches[0].getSuggestedReplacements().contains("تساعف"));
    assertTrue(matches[0].getSuggestedReplacements().contains("تضاعف"));

    matches = rule.match(lt.getAnalyzedSentence("مساءل"));
    assertTrue(matches[0].getSuggestedReplacements().contains("مسائل"));

    /* commented out because multi-lingual feature breaks this:
    matches = rule.match(langTool.getAnalyzedSentence("مصر دوله افريقية اهلها بيتكلموا مصرى"));
    assertThat(matches.length, is(5));
    assertThat(matches[3].getFromPos(), is(23));
    assertThat(matches[3].getToPos(), is(31));
    assertTrue(matches[3].getSuggestedReplacements().contains("يتكلموا"));
    assertThat(matches[4].getFromPos(), is(32));
    assertThat(matches[4].getToPos(), is(36));
    assertTrue(matches[4].getSuggestedReplacements().contains("مصري"));
     */

    matches = rule.match(lt.getAnalyzedSentence("اذا اردت الذهاب الى المكتبه اذهب فى الضهيرة مادام حكامنا يغدقون الاموال على الارجل بدل الرءوس، فلن نتقّدم خطوة"));
    assertThat(matches.length, is(9));
    assertThat(matches[0].getFromPos(), is(0));
    assertThat(matches[0].getToPos(), is(3));
    assertTrue(matches[0].getSuggestedReplacements().contains("إذا"));
    assertThat(matches[1].getFromPos(), is(4));
    assertThat(matches[1].getToPos(), is(8));
    assertTrue(matches[1].getSuggestedReplacements().contains("أردت"));
    assertThat(matches[8].getFromPos(), is(87));
    assertThat(matches[8].getToPos(), is(93));
    assertTrue(matches[8].getSuggestedReplacements().contains("الرؤوس"));

    matches = rule.match(lt.getAnalyzedSentence("مُقَدِّمَةُ الطَّبَرِيِّ شَيْخِ الدِّينِ فَجَاءَ فِيهِ بِالْعَجَبِ الْعُجَابِ"));
    assertThat(matches.length, is(1));
    assertThat(matches[0].getFromPos(), is(12));
    assertThat(matches[0].getToPos(), is(24));

    matches = rule.match(lt.getAnalyzedSentence("سمّيت الإقتصادي."));
    assertThat(matches.length, is(1));
    assertThat(matches[0].getFromPos(), is(6));

    matches = rule.match(lt.getAnalyzedSentence("وسمّيت الإقتصادي."));
    assertThat(matches.length, is(1));
    assertThat(matches[0].getFromPos(), is(7));

    matches = rule.match(lt.getAnalyzedSentence("وسميت الإقتصادي."));
    assertThat(matches.length, is(1));
    assertThat(matches[0].getFromPos(), is(6));
  }

}
