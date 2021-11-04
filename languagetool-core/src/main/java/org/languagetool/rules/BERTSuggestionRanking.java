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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.languagemodel.bert.RemoteLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * reorder suggestions from another rule using BERT as a LM
 */
public class BERTSuggestionRanking extends RemoteRule {

  // only for RemoteRuleConfig
  public static final String RULE_ID = "BERT_SUGGESTION_RANKING";

  private static final Logger logger = LoggerFactory.getLogger(BERTSuggestionRanking.class);

  private static final LoadingCache<RemoteRuleConfig, RemoteLanguageModel> models =
    CacheBuilder.newBuilder().build(CacheLoader.from(serviceConfiguration -> {
      String host = serviceConfiguration.getUrl();
      int port = serviceConfiguration.getPort();
      boolean ssl = Boolean.parseBoolean(serviceConfiguration.getOptions().getOrDefault("secure", "false"));
      String key = serviceConfiguration.getOptions().get("clientKey");
      String cert = serviceConfiguration.getOptions().get("clientCertificate");
      String ca = serviceConfiguration.getOptions().get("rootCertificate");
      try {
        return new RemoteLanguageModel(host, port, ssl, key, cert, ca);
      } catch (SSLException e) {
        throw new RuntimeException(e);
      }
    }));

  static {
    shutdownRoutines.add(() -> models.asMap().values().forEach(RemoteLanguageModel::shutdown));
  }

  // default behavior for prepareSuggestions: limit to top n candidates
  protected int suggestionLimit = 10;
  private final RemoteLanguageModel model;
  private final Rule wrappedRule;

  public BERTSuggestionRanking(Language language, Rule rule, RemoteRuleConfig config, boolean inputLogging) {
    super(language, rule.messages, config, inputLogging, rule.getId());
    this.wrappedRule = rule;
    super.setCategory(wrappedRule.getCategory());
    synchronized (models) {
      RemoteLanguageModel model = null;
      try {
        model = models.get(serviceConfiguration);
      } catch (Exception e) {
        logger.error("Could not connect to BERT service at " + serviceConfiguration + " for suggestion reranking", e);
      }
      this.model = model;
    }
  }

  class MatchesForReordering extends RemoteRequest {
    final List<AnalyzedSentence> sentences;
    final List<RuleMatch> matches;
    final List<RemoteLanguageModel.Request> requests;
    MatchesForReordering(List<AnalyzedSentence> sentences, List<RuleMatch> matches, List<RemoteLanguageModel.Request> requests) {
      this.sentences = sentences;
      this.matches = matches;
      this.requests = requests;
    }
  }

  /**
   * transform suggestions before resorting, e.g. limit resorting to top-n candidates
   * @return transformed suggestions
   */
  protected List<SuggestedReplacement> prepareSuggestions(List<SuggestedReplacement> suggestions) {
    // include more suggestions for resorting if there are translations included as original order isn't that good
    if (suggestions.stream().anyMatch(s -> s.getType() == SuggestedReplacement.SuggestionType.Translation)) {
      suggestionLimit = 25;
    } else {
      suggestionLimit = 10;
    }
    return suggestions.subList(0, Math.min(suggestions.size(), suggestionLimit));
  }

  private static final int MIN_WORDS = 8;
  private static final double MAX_ERROR_RATE = 0.5;

  @Override
  protected RemoteRequest prepareRequest(List<AnalyzedSentence> sentences, Long textSessionId) {
    List<RuleMatch> matches = new LinkedList<>();
    int totalWords = 0;
    try {
      for (AnalyzedSentence sentence : sentences) {
        RuleMatch[] sentenceMatches = wrappedRule.match(sentence);
        Collections.addAll(matches, sentenceMatches);

        // computing many suggestions (e.g. for requests with the language incorrectly set, or with garbled input) is very expensive
        // having this as a RemoteRule circumvents the normal ErrorRateTooHighException
        // so build this logic again here
        int words = sentence.getTokensWithoutWhitespace().length;
        totalWords += words;
        if (words > MIN_WORDS && (double) sentenceMatches.length / words > MAX_ERROR_RATE) {
          for (RuleMatch m : sentenceMatches) {
            m.discardLazySuggestedReplacements();
          }
          logger.info("Skipping suggestion generation for sentence, too many matches ({} matches in {} words)",
            sentenceMatches.length, words);
        }
      }
    } catch (IOException e) {
      logger.error("Error while executing rule " + wrappedRule.getId(), e);
      return new MatchesForReordering(sentences, Collections.emptyList(), Collections.emptyList());
    }
    if (totalWords > MIN_WORDS && (double) matches.size() / totalWords > MAX_ERROR_RATE) {
      logger.info("Skipping suggestion generation for request, too many matches ({} matches in {} words)",
        matches.size(), totalWords);
      matches.forEach(RuleMatch::discardLazySuggestedReplacements);
      return new MatchesForReordering(sentences, matches, Collections.emptyList());
    }
    List<RemoteLanguageModel.Request> requests = new LinkedList<>();
    for (RuleMatch match : matches) {
      match.setSuggestedReplacementObjects(prepareSuggestions(match.getSuggestedReplacementObjects()));
      requests.add(buildRequest(match));
    }
    return new MatchesForReordering(sentences, matches, requests);
  }

  @Override
  protected RemoteRuleResult fallbackResults(RemoteRequest request) {
    MatchesForReordering req = (MatchesForReordering) request;
    return new RemoteRuleResult(false, false, req.matches, req.sentences);
  }

  @Override
  protected Callable<RemoteRuleResult> executeRequest(RemoteRequest request, long timeoutMilliseconds) throws TimeoutException {
    return () -> {
      if (model == null) {
        return fallbackResults(request);
      }
      MatchesForReordering data = (MatchesForReordering) request;
      List<RuleMatch> matches = data.matches;
      List<RemoteLanguageModel.Request> requests = data.requests;
      Streams.FunctionWithIndex<RemoteLanguageModel.Request, Long> mapIndices = (req, index) -> req != null ? index : null;
      List<Long> indices = Streams.mapWithIndex(requests.stream(), mapIndices)
        .filter(Objects::nonNull).collect(Collectors.toList());
      requests = requests.stream().filter(Objects::nonNull).collect(Collectors.toList());

      if (requests.isEmpty()) {
        return new RemoteRuleResult(false, true, matches, data.sentences);
      } else {
        List<List<Double>> results = model.batchScore(requests, timeoutMilliseconds);
        // put curated at the top, then compare probabilities
        for (int i = 0; i < indices.size(); i++) {
          List<Double> scores = results.get(i);
          String userWord = requests.get(i).text.substring(requests.get(i).start, requests.get(i).end);
          RuleMatch match = matches.get(indices.get(i).intValue());
          //RemoteLanguageModel.Request req = requests.get(i);
          //String error = req.text.substring(req.start, req.end);
          //logger.info("Scored suggestions for '{}': {} -> {}", error, match.getSuggestedReplacements(), Streams
          //  .zip(match.getSuggestedReplacementObjects().stream(), scores.stream(), Pair::of)
          //  .sorted(new CuratedAndSameCaseComparator(userWord))
          //  .map(scored -> String.format("%s (%e)", scored.getLeft().getReplacement(), scored.getRight()))
          //  .collect(Collectors.toList()));
          List<SuggestedReplacement> ranked = Streams
            .zip(match.getSuggestedReplacementObjects().stream(), scores.stream(), Pair::of)
            .sorted(new CuratedAndSameCaseComparator(userWord))
            .map(Pair::getLeft)
            .collect(Collectors.toList());
          //logger.info("Reordered correction for '{}' from {} to {}", error, req.candidates, ranked);
          match.setSuggestedReplacementObjects(ranked);
        }
        return new RemoteRuleResult(true, true, matches, data.sentences);
      }
    };
  }

  @Nullable
  private RemoteLanguageModel.Request buildRequest(RuleMatch match) {
    List<String> suggestions = match.getSuggestedReplacements();
    if (suggestions != null && suggestions.size() > 1) {
      return new RemoteLanguageModel.Request(
        match.getSentence().getText(), match.getFromPos(), match.getToPos(), suggestions);
    } else {
      return null;
    }
  }

  @Override
  public String getId() {
    // return values of wrapped rule so that enabling/disabling rules works
    return wrappedRule.getId();
  }

  @Override
  public String getDescription() {
    return wrappedRule.getDescription();
  }

  private static class CuratedAndSameCaseComparator implements Comparator<Pair<SuggestedReplacement, Double>> {
    private final String userWord;
    CuratedAndSameCaseComparator(String userWord) {
      this.userWord = userWord;
    }
    @Override
    public int compare(Pair<SuggestedReplacement, Double> a, Pair<SuggestedReplacement, Double> b) {
      //System.out.println(userWord + " -- " + b.getKey().getReplacement());
      if (a.getKey().getReplacement().equalsIgnoreCase(userWord)) {
        return -1;
      } else if (b.getKey().getReplacement().equalsIgnoreCase(userWord)) {
        return 1;
      } else if (a.getKey().getType() != b.getKey().getType()) {
        if (a.getKey().getType() == SuggestedReplacement.SuggestionType.Curated) {
          return -1;
        } else if (b.getKey().getType() == SuggestedReplacement.SuggestionType.Curated) {
          return 1;
        } else {
          return b.getRight().compareTo(a.getRight());
        }
      } else {
        return b.getRight().compareTo(a.getRight());
      }
    }
  }

}
