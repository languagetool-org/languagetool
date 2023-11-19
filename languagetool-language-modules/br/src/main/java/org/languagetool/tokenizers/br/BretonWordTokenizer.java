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

package org.languagetool.tokenizers.br;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import org.languagetool.tokenizers.WordTokenizer;

import static java.util.regex.Pattern.*;

/**
 * @author Dominique Pelle
 */
public class BretonWordTokenizer extends WordTokenizer {

  private static final Pattern REPL_PATTERN_1 = compile("([Cc])['’‘ʼ]([Hh])");
  private static final Pattern REPL_PATTERN_2 = compile("(\\p{L})['’‘ʼ]");
  private static final Pattern REPL_PATTERN_3 = compile("\u0001\u0001BR@APOS\u0001\u0001", LITERAL);

  /**
   * Tokenizes just like WordTokenizer with the exception that "c’h"
   * is not split. "C’h" is considered as a letter in breton (trigraph)
   * and it occurs in many words.  So tokenizer should not split it.
   * Also split things like "n’eo" into 2 tokens only "n’" + "eo".
   * 
   * @param text Text to tokenize
   * @return List of tokens.
   *         Note: a special string ##BR_APOS## is used to replace apostrophes
   *         during tokenizing.
   */
  @Override
  public List<String> tokenize(String text) {

    // FIXME: this is a bit of a hacky way to tokenize.  It should work
    // but I should work on a more elegant way.
    String replaced = REPL_PATTERN_1.matcher(text).replaceAll("$1\u0001\u0001BR@APOS\u0001\u0001$2");
    replaced = REPL_PATTERN_2.matcher(replaced).replaceAll("$1\u0001\u0001BR@APOS\u0001\u0001 ");

    List<String> tokenList = super.tokenize(replaced);
    List<String> tokens = new ArrayList<>();

    // Put back apostrophes and remove spurious spaces.
    Iterator<String> itr = tokenList.iterator();
    while (itr.hasNext()) {
      String word = REPL_PATTERN_3.matcher(itr.next()).replaceAll("’");
      tokens.add(word);
      if (!word.equals("’") && word.endsWith("’")) {
        itr.next(); // Skip the next spurious white space.
      }
    }
    return tokens;
  }
}
