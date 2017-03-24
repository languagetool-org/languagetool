/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tagging.disambiguation.rules;

import org.jetbrains.annotations.Nullable;

/**
 * Disambiguated example. Used for testing
 * disambiguator rules.
 * @author Marcin Milkowski
 * @since 0.9.8
 */
public class DisambiguatedExample {

  private final String example;
  private final String input;
  private final String output;

  public DisambiguatedExample(String example) {
    this(example, null, null);
  }
  
  /**
   * @param example Example sentence
   * @param input Ambiguous forms of a token (specify in 'word[lemma/POS]' format)
   * @param output Disambiguated forms of a token (specify in 'word[lemma/POS]' format)
   */
  public DisambiguatedExample(String example, String input, String output) {
    this.example = example;
    this.input = input;
    this.output = output;
  }
  
  /**
   * Return the example that contains the error.
   */
  public String getExample() {
    return example;
  }

  public String getAmbiguous() {
    return input;
  }

  /**
   * Return the possible corrections. May be {@code null}.
   */
  @Nullable
  public String getDisambiguated() {
    return output;
  }

  @Override
  public String toString() {
    return example + ": " + input + " -> " + output;
  }

}
