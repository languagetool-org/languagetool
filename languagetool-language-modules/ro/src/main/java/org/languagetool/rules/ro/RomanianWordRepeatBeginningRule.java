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
package org.languagetool.rules.ro;

import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.WordRepeatBeginningRule;

/**
 * {@link WordRepeatBeginningRule} implementation for Romanian language.
 * 
 * @author Ionuț Păduraru
 */
public class RomanianWordRepeatBeginningRule extends WordRepeatBeginningRule {

  public RomanianWordRepeatBeginningRule(ResourceBundle messages, Language language) {
    super(messages, language);
  }

  @Override
  public String getId() {
    return "ROMANIAN_WORD_REPEAT_BEGINNING_RULE";
  }

  /**
   * Indicates if ambiguous adverbs are to be considered.
   * Ambiguous adverbs are words that have the 'adverb' tag along with other tags 
   * E.g.  romanian word "Și" can be "adverb predicativ", "conjuncție coordonatoare" or "pronume (își)".
   * @return true if ambiguous adverbs are to be considered.
   */
  protected boolean allowAmbiguousAdverbs() {
    return false;
  }
  
  @Override
  protected boolean isAdverb(AnalyzedTokenReadings token) {
    boolean isAdverb = false;
    List<AnalyzedToken> readings = token.getReadings();
    for (AnalyzedToken analyzedToken : readings) {
      if (analyzedToken.getPOSTag() != null) {
        if (analyzedToken.getPOSTag().startsWith("G")) { // see file /resource/ro/coduri.html for POS tag descriptions
          isAdverb = true;
        } else {
          if (!allowAmbiguousAdverbs()) {
            return false;
          }
        }
      }
    }
    return isAdverb;
  }

}
