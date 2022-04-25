/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.fr;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.Tag;

import java.util.Arrays;
import java.util.ResourceBundle;

/**
 * A rule that matches spaces before ?,:,; and ! (required for correct French
 * punctuation).
 *
 * @see <a href=
 *      "http://unicode.org/udhr/n/notes_fra.html">http://unicode.org/udhr/n/notes_fra.html</a>
 *
 * @author Marcin Miłkowski
 */
public class QuestionWhitespaceStrictRule extends QuestionWhitespaceRule {

  public QuestionWhitespaceStrictRule(ResourceBundle messages, Language language) {
    super(messages, language);
    setTags(Arrays.asList(Tag.picky));
    this.setDefaultOff();
  }

  @Override
  public String getId() {
    return "FRENCH_WHITESPACE_STRICT";
  }

  @Override
  public String getDescription() {
    return "Insertion des espaces fines insécables";
  }

  @Override
  protected boolean isAllowedWhitespaceChar(AnalyzedTokenReadings[] tokens, int i) {
    // Strictly speaking, the character before ?!; should be an
    // "espace fine insécable" (U+202f). In practise, an
    // "espace insécable" (U+00a0) is also often used. Let's accept both.
    if (i < 0) {
      return false;
    }
    return tokens[i].getToken().equals("\u202f") || tokens[i].getToken().equals("\u00a0");
  }

}
