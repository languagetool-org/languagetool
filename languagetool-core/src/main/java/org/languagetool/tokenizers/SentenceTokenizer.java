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
package org.languagetool.tokenizers;

import java.util.List;

/**
 * Tokenizes text into sentences.
 */
public interface SentenceTokenizer extends Tokenizer {

  /**
   * Tokenize the given string to sentences.
   */
  @Override
  List<String> tokenize(String text);

  /**
   * @param lineBreakParagraphs if <code>true</code>, single line breaks are assumed to end a paragraph,
   *  with <code>false</code>, only two ore more consecutive line breaks end a paragraph
   */
  void setSingleLineBreaksMarksParagraph(boolean lineBreakParagraphs);

  boolean singleLineBreaksMarksPara();

}
