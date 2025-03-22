/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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
package org.languagetool.rules.spelling.multitoken;


import org.languagetool.*;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

public class MultitokenSpellerFilter extends RuleFilter {

   /* Provide suggestions for misspelled multitoken expressions, usually proper nouns*/
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    if (Arrays.stream(patternTokens).allMatch(x -> x.isIgnoredBySpeller())) {
      return null;
    }
    String underlinedError = match.getOriginalErrorStr();
    Language lang = ((PatternRule) match.getRule()).getLanguage();
    // check the spelling for some languages in a different way
    boolean areTokensAcceptedBySpeller = false;
    if (lang.getShortCode().equals("en") || lang.getShortCode().equals("de") || lang.getShortCode().equals("pt")
      || lang.getShortCode().equals("nl")) {
      if (lang.getShortCodeWithCountryAndVariant().length()==2) {
        // needed in testing
        lang = lang.getDefaultLanguageVariant();
      }
      areTokensAcceptedBySpeller = !isMisspelled(underlinedError, lang) ;
    }
    List<String> replacements = lang.getMultitokenSpeller().getSuggestions(underlinedError, areTokensAcceptedBySpeller);
    if (replacements.isEmpty()) {
      return null;
    }
    // all upper-case suggestions
    if (underlinedError.length()>4 && StringTools.isAllUppercase(underlinedError)) {
      List<String> allupercaseReplacements = new ArrayList<>();
      for (String replacement : replacements) {
        String newReplacement = replacement.toUpperCase();
        if (!allupercaseReplacements.contains(newReplacement) && !underlinedError.equals(newReplacement)) {
          allupercaseReplacements.add(newReplacement);
        }
      }
      replacements = allupercaseReplacements;
    } else {
      // capitalize suggestion at sentence start
      int wordsStartPos = 1;
      // ignore punctuation marks at the sentence start to do the capitalization
      AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
      while (wordsStartPos<tokens.length && (StringTools.isPunctuationMark(tokens[wordsStartPos].getToken())
        || StringTools.isNotWordString((tokens[wordsStartPos].getToken())))) {
        wordsStartPos++;
      }
      if (patternTokenPos==wordsStartPos) {
        List<String> capitalizedReplacements = new ArrayList<>();
        for (String replacement : replacements) {
          String newReplacement = replacement;
          if (replacement.equals(replacement.toLowerCase())) {
            //do not capitalize iPad
            newReplacement = StringTools.uppercaseFirstChar(replacement);
          }
          if (!capitalizedReplacements.contains(newReplacement) && !underlinedError.equals(newReplacement)) {
            capitalizedReplacements.add(newReplacement);
          }
        }
        replacements = capitalizedReplacements;
      }
    }
    if (replacements.isEmpty()) {
      return null;
    }
    match.setSuggestedReplacements(replacements);
    return match;
  }

  public boolean isMisspelled(String s, Language language) throws IOException {
    SpellingCheckRule spellerRule = language.getDefaultSpellingRule();
    if (spellerRule == null) {
      return false;
    }
    List<String> tokens = language.getWordTokenizer().tokenize(s);
    for (String token : tokens) {
      if (spellerRule.isMisspelled(token)) {
        return true;
      };
    }
    return false;
  }
}
