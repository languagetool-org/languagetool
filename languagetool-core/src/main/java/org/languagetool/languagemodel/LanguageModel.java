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

/**
 * A very simple language model that contains information about ngram occurrences.
 * @since 2.7
 */
public interface LanguageModel extends AutoCloseable {
  
  /** ngram sentence start marker - note: this is not in the v1 data from Google */
  public static final String GOOGLE_SENTENCE_START = "_START_";
  /** ngram sentence end marker - note: this is not in the v1 data from Google */
  public static final String GOOGLE_SENTENCE_END = "_END_";

  /**
   * Get the occurrence count for the phrase {@code token1 token2}.
   */
  public long getCount(String token1, String token2);

  /**
   * Get the occurrence count for the phrase {@code token1 token2 token3}.
   */
  public long getCount(String token1, String token2, String token3);

  @Override
  public void close();

}
