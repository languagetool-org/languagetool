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
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.bert.RemoteLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * reorder suggestions from another rule using BERT as a LM
 */
public class BERTSuggestionRanking extends RemoteRule {
  private static final Logger logger = LoggerFactory.getLogger(BERTSuggestionRanking.class);
  public static final String RULE_ID = "BERT_SUGGESTION_RANKING";

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

  private final int suggestionLimit = 10;
  private final RemoteLanguageModel model;
  private final Rule wrappedRule;

  public BERTSuggestionRanking(Rule rule, RemoteRuleConfig config, UserConfig userConfig) {
    super(rule.messages, config);
    this.wrappedRule = rule;

    synchronized (models) {
      RemoteLanguageModel model = null;
      if (getId().equals(userConfig.getAbTest())) {
        try {
          model = models.get(serviceConfiguration);
        } catch (Exception e) {
          logger.error("Could not connect to BERT service at " + serviceConfiguration + " for suggestion reranking", e);
        }
      }
      this.model = model;
    }
  }

  class MatchesForReordering extends RemoteRequest {
    final List<RuleMatch> matches;
    final List<RemoteLanguageModel.Request> requests;
    MatchesForReordering(List<RuleMatch> matches, List<RemoteLanguageModel.Request> requests) {
      this.matches = matches;
      this.requests = requests;
    }
  }

  @Override
  protected RemoteRequest prepareRequest(List<AnalyzedSentence> sentences) {
    List<RuleMatch> matches = new LinkedList<>();
    List<RemoteLanguageModel.Request> requests = new LinkedList<>();
    try {
      int offset = 0;
      for (AnalyzedSentence sentence : sentences) {
        RuleMatch[] sentenceMatches = wrappedRule.match(sentence);
        for (RuleMatch match : sentenceMatches) {
          if (suggestionLimit > 0) {
            List<SuggestedReplacement> suggestions =  match.getSuggestedReplacementObjects();
            suggestions = suggestions.subList(0, Math.min(suggestions.size(), suggestionLimit));
            match.setSuggestedReplacementObjects(suggestions);
          }
          // build request before correcting offset, as we send only sentence as text
          requests.add(buildRequest(match));
          match.setOffsetPosition(match.getFromPos() + offset, match.getToPos() + offset);
        }
        Collections.addAll(matches, sentenceMatches);
        offset += sentence.getText().length();
      }
      return new MatchesForReordering(matches, requests);
    } catch (IOException e) {
      logger.error("Error while executing rule " + wrappedRule.getId(), e);
      return new MatchesForReordering(Collections.emptyList(), Collections.emptyList());
    }
  }

  @Override
  protected RemoteRuleResult fallbackResults(RemoteRequest request) {
    return new RemoteRuleResult(false, ((MatchesForReordering) request).matches);
  }

  @Override
  protected Callable<RemoteRuleResult> executeRequest(RemoteRequest request) {
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
        return new RemoteRuleResult(false, matches);
      } else {
        List<List<Double>> results = model.batchScore(requests);
        Comparator<Pair<SuggestedReplacement, Double>> suggestionOrdering = Comparator.comparing(Pair::getRight);
        suggestionOrdering = suggestionOrdering.reversed();

        for (int i = 0; i < indices.size(); i++) {
          List<Double> scores = results.get(i);
          RemoteLanguageModel.Request req = requests.get(i);
          RuleMatch match = matches.get(indices.get(i).intValue());
          String error = req.text.substring(req.start, req.end);
          logger.info("Scored suggestions for '{}': {} -> {}", error, match.getSuggestedReplacements(), Streams
            .zip(match.getSuggestedReplacementObjects().stream(), scores.stream(), Pair::of)
            .sorted(suggestionOrdering)
            .map(scored -> String.format("%s (%e)", scored.getLeft().getReplacement(), scored.getRight()))
            .collect(Collectors.toList()));
          List<SuggestedReplacement> ranked = Streams
            .zip(match.getSuggestedReplacementObjects().stream(), scores.stream(), Pair::of)
            .sorted(suggestionOrdering)
            .map(Pair::getLeft)
            .collect(Collectors.toList());
          //logger.info("Reordered correction for '{}' from {} to {}", error, req.candidates, ranked);
          match.setSuggestedReplacementObjects(ranked);
        }
        return new RemoteRuleResult(true, matches);
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
    return RULE_ID;
  }

  @Override
  public String getDescription() {
    return "Suggestion reordering based on the BERT model";
  }
}
