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
package org.languagetool.rules.es;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.tools.StringTools;

public class ConfusionCheckFilter extends RuleFilter {

  private static final Map<String, AnalyzedTokenReadings> relevantWords = 
      new ConfusionPairsDataLoader().loadWords("/es/confusion_pairs.txt");
  
  private static final Pattern MS = Pattern.compile("NC[MC][SN]000|A..[MC][SN].|V.P..SM");
  private static final Pattern FS = Pattern.compile("NC[FC][SN]000|A..[FC][SN].|V.P..SF");
  private static final Pattern MP = Pattern.compile("NC[MC][PN]000|A..[MC][PN].|V.P..PM");
  private static final Pattern FP = Pattern.compile("NC[FC][PN]000|A..[FC][PN].|V.P..PF");

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
      AnalyzedTokenReadings[] patternTokens) {

    Pattern desiredGenderNumberPattern = null;
    String replacement = null;
    String postag = getRequired("postag", arguments);
    String form = getRequired("form", arguments).toLowerCase();
    String gendernumber_from = getOptional("gendernumber_from", arguments);
    if (gendernumber_from != null) {
      int i = Integer.parseInt(gendernumber_from);
      if (i < 1 || i > patternTokens.length) {
        throw new IllegalArgumentException(
            "ConfusionCheckFilter: Index out of bounds in " + match.getRule().getFullId() + ", value: " + i);
      }
      AnalyzedTokenReadings atr = patternTokens[i - 1];
      if (atr.matchesPosTagRegex(".+MS.*")) { desiredGenderNumberPattern = MS;}
      if (atr.matchesPosTagRegex(".+MP.*")) { desiredGenderNumberPattern = MP;}
      if (atr.matchesPosTagRegex(".+FS.*")) { desiredGenderNumberPattern = FS;}
      if (atr.matchesPosTagRegex(".+FP.*")) { desiredGenderNumberPattern = FP;}
    }
    
    if (relevantWords.containsKey(form)) {
      if (relevantWords.get(form).matchesPosTagRegex(postag)) {
        if (desiredGenderNumberPattern != null) {
          Matcher m = desiredGenderNumberPattern.matcher(relevantWords.get(form).getReadings().get(0).getPOSTag());
          if (!m.matches()) {
            return null;
          }
        }
        replacement = relevantWords.get(form).getToken(); 
      }
    }
    if (replacement != null) {
      String message = match.getMessage();
      // Change the message if the replacement has no diacritic
      if (!(StringTools.hasDiacritics(replacement) && !StringTools.hasDiacritics(form))) {
        message = message.replace("se escribe con tilde", "se escribe de otra manera");
      }
      RuleMatch ruleMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(),
          message, match.getShortMessage());
      ruleMatch.setType(match.getType());
      String suggestion = match.getSuggestedReplacements().get(0).replace("{suggestion}", replacement);
      suggestion = suggestion.replace("{Suggestion}", StringTools.uppercaseFirstChar(replacement));
      ruleMatch.setSuggestedReplacement(suggestion);
      return ruleMatch;
    }    
    return null;
  }

}
