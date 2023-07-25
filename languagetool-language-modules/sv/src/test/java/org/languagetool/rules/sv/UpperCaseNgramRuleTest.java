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
package org.languagetool.rules.sv;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class UpperCaseNgramRuleTest {

  private final Language lang = Languages.getLanguageForShortCode("sv");
  private final JLanguageTool lt = new JLanguageTool(lang);

  @SuppressWarnings("ResultOfObjectAllocationIgnored")
  @Test
  public void testConstructor() {
    new UpperCaseNgramRule(TestTools.getEnglishMessages(), new FakeLanguageModel(), Languages.getLanguageForShortCode("sv"), new UserConfig());
  }
  
  @Test
  @Ignore
  public void testRule() throws IOException {
    Map<String, Integer> map = new HashMap<>();
    map.put("1 maj är kommen", 10);
    map.put("Maj heter hon", 100);
    map.put("S:t Johannesgatan", 100);
    map.put("sträck", 10);
    File indexDir = new File("/data/ngram-index/sv");
    if (!indexDir.exists()) {
      throw new RuntimeException("ngram data not found at " + indexDir + ", get more info at https://dev.languagetool.org/finding-errors-using-n-gram-data");
    }
    //FakeLanguageModel lm = new FakeLanguageModel(map);
    LuceneLanguageModel lm = new LuceneLanguageModel(indexDir);
    UpperCaseNgramRule rule = new UpperCaseNgramRule(TestTools.getMessages("sv"), lm, lang, new UserConfig());
    assertMatch(0, "Från 15 maj är det eldningsförbud.", rule);
    assertMatch(0, "Ett långt streck.", rule);
    assertMatch(0, "I ett sträck.", rule);
    assertMatch(2, "15e Maj är sista dag för antagningen.", rule);
    assertMatch(1, "Det är majs födelsedag idag.", rule);
    assertMatch(1, "Sträcket är rakt.", rule);
  }

  private void assertMatch(int expectedMatches, String input, UpperCaseNgramRule rule) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat("Got " + matches.length + " match(es) for: " + input, matches.length, is(expectedMatches));
  }

}
