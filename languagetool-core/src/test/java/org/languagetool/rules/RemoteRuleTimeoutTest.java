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

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.RuleMatchListener;
import org.languagetool.TestTools;
import org.languagetool.language.Demo;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

@Ignore("For interactive use")
public class RemoteRuleTimeoutTest {
  private static final long TIMEOUT = 50L;

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
      return new TestRemoteRule.TestRemoteRequest(sentences);
    }

    private RuleMatch testMatch(AnalyzedSentence s) {
      return new RuleMatch(this, s, 0, 1, "Test match");
    }

    @Override
    protected Callable<RemoteRuleResult> executeRequest(RemoteRequest request, long timeoutMilliseconds) throws TimeoutException {
      return () -> {
        TestRemoteRule.TestRemoteRequest req = (TestRemoteRule.TestRemoteRequest) request;
        List<RuleMatch> matches = req.sentences.stream().map(this::testMatch).collect(Collectors.toList());
        while (!Thread.interrupted()) {
          ;
        }
        // cancelling only works if implementations respect interrupts or timeouts
        //while(true);
        return new RemoteRuleResult(true, true, matches, req.sentences);
      };
    }

    @Override
    protected RemoteRuleResult fallbackResults(RemoteRequest request) {
      TestRemoteRule.TestRemoteRequest req = (TestRemoteRule.TestRemoteRequest) request;
      return new RemoteRuleResult(false, false, Collections.emptyList(), req.sentences);
    }

    @Override
    public String getDescription() {
      return "TEST REMOTE RULE";
    }
  }

  RemoteRule rule;
  JLanguageTool lt;
  AnnotatedText text;

  @Before
  public void setUp() throws Exception {
    rule = getTestRemoteRule();
    lt = new JLanguageTool(TestTools.getDemoLanguage());
    lt.getAllRules().forEach(r -> lt.disableRule(rule.getId()));
    lt.addRule(rule);
    lt.enableRule(rule.getId());
    text = new AnnotatedTextBuilder().addText("Foo").build();
  }

  @NotNull
  private RemoteRule getTestRemoteRule() throws ExecutionException {
    return new TestRemoteRule();
    //List<RemoteRuleConfig> configs = RemoteRuleConfig.load(new File("remoteRules.json"));
    //return GRPCRule.createAll(TestTools.getDemoLanguage(), configs,
    //  true, "AI", "Test rule").get(0);
  }

  @After
  public void tearDown() throws Exception {
  }

  @Test
  public void testCancelThreads() throws IOException, InterruptedException, TimeoutException, ExecutionException {
    // simulate cancelling remote rule requests because of timeouts
    // to see if threads remain
    // has happened before and lead to errors because we run into memory/thread limits
    // cancelling the future only sets Thread.interrupted()
    // workaround: pass timeout to implementations, e.g. deadline in GRPCRule
    ThreadFactory factory = new ThreadFactoryBuilder().setDaemon(true).setNameFormat("test-pool-%d").build();
    ExecutorService pool = Executors.newCachedThreadPool(factory);
    int runs = 100;
    int batches = 10;
    long wait = 500L;
    RuleMatchListener listener = ruleMatch -> {
      throw new RuntimeException("This shouldn't happen");
    };
    for (int i = 0; i < runs; i++) {
      for (int j = 0; j < batches; j++) {
        FutureTask<List<RuleMatch>> task = new FutureTask<>(() -> {
          List<RuleMatch> matches = lt.check(text, true,
            JLanguageTool.ParagraphHandling.NORMAL, listener, JLanguageTool.Mode.ALL, JLanguageTool.Level.DEFAULT);
          return matches;
        });
        pool.submit(task);
        try {
          List<RuleMatch> matches = task.get(TIMEOUT, TimeUnit.MILLISECONDS);
          assertTrue(matches.isEmpty());
        } catch (ExecutionException | TimeoutException e) {
          task.cancel(true);
        }
      }
      Thread.sleep(wait);
    }
    Thread.sleep(wait);
    int running = Thread.activeCount();
    Thread.getAllStackTraces().forEach( (thread, stack) -> {
      System.out.printf("Stacktrace for %s (%s)%n: %s%n", thread.getName(), thread.getState(), Arrays.toString(stack));
    });
    System.out.println("Running threads: " + running);
  }
}
