/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski (http://www.languagetool.org)
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
package org.languagetool.rules.spelling.morfologik;

import morfologik.speller.Speller;
import morfologik.stemming.Dictionary;

import org.languagetool.JLanguageTool;
import org.languagetool.rules.spelling.SpellingCheckRule;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.CharacterCodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Morfologik-based spell checker.
 */
public class MorfologikSpeller {

  private final Dictionary dictionary;
  private final Speller speller;

  /**
   * Creates a speller with the given maximum edit distance.
   * @param filename path in classpath to morfologik dictionary
   */
  public MorfologikSpeller(String filename, int maxEditDistance) throws IOException {
    if (maxEditDistance <= 0) {
      throw new RuntimeException("maxEditDistance must be > 0: " + maxEditDistance);
    }
    final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(filename);
    dictionary = Dictionary.read(url);
    speller = new Speller(dictionary, maxEditDistance);
  }

  /**
   * Creates a speller with a maximum edit distance of one.
   * @param filename path in classpath to morfologik dictionary
   */
  public MorfologikSpeller(String filename) throws IOException {
    this(filename, 1);
  }

  public boolean isMisspelled(String word) {
    return word.length() > 0 
            && !SpellingCheckRule.LANGUAGETOOL.equals(word)
            && !SpellingCheckRule.LANGUAGETOOL_FX.equals(word)
            && speller.isMisspelled(word);
  }

  public List<String> getSuggestions(String word) {
    final List<String> suggestions = new ArrayList<>();
    try {
      suggestions.addAll(speller.findReplacements(word));
      suggestions.addAll(speller.replaceRunOnWords(word));
    } catch (CharacterCodingException e) {
      throw new RuntimeException(e);
    }
    // capitalize suggestions if necessary
    if (dictionary.metadata.isConvertingCase() && StringTools.startsWithUppercase(word)) {
      for (int i = 0; i < suggestions.size(); i++) {
        String uppercaseFirst = StringTools.uppercaseFirstChar(suggestions.get(i));
        // remove capitalized duplicates
        int auxIndex = suggestions.indexOf(uppercaseFirst);
        if (auxIndex > i) {
          suggestions.remove(auxIndex);
        }
        if (auxIndex > -1 && auxIndex < i) {
          suggestions.remove(i);
          i--;
        } else {
          suggestions.set(i, uppercaseFirst);
        }
      }
    }
    return suggestions;
  }

  /**
   * Determines whether the dictionary uses case conversions.
   * @return True when the speller uses spell conversions.
   * @since 2.5
   */
  public boolean convertsCase() {
    return speller.convertsCase();
  }

}
