/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import java.util.ArrayList;
import java.util.List;

import org.languagetool.rules.Rule;

/**
 * Dummy class for UserConfig
 * has to be overridden by concrete linguistic service e.g. by LO extension
 * @author Fred Kruse
 * @since 4.4
 */
public class LinguServices {
  
  /**
   * Get all synonyms of a word as list of strings.
   */
  public List<String> getSynonyms(String word, Language lang) {
    return new ArrayList<String>();
  }
  
  /**
   * Returns true if the spell check is positive
   */
  public boolean isCorrectSpell(String word, Language lang) {
    return false;
  }
  
  /**
   * Returns the number of syllable of a word
   * Returns -1 if the word was not found or anything goes wrong
   */
  public int getNumberOfSyllables(String word, Language lang) {
    return 0;
  }
  
  /**
   * Set a thesaurus relevant rule
   */
  public void setThesaurusRelevantRule (Rule rule) {
  }
  
}
