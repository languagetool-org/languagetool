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
import java.util.Locale;

/**
 * Morfologik-based spell checker.
 */
public class MorfologikSpeller {

  private final Speller speller;
  private final Locale conversionLocale;

  /**
   * Creates a speller with the given maximum edit distance.
   * @param filename path in classpath to morfologik dictionary
   * @param conversionLocale used when transforming the word to lowercase
   */
  public MorfologikSpeller(String filename, Locale conversionLocale, int maxEditDistance) throws IOException {
    if (maxEditDistance <= 0) {
      throw new RuntimeException("maxEditDistance must be > 0: " + maxEditDistance);
    }
    final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(filename);
    speller = new Speller(Dictionary.read(url), maxEditDistance);
    this.conversionLocale = conversionLocale != null ? conversionLocale : Locale.getDefault();
  }

  /**
   * Creates a speller with a maximum edit distance of one.
   * @param filename path in classpath to morfologik dictionary
   * @param conversionLocale used when transforming the word to lowercase
   */
  public MorfologikSpeller(String filename, Locale conversionLocale) throws IOException {
    this(filename, conversionLocale, 1);
  }

  /**
   * Creates a speller with a maximum edit distance of one.
   * @param filename path in classpath to morfologik dictionary
   */
  public MorfologikSpeller(String filename) throws IOException {
    this(filename, null);
  }

  public boolean isMisspelled(String word) {
    boolean isAlphabetic = true;
    if (word.length() == 1) { // dictionaries usually do not contain punctuation
      isAlphabetic = StringTools.isAlphabetic(word.charAt(0));
    }
    return word.length() > 0 && isAlphabetic
            && !containsDigit(word)
            && !SpellingCheckRule.LANGUAGETOOL.equals(word)
            && !SpellingCheckRule.LANGUAGETOOL_FX.equals(word)
            && !speller.isInDictionary(word)
            && !(!StringTools.isMixedCase(word) 
                && speller.isInDictionary(word.toLowerCase(conversionLocale)));
  }

  public List<String> getSuggestions(String word) {
    final List<String> suggestions = new ArrayList<>();
    try {
      suggestions.addAll(speller.findReplacements(word));
      if (suggestions.isEmpty() && !word.toLowerCase(conversionLocale).equals(word)) {
        suggestions.addAll(speller.findReplacements(word.toLowerCase(conversionLocale)));
      }
      suggestions.addAll(speller.replaceRunOnWords(word));
    } catch (CharacterCodingException e) {
      throw new RuntimeException(e);
    }
    return suggestions;
  }

  private boolean containsDigit(final String s) {
    for (int k = 0; k < s.length(); k++) {
      if (Character.isDigit(s.charAt(k))) {
        return true;
      }
    }
    return false;
  }

}
