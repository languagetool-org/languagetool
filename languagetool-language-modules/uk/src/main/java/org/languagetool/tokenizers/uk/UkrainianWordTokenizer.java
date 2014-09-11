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
package org.languagetool.tokenizers.uk;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.Tokenizer;

/**
 * Tokenizes a sentence into words.
 * Punctuation and whitespace gets its own token.
 * Specific to Ukrainian: apostrophes (0x27 and U+2019) not in the list as they are part of the word
 * 
 * @author Andriy Rysin
 */
public class UkrainianWordTokenizer implements Tokenizer {
  private static final String SPLIT_CHARS = "\u0020\u00A0\u115f\u1160\u1680" 
        + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007" 
        + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
        + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
        + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
        + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb" 
        + ",.;()[]{}<>!?:/|\\\"«»„”“`´‘‛′…¿¡\t\n\r";

  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA_PATTERN = Pattern.compile("([\\d]),([\\d])", Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final char DECIMAL_COMMA_SUBST = '_'; // some unused character to hide comma in decimal number temporary for tokenizer run

  public UkrainianWordTokenizer() {
  }

  @Override
  public List<String> tokenize(String text) {
    text = cleanup(text);
    text = DECIMAL_COMMA_PATTERN.matcher(text).replaceAll("$1" + DECIMAL_COMMA_SUBST + "$2");

    List<String> tokenList = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, SPLIT_CHARS, true);

    while (st.hasMoreElements()) {
      String token = st.nextToken();
      token = token.replace(DECIMAL_COMMA_SUBST, ',');
      tokenList.add( token );
    }

    return tokenList;
  }

  private static String cleanup(String text) {
    return text.replace("\u0301", "").replace("\u00AD", "").replace('’', '\'').replace('ʼ', '\'');
  }
  
}
