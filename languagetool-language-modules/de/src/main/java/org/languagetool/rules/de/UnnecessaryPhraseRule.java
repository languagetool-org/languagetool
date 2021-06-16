/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractStatisticStyleRule;
import org.languagetool.rules.Example;

/**
 * A rule that gives Hints about the use of German phrases.
 * The Hints are only given when the percentage of phrases per chapter/text exceeds the given limit.
 * A limit of 0 shows all used filler words. Direct speech or citation is excluded otherwise. 
 * This rule detects no grammar error but gives stylistic hints (default off).
 * @author Fred Kruse
 * @since 5.3
 */
public class UnnecessaryPhraseRule extends AbstractStatisticStyleRule {
  
  private static final int DEFAULT_MIN_PER_MILL = 8;

  private static final String[][] unnecessaryPhrases = { new String[] { "dann", "und", "wann" },
                                                       new String[] { "des", "Ungeachtet" },
                                                       new String[] { "ganz", "und", "gar" },
                                                       new String[] { "hie", "und","da" },
                                                       new String[] { "im", "Allgemeinen"},
                                                       new String[] { "in", "der", "Tat" },
                                                       new String[] { "in", "diesem", "Zusammenhang" },
                                                       new String[] { "mehr", "oder", "weniger" },
                                                       new String[] { "meines", "Erachtens" },
                                                       new String[] { "ohne", "weiteres" },
                                                       new String[] { "ohne", "Zweifel" },
                                                       new String[] { "samt", "und", "sonders"},
                                                       new String[] { "sowohl", "als", "auch" },
                                                       new String[] { "voll", "und", "ganz" },
                                                       new String[] { "von", "Neuem" },
                                                       new String[] { "allem", "Anschein", "nach" },
                                                       new String[] { "aufs", "Neue" },
                                                       new String[] { "ein", "bisschen" },
                                                       new String[] { "ein", "wenig" },
                                                       new String[] { "des", "Öfteren" },
                                                       new String[] { "bei", "weitem" },
                                                       new String[] { "an", "sich" }
  };
  
  public UnnecessaryPhraseRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig, DEFAULT_MIN_PER_MILL);
    addExamplePair(Example.wrong("Das ist <marker>allem Anschein nach</marker> eine Phrase."),
        Example.fixed("Das ist eine Phrase."));
  }

  /**
   * Minimal value is given in per mil
   */
  @Override
  public double denominator() {
    return 10000.0;
  }
  
  /**
   * changes first char to upper as sentence begin
   */
  private static String firstCharToLower(AnalyzedTokenReadings[] tokens, int nToken) {
    String token = tokens[nToken].getToken();
    return ((nToken != 1 || token.length() < 2) ? token : token.substring(0, 1).toLowerCase() + token.substring(1));
  }
  
  @Override
  protected int conditionFulfilled(AnalyzedTokenReadings[] tokens, int nAnalysedToken) {
    for (int i = 0; i < unnecessaryPhrases.length; i++) {
      int j;
      for (j = 0; j < unnecessaryPhrases[i].length && nAnalysedToken + j < tokens.length && 
          unnecessaryPhrases[i][j].equals(firstCharToLower(tokens, nAnalysedToken + j)); j++);
      if (j == unnecessaryPhrases[i].length) {
        return nAnalysedToken + unnecessaryPhrases[i].length - 1;
      }
    }
    return -1;
  }
  
  @Override
  protected boolean sentenceConditionFulfilled(AnalyzedTokenReadings[] tokens, int nToken) {
    return false;
  }

  @Override
  protected boolean excludeDirectSpeech() {
    return true;
  }

  @Override
  protected String getLimitMessage(int limit, double percent) {
    if (limit == 0) {
      return "Der Ausdruck gilt als Phrase. Es wird empfohlen ihn zu löschen, falls möglich.";
    }
    return "Mehr als " + limit + "‱ potenzielle Phrasen {" + ((int) (percent +0.5d)) +
        "‱} gefunden. Es wird empfohlen den Ausdruck zu löschen, falls möglich.";
  }

  @Override
  protected String getSentenceMessage() {
    return null;
  }

  @Override
  public String getId() {
    return "UNNECESSARY_PHRASES_DE";
  }

  @Override
  public String getDescription() {
    return "Statistische Stilanalyse: Potenzielle Phrasen";
  }

  @Override
  public String getConfigureText() {
    return "Anzeigen wenn mehr als ...‱ eines Kapitels potenzielle Phrasen sind:";
  }

}
