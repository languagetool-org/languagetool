/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.patterns.AbstractPatternRule;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

/*
 * Synthesize suggestions using the lemma from one token (lemma_from) 
 * and the POS tag from another one (postag_from).
 * 
 * The lemma_select and postag_select attributes are required 
 * to choose one among several possible readings.
 */
public abstract class AbstractAdvancedSynthesizerFilter extends RuleFilter {

  abstract protected Synthesizer getSynthesizer();

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens) throws IOException {

    String postagSelect = getRequired("postagSelect", arguments);
    String lemmaSelect = getRequired("lemmaSelect", arguments);
    String postagFromStr = getRequired("postagFrom", arguments);
    String lemmaFromStr = getRequired("lemmaFrom", arguments);

    int postagFrom = calculateIndex(postagFromStr, patternTokens, match.getFromPos());
    int lemmaFrom = calculateIndex(lemmaFromStr, patternTokens, match.getFromPos());

    validateIndex("postagFrom", postagFrom, patternTokens, match);
    validateIndex("lemmaFrom", lemmaFrom, patternTokens, match);

    String postagReplace = getOptional("postagReplace", arguments);

    String desiredLemma = getAnalyzedToken(patternTokens[lemmaFrom - 1], lemmaSelect).getLemma();
    String originalPostag = getAnalyzedToken(patternTokens[lemmaFrom - 1], lemmaSelect).getPOSTag();
    String desiredPostag = getAnalyzedToken(patternTokens[postagFrom - 1], postagSelect).getPOSTag();

    handleUndefinedPosTag(match, desiredPostag, postagSelect, patternTokens, postagFrom);

    desiredPostag = replacePosTagIfNeeded(postagReplace, lemmaSelect, postagSelect, originalPostag, desiredPostag);

    boolean isWordCapitalized = StringTools.isCapitalizedWord(patternTokens[lemmaFrom - 1].getToken());
    boolean isWordAllupper = StringTools.isAllUppercase(patternTokens[lemmaFrom - 1].getToken());
    AnalyzedToken token = new AnalyzedToken("", desiredPostag, desiredLemma);
    String[] replacements = getSynthesizer().synthesize(token, desiredPostag, true);

    if (replacements.length > 0) {
      return createNewRuleMatch(match, isWordCapitalized, isWordAllupper, replacements, desiredPostag);
    }
    return match;
  }

  private int calculateIndex(String indexStr, AnalyzedTokenReadings[] patternTokens, int fromPos) {
    int index;
    if (indexStr.startsWith("marker")) {
      index = calculateMarkerIndex(indexStr, patternTokens, fromPos);
    } else {
      index = Integer.parseInt(indexStr);
    }
    return index;
  }

  private int calculateMarkerIndex(String marker, AnalyzedTokenReadings[] patternTokens, int fromPos) {
    int index = 0;
    while (index < patternTokens.length && patternTokens[index].getStartPos() < fromPos) {
      index++;
    }
    index++;
    if (marker.length() > 6) {
      index += Integer.parseInt(marker.replace("marker", ""));
    }
    return index;
  }

  private void validateIndex(String indexName, int index, AnalyzedTokenReadings[] patternTokens, RuleMatch match) {
    if (index < 1 || index > patternTokens.length) {
      throw new IllegalArgumentException("AdvancedSynthesizerFilter: Index out of bounds in "
        + match.getRule().getFullId() + ", value: " + index);
    }
  }

  private void handleUndefinedPosTag(RuleMatch match, String desiredPostag, String postagSelect, AnalyzedTokenReadings[] patternTokens, int postagFrom) {
    if (desiredPostag == null) {
      throw new IllegalArgumentException("AdvancedSynthesizerFilter: undefined POS tag for rule " +
        match.getRule().getFullId() + " with POS regex '" + postagSelect + "' for token: " +
        patternTokens[postagFrom - 1]);
    }
  }

  private String replacePosTagIfNeeded(String postagReplace, String lemmaSelect, String postagSelect, String originalPostag, String desiredPostag) {
    if (postagReplace != null) {
      desiredPostag = getCompositePostag(lemmaSelect, postagSelect, originalPostag, desiredPostag, postagReplace);
    }
    return desiredPostag;
  }

  private RuleMatch createNewRuleMatch(RuleMatch match, boolean isWordCapitalized, boolean isWordAllupper, String[] replacements, String desiredPostag) {
    RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
      match.getMessage(), match.getShortMessage());
    newMatch.setType(match.getType());
    List<String> replacementsList = new ArrayList<>();

    boolean suggestionUsed = false;
    for (String r : match.getSuggestedReplacements()) {
      for (String nr : replacements) {
        if (isSuggestionException(nr, desiredPostag)) {
          continue;
        }
        if (r.toLowerCase().contains("{suggestion}")) {
          suggestionUsed = true;
        }
        if (isWordCapitalized) {
          nr = StringTools.uppercaseFirstChar(nr);
        }
        if (isWordAllupper) {
          nr = nr.toUpperCase();
        }
        String completeSuggestion = r.replace("{suggestion}", nr);
        completeSuggestion = completeSuggestion.replace("{Suggestion}", StringTools.uppercaseFirstChar(nr));
        completeSuggestion = completeSuggestion.replace("{SUGGESTION}", nr.toUpperCase());
        if (!replacementsList.contains(completeSuggestion)) {
          replacementsList.add(completeSuggestion);
        }
      }
    }
    if (!suggestionUsed) {
      replacementsList.addAll(Arrays.asList(replacements));
    }

    List<String> adjustedReplacementsList = new ArrayList<>();
    Rule rule = match.getRule();
    if (rule instanceof AbstractPatternRule) {
      Language lang = ((AbstractPatternRule) rule).getLanguage();
      for (String replacement : replacementsList) {
        adjustedReplacementsList.add(lang.adaptSuggestion(replacement));
      }
    } else {
      adjustedReplacementsList = replacementsList;
    }
    newMatch.setSuggestedReplacements(adjustedReplacementsList);
    return newMatch;
  }


  private String getCompositePostag(String lemmaSelect, String postagSelect, String originalPostag,
      String desiredPostag, String postagReplace) {
    Pattern aPattern = Pattern.compile(lemmaSelect, Pattern.UNICODE_CASE);
    Pattern bPattern = Pattern.compile(postagSelect, Pattern.UNICODE_CASE);
    Matcher aMatcher = aPattern.matcher(originalPostag);
    Matcher bMatcher = bPattern.matcher(desiredPostag);
    String result = postagReplace;
    if (aMatcher.matches() && bMatcher.matches()) {
      for (int i = 1; i <= aMatcher.groupCount(); i++) {
        String groupStr = aMatcher.group(i);
        String toReplace = "\\\\a" + i;
        result = result.replaceAll(toReplace, groupStr);
      }
      for (int i = 1; i <= bMatcher.groupCount(); i++) {
        String groupStr = bMatcher.group(i);
        String toReplace = "\\\\b" + i;
        result = result.replaceAll(toReplace, groupStr);
      }
    }
    return result;
  }

  protected boolean isSuggestionException(String token, String desiredPostag) {
    return false;
  }

  private AnalyzedToken getAnalyzedToken(AnalyzedTokenReadings aToken, String regexp) {
    Pattern pattern = Pattern.compile(regexp);
    for (AnalyzedToken analyzedToken : aToken) {
      String posTag = analyzedToken.getPOSTag();
      if (posTag == null) {
        posTag = "UNKNOWN";
      }
      final Matcher m = pattern.matcher(posTag);
      if (m.matches()) {
        return analyzedToken;
      }
    }
    // Return the first one. Something is wrong, anyway
    return aToken.getAnalyzedToken(0);
  }

}
