/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatRule;

/**
 * Avoid false alarms in the word repetition rule.
 */
public class EnglishWordRepeatRule extends WordRepeatRule {

  public EnglishWordRepeatRule(final ResourceBundle messages, final Language language) {
    super(messages, language);
    addExamplePair(Example.wrong("This <marker>is is</marker> just an example sentence."),
                   Example.fixed("This <marker>is</marker> just an example sentence."));
  }

  @Override
  public String getId() {
    return "ENGLISH_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    if (wordRepetitionOf("had", tokens, position)) {
      return true;   // "If I had had time, I would have gone to see him."
    }
    if (wordRepetitionOf("that", tokens, position) && nextPOSIsIn(tokens, position, "NN", "PRP$", "JJ", "VBZ", "VBD")) {
      return true;   // "I don't think that that is a problem."
    }
    if (wordRepetitionOf("Pago", tokens, position)) {
      return true;   // "Pago Pago"
    }
    if (wordRepetitionOf("Wagga", tokens, position)) {
      return true;   // "Wagga Wagga"
    }
    if (wordRepetitionOf("Duran", tokens, position)) {
      return true;   // "Duran Duran"
    }
    if (wordRepetitionOf("sapiens", tokens, position)) {
      return true;   // "Homo sapiens sapiens"
    }
    if (wordRepetitionOf("tse", tokens, position)) {
      return true;   // "tse tse"
    }

    return false;
  }

  private boolean previousWordIsIn(AnalyzedTokenReadings[] tokens, int position, String... words) {
    if (position >= 2) {
      String prevWord = tokens[position - 2].getToken();
      for (String word : words) {
        if (prevWord.equals(word)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean nextPOSIsIn(AnalyzedTokenReadings[] tokens, int position, String... posTags) {
    if (tokens.length > position + 1) {
      for (String posTag : posTags) {
        if (tokens[position + 1].hasPartialPosTag(posTag)) {
          return true;
        }
      }

    }
    return false;
  }

  private boolean wordRepetitionOf(String word, AnalyzedTokenReadings[] tokens, int position) {
    return position > 0 && tokens[position - 1].getToken().equals(word) && tokens[position].getToken().equals(word);
  }

}
