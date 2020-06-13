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

package org.languagetool.rules;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.languagetool.AnalyzedSentence;
import org.languagetool.markup.AnnotatedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @since 4.9
 */
public abstract class RemoteRule extends Rule {
  
  private static final Logger logger = LoggerFactory.getLogger(RemoteRule.class);

  /* needs to be shared between rule instances because new instances may be created and discarded often
     needs to be a map because 'static' and inheritance don't play nice in Java */
  private static final ConcurrentMap<String, Long> lastFailure = new ConcurrentHashMap<>();
  private static final ConcurrentMap<String, AtomicInteger> consecutiveFailures = new ConcurrentHashMap<>();
  private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
    .setNameFormat("remote-rule-pool-{}").setDaemon(true).build();

  protected static final List<Runnable> shutdownRoutines = new LinkedList<>();

  // needed to run callables with timeout
  private static final ConcurrentMap<String, ExecutorService> executors = new ConcurrentHashMap<>();
  protected final RemoteRuleConfig serviceConfiguration;
  private AnnotatedText annotatedText;

  public RemoteRule(ResourceBundle messages, RemoteRuleConfig config) {
    super(messages);
    serviceConfiguration = config;
    String ruleId = getId();
    lastFailure.putIfAbsent(ruleId, 0L);
    consecutiveFailures.putIfAbsent(ruleId, new AtomicInteger());
    // TODO maybe use fixed pool, take number of concurrent requests from configuration?
    executors.putIfAbsent(ruleId, Executors.newCachedThreadPool(threadFactory));
  }

  public static void shutdown() {
    shutdownRoutines.forEach(Runnable::run);
  }

  protected class RemoteRequest {}

  protected abstract RemoteRequest prepareRequest(List<AnalyzedSentence> sentences, AnnotatedText annotatedText);
  protected abstract Callable<RemoteRuleResult> executeRequest(RemoteRequest request);
  protected abstract RemoteRuleResult fallbackResults(RemoteRequest request);

  public FutureTask<List<RuleMatch>> run(List<AnalyzedSentence> sentences, AnnotatedText annotatedText) {
    this.annotatedText = Objects.requireNonNull(annotatedText);
    return new FutureTask<>(() -> {
      long startTime = System.nanoTime();
      long characters = sentences.stream().mapToInt(sentence -> sentence.getText().length()).sum();
      String ruleId = getId();
      RemoteRequest req = prepareRequest(sentences, annotatedText);
      RemoteRuleResult result;

      if (consecutiveFailures.get(ruleId).get() >= serviceConfiguration.getFall()) {
        long failureInterval = System.currentTimeMillis() - lastFailure.get(ruleId);
        if (failureInterval < serviceConfiguration.getDownMilliseconds()) {
          RemoteRuleMetrics.request(ruleId, 0, 0, characters, RemoteRuleMetrics.RequestResult.DOWN);
          result = fallbackResults(req);
          return result.getMatches();
        }
      }
      RemoteRuleMetrics.up(ruleId, true);

      for (int i = 0; i <= serviceConfiguration.getMaxRetries(); i++) {
        Callable<RemoteRuleResult> task = executeRequest(req);
        long timeout = serviceConfiguration.getBaseTimeoutMilliseconds() +
          Math.round(characters * serviceConfiguration.getTimeoutPerCharacterMilliseconds());
        try {
          Future<RemoteRuleResult> future = executors.get(ruleId).submit(task);
          if (timeout <= 0)  { // for debugging, disable timeout
            result = future.get();
          } else {
            result = future.get(timeout, TimeUnit.MILLISECONDS);
          }
          future.cancel(true);

          if (result.isRemote()) { // don't reset failures if no remote call took place
            consecutiveFailures.get(ruleId).set(0);
            RemoteRuleMetrics.failures(ruleId, 0);
          }

          RemoteRuleMetrics.RequestResult requestResult = result.isRemote() ?
            RemoteRuleMetrics.RequestResult.SUCCESS : RemoteRuleMetrics.RequestResult.SKIPPED;
          RemoteRuleMetrics.request(ruleId, i, System.nanoTime() - startTime, characters, requestResult);
          return result.getMatches();
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
          logger.warn("Error while fetching results for remote rule " + ruleId + ", tried " + (i + 1) + " times, timeout: " + timeout + "ms" , e);

          RemoteRuleMetrics.RequestResult status;
          if (e instanceof TimeoutException || e instanceof InterruptedException) {
            status = RemoteRuleMetrics.RequestResult.TIMEOUT;
          } else {
            status = RemoteRuleMetrics.RequestResult.ERROR;
          }

          RemoteRuleMetrics.request(ruleId, i, System.nanoTime() - startTime, characters, status);
        }
      }
      RemoteRuleMetrics.failures(ruleId, consecutiveFailures.get(ruleId).incrementAndGet());
      logger.warn("Fetching results for remote rule " + ruleId + " failed.");
      if (consecutiveFailures.get(ruleId).get() >= serviceConfiguration.getFall()) {
        lastFailure.put(ruleId, System.currentTimeMillis());
        logger.warn("Remote rule " + ruleId + " marked as DOWN.");
        RemoteRuleMetrics.downtime(ruleId, serviceConfiguration.getDownMilliseconds());
        RemoteRuleMetrics.up(ruleId, false);
      }
      result = fallbackResults(req);
      return result.getMatches();
    });
  }

  @Override
  public String getId() {
    return serviceConfiguration.getRuleId();
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    FutureTask<List<RuleMatch>> task = run(Collections.singletonList(sentence), annotatedText);
    task.run();
    try {
      return task.get().toArray(new RuleMatch[0]);
    } catch (InterruptedException | ExecutionException e) {
      logger.warn("Fetching results for remote rule " + getId() + " failed.", e);
      return new RuleMatch[0];
    }
  }

}
