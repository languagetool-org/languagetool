/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

public class ProhibitedCompoundRuleTest {
  
  @Test
  public void testRule() throws IOException {
    Map<String,Integer> map = new HashMap<>();
    map.put("Leerzeile", 100);
    map.put("Urberliner", 100);
    map.put("Ureinwohner", 100);
    map.put("Wohnungsleerstand", 50);
    map.put("Xliseihflehrstand", 50);
    ProhibitedCompoundRule rule = new ProhibitedCompoundRule(TestTools.getEnglishMessages(), new FakeLanguageModel(map));
    JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    assertMatches("Er ist Uhrberliner.", 1, rule, lt);
    assertMatches("Hier leben die Uhreinwohner.", 1, rule, lt);
    assertMatches("Eine Leerzeile einfügen.", 0, rule, lt);
    assertMatches("Eine Lehrzeile einfügen.", 1, rule, lt);
    assertMatches("Viel Wohnungsleerstand.", 0, rule, lt);
    assertMatches("Viel Wohnungslehrstand.", 1, rule, lt);
    assertMatches("Viel Xliseihfleerstand.", 0, rule, lt);
    assertMatches("Viel Xliseihflehrstand.", 0, rule, lt);  // no correct spelling, so not suggested
  }

  ProhibitedCompoundRule getRule(String languageModelPath) throws IOException {
    return getRule(languageModelPath, ProhibitedCompoundRule.RULE_ID);
  }

  ProhibitedCompoundRule getRule(String languageModelPath, String ruleId) throws IOException {
    Language lang = Languages.getLanguageForShortCode("de");
    LanguageModel languageModel = new LuceneLanguageModel(new File(languageModelPath, lang.getShortCode()));
    List<Rule> rules = lang.getRelevantLanguageModelRules(JLanguageTool.getMessageBundle(), languageModel);
    if (rules == null) {
      throw new RuntimeException("Language " + lang + " doesn't seem to support a language model");
    }
    ProhibitedCompoundRule foundRule = null;
    for (Rule rule : rules) {
      if (rule.getId().equals(ruleId)) {
        foundRule = (ProhibitedCompoundRule) rule;
        break;
      }
    }
    return foundRule;
  }

  void assertMatches(String input, int expecteMatches, ProhibitedCompoundRule rule, JLanguageTool lt) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat("Got matches: " + Arrays.toString(matches), matches.length, is(expecteMatches));
  }

}
