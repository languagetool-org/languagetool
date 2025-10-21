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
package org.languagetool.rules.ca;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tools.StringTools;

import static org.languagetool.rules.ca.ApostophationHelper.getPrepositionAndDeterminer;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SynthesizeWithDAFilter extends RuleFilter {

  private List<String> genderNumberList = Arrays.asList ("MS", "FS", "MP", "FP");

  private static Map<String, Pattern> genderNumberPatterns = new HashMap<>();
  static {
    genderNumberPatterns.put("MS", Pattern.compile("(N|A.).[MC][SN].*|V.P.*SM.") );
    genderNumberPatterns.put("FS", Pattern.compile("(N|A.).[FC][SN].*|V.P.*SF.") );
    genderNumberPatterns.put("MP", Pattern.compile("(N|A.).[MC][PN].*|V.P.*PM.") );
    genderNumberPatterns.put("FP", Pattern.compile("(N|A.).[FC][PN].*|V.P.*PF.") );
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens, List<Integer> tokenPositions) throws IOException {
    CatalanSynthesizer synth = (CatalanSynthesizer) getSynthesizerFromRuleMatch(match);
    String lemmaFromStr = getRequired("lemmaFrom", arguments);
    String lemmaSelect = getRequired("lemmaSelect", arguments);
    boolean synthAllForms = getOptional("synthAllForms", arguments, "false").equalsIgnoreCase("true")? true: false;
    String prepositionFromStr = getOptional("prepositionFrom", arguments, "");
    int lemmaFrom = getPosition(lemmaFromStr, patternTokens, match);
    String preposition="";
    if (StringUtils.isNumeric(prepositionFromStr)) {
      int prepositionFrom = getPosition(prepositionFromStr, patternTokens, match);
      preposition = patternTokens[prepositionFrom].getToken().substring(0, 1).toLowerCase();
    } else if (!prepositionFromStr.isEmpty()) {
      //a=a d=de per=p
      preposition = prepositionFromStr.substring(0,1);
    }
    List<String> suggestions = new ArrayList<>();
    String originalWord = patternTokens[lemmaFrom].getToken();
    Pattern p = Pattern.compile(lemmaSelect);
    boolean isSentenceStart = isMatchAtSentenceStart(match.getSentence().getTokensWithoutWhitespace(), match);
    List<AnalyzedToken> potentialSuggestions = new ArrayList<>();
    // original word form in the first place
    AnalyzedToken originalAT = patternTokens[lemmaFrom].readingWithTagRegex(lemmaSelect);
    if (originalAT == null) {
      throw new RuntimeException("Cannot find analyzed token readings with postag "+lemmaSelect+" in sentence"+match.getSentence().getText());
    }
    potentialSuggestions.add(originalAT);
    // second-best suggestion
    String secondGenderNumber = "";
    if (lemmaFrom-1 > 0) {
      AnalyzedToken reading = patternTokens[lemmaFrom - 1].readingWithTagRegex("D.*");
      if (reading != null) {
        secondGenderNumber = reading.getPOSTag().substring(3,5);
      }
    }
    for (String tag : synth.getPossibleTags()) {
      Matcher m = p.matcher(tag);
      if (m.matches()) {
        String[] synthForms = synth.synthesize(originalAT, tag);
        for (String synthForm : synthForms) {
          AnalyzedToken at = new AnalyzedToken(synthForm, tag, originalAT.getLemma());
          if (!synthAllForms && !synthForm.equalsIgnoreCase(originalWord)) {
            continue;
          }
          if (!potentialSuggestions.contains(at)) {
            if (tag.contains(secondGenderNumber)
              || tag.contains(secondGenderNumber.substring(1,2)+secondGenderNumber.substring(0,1))) {
              potentialSuggestions.add(1, at);
            } else {
              potentialSuggestions.add(at);
            }
          }
        }
      }
    }
    for (AnalyzedToken potentialSuggestion : potentialSuggestions) {
      String newForm = potentialSuggestion.getToken();
      for (String genderNumber : genderNumberList) {
        if (genderNumberPatterns.get(genderNumber).matcher(potentialSuggestion.getPOSTag()).matches()) {
          String suggestion =
            getPrepositionAndDeterminer(newForm, genderNumber, preposition) + StringTools.preserveCase(newForm,
              originalWord);
          if (isSentenceStart) {
            suggestion = StringTools.uppercaseFirstChar(suggestion);
          }
          if (!suggestions.contains(suggestion)) {
            suggestions.add(suggestion);
          }
        }
      }
    }
    match.addSuggestedReplacements(suggestions);
    return match;
  }

}