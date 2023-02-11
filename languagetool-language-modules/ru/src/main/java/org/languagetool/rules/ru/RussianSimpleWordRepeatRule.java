/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.ru;

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.WordRepeatRule;

/**
 * Avoid false alarms in the word repetition rule.
 * @author Yakov Reztsov
 */
public class RussianSimpleWordRepeatRule extends WordRepeatRule {

  public RussianSimpleWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
  }

  @Override
  public String getId() {
    return "WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    if (wordRepetitionOf("-", tokens, position)) {
      return true;
    }
    if (wordRepetitionOf("и", tokens, position)) {
      return true;
    }
    if (wordRepetitionOf("по", tokens, position)) {
      return true;
    }
    if (tokens[position - 1].getToken().equals("ПО") && tokens[position].getToken().equals("по")) {
        return true;   // "ПО по"
    }
    if (tokens[position - 1].getToken().equals("по") && tokens[position].getToken().equals("ПО")) {
        return true;   // "по ПО"
    }
    if (wordRepetitionOf("что", tokens, position)) {
      return true;
    }

    if (tokens[position].getToken().matches("[a-zA-Zа-яёА-ЯЁ]") && position > 1 && tokens[position - 1].getToken().matches("[a-zA-Zа-яёА-ЯЁ]") ) {
      // spelling with spaces in between: "L L"
      return true;
    }

    return super.ignore(tokens, position);
  }

}
