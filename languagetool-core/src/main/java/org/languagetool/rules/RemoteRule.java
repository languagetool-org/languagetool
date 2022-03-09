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

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.CircuitBreakers;
import org.languagetool.tools.LtThreadPoolFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * @since 4.9
 */
public abstract class RemoteRule extends Rule {
  
  private static final Logger logger = LoggerFactory.getLogger(RemoteRule.class);

  protected static final List<Runnable> shutdownRoutines = new LinkedList<>();
  protected static final ConcurrentMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();

  protected final RemoteRuleConfig serviceConfiguration;
  protected final boolean inputLogging;
  protected final boolean filterMatches;
  protected final boolean fixOffsets;
  protected final boolean whitespaceNormalisation; // implemented only in GRPCRule for now
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
    whitespaceNormalisation = Boolean.parseBoolean(serviceConfiguration.getOptions().getOrDefault("whitespaceNormalisation", "true"));
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

  protected CircuitBreaker createCircuitBreaker(String id) {
    CircuitBreakerConfig.SlidingWindowType type;
    RemoteRuleConfig c = serviceConfiguration;
    try {
      type = CircuitBreakerConfig.SlidingWindowType.valueOf(serviceConfiguration.getSlidingWindowType());
    } catch (IllegalArgumentException e) {
      type = CircuitBreakerConfig.SlidingWindowType.COUNT_BASED;
      logger.warn("Couldn't parse slidingWindowType value '{}' for rule '{}', use one of {}; defaulting to '{}'", serviceConfiguration.getSlidingWindowType(), id, Arrays.asList(CircuitBreakerConfig.SlidingWindowType.values()), type);
    }

    CircuitBreakerConfig config = CircuitBreakerConfig
      .custom()
      .failureRateThreshold(c.getFailureRateThreshold())
      .slidingWindow(
        c.getSlidingWindowSize(), c.getMinimumNumberOfCalls(), type)
      .waitDurationInOpenState(Duration.ofMillis(Math.max(1, c.getDownMilliseconds())))
      .enableAutomaticTransitionFromOpenToHalfOpen()
      .build();
    return CircuitBreakers.registry().circuitBreaker("remote-rule-" + id, config);
  }

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
      long characters = sentences.stream().mapToInt(sentence -> sentence.getText().length()).sum();
      long timeout = getTimeout(characters);
      RemoteRequest req = prepareRequest(sentences, textSessionId);
      RemoteRuleResult result;

      result = executeRequest(req, timeout).call();

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
    });
  }

  public long getTimeout(long characters) {
    long timeout = serviceConfiguration.getBaseTimeoutMilliseconds() +
      Math.round(characters * serviceConfiguration.getTimeoutPerCharacterMilliseconds());
    return timeout;
  }

  public CircuitBreaker circuitBreaker() {
    return circuitBreakers.computeIfAbsent(getId(), this::createCircuitBreaker);
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
          if (suggestedReplacements.isEmpty()) {
            continue;
          }
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
    Optional<ThreadPoolExecutor> executor = LtThreadPoolFactory.getFixedThreadPoolExecutor(LtThreadPoolFactory.REMOTE_RULE_EXECUTING_POOL);
    try {
      long timeout = getTimeout(sentence.getText().length());
      if (executor.isPresent()) {
        executor.get().submit(task);
      } else {
        task.run();
      }
      RemoteRuleResult result = task.get(timeout, TimeUnit.MILLISECONDS);
      return result.getMatches().toArray(RuleMatch.EMPTY_ARRAY);
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
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
