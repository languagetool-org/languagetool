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

import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.AbstractStatisticSentenceStyleRule;

/**
 * A rule that gives Hints about the use of German filler words.
 * The Hints are only given when the percentage of filler words per chapter/text exceeds the given limit.
 * A limit of 0 shows all used filler words. Direct speech or citation is excluded otherwise. 
 * This rule detects no grammar error but gives stylistic hints (default off).
 * @author Fred Kruse
 * @since 4.2
 */
public class SentenceWithModalVerbRule extends AbstractStatisticSentenceStyleRule {
  
  private static final int DEFAULT_MIN_PERCENT = 18;
  
  String sentenceMessage = null;
  
  public SentenceWithModalVerbRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig, DEFAULT_MIN_PERCENT);
  }

  /**
   * Word is a modal verb
   */
  private static boolean isModalVerb(AnalyzedTokenReadings token) {
    return (token.hasPosTagStartingWith("VER:MOD"));  
  }
  
  /**
   * Is sentence with modal verb
   */
  @Override
  protected AnalyzedTokenReadings conditionFulfilled(List<AnalyzedTokenReadings> sentence) {
    for (int i = 0; i < sentence.size(); i++) {
      if(isModalVerb(sentence.get(i))) {
        AnalyzedTokenReadings token = sentence.get(i);
        for (i++; i < sentence.size(); i++) {
          if(sentence.get(i).hasPosTagStartingWith("VER:INF")) {
            return token;
          } else if(isMark(sentence.get(i))) {
            return null;
          }
        }
      } else if (sentence.get(i).hasPosTagStartingWith("VER:INF")) {
        for (i++; i < sentence.size(); i++) {
          if(isModalVerb(sentence.get(i))) {
            return sentence.get(i);
          } else if(isMark(sentence.get(i))) {
            return null;
          }
        }
      }
    }
    return null;
  }
  
  @Override
  protected boolean excludeDirectSpeech() {
    return true;
  }

  @Override
  protected String getLimitMessage(int limit, double percent) {
    if (limit == 0) {
      return "Modalverb: Modalverben blähen den Text häufig auf und sollten vermieden werden.";
    }
    return "Mehr als " + limit + "% Sätze mit Modalverben {" + ((int) (percent +0.5d)) + "%} gefunden. Modalverben blähen den Text häufig auf und sollten vermieden werden.";
  }

  @Override
  public String getId() {
    return "SENTENCE_WITH_MODAL_VERB_DE";
  }

  @Override
  public String getDescription() {
    return "Statistische Stilanalyse: Sätze mit Modalverben";
  }

  @Override
  public String getConfigureText() {
    return "Anzeigen wenn mehr als ...% Sätze eines Kapitels Modalverben enthalten:";
  }

}
