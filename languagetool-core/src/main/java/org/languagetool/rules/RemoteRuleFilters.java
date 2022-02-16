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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Streams;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.broker.ResourceDataBroker;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * rules in remote-rule-filter.xml, same syntax as pattern rules
 * used as filters for results from matches provided by remote rule
 * i.e. if there is a match with equal match span from remote rule and a corresponding filter (i.e. with the same ID as the match)
 * the match is discarded
 * filtering uses the IDs from the matches, so dynamically created rules using different IDs will work
 */
public final class RemoteRuleFilters {
  
  public static final String RULE_FILE = "remote-rule-filters.xml";

  private static final LoadingCache<Language, Map<String, List<AbstractPatternRule>>> rules =
    CacheBuilder.newBuilder()
      .build(CacheLoader.from(RemoteRuleFilters::load));

  private RemoteRuleFilters() {
  }


  public static List<RuleMatch> filterMatches(@NotNull Language lang, @NotNull AnalyzedSentence sentence, @NotNull List<RuleMatch> matches) throws ExecutionException, IOException {
    if (matches.isEmpty()) {
      return matches;
    }
    // load all relevant filters for given matches
    Set<String> matchIds = matches.stream().map(m -> m.getRule().getId()).collect(Collectors.toSet());
    List<AbstractPatternRule> filters = rules.get(lang).entrySet().stream()
      .filter(e -> matchIds.stream().anyMatch(id -> id.matches(e.getKey())))
      .flatMap(e -> e.getValue().stream())
      .collect(Collectors.toList());

    // prepare for lookup of matches
    Map<MatchPosition, Set<AbstractPatternRule>> filterRulesByPosition = new HashMap<>();
    for (AbstractPatternRule rule : filters) {
      RuleMatch[] filterMatches = rule.match(sentence);
      for (RuleMatch match : filterMatches) {
        MatchPosition pos = new MatchPosition(match.getFromPos(), match.getToPos());
        filterRulesByPosition.computeIfAbsent(pos, k -> new HashSet<>()).add(rule);
      }
    }

    List<RuleMatch> filteredMatches = matches.stream()
      .filter(match -> {
        MatchPosition pos = new MatchPosition(match.getFromPos(), match.getToPos());
        // is there a filter match with the right ID at this position?
        boolean matched = filterRulesByPosition.getOrDefault(pos, Collections.emptySet())
          .stream().anyMatch(rule -> match.getRule().getId().matches(rule.getId()));
        return !matched;
      })
      .collect(Collectors.toList());
    return filteredMatches;
  }


  static class ExpectedMatches {
    public String sentence;
    public List<ExpectedMatch> matches;
  }

  static class ExpectedMatch {
    public int offset;
    public int length;
    public String rule_id;
  }

  static class ExpectedRule extends Rule{
    private final String id;
    public ExpectedRule(String id)  {
      this.id = id;
    }
    @Override
    public String getId() {
      return id;
    }

    @Override
    public String getDescription() {
      return null;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      throw new IllegalStateException();
    }
  }

  /**
   * Print matches remaining after filtering; arguments are language code and JSON file with matches
   * e.g. java [...] org.languagetool.rules.RemoteRuleFilters en matches.json
   */
  public static void main(String[] args) throws Exception {
    String langCode = args[0];
    String matchesFile = args[1];
    Language lang = Languages.getLanguageForShortCode(langCode);
    List<AbstractPatternRule> rules = RemoteRuleFilters.load(lang)
      .values().stream().flatMap(Collection::stream).collect(Collectors.toList());
    Stream<String> lines = Files.lines(Paths.get(matchesFile), Charset.forName("UTF-8"));
    ObjectMapper mapper = new ObjectMapper();
    JLanguageTool lt = new JLanguageTool(lang);

    Map<String, List<AbstractMap.SimpleEntry<Boolean, RuleMatch>>> result = lines.parallel()
      .map(s -> {
        try {
          return mapper.readValue(s, ExpectedMatches.class);
        } catch (JsonProcessingException e) {
          throw new RuntimeException(e);
        }
      })
      .flatMap(matches -> {
        try {
          AnalyzedSentence s = lt.getAnalyzedSentence(matches.sentence);
          List<RuleMatch> ruleMatches = matches.matches.stream().map(
            m -> new RuleMatch(new ExpectedRule(m.rule_id), s, m.offset, m.offset + m.length, "")
          ).collect(Collectors.toList());
          List<RuleMatch> remaining = RemoteRuleFilters.filterMatches(lang, s, ruleMatches);
          ruleMatches.removeAll(remaining);
          return Streams.concat(
            remaining.stream().map(m -> new AbstractMap.SimpleEntry<>(true, m)),
            ruleMatches.stream().map(m -> new AbstractMap.SimpleEntry<>(false, m)));
        } catch (IOException | ExecutionException e) {
          throw new RuntimeException(e);
        }
      })
      .collect(Collectors.groupingBy(key ->
        (key.getKey() ? "Remaining" : "Removed" ) + " - " + key.getValue().getRule().getId()));

    result.forEach((section, matches) -> {
        System.out.println(section);
        System.out.println("---");
        for (Map.Entry<Boolean, RuleMatch> entry : matches) {
          RuleMatch match = entry.getValue();
          String s = match.getSentence().getText();
          String marked = s.substring(0, match.getFromPos()) + "<marker>" +
            s.substring(match.getFromPos(), match.getToPos()) + "</marker>" + s.substring(match.getToPos());
          System.out.println(marked);
        }
        System.out.println("---");
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
      // we don't support language variants in AI rules / remote rule filters at the moment;
      // this is another kind of variant, treat it as German
      if (lang.getShortCode().equals("de-DE-x-simple-language")) {
        lang = Languages.getLanguageForShortCode("de-DE");
      }
      return lang.getShortCode() + "/" + RULE_FILE;
  }
}
