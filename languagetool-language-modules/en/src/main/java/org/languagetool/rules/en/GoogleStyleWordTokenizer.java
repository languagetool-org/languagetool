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
package org.languagetool.rules.en;

import org.languagetool.tokenizers.WordTokenizer;

import java.util.*;

/**
 * Tokenize sentences to tokens like Google does for its ngram index. Note: there
 * doesn't seem to be official documentation about the way Google tokenizes there,
 * so this is just an approximation.
 * @since 3.2
 */
public class GoogleStyleWordTokenizer extends WordTokenizer {

  @Override
  public String getTokenizingCharacters() {
    return super.getTokenizingCharacters() + "-";
  }
  
  @Override
  public List<String> tokenize(String text) {
    List<String> tokens = super.tokenize(text);
    String prev = null;
    Stack<String> l = new Stack<>();
    for (String token : tokens) {
      if ("'".equals(prev)) {
        // TODO: add more cases if needed:
        if (token.equals("m")) {
          l.pop();
          l.push("'m");
        } else if (token.equals("re")) {
          l.pop();
          l.push("'re");
        } else if (token.equals("ve")) {
          l.pop();
          l.push("'ve");
        } else if (token.equals("ll")) {
          l.pop();
          l.push("'ll");
        } else {
          l.push(token);
        }
      } else {
        l.push(token);
      }
      prev = token;
    }
    return l;
  }

}
