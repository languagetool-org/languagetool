/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.WordListValidatorTest;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.PatternRuleLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.fail;

public class LanguageSpecificTest {

  protected void runTests(Language lang) throws IOException {
    new WordListValidatorTest().testWordListValidity(lang);
    testNoQuotesAroundSuggestion(lang);
  }

  // no quotes needed around <suggestion>...</suggestion> in XML:
  private void testNoQuotesAroundSuggestion(Language lang) throws IOException {
    if (lang.getShortCode().equals("fa") || lang.getShortCode().equals("zh")) {
      // ignore languages not maintained anyway
      System.out.println("Skipping testNoQuotesAroundSuggestion for " + lang.getName());
      return;
    }
    String dirBase = JLanguageTool.getDataBroker().getRulesDir() + "/" + lang.getShortCode() + "/";
    for (String ruleFileName : lang.getRuleFileNames()) {
      if (ruleFileName.contains("-test-")) {
        continue;
      }
      InputStream is = this.getClass().getResourceAsStream(ruleFileName);
      List<AbstractPatternRule> rules = new PatternRuleLoader().getRules(is, dirBase + "/" + ruleFileName);
      for (AbstractPatternRule rule : rules) {
        String message = rule.getMessage();
        if (message.matches(".*['\"«»“”’]<suggestion.*") && message.matches(".*</suggestion>['\"«»“”’].*")) {
          fail(lang.getName() + " rule " + rule.getFullId() + " uses quotes around <suggestion>...<suggestion> in its <message>, this should be avoided: '" + message + "'");
        }
      }
    }
  }

  protected void testDemoText(Language lang, String text, List<String> expectedMatchIds) throws IOException {
    JLanguageTool lt = new JLanguageTool(lang);
    List<RuleMatch> matches = lt.check(text);
    int i = 0;
    List<String> actualRuleIds = new ArrayList<>();
    for (RuleMatch match : matches) {
      actualRuleIds.add(match.getRule().getId());
    }
    if (expectedMatchIds.size() != actualRuleIds.size()) {
      failTest(lang, text, expectedMatchIds, actualRuleIds);
    }
    for (String actualRuleId : actualRuleIds) {
      if (!expectedMatchIds.get(i).equals(actualRuleId)) {
        failTest(lang, text, expectedMatchIds, actualRuleIds);
      }
      i++;
    }
  }

  private void failTest(Language lang, String text, List<String> expectedMatchIds, List<String> actualRuleIds) {
    fail("The website demo text matches for " + lang + " have changed. Demo text:\n" + text +
            "\nExpected rule matches:\n" + expectedMatchIds + "\nActual rule matches:\n" + actualRuleIds);
  }
  
}
