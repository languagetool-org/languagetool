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

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.LtThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

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

  protected static final List<Runnable> shutdownRoutines = new LinkedList<>();

  // needed to run callables with timeout
  static final ExecutorService executor = LtThreadPoolFactory.createFixedThreadPoolExecutor(
    "remote-rule-thread",
    RemoteRuleConfig.getRemoteRuleCount(),
    RemoteRuleConfig.getRemoteRuleCount() * 4,
    true, (thread, throwable) -> {
      logger.error("Thread: " + thread.getName() + " failed with: " + throwable.getMessage());
    },
    false);

  protected final RemoteRuleConfig serviceConfiguration;
  protected final boolean inputLogging;
  protected final boolean filterMatches;
  protected final boolean fixOffsets;
  protected final Language ruleLanguage;
  protected final JLanguageTool lt;
  protected final Pattern suppressMisspelledMatch;
  protected final Pattern suppressMisspelledSuggestions;

  public RemoteRule(Language language, ResourceBundle messages, RemoteRuleConfig config, boolean inputLogging, @Nullable String ruleId) {
    super(messages);
    serviceConfiguration = config;
    this.ruleLanguage = language;
    this.lt = new JLanguageTool(ruleLanguage);
    this.inputLogging = inputLogging;
    if (ruleId == null) { // allow both providing rule ID in constructor or overriding getId
      ruleId = getId();
    }
    filterMatches = Boolean.parseBoolean(serviceConfiguration.getOptions().getOrDefault("filterMatches", "false"));
    fixOffsets = Boolean.parseBoolean(serviceConfiguration.getOptions().getOrDefault("fixOffsets", "true"));
    try {
      if (serviceConfiguration.getOptions().containsKey("suppressMisspelledMatch")) {
        suppressMisspelledMatch = Pattern.compile(serviceConfiguration.getOptions().get("suppressMisspelledMatch"));
      } else {
        suppressMisspelledMatch = null;
      }
    } catch(PatternSyntaxException e) {
      throw new IllegalArgumentException("suppressMisspelledMatch must be a valid regex", e);
    }
    try {
      if (serviceConfiguration.getOptions().containsKey("suppressMisspelledSuggestions")) {
        suppressMisspelledSuggestions = Pattern.compile(serviceConfiguration.getOptions().get("suppressMisspelledSuggestions"));
      } else {
        suppressMisspelledSuggestions = null;
      }
    } catch(PatternSyntaxException e) {
      throw new IllegalArgumentException("suppressMisspelledSuggestions must be a valid regex", e);
    }
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

  protected static class RemoteRequest {}

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
    Map<String, String> context = MDC.getCopyOfContextMap();
    return new FutureTask<>(() -> {
      MDC.clear();
      if (context != null) {
        MDC.setContextMap(context);
      }
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
        //System.out.printf("Resetting timeoutTotal; was %d%n", timeoutTotal.get(ruleId).intValue());
        timeoutTotal.get(ruleId).set(0L);
        timeoutIntervalStart.put(ruleId, System.nanoTime());
      } else if (serviceConfiguration.getTimeoutLimitTotalMilliseconds() > 0L &&
                 timeoutTotal.get(ruleId).get() > serviceConfiguration.getTimeoutLimitTotalMilliseconds()) {
        //System.out.printf("Down because of timeoutTotal; was %d%n", timeoutTotal.get(ruleId).intValue());
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

          if (fixOffsets) {
            for (AnalyzedSentence sentence : sentences) {
              List<RuleMatch> toFix = result.matchesForSentence(sentence);
              if (toFix != null) {
                fixMatchOffsets(sentence, toFix);
              }
            }
          }

          if (filterMatches) {
            List<RuleMatch> filteredMatches = new ArrayList<>();
            for (AnalyzedSentence sentence : sentences) {
              List<RuleMatch> sentenceMatches = result.matchesForSentence(sentence);
              if (sentenceMatches != null) {
                List<RuleMatch> filteredSentenceMatches = RemoteRuleFilters.filterMatches(
                  ruleLanguage, sentence, sentenceMatches);
                filteredMatches.addAll(filteredSentenceMatches);
              }
            }
            result = new RemoteRuleResult(result.isRemote(), result.isSuccess(), filteredMatches, sentences);
          }

          List<RuleMatch> filteredMatches = new ArrayList<>();
          for (AnalyzedSentence sentence : sentences) {
              List<RuleMatch> sentenceMatches = result.matchesForSentence(sentence);
              if (sentenceMatches != null) {
                List<RuleMatch> filteredSentenceMatches = suppressMisspelled(sentenceMatches);
                filteredMatches.addAll(filteredSentenceMatches);
              }
          }
          result = new RemoteRuleResult(result.isRemote(), result.isSuccess(), filteredMatches, sentences);

          return result;
        } catch (Exception e) {
          RemoteRuleMetrics.RequestResult status;
          if (e instanceof TimeoutException || e instanceof InterruptedException || e instanceof CancellationException ||
            (e.getCause() != null && e.getCause() instanceof TimeoutException)) {
            status = RemoteRuleMetrics.RequestResult.TIMEOUT;
            logger.info("Timed out while fetching results for remote rule " + ruleId + ", tried " + (i + 1) + " times, timeout: " + timeout + "ms" , e);
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
      logger.info("Fetching results for remote rule " + ruleId + " failed.");
      if (consecutiveFailures.get(ruleId).get() >= serviceConfiguration.getFall() ||
          serviceConfiguration.getTimeoutLimitTotalMilliseconds() > 0 &&
          timeoutTotal.get(ruleId).get() > serviceConfiguration.getTimeoutLimitTotalMilliseconds()) {
        lastFailure.put(ruleId, System.currentTimeMillis());
        logger.info("Remote rule " + ruleId + " marked as DOWN.");
        RemoteRuleMetrics.downtime(ruleId, serviceConfiguration.getDownMilliseconds());
        RemoteRuleMetrics.up(ruleId, false);
        logger.info("Aborting {} tasks.", runningTasks.get(ruleId).size());
        runningTasks.get(ruleId).forEach(task -> task.cancel(true));
      }
      result = fallbackResults(req);
      return result;
    });
  }

  private List<RuleMatch> suppressMisspelled(List<RuleMatch> sentenceMatches) {
    List<RuleMatch> result = new ArrayList<>();
    SpellingCheckRule speller = ruleLanguage.getDefaultSpellingRule(messages);
    Predicate<SuggestedReplacement> checkSpelling = (s) -> {
     try {
       AnalyzedSentence sentence = lt.getRawAnalyzedSentence(s.getReplacement());
       RuleMatch[] matches = speller.match(sentence);
       return matches.length == 0;
     } catch(IOException e) {
       throw new RuntimeException(e);
     }
    };
    if (speller == null) {
      if (suppressMisspelledMatch != null || suppressMisspelledSuggestions != null) {
        logger.warn("Cannot activate suppression of misspelled matches for rule {}, no spelling rule found for language {}.",
          getId(), ruleLanguage.getShortCodeWithCountryAndVariant());
      }
      return sentenceMatches;
    }

    for (RuleMatch m : sentenceMatches) {
        String id = m.getRule().getId();
        if (suppressMisspelledMatch != null && suppressMisspelledMatch.matcher(id).matches()) {
          if (!m.getSuggestedReplacementObjects().stream().allMatch(checkSpelling)) {
            continue;
          }
        }
        if (suppressMisspelledSuggestions != null && suppressMisspelledSuggestions.matcher(id).matches()) {
          List<SuggestedReplacement> suggestedReplacements = m.getSuggestedReplacementObjects().stream()
            .filter(checkSpelling).collect(Collectors.toList());
          m.setSuggestedReplacementObjects(suggestedReplacements);
        }
        result.add(m);
    }
    return result;
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
      return task.get().getMatches().toArray(RuleMatch.EMPTY_ARRAY);
    } catch (InterruptedException | ExecutionException e) {
      logger.info("Fetching results for remote rule " + getId() + " failed.", e);
      return RuleMatch.EMPTY_ARRAY;
    }
  }

  public RemoteRuleConfig getServiceConfiguration() {
    return serviceConfiguration;
  }


  /**
   *  Helper for {@link #fixMatchOffsets}
   *  lookup table, find shifted index for i at shifts[i];
   */
  static int[] computeOffsetShifts(String s) {
    int len = s.length() + 1;
    int[] offsets = new int[len];
    int shifted = 0, original = 0;

    // go from codepoint to codepoint using shifted
    // offset saved in original will correspond to Java string index shifted
    while(shifted < s.length()) {
      offsets[original] = shifted;
      shifted = s.offsetByCodePoints(shifted, 1);
      original++;
    }
    // save last shifted value if there is one remaining
    if (original < len) {
      offsets[original] = shifted;
    }
    // fill the rest of the array for exclusive toPos indices
    for (int i = original + 1; i < len; i++) {
      offsets[i] = offsets[i - 1] + 1;
    }
    return offsets;
  }

  /**
   * Adapt match positions so that results from languages that thread emojis, etc. as length 1
   * work for Java and match the normal offsets we use
   * JavaScript also behaves like Java, so most clients will expect this behavior;
   * but servers used for RemoteRules will often be written in Python (e.g. to access ML frameworks)
   *
   * based on offsetByCodePoints since codePointCount can be confusing,
   * e.g. "👪".codePointCount(0,2) == 1, but length is 2
   *
   * Java substring methods use this length (which can be &gt;1 for a single character)
   * whereas Python 3 indexing/slicing and len() in strings treat them as a single character
   * so "😁foo".length() == 5, but len("😁foo") == 4;
   * "😁foo".substring(2,5) == "foo" but "😁foo"[1:4] == 'foo'
   */
  public static void fixMatchOffsets(AnalyzedSentence sentence, List<RuleMatch> matches) {
    int[] shifts = computeOffsetShifts(sentence.getText());
    matches.forEach(m -> {
      int from = shifts[m.getFromPos()];
      int to = shifts[m.getToPos()];
      m.setOffsetPosition(from, to);
    });
  }
}
