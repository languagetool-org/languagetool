/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2021 Fabian Richter
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

package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RemoteRule;
import org.languagetool.rules.RemoteRuleConfig;
import org.languagetool.rules.RemoteRuleResult;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;

public class EnglishRemoteRuleSuppressMisspelledTest {
  private static Language testLang = new AmericanEnglish();
  private static final String TEST_RULE = "TEST_REMOTE_RULE";
  private static final String TEST_SENTENCE = "This is a test sentence.";

  class TestRule extends RemoteRule {

    TestRule(RemoteRuleConfig config) {
      super(testLang, JLanguageTool.getMessageBundle(), config, true, TEST_RULE);
    }

    class TestRequest extends RemoteRequest {
      List<AnalyzedSentence> sentences;
    }

    @Override
    protected RemoteRequest prepareRequest(List<AnalyzedSentence> sentences, Long textSessionId) {
      TestRequest r = new TestRequest();
      r.sentences = sentences;
      return r;
    }

    @Override
    protected Callable<RemoteRuleResult> executeRequest(RemoteRequest req, long timeoutMilliseconds)
        throws TimeoutException {
      return () -> {
        List<RuleMatch> matches = new ArrayList<>();
        TestRequest r = (TestRequest) req;
        for (AnalyzedSentence s : r.sentences) {
          RuleMatch m = new RuleMatch(this, s, 0, s.getText().length(), "Test match");
          m.addSuggestedReplacement("mistake");
          m.addSuggestedReplacement("mistak");
          matches.add(m);
        }
        return new RemoteRuleResult(true, true, matches, r.sentences);
      };
    }

    @Override
    protected RemoteRuleResult fallbackResults(RemoteRequest r) {
      return new RemoteRuleResult(false, false, Collections.emptyList(), ((TestRequest) r).sentences);
    }

    @Override
    public String getDescription() {
      return "Test rule";
    }

  }

  private RemoteRuleConfig withOptions(String... valStrings) {
    Map<String, String> options = new HashMap<>();
    for (int i = 0; i < valStrings.length; i+=2) {
      options.put(valStrings[i], valStrings[i+1]);
    }
    RemoteRuleConfig c = new RemoteRuleConfig();
    c.ruleId = TEST_RULE;
    c.options = options;
    return c;
  }

  @Test
  public void test() throws IOException {
    JLanguageTool lt = new JLanguageTool(testLang);
    AnalyzedSentence s = lt.getAnalyzedSentence(TEST_SENTENCE);

    TestRule r = new TestRule(withOptions());
    RuleMatch[] m = r.match(s);
    assertEquals("Test rule creates match without match suppression", 1, m.length);
    assertEquals("Test rule creates match with two suggestions", 2, m[0].getSuggestedReplacements().size());

    r = new TestRule(withOptions("suppressMisspelledMatch", TEST_RULE));
    m = r.match(s);
    assertEquals("Test rule creates no match with match suppression", 0, m.length);

    r = new TestRule(withOptions("suppressMisspelledSuggestions", TEST_RULE));
    m = r.match(s);
    assertEquals("Test rule creates match with suggestion suppression", 1, m.length);
    assertEquals("Test rule creates match with correctly spelled suggestions", Arrays.asList("mistake"), m[0].getSuggestedReplacements());

    r = new TestRule(withOptions("suppressMisspelledMatch", ".*REMOTE.*"));
    m = r.match(s);
    assertEquals("Test rule creates no match with match suppression (regex, full match)", 0, m.length);

    r = new TestRule(withOptions("suppressMisspelledMatch", ".*REMOTE"));
    m = r.match(s);
    assertEquals("no match suppression (regex only partial match)", 1, m.length);
  }

}
