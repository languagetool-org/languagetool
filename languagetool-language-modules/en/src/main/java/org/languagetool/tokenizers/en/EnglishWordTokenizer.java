/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Marcin Milkowski (http://www.languagetool.org)
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
package org.languagetool.tokenizers.en;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.WordTokenizer;

/**
 * @author Marcin Milkowski
 * @since 2.5
 */
public class EnglishWordTokenizer extends WordTokenizer {

  public EnglishWordTokenizer() {
  }

  @Override
  public String getTokenizingCharacters() {
    return super.getTokenizingCharacters() + "–";  // n-dash
  }

  /**
   * Tokenizes text.
   * The English tokenizer differs from the standard one
   * in two respects:
   * <ol>
   * <li> it does not treat the hyphen as part of the
   * word if the hyphen is at the end of the word;</li>
   * <li> it includes n-dash as a tokenizing character,
   * as it is used without a whitespace in English.
   * </ol>
   * @param text String of words to tokenize.
   */
  @Override
  public List<String> tokenize(String text) {
    List<String> l = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, getTokenizingCharacters(), true);
    while (st.hasMoreElements()) {
      String token = st.nextToken();
      if (token.length() > 1 && token.endsWith("-")) {
        l.add(token.substring(0, token.length() - 1));
        l.add("-");
      } else {
        l.add(token);
      }
    }
    return joinEMailsAndUrls(l);
  }
}
