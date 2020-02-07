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
import org.languagetool.languagemodel.bert.RemoteLanguageModel;
import org.languagetool.languagemodel.bert.grpc.BertLmProto;

import javax.net.ssl.SSLException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * reorder suggestions from another rule using BERT as a LM
 */
public class BERTSuggestionRanking extends RemoteRule {

  private final int suggestionLimit = 10;

  private final Rule wrappedRule;
  private final RemoteLanguageModel model;
  protected Stream<String> stream;

  public BERTSuggestionRanking(Rule rule, RemoteRuleConfig config) {
    super(rule.messages, config);
    this.wrappedRule = rule;
    String host = serviceConfiguration.getUrl();
    int port = serviceConfiguration.getPort();
    boolean ssl = Boolean.parseBoolean(serviceConfiguration.getOptions().getOrDefault("secure", "false"));
    String key = serviceConfiguration.getOptions().get("clientKey");
    String cert = serviceConfiguration.getOptions().get("clientCertificate");
    String ca = serviceConfiguration.getOptions().get("rootCertificate");

    // mb just log error and continue without reranking
    try {
      this.model = new RemoteLanguageModel(host, port, ssl, key, cert, ca);
    } catch (SSLException e) {
      throw new RuntimeException(String.format(
        "Could not connect to BERT service at %s for suggestion reranking", serviceConfiguration), e);
    }
  }

  @Override
  protected Callable<RemoteRuleResult> fetchMatches(List<AnalyzedSentence> sentences) {
    return () -> {
      List<RuleMatch> matches = new LinkedList<>();
      for (AnalyzedSentence sentence : sentences) {
        Collections.addAll(matches, wrappedRule.match(sentence));
      }
      for (RuleMatch match : matches) {
        List<String> suggestions = match.getSuggestedReplacements();
        if (suggestions != null && suggestions.size() > 1) {
          if (suggestionLimit > 0) {
            suggestions = suggestions.subList(0, Math.min(suggestions.size(), suggestionLimit));
          }
          Future<BertLmProto.BertLmResponse> response = model.score(
            match.getSentence(), match.getFromPos(), match.getToPos(), suggestions);
          // TODO timeout, return original ordering
          List<Double> scores = RemoteLanguageModel.scores(response.get());
          Comparator<Pair<String, Double>> suggestionOrdering = Comparator.comparing(Pair::getRight);
          suggestionOrdering = suggestionOrdering.reversed();
          List<String> ranked = Streams.zip(suggestions.stream(), scores.stream(), Pair::of)
            .sorted(suggestionOrdering)
            .map(Pair::getLeft)
            .collect(Collectors.toList());
          System.out.printf("Reordered from %s to %s%n", suggestions, ranked);
          match.setSuggestedReplacements(ranked);
        }
      }
      return new RemoteRuleResult(true, matches);
    };
  }

  @Override
  public String getId() {
    return "BERT";
  }

  @Override
  public String getDescription() {
    return "Suggestion reordering based on the BERT model";
  }
}
