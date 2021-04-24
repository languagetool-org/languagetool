/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.languagemodel;

import org.languagetool.rules.ngrams.Probability;

import java.util.List;

/**
 * A language model that provides information about ngram probabilities.
 * The implementations don't necessarily deal well with the occurrence = 0 case.
 * @since 2.7
 */
public interface LanguageModel extends AutoCloseable {
  
  /** ngram sentence start marker - note: this is not in the v1 data from Google */
  String GOOGLE_SENTENCE_START = "_START_";
  /** ngram sentence end marker - note: this is not in the v1 data from Google */
  String GOOGLE_SENTENCE_END = "_END_";

  /** 
   * This is not always guaranteed to be a real probability (0.0 to 1.0).
   * Throws exception if context is longer than the ngram index supports. 
   * @since 3.2
   */
  Probability getPseudoProbability(List<String> context);

  @Override
  void close();
  
}
