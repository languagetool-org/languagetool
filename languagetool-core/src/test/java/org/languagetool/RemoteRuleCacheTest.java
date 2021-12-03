/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
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

package org.languagetool;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.language.Demo;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.RemoteRule;
import org.languagetool.rules.RemoteRuleConfig;
import org.languagetool.rules.RemoteRuleResult;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RemoteRuleCacheTest {

  private JLanguageTool lt;
  private ResultCache cache;
  private RemoteRule rule;

  static class TestRemoteRule extends RemoteRule {
    private static final RemoteRuleConfig testConfig = new RemoteRuleConfig();

    static {
      testConfig.ruleId = "TEST_REMOTE_RULE";
    }

    TestRemoteRule() {
      super(new Demo(), JLanguageTool.getMessageBundle(), testConfig, false);
    }

    class TestRemoteRequest extends RemoteRequest {
      private final List<AnalyzedSentence> sentences;

      TestRemoteRequest(List<AnalyzedSentence> sentences) {
        this.sentences = sentences;
      }
    }

    @Override
    protected RemoteRequest prepareRequest(List<AnalyzedSentence> sentences, Long textSessionId) {
      return new TestRemoteRequest(sentences);
    }

    private RuleMatch testMatch(AnalyzedSentence s) {
      return new RuleMatch(this, s, 0, 1, "Test match");
    }

    @Override
    protected Callable<RemoteRuleResult> executeRequest(RemoteRequest request, long timeoutMilliseconds) throws TimeoutException {
      return () -> {
        TestRemoteRequest req = (TestRemoteRequest) request;
        List<RuleMatch> matches = req.sentences.stream().map(this::testMatch).collect(Collectors.toList());
        return new RemoteRuleResult(true, true, matches, req.sentences);
      };
    }

    @Override
    protected RemoteRuleResult fallbackResults(RemoteRequest request) {
      TestRemoteRequest req = (TestRemoteRequest) request;
      return new RemoteRuleResult(false, false, Collections.emptyList(), req.sentences);
    }

    @Override
    public String getDescription() {
      return "TEST REMOTE RULE";
    }
  }

  @Before
  public void init() {
    cache = new ResultCache(1000);
    lt = new JLanguageTool(new FakeLanguage(), cache, new UserConfig());
    rule = new TestRemoteRule();
    lt.addRule(rule);
  }

  private List<RuleMatch> check(String text) {
    AnnotatedText annotatedText = new AnnotatedTextBuilder().addText(text).build();

    try {
      return lt.check(annotatedText, true, JLanguageTool.ParagraphHandling.NORMAL, null,
        JLanguageTool.Mode.ALL, JLanguageTool.Level.DEFAULT);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  public void testDuplicateSentence() throws IOException {
    String text = "Foo. Foo. Bar."; // end of paragraph is included as tokens, would otherwise make them distinct.

    List<AnalyzedSentence> sentences = lt.analyzeText(text);
    List<AnalyzedSentence> distinct = sentences.stream().distinct().collect(Collectors.toList());
    System.out.println("distinct sentences");
    distinct.forEach(System.out::println);

    assertThat(distinct.size(), is(equalTo(2)));

    List<RuleMatch> directMatches = sentences.stream().flatMap(s -> {
      try {
        return Arrays.stream(rule.match(s));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }).collect(Collectors.toList());

    List<RuleMatch> matches = check(text);

    System.out.println("direct matches");
    directMatches.forEach(System.out::println);
    System.out.println("matches");
    matches.forEach(System.out::println);
    assertThat("Test rule matches when called directly", directMatches.size(), is(equalTo(3)));
    assertThat("Matches are collected and transformed correctly", matches.size(), is(equalTo(3)));

    assertThat("Correct offsets", matches.stream().map(RuleMatch::getFromPos).collect(Collectors.toList()),
      equalTo(Arrays.asList(0, 5, 10)));

    List<RuleMatch> cachedMatches = check(text);

    assertThat("Cached Matches are collected and transformed correctly", cachedMatches.size(), is(equalTo(3)));

    assertThat("Correct cached offsets", cachedMatches.stream().map(RuleMatch::getFromPos).collect(Collectors.toList()),
      equalTo(Arrays.asList(0, 5, 10)));

  }
}
