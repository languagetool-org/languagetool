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

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * rules in remote-rule-filter.xml, same syntax as pattern rules
 * used as filters for results from matches provided by remote rule
 * i.e. if there is a match with equal match span from remote rule and a corresponding filter (i.e. with the same ID as the match)
 * the match is discarded
 * filtering uses the IDs from the matches, so dynamically created rules using different IDs will work
 */
public class RemoteRuleFilters {
  public static final String RULE_FILE = "remote-rule-filters.xml";

  private static final LoadingCache<Language, Map<String, List<AbstractPatternRule>>> rules =
    CacheBuilder.newBuilder()
      .build(CacheLoader.from(RemoteRuleFilters::load));


  public static final List<RuleMatch> filterMatches(@NotNull Language lang, @NotNull AnalyzedSentence sentence, @NotNull List<RuleMatch> matches) throws ExecutionException, IOException {
    if (matches.isEmpty()) {
      return matches;
    }
    // load all relevant filters for given matches
    Set<String> matchIds = matches.stream().map(m -> m.getRule().getId()).collect(Collectors.toSet());
    List<AbstractPatternRule> filters = rules.get(lang).entrySet()
      .stream().filter(e -> matchIds.contains(e.getKey())).flatMap(e -> e.getValue().stream()).collect(Collectors.toList());

    // prepare for lookup of matches
    Map<MatchPosition, Set<AbstractPatternRule>> filterRulesByPosition = new HashMap<>();
    for (AbstractPatternRule rule : filters) {
      RuleMatch[] filterMatches = rule.match(sentence);
      for (RuleMatch match : filterMatches) {
        MatchPosition pos = new MatchPosition(match.getFromPos(), match.getToPos());
        filterRulesByPosition.computeIfAbsent(pos, k -> new HashSet<>()).add(rule);
      }
    }

    List<RuleMatch>  filteredMatches = matches.stream()
      .filter(match -> {
        MatchPosition pos = new MatchPosition(match.getFromPos(), match.getToPos());
        // is there a filter match with the right ID at this position?
        boolean matched = filterRulesByPosition.getOrDefault(pos, Collections.emptySet())
          .stream().anyMatch(rule -> rule.getId().equals(match.getRule().getId()));
        if (matched) {
          System.out.println("Removing match " + match + " - matched filter");
        }
        return !matched;
      })
      .collect(Collectors.toList());
    return filteredMatches;
  }


  public static void main(String[] args) throws ExecutionException {
    String langCode;
    if (args.length > 0) {
      langCode = args[0];
    } else {
      langCode = "en";
    }
    Language lang = Languages.getLanguageForShortCode(langCode);
    rules.get(lang).forEach((id, rules) -> {
      System.out.println("=== " + id + " ===");
      rules.forEach(System.out::println);
      System.out.println();
    });
  }

  static Map<String, List<AbstractPatternRule>> load(Language lang) {
    JLanguageTool lt = new JLanguageTool(lang);
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    String filename = dataBroker.getRulesDir() + "/" + getFilename(lang);
    try {
      List<AbstractPatternRule> allRules = lt.loadPatternRules(filename);
      Map<String, List<AbstractPatternRule>> rules = new HashMap<>();
      for (AbstractPatternRule rule : allRules) {
        rules.computeIfAbsent(rule.getId(), k -> new ArrayList<>()).add(rule);
      }
      return rules;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @NotNull
  static String getFilename(Language lang) {
      return lang.getShortCode() + "/" + RULE_FILE;
  }
}
