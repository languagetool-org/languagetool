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
package org.languagetool.rules.it;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.WordRepeatRule;

/**
 * Avoid false alarms in the word repetition rule.
 */
public class ItalianWordRepeatRule extends WordRepeatRule {

  public ItalianWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
  }

  @Override
  public String getId() {
    return "ITALIAN_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    if (wordRepetitionOf("via", tokens, position)) {
      return true;   // "Il lessico si andava via via modificando."
    }
    if (wordRepetitionOf("così", tokens, position)) {
      return true;   // "Mi è sembrato così così."
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
    return false;
  }

  private boolean wordRepetitionOf(String word, AnalyzedTokenReadings[] tokens, int position) {
    if (position > 2) {
      return (tokens[position - 1].getToken().equals(word) && tokens[position].getToken().equals(word));
    }
    if (position == 2) {
      return (tokens[1].getToken().equalsIgnoreCase(word) && tokens[2].getToken().equals(word));
    }
    return false;
  }

}
