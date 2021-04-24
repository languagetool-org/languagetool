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
package org.languagetool.tokenizers.ro;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.Tokenizer;
import org.languagetool.tokenizers.WordTokenizer;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own
 * token. Like EnglishWordTokenizer except for some characters: eg: "-'
 *
 * @author Ionuț Păduraru
 * @since 20.02.2009 19:53:50
 */
public class RomanianWordTokenizer extends WordTokenizer {

  @Override
  public List<String> tokenize(final String text) {
    final List<String> l = new ArrayList<>();
    final StringTokenizer st = new StringTokenizer(
            text,
            "\u0020\u00A0\u115f\u1160\u1680"
                    + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
                    + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
                    + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
                    + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
                    + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
                    + ",.;()[]{}!?:\"'’‘„“”…\\/\t\n\r«»<>%°" + "-|=", true);
    while (st.hasMoreElements()) {
      l.add(st.nextToken());
    }
    return joinEMailsAndUrls(l);
  }

}
