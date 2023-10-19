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
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.WordTokenizer;

public class DutchWordTokenizer extends WordTokenizer {

  private static final List<String> QUOTES = Arrays.asList("'", "`", "’",  "‘", "´");

  //the string used to tokenize characters
  private final String nlTokenizingChars;

  public DutchWordTokenizer() {
    //remove the apostrophe etc. from the standard tokenizing characters:
    String chars = super.getTokenizingCharacters() + "\u005f"; //underscore
    for (String quote : QUOTES) {
      chars = chars.replace(quote, "");
    }
    nlTokenizingChars = chars;
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
      String origToken = token;
      if (token.length() > 1) {
        if (startsWithQuote(token) && endsWithQuote(token) && token.length() > 2) {
          l.add(token.substring(0, 1));
          l.add(token.substring(1, token.length()-1));
          l.add(token.substring(token.length()-1));
        } else if (endsWithQuote(token)) {
          int cnt = 0;
          while (endsWithQuote(token)) {
            token = token.substring(0, token.length() - 1);
            cnt++;
          }
          l.add(token);
          for (int i = origToken.length() - cnt; i < origToken.length(); i++) {
            l.add(origToken.substring(i, i + 1));
          }
        } else if (startsWithQuote(token)) {
          while (startsWithQuote(token)) {
            l.add(token.substring(0, 1));
            token = token.substring(1);
          }
          l.add(token);
        } else {
          l.add(token);
        }
      } else {
        l.add(token);
      }
    }
    return joinEMailsAndUrls(l);
  }

  private boolean startsWithQuote(String token) {
    for (String quote : QUOTES) {
      if (token.startsWith(quote)) {
        return true;
      }
    }
    return false;
  }

  private boolean endsWithQuote(String token) {
    for (String quote : QUOTES) {
      if (token.endsWith(quote)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String getTokenizingCharacters() {
    return nlTokenizingChars;
  }
}
