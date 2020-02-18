/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.de;

import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;
import org.languagetool.tagging.de.GermanTagger;

import java.io.IOException;
import java.util.*;

/**
 * Old to new spelling data and similar formats loaded form CSV.
 * @since 4.3
 */
class SpellingData {

  private final List<SpellingRuleWithSuggestions> spellingRules = new ArrayList<>();
  
  SpellingData(String ruleDesc, String filePath, String message, String shortMessage, String ruleId, ITSIssueType issueType) {
    this(ruleDesc, filePath, message, shortMessage, ruleId, issueType, false);
  }
  
  SpellingData(String ruleDesc, String filePath, String message, String shortMessage, String ruleId, ITSIssueType issueType, boolean ignoreAfterQuote) {
    Language german = Languages.getLanguageForShortCode("de");
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(filePath);
    Map<String,String> coherencyMap = new HashMap<>();
    for (String line : lines) {
      if (line.startsWith("#")) {
        continue;
      }
      String[] parts = line.split(";");
      if (parts.length < 2) {
        throw new RuntimeException("Unexpected format in file " + filePath + ": " + line);
      }
      String alternative = parts[0];
      String lookup = coherencyMap.get(parts[1]);
      if (lookup != null && lookup.equals(alternative)) {
        throw new RuntimeException("Contradictory entry in " + filePath + ": '" + alternative + "' suggests '" + lookup + "' and vice versa");
      }
      coherencyMap.put(parts[0], parts[1]);
      List<String> suggestions = new ArrayList<>(Arrays.asList(parts).subList(1, parts.length));
      List<PatternToken> patternTokens = getTokens(alternative, german);
      PatternRule rule = new PatternRule(ruleId, german, patternTokens, ruleDesc, message, shortMessage);
      rule.setLocQualityIssueType(issueType);
      spellingRules.add(new SpellingRuleWithSuggestions(rule, alternative, suggestions, ignoreAfterQuote));
    }
  }

  @NotNull
  private List<PatternToken> getTokens(String alternative, Language lang) {
    PatternTokenBuilder builder = new PatternTokenBuilder();
    String[] suggestionTokens = alternative.split(" ");
    List<PatternToken> patternTokens = new ArrayList<>();
    for (String part : suggestionTokens) {
      PatternToken token;
      if (isBaseform(alternative, lang)) {
        token = builder.csToken(part).matchInflectedForms().build();
      } else {
        token = builder.csToken(part).build();
      }
      patternTokens.add(token);
    }
    return patternTokens;
  }

  private boolean isBaseform(String term, Language lang) {
    try {
      AnalyzedTokenReadings lookup = ((GermanTagger) lang.getTagger()).lookup(term);
      if (lookup != null) {
        return lookup.hasLemma(term);
      }
      return false;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public List<SpellingRuleWithSuggestions> get() {
    return spellingRules;
  }

}
