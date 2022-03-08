/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2020 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * test the filtering logic implemented in RemoteRuleFilters
 * @see RemoteRuleFilterTest for testing pattern rules in remote-rule-filters.xml
 *  */
public class RemoteRuleFiltersTest {
  private static Language testLang = TestTools.getDemoLanguage();
  private static final String TEST_RULE = "TEST_REMOTE_RULE";

  @Test
  public void load() throws Exception {
    Map<String, List<AbstractPatternRule>> rules = RemoteRuleFilters.load(testLang);
    Assertions.assertTrue(rules.containsKey(TEST_RULE), "loaded test rule");
    Assertions.assertEquals(rules.get(TEST_RULE).size(), 1, "loaded test rule");
  }

  private RuleMatch matchSubstring(String ruleId, AnalyzedSentence sentence, String substring) {
    Rule r = new FakeRule() {
      @Override
      public String getId() {
        return ruleId;
      }
    };
    int start = sentence.getText().indexOf(substring);
    int end = start + substring.length();
    return new RuleMatch(r, sentence, start, end, "test match");
  }

  @Test
  public void testSimpleFilter() throws IOException, ExecutionException {
    JLanguageTool lt = new JLanguageTool(testLang);
    AnalyzedSentence s = lt.getAnalyzedSentence("This is a test.");
    Assertions.assertTrue(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(TEST_RULE, s, "test"))
      ).isEmpty(), "Simple filter works");
    Assertions.assertFalse(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(TEST_RULE + "_NEW", s, "test"))).isEmpty(), "Filter respects IDs");
  }

  @Test
  public void testMultiTokenWhitespace() throws IOException, ExecutionException {
    String ruleId = "TEST_WHITESPACE";
    JLanguageTool lt = new JLanguageTool(testLang);
    String sub; AnalyzedSentence s;

    sub = "foo bar";
    s = lt.getAnalyzedSentence("This is " + sub + " test.");
    Assertions.assertTrue(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId, s, "foo bar"))
    ).isEmpty(), "Filter works");

    sub = "foo   bar";
    s = lt.getAnalyzedSentence("This is " + sub +  " test.");
    Assertions.assertTrue(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId, s, sub))
    ).isEmpty(), "Filter works with multiple spaces");

    sub = "foo \n bar";
    s = lt.getAnalyzedSentence("This is " + sub +  " test.");
    Assertions.assertTrue(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId, s, sub))
    ).isEmpty(), "Filter works with newlines");
  }

  @Test
  public void testMarker() throws IOException, ExecutionException {
    String ruleId = "TEST_MARKER";
    JLanguageTool lt = new JLanguageTool(testLang);
    String sub; AnalyzedSentence s;

    s = lt.getAnalyzedSentence("I went to the bar.");
    Assertions.assertFalse(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId, s, "bar"))
    ).isEmpty(), "Filter doesn't apply because of pre-marker context");

    s = lt.getAnalyzedSentence("I went to the foo bar.");
    Assertions.assertTrue(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId, s, "bar"))
    ).isEmpty(), "Filter applies, marker wroks");
  }

  @Test
  public void testAntipattern() throws IOException, ExecutionException {
    String ruleId = "TEST_ANTIPATTERN";
    JLanguageTool lt = new JLanguageTool(testLang);
    String sub; AnalyzedSentence s;

    s = lt.getAnalyzedSentence("I went to the test bar.");
    Assertions.assertFalse(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId, s, "test bar"))
    ).isEmpty(), "Filter doesn't apply because of antipattern");

    s = lt.getAnalyzedSentence("I went to the best bar.");
    Assertions.assertTrue(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId, s, "best bar"))
    ).isEmpty(), "Filter applies, antipattern did not match");
  }

  @Test
  public void testIDRegexFilter() throws IOException, ExecutionException {
    String ruleId = "TEST_ID_REGEX";
    JLanguageTool lt = new JLanguageTool(testLang);
    AnalyzedSentence s = lt.getAnalyzedSentence("This is a foo.");

    Assertions.assertFalse(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId, s, "foo"))
      ).isEmpty(), "Regex doesn't match");

    Assertions.assertTrue(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId + "1", s, "foo"))
      ).isEmpty(), "Regex matches 1");
    Assertions.assertTrue(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId + "2", s, "foo"))
      ).isEmpty(), "Regex matches 2");
    Assertions.assertTrue(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId + "99", s, "foo"))
      ).isEmpty(), "Regex matches 3");

    Assertions.assertFalse(RemoteRuleFilters.filterMatches(testLang, s,
            Collections.singletonList(matchSubstring(ruleId + "100", s, "foo"))
      ).isEmpty(), "Regex must match complete string");
  }
}
