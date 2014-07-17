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
 * Information about ngram occurrences.
 * @since 2.7
 */
public interface LanguageModel {
  
  public static final String GOOGLE_SENTENCE_START = "_START_";
  public static final String GOOGLE_SENTENCE_END = "_END_";

  public long getCount(String token1, String token2);
  
  public long getCount(String token1, String token2, String token3);
  
  public void close();

}
