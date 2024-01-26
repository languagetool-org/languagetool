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
package org.languagetool.tokenizers.es;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.tagging.es.SpanishTagger;
import org.languagetool.tokenizers.WordTokenizer;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own
 * token.
 *
 * @author Juan Martorell
 */
public class SpanishWordTokenizer extends WordTokenizer {
  
  //decimal point between digits
  private static final Pattern DECIMAL_POINT= Pattern.compile("([\\d])\\.([\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // decimal comma between digits
  private static final Pattern DECIMAL_COMMA= Pattern.compile("([\\d]),([\\d])",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  // ordinals
  private static final Pattern ORDINAL_POINT= Pattern.compile("\\b([\\d]+)\\.(º|ª|o|a|er|os|as)\\b",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern PATTERN_1 = Pattern.compile("\u0001\u0001ES_DECIMAL_POINT\u0001\u0001", Pattern.LITERAL);
  private static final Pattern PATTERN_2 = Pattern.compile("\u0001\u0001ES_DECIMAL_COMMA\u0001\u0001", Pattern.LITERAL);
  private static final Pattern PATTERN_3 = Pattern.compile("\u0001\u0001ES_ORDINAL_POINT\u0001\u0001", Pattern.LITERAL);
  private static final Pattern SOFT_HYPHEN = Pattern.compile("\u00AD", Pattern.LITERAL);

  private final String ES_TOKENIZING_CHARACTERS = getTokenizingCharacters() + "−";
  
  @Override
  public List<String> tokenize(final String text) {
    final List<String> l = new ArrayList<>();
    String auxText = text;
    List<String> removedEmojis = replaceEmojis(auxText);
    auxText = removedEmojis.get(0);
    Matcher matcher = DECIMAL_POINT.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001ES_DECIMAL_POINT\u0001\u0001$2");
    matcher = DECIMAL_COMMA.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001ES_DECIMAL_COMMA\u0001\u0001$2");
    matcher = ORDINAL_POINT.matcher(auxText);
    auxText = matcher.replaceAll("$1\u0001\u0001ES_ORDINAL_POINT\u0001\u0001$2");

    StringTokenizer st = new StringTokenizer(auxText, ES_TOKENIZING_CHARACTERS, true);
    String s;
    while (st.hasMoreElements()) {
      s = st.nextToken();
      s = PATTERN_1.matcher(s).replaceAll(".");
      s = PATTERN_2.matcher(s).replaceAll(",");
      s = PATTERN_3.matcher(s).replaceAll(".");
      l.addAll(wordsToAdd(s));   
    }
    return joinEMailsAndUrls(restoreEmojis(l, removedEmojis));
  }

  /* Splits a word containing hyphen(-) if it doesn't exist in the dictionary. */
  private List<String> wordsToAdd(String s) {
    final List<String> l = new ArrayList<>();
    synchronized (this) { // speller is not thread-safe
      if (!s.isEmpty()) {
        if (!s.contains("-")) {
          l.add(s);
        } else {
          // words containing hyphen (-) are looked up in the dictionary
          if (SpanishTagger.INSTANCE.tag(Arrays.asList(SOFT_HYPHEN.matcher(s).replaceAll("").replace('’', '\''))).get(0).isTagged()) {
            l.add(s);
          }
          // some camel-case words containing hyphen (is there any better fix?)
          else if (s.equalsIgnoreCase("mers-cov") || s.equalsIgnoreCase("mcgraw-hill")
              || s.equalsIgnoreCase("sars-cov-2") || s.equalsIgnoreCase("sars-cov") || s.equalsIgnoreCase("ph-metre")
              || s.equalsIgnoreCase("ph-metres")) {
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

}
