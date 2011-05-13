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

package de.danielnaber.languagetool.tokenizers.nl;

import java.util.Arrays;
import java.util.List;

import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class DutchWordTokenizer extends WordTokenizer {

  public DutchWordTokenizer() {
  }

  /**
   * Tokenizes just like WordTokenizer with the exception for words such as
   * "oma's" that contains an apostrophe in their middle.
   * 
   * @param text
   *          - Text to tokenize
   * @return List of tokens.
   * 
   *         Note: a special string ##NL_APOS## is used to replace apostrophe
   *         during tokenizing.
   */
  @Override
  public List<String> tokenize(final String text) {
    // TODO: find a cleaner implementation, this is a hack
    final List<String> tokenList = super.tokenize(text.replaceAll(
        "([\\p{L}])'([\\p{L}])", "$1##NL_APOS##$2"));
    final String[] tokens = tokenList.toArray(new String[tokenList.size()]);
    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = tokens[i].replace("##NL_APOS##", "'");
    }
    return Arrays.asList(tokens);
  }
}
