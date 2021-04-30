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
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.markup.AnnotatedText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @since 4.9
 */
public abstract class RemoteRule extends Rule {
  
  private static final Logger logger = LoggerFactory.getLogger(RemoteRule.class);

  /* needs to be shared between rule instances because new instances may be created and discarded often
     needs to be a map because 'static' and inheritance don't play nice in Java */
  private static final ConcurrentMap<String, Long> lastFailure = new ConcurrentHashMap<>();
  private static final ConcurrentMap<String, Long> timeoutIntervalStart = new ConcurrentHashMap<>();
  private static final ConcurrentMap<String, AtomicInteger> consecutiveFailures = new ConcurrentHashMap<>();
  private static final ConcurrentMap<String, AtomicLong> timeoutTotal = new ConcurrentHashMap<>();
  private static final ConcurrentMap<String, ConcurrentLinkedQueue<Future>> runningTasks = new ConcurrentHashMap<>();
  private static final ThreadFactory threadFactory = new ThreadFactoryBuilder()
    .setNameFormat("remote-rule-pool-%d").setDaemon(true).build();

  protected static final List<Runnable> shutdownRoutines = new LinkedList<>();

  // needed to run callables with timeout
  static final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);

  protected final RemoteRuleConfig serviceConfiguration;
  protected final boolean inputLogging;
  private AnnotatedText annotatedText;
  protected final boolean filterMatches;
  protected final Language ruleLanguage;

  public RemoteRule(Language language, ResourceBundle messages, RemoteRuleConfig config, boolean inputLogging, @Nullable String ruleId) {
    super(messages);
    serviceConfiguration = config;
    this.ruleLanguage = language;
    this.inputLogging = inputLogging;
    if (ruleId == null) { // allow both providing rule ID in constructor or overriding getId
      ruleId = getId();
    }
    filterMatches = Boolean.parseBoolean(serviceConfiguration.getOptions().getOrDefault("filterMatches", "false"));
    lastFailure.putIfAbsent(ruleId, 0L);
    timeoutIntervalStart.putIfAbsent(ruleId, 0L);
    timeoutTotal.putIfAbsent(ruleId, new AtomicLong());
    consecutiveFailures.putIfAbsent(ruleId, new AtomicInteger());
    runningTasks.putIfAbsent(ruleId, new ConcurrentLinkedQueue<>());
  }

  public RemoteRule(Language language, ResourceBundle messages, RemoteRuleConfig config, boolean inputLogging) {
    this(language, messages, config, inputLogging, null);
  }

  public static void shutdown() {
    shutdownRoutines.forEach(Runnable::run);
  }

  public FutureTask<RemoteRuleResult> run(List<AnalyzedSentence> sentences) {
    return run(sentences, null);
  }

  protected class RemoteRequest {}

  /**
   * run local preprocessing steps (or just store sentences)
   * @param sentences text to process
   * @param textSessionId session ID for caching, partial rollout, A/B testing
   * @return parameter for executeRequest/fallbackResults
   */
  protected abstract RemoteRequest prepareRequest(List<AnalyzedSentence> sentences, @Nullable Long textSessionId);

  /**
   * @param request returned by prepareRequest
   * @param timeoutMilliseconds timeout for this operation, &lt;=0 -&gt; unlimited
   * @return callable that sends request, parses and returns result for this remote rule
   * @throws TimeoutException if timeout was exceeded
   */
  protected abstract Callable<RemoteRuleResult> executeRequest(RemoteRequest request, long timeoutMilliseconds) throws TimeoutException;

  /**
   * fallback if executeRequest times out or throws an error
   * @param request returned by prepareRequest
   * @return local results for this rule
   */
  protected abstract RemoteRuleResult fallbackResults(RemoteRequest request);

  /**
   * @param sentences text to check
   * @param textSessionId ID for texts, should stay constant for a user session; used for A/B tests of experimental rules
   * @return Future with result
   */
  public FutureTask<RemoteRuleResult> run(List<AnalyzedSentence> sentences, @Nullable Long textSessionId) {
    if (sentences.isEmpty()) {
      return new FutureTask<>(() -> new RemoteRuleResult(false, true, Collections.emptyList(), sentences));
    }
    return new FutureTask<>(() -> {
      long startTime = System.nanoTime();
      long characters = sentences.stream().mapToInt(sentence -> sentence.getText().length()).sum();
      String ruleId = getId();
      RemoteRequest req = prepareRequest(sentences, textSessionId);
      RemoteRuleResult result;

      if (consecutiveFailures.get(ruleId).get() >= serviceConfiguration.getFall()) {
        long failureInterval = System.currentTimeMillis() - lastFailure.get(ruleId);
        if (failureInterval < serviceConfiguration.getDownMilliseconds()) {
          RemoteRuleMetrics.request(ruleId, 0, 0, characters, RemoteRuleMetrics.RequestResult.DOWN);
          result = fallbackResults(req);
          return result;
        }
      }
      if (System.nanoTime() - timeoutIntervalStart.get(ruleId) >
          TimeUnit.MILLISECONDS.toNanos(serviceConfiguration.getTimeoutLimitIntervalMilliseconds())) {
        System.out.printf("Resetting timeoutTotal; was %d%n", timeoutTotal.get(ruleId).intValue());
        timeoutTotal.get(ruleId).set(0L);
        timeoutIntervalStart.put(ruleId, System.nanoTime());
      } else if (serviceConfiguration.getTimeoutLimitTotalMilliseconds() > 0L &&
                 timeoutTotal.get(ruleId).get() > serviceConfiguration.getTimeoutLimitTotalMilliseconds()) {
        System.out.printf("Down because of timeoutTotal; was %d%n", timeoutTotal.get(ruleId).intValue());
        RemoteRuleMetrics.request(ruleId, 0, 0, characters, RemoteRuleMetrics.RequestResult.DOWN);
        result = fallbackResults(req);
        return result;
      }
      RemoteRuleMetrics.up(ruleId, true);

      for (int i = 0; i <= serviceConfiguration.getMaxRetries(); i++) {
        long timeout = serviceConfiguration.getBaseTimeoutMilliseconds() +
          Math.round(characters * serviceConfiguration.getTimeoutPerCharacterMilliseconds());
        Callable<RemoteRuleResult> task = executeRequest(req, timeout);
        Future<RemoteRuleResult> future = null;
        try {
          future = executor.submit(task);
          runningTasks.get(ruleId).add(future);
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

          if (filterMatches) {
            List<RuleMatch> filteredMatches = new ArrayList<>();
            for (AnalyzedSentence sentence : sentences) {
              List<RuleMatch> sentenceMatches = result.matchesForSentence(sentence);
              List<RuleMatch> filteredSentenceMatches = RemoteRuleFilters.filterMatches(
                ruleLanguage, sentence, sentenceMatches);
              filteredMatches.addAll(filteredSentenceMatches);
            }
            result = new RemoteRuleResult(result.isRemote(), result.isSuccess(), filteredMatches, sentences);
          }

          return result;
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
          RemoteRuleMetrics.RequestResult status;
          if (e instanceof TimeoutException || e instanceof InterruptedException ||
            (e.getCause() != null && e.getCause() instanceof TimeoutException)) {
            status = RemoteRuleMetrics.RequestResult.TIMEOUT;
            logger.warn("Timed out while fetching results for remote rule " + ruleId + ", tried " + (i + 1) + " times, timeout: " + timeout + "ms" , e);
            timeoutTotal.get(ruleId).addAndGet(timeout);
          } else {
            status = RemoteRuleMetrics.RequestResult.ERROR;
            logger.warn("Error while fetching results for remote rule " + ruleId + ", tried " + (i + 1) + " times, timeout: " + timeout + "ms" , e);
          }

          RemoteRuleMetrics.request(ruleId, i, System.nanoTime() - startTime, characters, status);
        } finally {
          if (future != null) {
            future.cancel(true);
            runningTasks.get(ruleId).remove(future);
          }
        }
      }
      RemoteRuleMetrics.failures(ruleId, consecutiveFailures.get(ruleId).incrementAndGet());
      logger.warn("Fetching results for remote rule " + ruleId + " failed.");
      if (consecutiveFailures.get(ruleId).get() >= serviceConfiguration.getFall() ||
          serviceConfiguration.getTimeoutLimitTotalMilliseconds() > 0 &&
          timeoutTotal.get(ruleId).get() > serviceConfiguration.getTimeoutLimitTotalMilliseconds()) {
        lastFailure.put(ruleId, System.currentTimeMillis());
        logger.warn("Remote rule " + ruleId + " marked as DOWN.");
        RemoteRuleMetrics.downtime(ruleId, serviceConfiguration.getDownMilliseconds());
        RemoteRuleMetrics.up(ruleId, false);
        System.out.printf("Aborting %d tasks.%n", runningTasks.get(ruleId).size());
        runningTasks.get(ruleId).forEach(task -> task.cancel(true));
      }
      result = fallbackResults(req);
      return result;
    });
  }

  @Override
  public String getId() {
    return serviceConfiguration.getRuleId();
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    FutureTask<RemoteRuleResult> task = run(Collections.singletonList(sentence));
    task.run();
    try {
      return task.get().getMatches().toArray(new RuleMatch[0]);
    } catch (InterruptedException | ExecutionException e) {
      logger.warn("Fetching results for remote rule " + getId() + " failed.", e);
      return new RuleMatch[0];
    }
  }

  public RemoteRuleConfig getServiceConfiguration() {
    return serviceConfiguration;
  }


}
