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

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

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
    assertTrue("loaded test rule", rules.containsKey(TEST_RULE));
    assertEquals("loaded test rule", rules.get(TEST_RULE).size(), 1);
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
    assertTrue("Simple filter works", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(TEST_RULE, s, "test"))
      ).isEmpty());
    assertFalse("Filter respects IDs", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(TEST_RULE + "_NEW", s, "test"))).isEmpty()
    );
  }

  @Test
  public void testMultiTokenWhitespace() throws IOException, ExecutionException {
    String ruleId = "TEST_WHITESPACE";
    JLanguageTool lt = new JLanguageTool(testLang);
    String sub; AnalyzedSentence s;

    sub = "foo bar";
    s = lt.getAnalyzedSentence("This is " + sub + " test.");
    assertTrue("Filter works", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId, s, "foo bar"))
    ).isEmpty());

    sub = "foo   bar";
    s = lt.getAnalyzedSentence("This is " + sub +  " test.");
    assertTrue("Filter works with multiple spaces", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId, s, sub))
    ).isEmpty());

    sub = "foo \n bar";
    s = lt.getAnalyzedSentence("This is " + sub +  " test.");
    assertTrue("Filter works with newlines", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId, s, sub))
    ).isEmpty());
  }

  @Test
  public void testMarker() throws IOException, ExecutionException {
    String ruleId = "TEST_MARKER";
    JLanguageTool lt = new JLanguageTool(testLang);
    String sub; AnalyzedSentence s;

    s = lt.getAnalyzedSentence("I went to the bar.");
    assertFalse("Filter doesn't apply because of pre-marker context", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId, s, "bar"))
    ).isEmpty());

    s = lt.getAnalyzedSentence("I went to the foo bar.");
    assertTrue("Filter applies, marker wroks", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId, s, "bar"))
    ).isEmpty());
  }

  @Test
  public void testAntipattern() throws IOException, ExecutionException {
    String ruleId = "TEST_ANTIPATTERN";
    JLanguageTool lt = new JLanguageTool(testLang);
    String sub; AnalyzedSentence s;

    s = lt.getAnalyzedSentence("I went to the test bar.");
    assertFalse("Filter doesn't apply because of antipattern", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId, s, "test bar"))
    ).isEmpty());

    s = lt.getAnalyzedSentence("I went to the best bar.");
    assertTrue("Filter applies, antipattern did not match", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId, s, "best bar"))
    ).isEmpty());
  }

  @Test
  public void testIDRegexFilter() throws IOException, ExecutionException {
    String ruleId = "TEST_ID_REGEX";
    JLanguageTool lt = new JLanguageTool(testLang);
    AnalyzedSentence s = lt.getAnalyzedSentence("This is a foo.");

    assertFalse("Regex doesn't match", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId, s, "foo"))
      ).isEmpty());

    assertTrue("Regex matches 1", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId + "1", s, "foo"))
      ).isEmpty());
    assertTrue("Regex matches 2", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId + "2", s, "foo"))
      ).isEmpty());
    assertTrue("Regex matches 3", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId + "99", s, "foo"))
      ).isEmpty());

    assertFalse("Regex must match complete string", RemoteRuleFilters.filterMatches(testLang, s,
      Arrays.asList(matchSubstring(ruleId + "100", s, "foo"))
      ).isEmpty());
  }
}
