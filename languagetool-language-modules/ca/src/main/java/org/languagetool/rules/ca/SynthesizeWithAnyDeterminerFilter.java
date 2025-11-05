/* LanguageTool, a natural language style checker
 * Copyright (C) 2025 Jaume Ortolà
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

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.ca.CatalanSynthesizer;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.languagetool.rules.ca.ApostophationHelper.getPrepositionAndDeterminer;

public class SynthesizeWithAnyDeterminerFilter extends RuleFilter {

  private List<String> genderNumberList = Arrays.asList ("MS", "FS", "MP", "FP");
  private List<String> prepositions = Arrays.asList ("a", "de", "per", "pe");

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
    boolean synthAllForms = getOptional("synthAllForms", arguments, "false").equalsIgnoreCase("true")? true: false;
    String lemmaSelect = getRequired("lemmaSelect", arguments);
    int posWord = 0;
    AnalyzedTokenReadings[] tokens = match.getSentence().getTokensWithoutWhitespace();
    while (posWord < tokens.length
      && (tokens[posWord].getStartPos() < match.getFromPos() || tokens[posWord].isSentenceStart())) {
      posWord++;
    }
    if (posWord >= tokens.length) {
      posWord = tokens.length - 1;
    }
    String originalWord = tokens[posWord].getToken();
    AnalyzedToken originalAT = tokens[posWord].readingWithTagRegex(lemmaSelect);
    if (originalAT == null) {
      throw new RuntimeException("Cannot find analyzed token readings with postag "+lemmaSelect+" in sentence"+match.getSentence().getText());
    }

    String secondGenderNumber = "";
    String determinerType = "";
    AnalyzedToken determinerReading = null;
    AnalyzedTokenReadings betweenToken = null;
    String preposition="";
    boolean done = false;
    int k = 1;
    int firstUnderlinedToken = posWord;
    while (posWord - k > 0 && !done) {
      done = true;
      if (determinerReading == null) {
        determinerReading = tokens[posWord - k].readingWithTagRegex("D.*");
        if (determinerReading != null) {
          secondGenderNumber = determinerReading.getPOSTag().substring(3, 5);
          determinerType = determinerReading.getPOSTag().substring(0, 2);
          done = !determinerType.equals("DA");
          firstUnderlinedToken = posWord - k;
        }
      }
      if (tokens[posWord - k].hasPosTag("_QM_OPEN")) {
        betweenToken = tokens[posWord - k];
        done = false;
      }
      if (prepositions.contains(tokens[posWord - k].getToken())) {
        //a=a d=de per=p
        preposition = tokens[posWord - k].getToken().substring(0, 1).toLowerCase();
        firstUnderlinedToken = posWord - k;
      }
      k++;
    }
    String betweenString = "";
    if (betweenToken != null) {
      betweenString = betweenToken.getToken();
    }
    List<String> suggestions = new ArrayList<>();
    Pattern p = Pattern.compile(lemmaSelect);

    List<AnalyzedToken> potentialSuggestions = new ArrayList<>();
    // original word form in the first place
    potentialSuggestions.add(originalAT);
    // second-best suggestion from the determiner
    for (String tag : synth.getPossibleTags()) {
      Matcher m = p.matcher(tag);
      if (m.matches()) {
        String[] synthForms = synth.synthesize(originalAT, tag);
        for (String synthForm : synthForms) {
          AnalyzedToken at = new AnalyzedToken(synthForm, tag, originalAT.getLemma());
          if (!synthAllForms && !synthForm.equalsIgnoreCase(originalWord)) {
            continue;
          }
          if (!listContainsAnalizedToken(potentialSuggestions, at)) {
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
          String suggestion;
          if (determinerType.equals("DA") || determinerType.isEmpty()) {
            suggestion = StringTools.preserveCase(getPrepositionAndDeterminer(newForm, genderNumber, preposition),
              tokens[firstUnderlinedToken].getToken())+ betweenString + StringTools.preserveCase(newForm, originalWord);
            if (firstUnderlinedToken==1) {
              suggestion = StringTools.uppercaseFirstChar(suggestion);
            }
            if (!suggestions.contains(suggestion)) {
              suggestions.add(suggestion);
            }
          } else {
            String[] synthForms = synth.synthesize(determinerReading,determinerType+
              ".[C"+genderNumber.substring(0,1)+"]"+genderNumber.substring(1,2)+".", true);
            for (String synthForm : synthForms) {
              suggestion = StringTools.preserveCase(synthForm, tokens[firstUnderlinedToken].getToken())
                + " " + betweenString + StringTools.preserveCase(newForm, originalWord);
              if (firstUnderlinedToken==1) {
                suggestion = StringTools.uppercaseFirstChar(suggestion);
              }
              if (!suggestions.contains(suggestion)) {
                suggestions.add(suggestion);
              }
            }
          }
        }
      }
    }

    RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), tokens[firstUnderlinedToken].getStartPos(),
      match.getToPos(), match.getMessage(), match.getShortMessage());
    ruleMatch.setType(match.getType());
    ruleMatch.setSuggestedReplacements(suggestions);
    return ruleMatch;
  }

  private boolean listContainsAnalizedToken(List<AnalyzedToken> list, AnalyzedToken at) {
    for (AnalyzedToken item: list){
      if (item.getLemma().equals(at.getLemma()) && item.getPOSTag().equals(at.getPOSTag()))
        return true;
    }
    return false;
  }

}
