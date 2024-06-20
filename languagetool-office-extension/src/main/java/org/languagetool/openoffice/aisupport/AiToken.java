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
package org.languagetool.openoffice.aisupport;

import org.languagetool.AnalyzedSentence;

/**
 * Defines a easy token
 * @since 6.5
 * @author Fred Kruse
 */
public class AiToken {
  final String token;
  final AnalyzedSentence sentence;
  final int startPos;
  final int endPos;
  final boolean isNonWord;
  
  AiToken(String token, int startPos, AnalyzedSentence sentence, boolean isNonWord) {
    this.sentence = sentence;
    this.token = token;
    this.startPos = startPos;
    this.endPos = startPos + token.length();
    this.isNonWord = isNonWord;
  }

}
