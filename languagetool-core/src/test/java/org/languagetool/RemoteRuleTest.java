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

package org.languagetool;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.language.Demo;
import org.languagetool.rules.RemoteRule;
import org.languagetool.rules.RemoteRuleConfig;
import org.languagetool.rules.RemoteRuleResult;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class RemoteRuleTest {

  protected String sentence;
  private JLanguageTool lt;
  private RemoteRule rule;
  private static final RemoteRuleConfig config;

  private long waitTime;
  private int calls;

  static {
    config = new RemoteRuleConfig();
    config.ruleId = TestRemoteRule.ID;
    config.baseTimeoutMilliseconds = 50;
    config.minimumNumberOfCalls = 2;
    config.failureRateThreshold = 50f;
    config.downMilliseconds = 200L;
    config.slidingWindowSize = 2;
    config.slidingWindowType = CircuitBreakerConfig.SlidingWindowType.COUNT_BASED.name();
  }

  class TestRemoteRule extends RemoteRule {
    static final String ID =  "TEST_REMOTE_RULE";

    TestRemoteRule(RemoteRuleConfig config) {
      super(new Demo(), JLanguageTool.getMessageBundle(), config, false);
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
        calls++;
        TestRemoteRequest req = (TestRemoteRequest) request;
        long deadline = System.currentTimeMillis() + waitTime;
        while (System.currentTimeMillis() < deadline);
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
  public void setUp() throws IOException {
    lt = new JLanguageTool(new Demo());
    lt.getAllActiveRules().forEach(r -> lt.disableRule(r.getId()));
    rule = new TestRemoteRule(config);
    sentence = "This is a test.";
  }

  private void assertMatches(String msg, int expected) throws IOException {
    List<RuleMatch> matches = lt.check(sentence);
    assertEquals(msg, expected, matches.size());
  }


  @Test
  @Ignore("Unstable in CI because of reliance on timings, for local testing only")
  public void testCircuitbreaker() throws IOException, InterruptedException {
    assertMatches("no matches before - sanity check", 0);
    lt.addRule(rule);

    assertMatches("test rule creates match", 1);

    waitTime = config.baseTimeoutMilliseconds * 2;
    assertMatches("timeouts work", 0);
    assertMatches("timeouts work", 0);

    waitTime = 0;
    int callsBefore = calls;
    assertMatches("No matches when circuitbreaker open", 0);
    assertEquals("No calls when circuitbreaker open", callsBefore, calls);

    Thread.sleep(config.downMilliseconds);
    assertMatches("matches when circuitbreaker half-open", 1);
    assertEquals("calls when circuitbreaker half-open", callsBefore+1, calls);
  }

}
