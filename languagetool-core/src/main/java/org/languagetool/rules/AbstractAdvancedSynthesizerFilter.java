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
import org.languagetool.rules.RuleMatch;
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
    
    //if (match.getSentence().getText().contains("Das ist wider der Vernunft")) {
    //  int ii=0;
    //  ii++;
    //}

    String postagSelect = getRequired("postagSelect", arguments);
    String lemmaSelect = getRequired("lemmaSelect", arguments);
    String postagFromStr = getRequired("postagFrom", arguments);
    String lemmaFromStr = getRequired("lemmaFrom", arguments);
    
    int postagFrom = 0;
    if (postagFromStr.equals("marker")) {
      while (postagFrom < patternTokens.length && patternTokens[postagFrom].getStartPos() < match.getFromPos()) {
        postagFrom++;
      }
      postagFrom++;
    } else {
      postagFrom = Integer.parseInt(postagFromStr);
    }
    if (postagFrom < 1 || postagFrom > patternTokens.length) {
      throw new IllegalArgumentException("AdvancedSynthesizerFilter: Index out of bounds in "
          + match.getRule().getFullId() + ", value: " + postagFromStr);
    }
    int lemmaFrom = 0;
    if (lemmaFromStr.equals("marker")) {
      while (lemmaFrom < patternTokens.length && patternTokens[lemmaFrom].getStartPos() < match.getFromPos()) {
        lemmaFrom++;
      }
      lemmaFrom++;
    } else {
      lemmaFrom = Integer.parseInt(lemmaFromStr);
    }
    if (lemmaFrom < 1 || lemmaFrom > patternTokens.length) {
      throw new IllegalArgumentException("AdvancedSynthesizerFilter: Index out of bounds in "
          + match.getRule().getFullId() + ", value: " + lemmaFromStr);
    }

    String postagReplace = getOptional("postagReplace", arguments);

    String desiredLemma = getAnalyzedToken(patternTokens[lemmaFrom - 1], lemmaSelect).getLemma();
    String originalPostag = getAnalyzedToken(patternTokens[lemmaFrom - 1], lemmaSelect).getPOSTag();
    String desiredPostag = getAnalyzedToken(patternTokens[postagFrom - 1], postagSelect).getPOSTag();
    
    if (desiredPostag == null) {
      throw new IllegalArgumentException("AdvancedSynthesizerFilter: undefined POS tag");
    }

    if (postagReplace != null) {
      desiredPostag = getCompositePostag(lemmaSelect, postagSelect, originalPostag, desiredPostag, postagReplace);
    }

    // take capitalization from the lemma (?)
    boolean isWordCapitalized = StringTools.isCapitalizedWord(patternTokens[lemmaFrom - 1].getToken());
    boolean isWordAllupper = StringTools.isAllUppercase(patternTokens[lemmaFrom - 1].getToken());
    AnalyzedToken token = new AnalyzedToken("", desiredPostag, desiredLemma);
    String[] replacements = getSynthesizer().synthesize(token, desiredPostag, true);
    
    if (replacements.length > 0) {
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
          if (r.contains("{suggestion}") || r.contains("{Suggestion}")) {
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
          replacementsList.add(completeSuggestion);
        }
      }
      if (!suggestionUsed) {
        replacementsList.addAll(Arrays.asList(replacements));
      }
      newMatch.setSuggestedReplacements(replacementsList);
      return newMatch;
    }
    return match;
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
