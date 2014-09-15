/* LanguageTool, a natural language style checker
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
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

package org.languagetool.tokenizers.nl;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.WordTokenizer;

public class DutchWordTokenizer extends WordTokenizer {

  //the string used to tokenize characters
  private final String nlTokenizingChars;

  public DutchWordTokenizer() {
    //remove the apostrophe from the standard tokenizing characters
    nlTokenizingChars = super.getTokenizingCharacters().replace("'", "");
  }

  /**
   * Tokenizes just like WordTokenizer with the exception for words such as
   * "oma's" that contain an apostrophe in their middle.
   * @param text Text to tokenize
   * @return List of tokens
   */
  @Override
  public List<String> tokenize(final String text) {

    final List<String> l = new ArrayList<>();
    final StringTokenizer st = new StringTokenizer(text, nlTokenizingChars, true);
    while (st.hasMoreElements()) {
      String token = st.nextToken();
      if (token.length() > 1) {
        if (token.startsWith("'") && token.endsWith("'") && token.length() > 2) {
          l.add("'");
          l.add(token.substring(1, token.length()-1));
          l.add("'");
        } else if (token.endsWith("'")) {
          int cnt = 0;
          while (token.endsWith("'")) {
            token = token.substring(0, token.length() - 1);
            cnt++;
          }
          l.add(token);
          for (int i = 0; i < cnt; i++) {
            l.add("'");
          }
        } else if (token.startsWith("'")) {
          while (token.startsWith("'")) {
            token = token.substring(1, token.length());
            l.add("'");
          }
          l.add(token);
        } else {
          l.add(token);
        }
      } else {
        l.add(token);
      }
    }
    return joinUrls(l);
  }

  @Override
  public String getTokenizingCharacters() {
    return nlTokenizingChars;
  }
}
