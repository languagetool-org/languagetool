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

package de.danielnaber.languagetool.tagging.disambiguation.rules;

/**
 * Disambiguated example. Used for testing
 * disambiguator rules.
 * @author Marcin Milkowski
 * @since 0.9.8
 */
public class DisambiguatedExample {

  private String example;
  private String inputForms;
  private String outputForms;
  

  public DisambiguatedExample(final String example) {
    this.example = example;
  }
  
  /**
   * @param example
   *      Example sentence
   * @param input
   *       Ambiguous forms of a token 
   *       (specify in 'word[lemma/POS]' format)
   * @param output
   *      Disambiguated forms of a token
   *      (specify in 'word[lemma/POS]' format)  
   */
  public DisambiguatedExample(final String example, final String input, final String output) {
    this(example);
    inputForms = input;
    outputForms = output;
  }
  
  /**
   * Return the example that contains the error.
   */
  public String getExample() {
    return example;
  }

  /**
   * Return the possible corrections. May be null.
   */
  public String getDisambiguated() {
    return outputForms;
  }
  
  public String getAmbiguous() {
    return inputForms;
  }
  
  @Override
  public String toString() {
    return example + ": " + inputForms + " -> " + outputForms;
  }

}
