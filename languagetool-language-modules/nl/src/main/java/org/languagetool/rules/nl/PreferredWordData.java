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
package org.languagetool.rules.nl;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternTokenBuilder;

/**
 * Less to more common words loaded from CSV.
 * @since 4.1
 */
class PreferredWordData {

  private final List<PreferredWordRuleWithSuggestion> spellingRules = new ArrayList<>();

  PreferredWordData(String ruleDesc, String filePath, String ruleId) {
    Language dutch = Languages.getLanguageForShortCode("nl");
    String message = "Voor dit woord is een gebruikelijker alternatief.";
    String shortMessage = "Gebruikelijker woord";
    List<String> lines = JLanguageTool.getDataBroker().getFromResourceDirAsLines(filePath);
    for (String line : lines) {
      if (line.startsWith("#")) {
        continue;
      }
      String[] parts = line.split(";");
      if (parts.length != 2) {
        throw new RuntimeException("Unexpected format in file " + filePath + ": " + line);
      }
      String oldWord = parts[0];
      String newWord = parts[1];
      List<PatternToken> patternTokens = getTokens(oldWord);
      PatternRule rule = new PatternRule(ruleId + "_INTERNAL", dutch, patternTokens, ruleDesc, message, shortMessage);
      spellingRules.add(new PreferredWordRuleWithSuggestion(rule, oldWord, newWord));
    }
  }

  @NotNull
  private List<PatternToken> getTokens(String oldWord) {
    PatternTokenBuilder builder = new PatternTokenBuilder();
    String[] newWordTokens = oldWord.split(" ");
    List<PatternToken> patternTokens = new ArrayList<>();
    for (String part : newWordTokens) {
      patternTokens.add(builder.csToken(part).build());
    }
    return patternTokens;
  }

  public List<PreferredWordRuleWithSuggestion> get() {
    return spellingRules;
  }

}
