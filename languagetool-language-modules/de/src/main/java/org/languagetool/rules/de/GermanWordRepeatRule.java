/* LanguageTool, a natural language style checker
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Categories;
import org.languagetool.rules.Example;
import org.languagetool.rules.WordRepeatRule;

/**
 * Check if a word is repeated twice, taking into account an exception 
 * for German where e.g. "..., die die ..." is often okay.
 *   
 * @author Daniel Naber
 */
public class GermanWordRepeatRule extends WordRepeatRule {

  private static final Pattern PREPOSITIONS = Pattern.compile("ab|an|auf|bei|durch|für|in|mit|nach|ohne|über|von|zu");

  public GermanWordRepeatRule(ResourceBundle messages, Language language) {
    super(messages, language);
    super.setCategory(Categories.REDUNDANCY.getCategory(messages));
    addExamplePair(Example.wrong("In diesem Satz <marker>ist ist</marker> ein Wort doppelt."),
                   Example.fixed("In diesem Satz <marker>ist</marker> ein Wort doppelt."));
  }

  @Override
  public String getId() {
    return "GERMAN_WORD_REPEAT_RULE";
  }

  @Override
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position) {
    // Don't mark error for cases like:
    // "wie Honda und Samsung, die die Bezahlung ihrer Firmenchefs..."
    // "Das Haus, in das das Kind läuft."
    if (tokens[position - 1].getToken().length() == 3 && tokens[position - 1].getToken().charAt(0) == 'd') {
      if (position >= 2 && ",".equals(tokens[position - 2].getToken())) {
        return true;
      }
      if (position >= 3 && ",".equals(tokens[position - 3].getToken()) && isPreposition(tokens[position - 2])) {
        return true;
      }
      return false;
    }
    // "Warum fragen Sie sie nicht selbst?"
    if (position != 2 && tokens[position - 1].getToken().equals("Sie") && tokens[position].getToken().equals("sie") ||
        tokens[position - 1].getToken().equals("sie") && tokens[position].getToken().equals("Sie")) {
      return true;
    }
    return false;
  }

  private boolean isPreposition(AnalyzedTokenReadings token) {
    return PREPOSITIONS.matcher(token.getToken()).matches();
  }

}
