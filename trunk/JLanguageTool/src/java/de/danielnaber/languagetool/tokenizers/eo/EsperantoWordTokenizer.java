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

package de.danielnaber.languagetool.tokenizers.eo;

import java.util.Arrays;
import java.util.List;

import de.danielnaber.languagetool.tokenizers.WordTokenizer;

public class EsperantoWordTokenizer extends WordTokenizer {

  public EsperantoWordTokenizer() {
  }

  /**
   * Tokenizes just like WordTokenizer with the exception that words
   * such as "dank'" contain an apostrophe within it.
   * 
   * @param text
   *          - Text to tokenize
   * @return List of tokens.
   * 
   *         Note: a special string ##EO_APOS## is used to replace apostrophe
   *         during tokenizing.
   */
  @Override
  public List<String> tokenize(final String text) {
    // TODO: find a cleaner implementation, this is a hack
    String replaced = text.replaceAll(
      "(?<!')\\b([a-zA-ZĉĝĥĵŝŭĈĜĤĴŜŬ]+)'(?![a-zA-ZĉĝĥĵŝŭĈĜĤĴŜŬ-])",
      "$1##EO_APOS##").replaceAll(
      "(?<!')\\b([a-zA-ZĉĝĥĵŝŭĈĜĤĴŜŬ]+)'(?=[a-zA-ZĉĝĥĵŝŭĈĜĤĴŜŬ-])",
      "$1##EO_APOS## ");
    final List<String> tokenList = super.tokenize(replaced);
    final String[] tokens = tokenList.toArray(new String[tokenList.size()]);
    for (int i = 0; i < tokens.length; i++) {
      tokens[i] = tokens[i].replace("##EO_APOS##", "'");
    }
    return Arrays.asList(tokens);
  }
}
