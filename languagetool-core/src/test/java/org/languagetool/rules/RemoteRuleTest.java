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

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.UserConfig;
import org.languagetool.language.Demo;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class RemoteRuleTest {

  protected static String sentence;
  private static JLanguageTool lt;
  private static RemoteRule rule;
  private static final RemoteRuleConfig config;

  private static long waitTime;
  private static boolean fail = false;
  private static int calls;

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

  static class TestRemoteRule extends RemoteRule {
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
        if (fail) {
          throw new RuntimeException("Failing for testing purposes");
        }
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
      System.out.println("Fallback matches");
      return new RemoteRuleResult(false, false, Collections.emptyList(), req.sentences);
    }

    @Override
    public String getDescription() {
      return "TEST REMOTE RULE";
    }
  }

  @BeforeClass
  public static void setUp() throws IOException {
    lt = new JLanguageTool(new Demo());
    lt.getAllActiveRules().forEach(r -> lt.disableRule(r.getId()));
    rule = new TestRemoteRule(config);
    sentence = "This is a test.";
    lt.addRule(rule);
  }

  @After
  public void tearDown() {
    rule.circuitBreaker().reset();
    waitTime = 0;
    calls = 0;
    fail = false;
  }


  private void assertMatches(String msg, int expected) throws IOException {
    List<RuleMatch> matches = lt.check(sentence);
    assertEquals(msg, expected, matches.size());
  }

  @Test
  public void testMatch() throws IOException {
    lt.disableRule(rule.getId());
    assertMatches("no matches before - sanity check", 0);
    lt.enableRule(rule.getId());
    assertMatches("test rule creates match", 1);
  }

  @Test
  public void testTimeout() throws IOException, InterruptedException {
    waitTime = config.baseTimeoutMilliseconds * 2;
    assertMatches("timeouts work", 0);
  }

  @Test
  @Ignore("Unstable in CI because of reliance on timings, for local testing only")
  public void testCircuitbreaker() throws IOException, InterruptedException {
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

  @Test
  public void testFailedRequests() throws IOException {
    fail = true;
    assertMatches("no matches for failing requests", 0);
  }

  private UserConfig getUserConfigWithAbTest(List<String> abTest) {
    UserConfig userConfig = new UserConfig(Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(),
      5, null, null, null, null, false, abTest, null, false, null);
    return userConfig;
  }

  private UserConfig getUserConfigWithThirdPartyAI(boolean thirdPartyAI) {
    UserConfig userConfig = new UserConfig(Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(),
      5, null, null, null, null, false, null, null, false, null, true, thirdPartyAI);
    return userConfig;
  }

  private UserConfig getUserConfigWithThirdPartyAIAndABTest(boolean thirdPartyAI, List<String> abTest) {
    UserConfig userConfig = new UserConfig(Collections.emptyList(), Collections.emptyList(), Collections.emptyMap(),
      5, null, null, null, null, false, abTest, null, false, null, true, thirdPartyAI);
    return userConfig;
  }

  @Test
  public void testAbFlags() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Demo());
    assertFalse(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));

    RemoteRuleConfig c1 = new RemoteRuleConfig(config);
    c1.options.put("abtest", "foo");
    lt = new JLanguageTool(new Demo());
    lt.activateRemoteRules(Arrays.asList(c1));

    assertFalse(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));

    UserConfig u1 = getUserConfigWithAbTest(Arrays.asList("foo"));
    lt = new JLanguageTool(new Demo(), null, u1);
    lt.activateRemoteRules(Arrays.asList(c1));

    assertTrue(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));

    UserConfig u2 = getUserConfigWithAbTest(Arrays.asList("foo", "bar"));
    RemoteRuleConfig c2 = new RemoteRuleConfig(config);
    c2.options.put("abtest", "bar");
    c2.options.put("excludeABTest", "fo.");
    lt = new JLanguageTool(new Demo(), null, u2);
    lt.activateRemoteRules(Arrays.asList(c2));

    assertFalse(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));

    UserConfig u3 = getUserConfigWithAbTest(Arrays.asList("bar"));
    lt = new JLanguageTool(new Demo(), null, u3);
    lt.activateRemoteRules(Arrays.asList(c2));

    assertTrue(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));

    RemoteRuleConfig c3 = new RemoteRuleConfig(config);
    UserConfig u4 = getUserConfigWithAbTest(Arrays.asList());
    c3.options.put("excludeABTest", "foo");
    lt = new JLanguageTool(new Demo(), null, u4);
    lt.activateRemoteRules(Arrays.asList(c3));

    assertTrue(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));
  }

  @Test
  public void testThirdPartyAI() throws IOException {

    JLanguageTool lt = new JLanguageTool(new Demo());
    assertFalse(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));

    // opt-in, third-party AI is active
    RemoteRuleConfig c1 = new RemoteRuleConfig(config);
    c1.options.put(RemoteRuleConfig.THIRD_PARTY_AI, "true");

    UserConfig config1 = getUserConfigWithThirdPartyAI(true);
    lt = new JLanguageTool(new Demo(), null, config1);
    lt.activateRemoteRules(Arrays.asList(c1));

    assertTrue(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));

    // no third party AI (default), opt-out, rule active
    UserConfig config2 = getUserConfigWithThirdPartyAI(false);
    RemoteRuleConfig c2 = new RemoteRuleConfig(config);
    lt = new JLanguageTool(new Demo(), null, config2);
    lt.activateRemoteRules(Arrays.asList(c2));

    assertTrue(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));

    // third-party AI, opt out, rule not active

    RemoteRuleConfig c3 = new RemoteRuleConfig(config);
    c3.options.put(RemoteRuleConfig.THIRD_PARTY_AI, "true");

    UserConfig config3 = getUserConfigWithThirdPartyAI(false);
    lt = new JLanguageTool(new Demo(), null, config3);
    lt.activateRemoteRules(Arrays.asList(c3));

    assertFalse(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));

    // no third-party AI, opt in, rule active

    RemoteRuleConfig c4 = new RemoteRuleConfig(config);
    c4.options.put(RemoteRuleConfig.THIRD_PARTY_AI, "false");

    UserConfig config4 = getUserConfigWithThirdPartyAI(true);
    lt = new JLanguageTool(new Demo(), null, config4);
    lt.activateRemoteRules(Arrays.asList(c4));

    assertTrue(lt.getAllActiveRules().stream().anyMatch(r -> r.getId().equals(config.ruleId)));
  }


  @Test
  public void testThirdPartyAIFallback() throws IOException {
    // Setup third-party rule
    RemoteRuleConfig thirdPartyRule = new RemoteRuleConfig();
    thirdPartyRule.ruleId = "TEST_THIRD_PARTY_RULE";
    thirdPartyRule.url = "http://example.com";
    thirdPartyRule.options = new HashMap<>();
    thirdPartyRule.options.put(RemoteRuleConfig.THIRD_PARTY_AI, "true");
    thirdPartyRule.options.put(RemoteRuleConfig.FALLBACK_RULE_ID, "TEST_FALLBACK_RULE");
  
    // Setup fallback rule
    RemoteRuleConfig fallbackRule = new RemoteRuleConfig();
    fallbackRule.ruleId = "TEST_FALLBACK_RULE";
    fallbackRule.url = "http://localhost";
    fallbackRule.options = new HashMap<>();
  
    // Setup another rule (not related to third-party/fallback)
    RemoteRuleConfig anotherRule = new RemoteRuleConfig();
    anotherRule.ruleId = "TEST_ANOTHER_RULE";
    anotherRule.url = "http://localhost";
    anotherRule.options = new HashMap<>();
  
    List<RemoteRuleConfig> configs = new ArrayList<>();
    configs.add(thirdPartyRule);
    configs.add(fallbackRule);
    configs.add(anotherRule);
  
    // Test with opt-in to third-party AI
    UserConfig optInConfig = getUserConfigWithThirdPartyAI(true);
    JLanguageTool ltOptIn = new JLanguageTool(new Demo(), null, optInConfig);
    ltOptIn.activateRemoteRules(configs);
  
    // Verify that with opt-in, third-party rule is active and fallback is not
    assertTrue("Third-party rule should be active when opted in", 
      ltOptIn.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_THIRD_PARTY_RULE")));
    assertFalse("Fallback rule should not be active when opted in", 
      ltOptIn.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_FALLBACK_RULE")));
    assertTrue("Unrelated rule should be active", 
      ltOptIn.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_ANOTHER_RULE")));
  
    // Test with opt-out of third-party AI
    UserConfig optOutConfig = getUserConfigWithThirdPartyAI(false);
    JLanguageTool ltOptOut = new JLanguageTool(new Demo(), null, optOutConfig);
    ltOptOut.activateRemoteRules(configs);
  
    // Verify that with opt-out, third-party rule is not active and fallback is active
    assertFalse("Third-party rule should not be active when opted out", 
      ltOptOut.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_THIRD_PARTY_RULE")));
    assertTrue("Fallback rule should be active when opted out", 
      ltOptOut.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_FALLBACK_RULE")));
    assertTrue("Unrelated rule should be active", 
      ltOptOut.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_ANOTHER_RULE")));

    // test with A/B test
    RemoteRuleConfig thirdPartyRuleAB = new RemoteRuleConfig();
    thirdPartyRuleAB.ruleId = "TEST_THIRD_PARTY_RULE_AB";
    thirdPartyRuleAB.url = "http://example.com";
    thirdPartyRuleAB.options = new HashMap<>();
    thirdPartyRuleAB.options.put(RemoteRuleConfig.THIRD_PARTY_AI, "true");
    thirdPartyRuleAB.options.put(RemoteRuleConfig.FALLBACK_RULE_ID, "TEST_FALLBACK_RULE");
    thirdPartyRuleAB.options.put("abtest", "test_third_party");

    configs = new ArrayList<>();
    configs.add(thirdPartyRuleAB);
    configs.add(fallbackRule);
    configs.add(anotherRule);

    UserConfig optInConfigABEnabled = getUserConfigWithThirdPartyAIAndABTest(true, Arrays.asList("test_third_party"));
    JLanguageTool ltOptInABEnabled = new JLanguageTool(new Demo(), null, optInConfigABEnabled);
    ltOptInABEnabled.activateRemoteRules(configs);

    // Verify that with opt-in and A/B test enabled, third-party rule is active and fallback is not
    assertTrue("Third-party rule should be active when opted in",
      ltOptInABEnabled.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_THIRD_PARTY_RULE_AB")));
    assertFalse("Fallback rule should not be active when opted in",
      ltOptInABEnabled.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_FALLBACK_RULE")));
    assertTrue("Unrelated rule should be active",
      ltOptInABEnabled.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_ANOTHER_RULE")));

    UserConfig optInConfigABDisabled = getUserConfigWithThirdPartyAIAndABTest(true, Arrays.asList());
    JLanguageTool ltOptInABDisabled = new JLanguageTool(new Demo(), null, optInConfigABDisabled);
    ltOptInABDisabled.activateRemoteRules(configs);

    assertFalse("Third-party rule should not be active when opted in but no A/B test enabled",
      ltOptInABDisabled.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_THIRD_PARTY_RULE_AB")));
    assertTrue("Fallback rule should be active when opted in but A/B test not enabled",
      ltOptInABDisabled.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_FALLBACK_RULE")));
    assertTrue("Unrelated rule should be active",
      ltOptInABDisabled.getAllActiveRules().stream().anyMatch(r -> r.getId().equals("TEST_ANOTHER_RULE")));
  }

}
