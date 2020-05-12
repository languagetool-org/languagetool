/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

public class EnglishForL2SpeakersFalseFriendRuleTest {

  @Test
  public void testRule() throws IOException {
    Language en = Languages.getLanguageForShortCode("en");
    Language fr = Languages.getLanguageForShortCode("fr");
    Map<String, Integer> map = new HashMap<>();
    // first test:
    map.put("will achieve", 1);
    map.put("will complete", 10);
    map.put("achieve her", 1);
    map.put("complete her", 5);
    map.put("achieve her task", 1);
    map.put("complete her task", 10);
    // second test:
    map.put("was completed", 100);
    map.put("was completed .", 100);
    map.put("form was completed", 100);
    FakeLanguageModel lm = new FakeLanguageModel(map);
    Rule rule = new EnglishForFrenchFalseFriendRule(TestTools.getEnglishMessages(), lm, fr, en);
    JLanguageTool lt = new JLanguageTool(en, fr);

    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("She will achieve her task."));
    assertThat(matches1.length, is(1));
    assertTrue(matches1[0].getMessage().contains("\"achieve\" (English) means \"réaliser\" (French)"));

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("The code only worked if the form was achieved."));
    assertThat(matches2.length, is(1));
    assertTrue(matches2[0].getMessage().contains("\"achieve\" (English) means \"réaliser\" (French)"));
  }

}
