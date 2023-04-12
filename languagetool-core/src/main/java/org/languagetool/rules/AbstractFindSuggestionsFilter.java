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
package org.languagetool.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.rules.spelling.symspell.implementation.EditDistance;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tagging.Tagger;
import org.languagetool.tools.StringTools;

public abstract class AbstractFindSuggestionsFilter extends RuleFilter {

  final protected int MAX_SUGGESTIONS = 10;

  abstract protected Tagger getTagger();

  //abstract protected MorfologikSpeller getSpeller();
  abstract protected List<String> getSpellingSuggestions(AnalyzedTokenReadings atr) throws IOException; 

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) throws IOException {
    
//    if (match.getSentence().getText().contains("La primera compren")) {
//      int ii=0;
//      ii++;
//    }

    //TODO: remove suggestions that trigger the rule again.
    // It would be needed to run again the rule with the full sentence. 
    List<String> replacements = new ArrayList<>();
    List<String> replacements2 = new ArrayList<>();
    String wordFrom = getRequired("wordFrom", arguments);
    String desiredPostag = getRequired("desiredPostag", arguments);
    String priorityPostag = getOptional("priorityPostag", arguments);
    String removeSuggestionsRegexp = getOptional("removeSuggestionsRegexp", arguments);
    // supress match if there are no suggestions
    String suppressMatch = getOptional("suppressMatch", arguments);
    boolean bSuppressMatch = false;
    if (suppressMatch != null && suppressMatch.equalsIgnoreCase("true")) {
      bSuppressMatch = true;
    }
    
    // diacriticsMode: return only changes in diacritics. If there is none, the
    // match is removed.
    String mode = getOptional("Mode", arguments);
    boolean diacriticsMode = (mode != null) && mode.equals("diacritics");
    boolean generateSuggestions = true;
    Pattern regexpPattern = null;
    Synthesizer synth = getSynthesizer();
    List<String> usedLemmas = new ArrayList<>();
    StringComparator stringComparator = new StringComparator("");

    if (wordFrom != null && desiredPostag != null) {
      int posWord = 0;
      if (wordFrom.startsWith("marker")) {
        while (posWord < patternTokens.length && (patternTokens[posWord].getStartPos() < match.getFromPos()
            || patternTokens[posWord].isSentenceStart())) {
          posWord++;
        }
        posWord++;
        if (wordFrom.length()>6) {
          wordFrom += Integer.parseInt(wordFrom.replace("marker", ""));
        }
      } else {
        posWord = Integer.parseInt(wordFrom);
      }
      if (posWord < 1 || posWord > patternTokens.length) {
        throw new IllegalArgumentException("FindSuggestionsFilter: Index out of bounds in "
            + match.getRule().getFullId() + ", wordFrom: " + posWord);
      }
      AnalyzedTokenReadings atrWord = patternTokens[posWord - 1];
      stringComparator = new StringComparator(atrWord.getToken());
      boolean isWordCapitalized = StringTools.isCapitalizedWord(atrWord.getToken());
      boolean isWordAllupper = StringTools.isAllUppercase(atrWord.getToken());

      // Check if the original token (before disambiguation) meets the requirements
      List<String> originalWord = Collections.singletonList(atrWord.getToken());
      List<AnalyzedTokenReadings> aOriginalWord = getTagger().tag(originalWord);
      for (AnalyzedTokenReadings atr : aOriginalWord) {
        if (atr.matchesPosTagRegex(desiredPostag)) {
          if (diacriticsMode) {
            return null;
          }
        }
      }

      if (generateSuggestions) {
        if (removeSuggestionsRegexp != null) {
          regexpPattern = Pattern.compile(removeSuggestionsRegexp, Pattern.UNICODE_CASE);
        }
        List<String> suggestions = getSpellingSuggestions(atrWord);
        int usedPriorityPostagPos = 0;
        if (suggestions.size() > 0) {
          for (String suggestion : suggestions) {
            // TODO: do not tag capitalized words with tags for lower case
            List<AnalyzedTokenReadings> analyzedSuggestions = getTagger().tag(Collections.singletonList(cleanSuggestion(suggestion)));
            for (AnalyzedTokenReadings analyzedSuggestion : analyzedSuggestions) {
              if (isSuggestionException(analyzedSuggestion)) {
                continue;
              }
              if (replacements.size() >= 2 * MAX_SUGGESTIONS) {
                break;
              }
              boolean used = false;
              if (!suggestion.equals(atrWord.getToken())
                  && analyzedSuggestion.matchesPosTagRegex(desiredPostag)) {
                if (!replacements.contains(suggestion)
                    && !replacements.contains(suggestion.toLowerCase())
                    && (!diacriticsMode || equalWithoutDiacritics(suggestion, atrWord.getToken()))) {
                  if (regexpPattern == null || !regexpPattern.matcher(suggestion).matches()) {
                    String replacement = suggestion;
                    if (isWordAllupper) {
                      replacement = replacement.toUpperCase();
                    }
                    if (isWordCapitalized) {
                      replacement = StringTools.uppercaseFirstChar(replacement);
                    }
                    if (priorityPostag!= null && analyzedSuggestion.matchesPosTagRegex(priorityPostag)) {
                      replacements.add(usedPriorityPostagPos, replacement);
                      usedPriorityPostagPos++;
                      used = true;
                    } else {
                      replacements.add(replacement);
                      used = true;
                    }
                  }
                }
              }
              // try with the synthesizer
              if (!used && synth != null) {
                List<String> synthesizedSuggestions = new ArrayList<>();
                for (AnalyzedToken at : analyzedSuggestion) {
                  if (usedLemmas.contains(at.getLemma())) {
                    continue;
                  }
                  String[] synthesizedArray = synth.synthesize(at, desiredPostag, true);
                  usedLemmas.add(at.getLemma());
                  for (String synthesizedSuggestion : synthesizedArray) {
                    if (!synthesizedSuggestions.contains(synthesizedSuggestion)) {
                      synthesizedSuggestions.add(synthesizedSuggestion);
                    }
                  }
                  for (String replacement : synthesizedSuggestions) {
                    if (isWordAllupper) {
                      replacement = replacement.toUpperCase();
                    }
                    if (isWordCapitalized) {
                      replacement = StringTools.uppercaseFirstChar(replacement);
                    }
                    replacements2.add(replacement);
                  }
                }
              }
            }
          }
        }
      }
    }

    if (diacriticsMode && replacements.size() == 0) {
      return null;
    }
    if (replacements.size() + replacements2.size() == 0 && bSuppressMatch) {
      return null;
    }
    String message = match.getMessage();
    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
        message, match.getShortMessage());
    ruleMatch.setType(match.getType());

    List<String> definitiveReplacements = new ArrayList<>();
    boolean replacementsUsed = false;
    if (generateSuggestions) {
      for (String s : match.getSuggestedReplacements()) {
        if (s.contains("{suggestion}") || s.contains("{Suggestion}")) {
          replacementsUsed = true;
          for (String s2 : replacements) {
            if (definitiveReplacements.size() >= MAX_SUGGESTIONS) {
              break;
            }
            if (s.contains("{suggestion}")) {
              if (!definitiveReplacements.contains(s2)) {
                definitiveReplacements.add(s.replace("{suggestion}", s2));
              }
            } else {
              if (!definitiveReplacements.contains(StringTools.uppercaseFirstChar(s2))) {
                definitiveReplacements.add(s.replace("{Suggestion}", StringTools.uppercaseFirstChar(s2)));
              }
            }
          }
        } else {
          if (!definitiveReplacements.contains(s)) {
            definitiveReplacements.add(s);
          }
        }
      }
      if (!replacementsUsed) {
        if (replacements.size()==0) {
          Collections.sort(replacements2, stringComparator);
          for (String replacement : replacements2) {
            if (!replacements.contains(replacement) && !definitiveReplacements.contains(replacement)) {
              replacements.add(replacement);
            }
          }  
        }
        for (String replacement: replacements) { 
          if (definitiveReplacements.size() >= MAX_SUGGESTIONS) {
            break;
          }
          if (!definitiveReplacements.contains(replacement)) {
            definitiveReplacements.add(replacement);
          }
        }
      }
    }

    if (!definitiveReplacements.isEmpty()) {
      ruleMatch.setSuggestedReplacements(definitiveReplacements.stream().distinct().collect(Collectors.toList()));
    }
    return ruleMatch;
  }

  protected boolean isSuggestionException(AnalyzedTokenReadings analyzedSuggestion) {
    return false;
  };

  private boolean equalWithoutDiacritics(String s, String t) {
    return StringTools.removeDiacritics(s).equalsIgnoreCase(StringTools.removeDiacritics(t));
  }

  protected String cleanSuggestion(String s) {
    return s;
  }

  protected Synthesizer getSynthesizer() {
    return null;
  }

  public class StringComparator implements Comparator<String> {
    EditDistance levenstheinDistance;
    int maxDistance = 4;

    StringComparator(String word) {
      levenstheinDistance = new EditDistance(word, EditDistance.DistanceAlgorithm.Damerau);
    }

    @Override
    public int compare(String o1, String o2) {
      int d1 = levenstheinDistance.compare(o1, maxDistance);
      int d2 = levenstheinDistance.compare(o2, maxDistance);
      if (d1 < 0) {
        d1 = 2 * maxDistance;
      }
      if (d2 < 0) {
        d2 = 2 * maxDistance;
      }
      return d1 - d2;
    }
  }

}