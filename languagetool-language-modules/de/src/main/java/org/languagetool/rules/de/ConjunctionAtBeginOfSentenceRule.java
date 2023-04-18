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
public class ConjunctionAtBeginOfSentenceRule extends AbstractStatisticSentenceStyleRule {
  
  private static final int DEFAULT_MIN_PERCENT = 10;
/*
  private static final Set<String> fillerWords = new HashSet<>(Arrays.asList("aber", "als", "also", "andererseits", "anschließend", "anschliessend", "anstatt", 
      "außer", "ausserdem", "bevor", "beziehungsweise", "bis", "da", "dadurch", "dafür", "dagegen", "damit", "danach", "dann", "darauf", "darum", "dass", 
      "davor", "dazu", "denn", "deshalb", "dessen", "desto", "desungeachtet", "deswegen", "doch", "ehe", "eh", "entweder", "falls", "ferner", "folglich", 
      "genauso", "geschweige", "immerhin", "indem", "indes", "indessen", "insofern", "insoweit", "inzwischen", "je", "jedoch", "nachdem", "ob", 
      "obgleich", "obschon", "obwohl", "obzwar", "oder", "respektive", "seit", "seitdem", "so", "sodass", "sofern", "solang", "solange", "sondern", "sooft", 
      "soviel", "soweit", "sowie", "sowohl", "später", "statt", "trotzdem", "um", "umso", "und", "ungeachtet", "vorher", "während", "währenddem", 
      "währenddessen", "weder", "weil", "wenn", "wenngleich", "wennschon", "wie", "wiewohl", "wobei", "wohingegen", "zumal", "zuvor", "zwar")); 
*/      
  String sentenceMessage = null;
  
  public ConjunctionAtBeginOfSentenceRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    super(messages, lang, userConfig, DEFAULT_MIN_PERCENT);
  }

  /**
   * Word is a Conjunction
   */
  private static boolean isConjunction(AnalyzedTokenReadings token) {
    return (token.hasPosTagStartingWith("KON"));  
  }
  
  /**
   * Token is comma
   */
  private static boolean isComma(AnalyzedTokenReadings token) {
    return (",".equals(token.getToken()));  
  }
  
  /**
   * Is sentence with modal verb
   */
  @Override
  protected AnalyzedTokenReadings conditionFulfilled(List<AnalyzedTokenReadings> sentence) {
    if (sentence.size() < 3) {
      return null;
    }
    AnalyzedTokenReadings token = null;
    int num = 0;
    if (isOpeningQuote(sentence.get(0))) {
      num++;
    }
    if (isConjunction(sentence.get(num))) {
      token = sentence.get(num);
    }
    if (token == null || token.getToken().equals("Wie") || token.getToken().equals("Seit") || token.getToken().equals("Allerdings")
        || (token.getToken().equals("Aber") && sentence.get(num + 1).getToken().equals("auch"))) {
      return null;
    }
    if (token.getToken().equals("Um")) {
      for (int i = 1; i < sentence.size(); i++) {
        if(isComma(sentence.get(i)) || sentence.get(i).getToken().equals("herum")) {
          return null;
        }
      }
      return token;
    }
    if (!token.hasPosTagStartingWith("KON:UNT") || token.getToken().equals("Sondern")
        || (token.getToken().equals("Auch") && sentence.get(num + 1).getToken().equals("wenn"))) {
      if (token.getToken().equals("Entweder")) {
        for (int i = 1; i < sentence.size(); i++) {
          if(sentence.get(i).getToken().equals("oder")) {
            return null;
          }
        }
      } else if (token.getToken().equals("Sowohl")) {
        for (int i = 1; i < sentence.size() - 1; i++) {
          if(sentence.get(i).getToken().equals("als") && sentence.get(i + 1).getToken().equals("auch")) {
            return null;
          }
        }
      } else if (token.getToken().equals("Weder")) {
        for (int i = 1; i < sentence.size(); i++) {
          if(sentence.get(i).getToken().equals("noch")) {
            return null;
          }
        }
      } else {
        if(sentence.get(sentence.size() - 1).getToken().equals("?")) {
          return null;
        }
        return token;
      }
    }
    for (int i = 2; i < sentence.size(); i++) {
      if(isComma(sentence.get(i))) {
        return null;
      }
    }
    return token;
  }
  
  @Override
  protected boolean excludeDirectSpeech() {
    return true;
  }

  @Override
  protected String getLimitMessage(int limit, double percent) {
    if (limit == 0) {
      return "Eine Konjunktion sollte nur in Ausnahmefällen am Satzanfang verwendet werden. Formulieren Sie den Satz um, falls möglich.";
    }
    return "Mehr als " + limit + "% Sätze beginnen mit einer Konjunktion {" + ((int) (percent +0.5d)) + "%} gefunden. Formulieren Sie den Satz um, falls möglich.";
  }

  @Override
  public String getId() {
    return "SENTENCE_BEGINNING_WITH_CONJUNCTION_DE";
  }

  @Override
  public String getDescription() {
    return "Statistische Stilanalyse: Sätze beginnend mit Konjunktion";
  }

  @Override
  public String getConfigureText() {
    return "Anzeigen wenn mehr als ...% Sätze eines Kapitels mit einer Konjunktion beginnen:";
  }

}
