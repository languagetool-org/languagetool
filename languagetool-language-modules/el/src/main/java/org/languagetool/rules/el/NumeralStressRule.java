/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.el;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Category;
import org.languagetool.rules.CategoryId;
import org.languagetool.rules.Example;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * Rule for checking correct spell of ordinal numerals.
 * <p>
 * Greek ordinal numerals are declined like adjectives. When a numeral is
 * written as an arabic number followed by a suffix, the suffix may need a
 * stress (e.g. 20ός, 100ός but 1ος, 10ος). This rule will check for incorrect
 * usage or absence of stress.
 * 
 * @author Panagiotis Minos
 * @since 3.3
 */
public class NumeralStressRule extends Rule {

  //map from stressed to unstressed form and unstressed to stressed form
  private final Map<String, String> suffixMap = new HashMap<>();
  // pattern to match an arabic number followed by a possible suffix
  private final Pattern numeral;
  // pattern to match an arabic number that needs stress
  private final Pattern stressedNumber;
  // pattern to match a stressed suffix
  private final Pattern stressedSuffix;

  public NumeralStressRule(ResourceBundle messages) {
    super(messages);

    String[] unstressedSfx = {
      "ος", "ου", "ο", "ον", "οι", "ων", "ους", "η", "ης", "ην", "ες", "α"
    };
    String[] stressedSfx = {
      "ός", "ού", "ό", "όν", "οί", "ών", "ούς", "ή", "ής", "ήν", "ές", "ά"
    };
    StringBuilder stressedSuffixRE = new StringBuilder();
    for (int i = 0; i < stressedSfx.length; i++) {
      if (i > 0) {
        stressedSuffixRE.append('|');
      }
      stressedSuffixRE.append(stressedSfx[i]);
      suffixMap.put(stressedSfx[i], unstressedSfx[i]);
      suffixMap.put(unstressedSfx[i], stressedSfx[i]);
    }
    StringBuilder pattern = new StringBuilder("([1-9][0-9]*)(");
    pattern.append(stressedSuffixRE);
    for (String sfx : unstressedSfx) {
      pattern.append('|').append(sfx);
    }
    pattern.append(')');

    numeral = Pattern.compile(pattern.toString());
    //we know the token can not start with 0
    stressedNumber = Pattern.compile("[0-9]*[0|2-9]0");
    stressedSuffix = Pattern.compile(stressedSuffixRE.toString());
    setCategory(new Category(new CategoryId("ORTHOGRAPHY"), "Orthography"));
    init();
  }

  private void init() {
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(
        Example.wrong("Ο <marker>20ος</marker> αιώνας μαζί με τον 21ο αιώνα κατατάσσεται από τους ιστορικούς στη Σύγχρονη Ιστορία."),
        Example.fixed("Ο <marker>20ός</marker> αιώνας μαζί με τον 21ο αιώνα κατατάσσεται από τους ιστορικούς στη Σύγχρονη Ιστορία."));
  }

  @Override
  public String getId() {
    return "GREEK_ORTHOGRAPHY_NUMERAL_STRESS";
  }

  @Override
  public String getDescription() {
    return "Έλεγχος τονισμού αριθμητικών";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    for (AnalyzedTokenReadings token : tokens) {
      Matcher m = numeral.matcher(token.getToken());
      if (m.matches()) {
        String number = m.group(1);
        String suffix = m.group(2);
        boolean needsStress = stressedNumber.matcher(number).matches();
        boolean hasStress = stressedSuffix.matcher(suffix).matches();
        if (needsStress != hasStress) {
          suffix = suffixMap.get(suffix);
          String suggestion = number + suffix;
          String msg = "<suggestion>" + suggestion + "</suggestion>";
          RuleMatch match = new RuleMatch(this, sentence, token.getStartPos(),
                  token.getEndPos(), msg, "Πρόβλημα ορθογραφίας");
          ruleMatches.add(match);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}
