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
package org.languagetool.synthesis;

import java.io.IOException;

import org.languagetool.AnalyzedToken;

/**
 * Part-of-speech synthesizer interface. Implementations are 
 * heavily language-dependent.
 * 
 * @author Marcin Miłkowski
 */
public interface Synthesizer {

  /**
   * Generates a form of the word with a given POS tag for a given lemma.
   * @param token the token to be used for synthesis
   * @param posTag POS tag of the form to be generated
   */
  String[] synthesize(AnalyzedToken token, String posTag) throws IOException;

  /**
   * Generates a form of the word with a given POS tag for a given lemma.
   * POS tag can be specified using regular expressions. 
   * @param token the token to be used for synthesis
   * @param posTag POS tag of the form to be generated
   * @param posTagRegExp Specifies whether the posTag string is a regular expression.
   */
  String[] synthesize(AnalyzedToken token, String posTag, boolean posTagRegExp) throws IOException;
  
  /**
   * Gets a corrected version of the POS tag used for synthesis.
   * Useful when the tagset defines special disjunction that
   * need to be converted into regexp disjunctions.
   * @param posTag original POS tag to correct
   * @return converted POS tag
   */
  String getPosTagCorrection(String posTag);
}
