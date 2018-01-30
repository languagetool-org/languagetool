/* LanguageTool, a natural language style checker 
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.languagemodel.BaseLanguageModel;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.util.*;

/**
 * Find compounds that might be morphologically correct but are still probably wrong, like 'Lehrzeile'.
 * @since 4.1
 */
public class ProhibitedCompoundRule extends Rule {

  private static final List<Pair> lowercasePairs = Arrays.asList(
          // NOTE: words here must be all-lowercase
          new Pair("abschluss", "Ende", "Abschuss", "Vorgang des Abschießens, z.B. mit einer Waffe"),
          new Pair("wieder", "wieder: erneut, wiederholt, nochmal (Wiederholung, Wiedervorlage, ...)", "wider", "wider: gegen, entgegen (Widerwille, Widerstand, Widerspruch, ...)"),
          new Pair("leer", "leer: ohne Inhalt", "lehr", "Lehr-: bezogen auf Ausbildung und Wissen"),
          new Pair("Gewerbe", "Gewerbe: wirtschaftliche Tätigkeit", "Gewebe", "Gewebe: gewebter Stoff; Verbund ähnlicher Zellen"),
          new Pair("Schuh", "Schuh: Fußbekleidung", "Schul", "Schul-: auf die Schule bezogen"),
          new Pair("modell", "Modell: vereinfachtes Abbild der Wirklichkeit", "model", "Model: Fotomodell")
  );
  private static final List<Pair> pairs = new ArrayList<>();
  static {
    for (Pair lcPair : lowercasePairs) {
      pairs.add(new Pair(lcPair.part1, lcPair.part1Desc, lcPair.part2, lcPair.part2Desc));
      String ucPart1 = StringTools.uppercaseFirstChar(lcPair.part1);
      String ucPart2 = StringTools.uppercaseFirstChar(lcPair.part2);
      if (!lcPair.part1.equals(ucPart1) || !lcPair.part2.equals(ucPart2)) {
        pairs.add(new Pair(ucPart1, lcPair.part1Desc, ucPart2, lcPair.part2Desc));
      }
    }  
  }
  
  private final BaseLanguageModel lm;

  public ProhibitedCompoundRule(ResourceBundle messages, LanguageModel lm) {
    this.lm = (BaseLanguageModel) Objects.requireNonNull(lm);
  }

  @Override
  public String getId() {
    return "DE_PROHIBITED_COMPOUNDS";
  }

  @Override
  public String getDescription() {
    return "Markiert wahrscheinlich falsche Komposita wie 'Lehrzeile', wenn 'Leerzeile' häufiger vorkommt.";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    for (AnalyzedTokenReadings readings : sentence.getTokensWithoutWhitespace()) {
      String word = readings.getToken();
      for (Pair pair : pairs) {
        String variant = null;
        if (word.contains(pair.part1)) {
          variant = word.replaceFirst(pair.part1, pair.part2);
        } else if (word.contains(pair.part2)) {
          variant = word.replaceFirst(pair.part2, pair.part1);
        }
        if (variant == null) {
          continue;
        }
        long wordCount = lm.getCount(word);
        long variantCount = lm.getCount(variant);
        //System.out.println("word: " + word + " (" + wordCount + "), variant: " + variant + " (" + variantCount + "), pair: " + pair);
        if (variantCount > 0 && wordCount == 0) {
          String msg = "Möglicher Tippfehler. " + pair.part1Desc + ", " + pair.part2Desc;
          RuleMatch match = new RuleMatch(this, sentence, readings.getStartPos(), readings.getEndPos(), msg);
          match.setSuggestedReplacement(variant);
          ruleMatches.add(match);
          break;
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }
  
  static class Pair {
    private final String part1;
    private final String part1Desc;
    private final String part2;
    private final String part2Desc;
    Pair(String part1, String part1Desc, String part2, String part2Desc) {
      this.part1 = part1;
      this.part1Desc = part1Desc;
      this.part2 = part2;
      this.part2Desc = part2Desc;
    }
    @Override
    public String toString() {
      return part1 + "/" + part2;
    }
  }
  
}
