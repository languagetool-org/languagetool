/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.Language;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.ngrams.ConfusionProbabilityRule;
import org.languagetool.rules.Example;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @since 3.1
 */
public class GermanConfusionProbabilityRule extends ConfusionProbabilityRule {

  private static final List<Pattern> SENTENCE_EXCEPTION_PATTERNS = Arrays.asList(
    Pattern.compile("wir \\("),  // "Hallo, wir (die Dingsbums Gmbh)"
    Pattern.compile("Wie .*?en Sie"),  // "Wie heizen Sie das Haus?"
    Pattern.compile("fiel(e|en)? .* (aus|auf)")
  );

  private static final List<String> EXCEPTIONS = Arrays.asList(
    // Use all-lowercase, matches will be case-insensitive.
    // See https://github.com/languagetool-org/languagetool/issues/1516
    "möglichkeit weißt",
    "du doch trotzdem",
    "wir stark ausgelastet sind",
    "wir entwickeln für",
    "nutzen wir Google",
    "vertreiben wir",
    "wir auch nicht",
    ", dir bei",  // "froh, dir bei deiner Arbeit zu helfen"
    "fiel hinaus",
    "setz dir",  // "Setz dir doch bitte einen Termin am Donnerstag"
    "du hast dir",
    "vielen als held",
    "seht gut",  // "Ihr seht gut aus"
    "so viel das",
    "wie erinnern sie sich",
    "dürfen wir nicht",
    "kann dich auch",
    "wie schicken wir",
    "wie benutzen sie",
    "wir ja nicht",
    "wie wir oder",
    "eine uhrzeit hatten",
    "damit wir das",
    "damit wir die",
    "damit wir dir",
    "was wird in",
    "warum wird da",
    "da mir der",
    "das wir uns",
    "so wir können",
    "bestellt Botschafter ein",
    "bestellt Botschafterin ein",
    "wie zahlen sie",
    "unser business",
    "journalisten gefiltert worden",
    "für uns filtern",
    "leinwand gezeigte",
    "war sich für nichts", // war sich für nichts zu schade
    "dover corporation",
    "bringt dich ein",
    "bringt dich eine",
    "womit arbeitet",
    "womit arbeiten",
    "ich drei bin", // seit ich drei bin.
    "was wird unser",
    "die wird wieder",
    "damit wir für",
    "wie finden sie",
    "ach die armen",
    "wie stehen da die", // vs wir
    "wir würden sie", // vs wird
    "damit wir ihre daten", // vs wird
    "kannst du doch gerne", // vs dich
    "wie ist hier der Stand", // vs Sand
    "wie ist der Stand", // vs Sand
    "dass da Potenzial zu",
    "das auch hergibt", // vs ergibt
    "hat mich angeschrieben", // vs abgeschrieben
    "sehe gerade", // vs siehe gerade
    "hole dich auch ab", // vs dir
    "würdest du dich vorstellen", // vs dir
    "daten wir über", // "welche Daten wir über unsere Nutzer erfassen"
    "anders seht", // falls ihr das anders seht (weht)
    "weit fallendem", // vs weiht
    "weit fallenden", // vs weiht
    "weit fallendes", // vs weiht
    "weit fallende", // vs weiht
    "weit fallender", // vs weiht
    "wir ja.", // vs wie
    "weißt, wie", // vs weist
    "weißt ja, wie", // vs weist
    "weißt, dass", // vs weist
    "weißt ja, dass", // vs weist
    "Vorgestern und Gestern" // vs Gesten
  );

  public GermanConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language) {
    this(messages, languageModel, language, 3);
  }

  public GermanConfusionProbabilityRule(ResourceBundle messages, LanguageModel languageModel, Language language, int grams) {
    super(messages, languageModel, language, grams, EXCEPTIONS);
    addExamplePair(Example.wrong("Während Sie das Ganze <marker>mir</marker> einem Holzlöffel rühren…"),
                   Example.fixed("Während Sie das Ganze <marker>mit</marker> einem Holzlöffel rühren…"));
  }

  @Override
  protected boolean isException(String sentenceText, int startPos, int endPos) {
    for (Pattern pattern : SENTENCE_EXCEPTION_PATTERNS) {
      Matcher m = pattern.matcher(sentenceText);
      if (m.find()) {
        return true;
      }
    }
    return false;
  }

  protected boolean isCommonWord(String token) {
    return token.matches("[\\wöäüßÖÄÜ]+");
  }

}
