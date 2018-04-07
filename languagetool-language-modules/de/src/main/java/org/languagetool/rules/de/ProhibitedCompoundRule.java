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
import org.languagetool.JLanguageTool;
import org.languagetool.databroker.ResourceDataBroker;
import org.languagetool.language.GermanyGerman;
import org.languagetool.languagemodel.BaseLanguageModel;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.languagetool.tools.StringTools.*;

/**
 * Find compounds that might be morphologically correct but are still probably wrong, like 'Lehrzeile'.
 * @since 4.1
 */
public class ProhibitedCompoundRule extends Rule {

  // have objects static for better performance (rule gets initialized for every check)
  private static final List<Pair> lowercasePairs = Arrays.asList(
          // NOTE: words here must be all-lowercase
          // NOTE: no need to add words from confusion_sets.txt, they will be used automatically (if starting with uppercase char)
          new Pair("abschluss", "Ende", "Abschuss", "Vorgang des Abschießens, z.B. mit einer Waffe"),
          new Pair("brache", "verlassenes Grundstück", "branche", "Wirtschaftszweig"),
          new Pair("wieder", "erneut, wiederholt, nochmal (Wiederholung, Wiedervorlage, ...)", "wider", "gegen, entgegen (Widerwille, Widerstand, Widerspruch, ...)"),
          new Pair("leer", "ohne Inhalt", "lehr", "bezogen auf Ausbildung und Wissen"),
          new Pair("Gewerbe", "wirtschaftliche Tätigkeit", "Gewebe", "gewebter Stoff; Verbund ähnlicher Zellen"),
          new Pair("Schuh", "Fußbekleidung", "Schul", "auf die Schule bezogen"),
          new Pair("klima", "langfristige Wetterzustände", "lima", "Hauptstadt von Peru"),
          new Pair("modell", "vereinfachtes Abbild der Wirklichkeit", "model", "Fotomodell"),
          new Pair("treppen", "Folge von Stufen (Mehrzahl)", "truppen", "Armee oder Teil einer Armee (Mehrzahl)"),
          new Pair("häufigkeit", "Anzahl von Ereignissen", "häutigkeit", "z.B. in Dunkelhäutigkeit"),
          new Pair("spitze", "spitzes Ende eines Gegenstandes", "spritze", "medizinisches Instrument zur Injektion")
  );
  private static final GermanSpellerRule spellerRule = new GermanSpellerRule(JLanguageTool.getMessageBundle(), new GermanyGerman());
  private static final List<String> ignoreWords = Arrays.asList("Die", "De");
  private static final List<Pair> pairs = new ArrayList<>();
  static {
    addUpperCaseVariants();
    addItemsFromConfusionSets();
  }

  private static void addUpperCaseVariants() {
    for (Pair lcPair : lowercasePairs) {
      pairs.add(new Pair(lcPair.part1, lcPair.part1Desc, lcPair.part2, lcPair.part2Desc));
      String ucPart1 = uppercaseFirstChar(lcPair.part1);
      String ucPart2 = uppercaseFirstChar(lcPair.part2);
      if (!lcPair.part1.equals(ucPart1) || !lcPair.part2.equals(ucPart2)) {
        pairs.add(new Pair(ucPart1, lcPair.part1Desc, ucPart2, lcPair.part2Desc));
      }
    }
  }

  private static void addItemsFromConfusionSets() {
    try {
      ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
      try (InputStream confusionSetStream = dataBroker.getFromResourceDirAsStream("/de/confusion_sets.txt")) {
        ConfusionSetLoader loader = new ConfusionSetLoader();
        Map<String, List<ConfusionSet>> confusionSet = loader.loadConfusionSet(confusionSetStream);
        for (Map.Entry<String, List<ConfusionSet>> entry : confusionSet.entrySet()) {
          for (ConfusionSet set : entry.getValue()) {
            boolean allUpper = set.getSet().stream().allMatch(k -> startsWithUppercase(k.getString()) && !ignoreWords.contains(k.getString()));
            if (allUpper) {
              Set<ConfusionString> cSet = set.getSet();
              if (cSet.size() != 2) {
                throw new RuntimeException("Got confusion set with != 2 items: " + cSet);
              }
              Iterator<ConfusionString> it = cSet.iterator();
              ConfusionString part1 = it.next();
              ConfusionString part2 = it.next();
              pairs.add(new Pair(part1.getString(), part1.getDescription(), part2.getString(), part2.getDescription()));
              pairs.add(new Pair(lowercaseFirstChar(part1.getString()), part1.getDescription(), lowercaseFirstChar(part2.getString()), part2.getDescription()));
            }
          }
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final BaseLanguageModel lm;

  public ProhibitedCompoundRule(ResourceBundle messages, LanguageModel lm) {
    this.lm = (BaseLanguageModel) Objects.requireNonNull(lm);
    super.setCategory(Categories.TYPOS.getCategory(messages));
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
        //float factor = variantCount / (float)Math.max(wordCount, 1);
        //System.out.println("word: " + word + " (" + wordCount + "), variant: " + variant + " (" + variantCount + "), factor: " + factor + ", pair: " + pair);
        if (variantCount > 0 && wordCount == 0 && !spellerRule.isMisspelled(variant)) {
          String msg;
          if (pair.part1Desc != null && pair.part2Desc != null) {
            msg = "Möglicher Tippfehler. " + uppercaseFirstChar(pair.part1) + ": " + pair.part1Desc + ", " + uppercaseFirstChar(pair.part2) + ": " + pair.part2Desc;
          } else {
            msg = "Möglicher Tippfehler.";
          }
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
