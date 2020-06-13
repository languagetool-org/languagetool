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
import org.languagetool.markup.AnnotatedText;
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

  public BERTSuggestionRanking(Rule rule, RemoteRuleConfig config, UserConfig userConfig) {
    super(rule.messages, config);
    this.wrappedRule = rule;
    super.setCategory(wrappedRule.getCategory());
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

  @Override
  protected RemoteRequest prepareRequest(List<AnalyzedSentence> sentences, AnnotatedText annotatedText) {
    List<RuleMatch> matches = new LinkedList<>();
    List<RemoteLanguageModel.Request> requests = new LinkedList<>();
    try {
      int offset = 0;
      for (AnalyzedSentence sentence : sentences) {
        RuleMatch[] sentenceMatches = wrappedRule.match(sentence);
        for (RuleMatch match : sentenceMatches) {
          match.setSuggestedReplacementObjects(prepareSuggestions(match.getSuggestedReplacementObjects()));
          // build request before correcting offset, as we send only sentence as text
          requests.add(buildRequest(match));
          int fromPos = annotatedText.getOriginalTextPositionFor(match.getFromPos() + offset, false);
          int toPos = annotatedText.getOriginalTextPositionFor(match.getToPos() + offset - 1, true) + 1;
          match.setOffsetPosition(fromPos, toPos);
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
        // put curated at the top, then compare probabilities
        Comparator<Pair<SuggestedReplacement, Double>> suggestionOrdering = (a, b) -> {
          if (a.getKey().getType() != b.getKey().getType()) {
            if (a.getKey().getType() == SuggestedReplacement.SuggestionType.Curated) {
              return 1;
            } else if (b.getKey().getType() == SuggestedReplacement.SuggestionType.Curated) {
              return -1;
            } else {
              return a.getRight().compareTo(b.getRight());
            }
          } else {
            return a.getRight().compareTo(b.getRight());
          }
        };
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
