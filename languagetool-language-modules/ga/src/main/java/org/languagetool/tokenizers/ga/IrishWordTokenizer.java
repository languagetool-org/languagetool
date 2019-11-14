/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Jaume Ortolà
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

/**
 * Tokenises a sentence into words. Punctuation and whitespace gets its own token.
 * Special treatment for hyphens in Irish.
 *
 * Based on CatalanWordTokenizer.java
 */
package org.languagetool.tokenizers.ga;

import org.languagetool.JLanguageTool;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tagging.ga.Utils;
import org.languagetool.tokenizers.WordTokenizer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class IrishWordTokenizer extends WordTokenizer {
  private static final String DICT_FILENAME = "/ga/hunspell/ga_IE.dict";
  protected MorfologikSpeller speller;

  public IrishWordTokenizer() {
    // lazy init
    if (speller == null) {
      if (JLanguageTool.getDataBroker().resourceExists(DICT_FILENAME)) {
        try {
          speller = new MorfologikSpeller(DICT_FILENAME);
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  public List<String> tokenize(final String text) {
    final List<String> l = new ArrayList<>();
    String auxText = text;

    final StringTokenizer st = new StringTokenizer(auxText,
      "\u0020\u00A0\u115f\u1160\u1680"
        + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
        + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
        + "\u2012\u2013\u2014\u2015\u2022"
        + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
        + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
        + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
        + "|,.;()[]{}=*#∗+×÷<>!?:~/\\\"'«»„”“‘’`´…¿¡\t\n\r·", true);
    String s;
    String groupStr;

    while (st.hasMoreElements()) {
      s = st.nextToken();
      l.addAll(wordsToAdd(s));
    }
    return joinEMailsAndUrls(l);
  }

  /* Splits a word containing hyphen(-) if it doesn't exist in the dictionary. */
  private List<String> wordsToAdd(String s) {
    final List<String> l = new ArrayList<>();
    synchronized (this) { //speller is not thread-safe
      if (!s.isEmpty()) {
        if (!s.contains("-")) {
          l.add(s);
        } else {
          // words containing hyphen (-) are looked up in the dictionary
          if (!speller.isMisspelled(s)) {
            l.add(s);
          } else if(hyphenBeginning(s)) {
            l.add(s);
          } else {
            // if not found, the word is split
            final StringTokenizer st2 = new StringTokenizer(s, "-", true);
            while (st2.hasMoreElements()) {
              l.add(st2.nextToken());
            }
          }
        }
      }
      return l;
    }
  }

  private boolean hyphenBeginning(String s) {
    String lc = s.toLowerCase();
    if(s.length() < 3) {
      return false;
    }
    if(lc.startsWith("t-s")) {
      return true;
    } else if(lc.startsWith("n-") && Utils.isLowerVowel(lc.charAt(2))) {
      return true;
    } else if(lc.startsWith("t-") && Utils.isLowerVowel(lc.charAt(2))) {
      return true;
    } else if(lc.startsWith("h-") && Utils.isLowerVowel(lc.charAt(2))) {
      return true;
    } else {
      return false;
    }
  }
}
