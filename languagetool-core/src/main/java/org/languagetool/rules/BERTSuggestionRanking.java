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

import com.google.common.collect.Streams;
import org.apache.commons.lang3.tuple.Pair;
import org.languagetool.AnalyzedSentence;
import org.languagetool.UserConfig;
import org.languagetool.languagemodel.bert.RemoteLanguageModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * reorder suggestions from another rule using BERT as a LM
 */
public class BERTSuggestionRanking extends RemoteRule {
  private static final Logger logger = LoggerFactory.getLogger(BERTSuggestionRanking.class);
  public static final String RULE_ID = "BERT_SUGGESTION_RANKING";

  private final int suggestionLimit = 10;

  private final Rule wrappedRule;
  private final RemoteLanguageModel model;
  protected Stream<String> stream;

  public BERTSuggestionRanking(Rule rule, RemoteRuleConfig config, UserConfig userConfig) {
    super(rule.messages, config);
    this.wrappedRule = rule;
    String host = serviceConfiguration.getUrl();
    int port = serviceConfiguration.getPort();
    boolean ssl = Boolean.parseBoolean(serviceConfiguration.getOptions().getOrDefault("secure", "false"));
    String key = serviceConfiguration.getOptions().get("clientKey");
    String cert = serviceConfiguration.getOptions().get("clientCertificate");
    String ca = serviceConfiguration.getOptions().get("rootCertificate");

    RemoteLanguageModel model = null;
    if (getId().equals(userConfig.getAbTest())) {
      try {
        model = new RemoteLanguageModel(host, port, ssl, key, cert, ca);
      } catch (Exception e) {
        logger.error("Could not connect to BERT service at " + serviceConfiguration + " for suggestion reranking", e);
      }
    }
    this.model = model;
  }

  class MatchesForReordering extends RemoteRequest {
    final List<RuleMatch> matches;
    MatchesForReordering(List<RuleMatch> matches) {
      this.matches = matches;
    }
  }

  @Override
  protected RemoteRequest prepareRequest(List<AnalyzedSentence> sentences) {
    List<RuleMatch> matches = new LinkedList<>();
    try {
      int offset = 0;
      for (AnalyzedSentence sentence : sentences) {
        RuleMatch[] sentenceMatches = wrappedRule.match(sentence);
        for (RuleMatch match : sentenceMatches) {
          match.setOffsetPosition(match.getFromPos() + offset, match.getToPos() + offset);
        }
        Collections.addAll(matches, sentenceMatches);
        offset += sentence.getText().length();
      }
      return new MatchesForReordering(matches);
    } catch (IOException e) {
      return new MatchesForReordering(Collections.emptyList());
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
      List<RuleMatch> matches = ((MatchesForReordering) request).matches;
      List<RemoteLanguageModel.Request> requests = matches.stream().map(match -> {
        List<String> suggestions = match.getSuggestedReplacements();
        if (suggestions != null && suggestions.size() > 1) {
          if (suggestionLimit > 0) {
            suggestions = suggestions.subList(0, Math.min(suggestions.size(), suggestionLimit));
          }
          return new RemoteLanguageModel.Request(
            match.getSentence().getText(), match.getFromPos(), match.getToPos(), suggestions);
        } else {
          return null;
        }
      }).collect(Collectors.toList());
      Streams.FunctionWithIndex<RemoteLanguageModel.Request, Long> mapIndices = (req, index) -> req != null ? index : null;
      List<Long> indices = Streams.mapWithIndex(requests.stream(), mapIndices)
        .filter(Objects::nonNull).collect(Collectors.toList());
      requests = requests.stream().filter(Objects::nonNull).collect(Collectors.toList());

      if (requests.isEmpty()) {
        return new RemoteRuleResult(false, matches);
      } else {
        List<List<Double>> results = model.batchScore(requests);
        Comparator<Pair<String, Double>> suggestionOrdering = Comparator.comparing(Pair::getRight);
        suggestionOrdering = suggestionOrdering.reversed();

        for (int i = 0; i < indices.size(); i++) {
          List<Double> scores = results.get(i);
          RemoteLanguageModel.Request req = requests.get(i);
          RuleMatch match = matches.get(indices.get(i).intValue());
          List<String> ranked = Streams.zip(req.candidates.stream(), scores.stream(), Pair::of)
            .sorted(suggestionOrdering)
            .map(Pair::getLeft)
            .collect(Collectors.toList());
          logger.info("Reordered from {} to {}", req.candidates, ranked);
          match.setSuggestedReplacements(ranked);
        }
        return new RemoteRuleResult(true, matches);
      }
    };
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
