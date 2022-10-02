/* LanguageTool, a natural language style checker 
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tools.StringTools;

public class FindSuggestionsEsFilter extends FindSuggestionsFilter {

  // es + unknown -> és + noun/adj | es + verb 3rd person

  Pattern pApostropheNeeded = Pattern.compile("h?[aeiouàèéíòóú].*", Pattern.CASE_INSENSITIVE);

  public FindSuggestionsEsFilter() throws IOException {
    // lazy init
    if (speller == null) {
      if (JLanguageTool.getDataBroker().resourceExists(DICT_FILENAME)) {
        speller = new MorfologikSpeller(DICT_FILENAME);
      }
    }
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {

    List<String> replacements = new ArrayList<>();
    int posWord = 0;
    while (posWord < patternTokens.length
        && (patternTokens[posWord].getStartPos() < match.getFromPos() || patternTokens[posWord].isSentenceStart())) {
      posWord++;
    }
    posWord++;
    AnalyzedTokenReadings atrWord = patternTokens[posWord];
    List<String> suggestions = getSpellingSuggestions(atrWord);
    boolean usedEsAccent = false;
    boolean usedEs = false;
    if (suggestions.size() > 0) {
      for (String suggestion : suggestions) {
        // TODO: do not tag capitalized words with tags for lower case
        List<AnalyzedTokenReadings> analyzedSuggestions = getTagger()
            .tag(Collections.singletonList(cleanSuggestion(suggestion)));
        for (AnalyzedTokenReadings analyzedSuggestion : analyzedSuggestions) {
          if (replacements.size() >= 2 * MAX_SUGGESTIONS) {
            break;
          }
          if (analyzedSuggestion.matchesPosTagRegex("NP.*|NC.[SN].*|A...[SN].|V.P..S..|V.[NG].*|RG|PX..S...")) {
            replacements.add("és " + analyzedSuggestion.getToken());
            usedEsAccent = true;
          }
          if (analyzedSuggestion.matchesPosTagRegex("V...3.*")) {
            Matcher m = pApostropheNeeded.matcher(analyzedSuggestion.getToken());
            if (!m.matches()) {
              replacements.add("es " + analyzedSuggestion.getToken().toLowerCase());
              usedEs = true;
            }
          }
        }
      }
    }
    if (replacements.isEmpty()) {
      return null;
    }

    List<String> definitiveReplacements = new ArrayList<>();
    String firstCh = patternTokens[posWord - 1].getToken().substring(0, 1);
    if (firstCh.toUpperCase().equals(firstCh)) {
      for (String r : replacements) {
        definitiveReplacements.add(StringTools.uppercaseFirstChar(r));
      }
    } else {
      definitiveReplacements.addAll(replacements);
    }
    boolean isFirstEsAccent = patternTokens[posWord - 1].getToken().equalsIgnoreCase("és");
    String message = match.getMessage();
    
    
    if (isFirstEsAccent && usedEsAccent && !usedEs) {
      // show just the spelling rule;
      return null;
    }
//    if (!isFirstEsAccent && !usedEsAccent && usedEs) {
//      // show just the spelling rule;
//      return null;
//    }  
    if (usedEsAccent) {
      message = message + " \"És\" (del v. 'ser') s'escriu amb accent.";
    }
    if (usedEs) {
      message = message + " \"Es\" (pronom) acompanya un verb en tercera persona.";
    }
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
        message, match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(definitiveReplacements);
    return ruleMatch;
  }

}
