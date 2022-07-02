/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Marcin Milkowski (http://www.languagetool.org)
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
package org.languagetool.tokenizers.pl;

import java.io.IOException;
import java.util.*;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.WordTokenizer;

/**
 * @author Marcin Milkowski
 * @since 2.5
 */
public class PolishWordTokenizer extends WordTokenizer {

  private final String plTokenizing;

  private Tagger tagger;

  // The set of prefixes that are not allowed to be split.
  private static final Set<String> prefixes;

  // Polish prefixes that should never be used to
  // split parts of words
  static {
    final Set<String> tempSet = new HashSet<>(Arrays.asList(
      "arcy",  "neo",
      "pre",   "anty",
      "eks",   "bez",
      "beze",  "ekstra",
      "hiper", "infra",
      "kontr", "maksi",
      "midi",  "między",
      "mini",  "nad",
      "nade",  "około",
      "ponad", "post",
      "pro",   "przeciw",
      "pseudo", "super",
      "śród",  "ultra",
      "wice",  "wokół",
      "wokoło"
    ));
    prefixes = Collections.unmodifiableSet(tempSet);
  }

  public PolishWordTokenizer() {
    plTokenizing = super.getTokenizingCharacters() + "–‚";   // n-dash
  }

  /**
   * Tokenizes text.
   * The Polish tokenizer differs from the standard one
   * in the following respects:
   * <ol>
   * <li> it does not treat the hyphen as part of the
   * word if the hyphen is at the end of the word;</li>
   * <li> it includes n-dash and m-dash as tokenizing characters,
   * as these are not included in the spelling dictionary;
   * <li> it splits two kinds of compound words containing a hyphen,
   * such as <em>dziecko-geniusz</em> (two nouns),
   * <em>polsko-indonezyjski</em> (an ad-adjectival adjective and adjective),
   * <em>polsko-francusko-niemiecki</em> (two ad-adjectival adjectives and adjective),
   * or <em>osiemnaście-dwadzieścia</em> (two numerals)
   * but not words in which the hyphen occurs before a morphological ending
   * (such as <em>SMS-y</em>).
   * </ol>
   * 
   * @param text String of words to tokenize.
   */
  @Override
  public List<String> tokenize(String text) {
    List<String> l = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, plTokenizing, true);
    while (st.hasMoreElements()) {
      String token = st.nextToken();
      if (token.length() > 1) {
        if (token.endsWith("-")) {
          l.add(token.substring(0, token.length() - 1));
          l.add("-");
        } else if (token.charAt(0) == '-') {
          l.add("-");
          l.addAll(tokenize(token.substring(1)));
        } else if (token.contains("-")) {
          String[] tokenParts = token.split("-");
          if (prefixes.contains(tokenParts[0])
              || tagger == null) {
            l.add(token);
          } else if (Character.isDigit(tokenParts[tokenParts.length - 1].charAt(0))) {
            //split numbers at dash or minus sign, 1-10
            for (int i = 0; i < tokenParts.length; i++) {
              l.add(tokenParts[i]);
              if (i != tokenParts.length - 1) {
                l.add("-");
              }
            }
          } else {
            List<String> testedTokens = new ArrayList<>(tokenParts.length + 1);
            Collections.addAll(testedTokens, tokenParts);
            testedTokens.add(token);
            try {

              List<AnalyzedTokenReadings> taggedToks = tagger.tag(testedTokens);
              if (taggedToks.size() == tokenParts.length + 1
                  && !taggedToks.get(tokenParts.length).isTagged()){
                boolean isCompound = false;
                switch (tokenParts.length) {
                  case 2:
                    if ((taggedToks.get(0).hasPosTag("adja") // "niemiecko-indonezyjski"
                        && taggedToks.get(1).hasPartialPosTag("adj:"))
                        || (taggedToks.get(0).hasPartialPosTag("subst:") // "kobieta-wojownik"
                        && taggedToks.get(1).hasPartialPosTag("subst:"))
                        || (taggedToks.get(0).hasPartialPosTag("num:")       //osiemnaście-dwadzieścia
                        && taggedToks.get(1).hasPartialPosTag("num:"))) {
                      isCompound = true;
                    }
                    break;
                  case 3:
                    if (taggedToks.get(0).hasPosTag("adja")
                        && taggedToks.get(1).hasPosTag("adja")
                        && taggedToks.get(2).hasPartialPosTag("adj:")) {
                      isCompound = true;
                    }
                    break;
                }
                if (isCompound) {
                  for (int i = 0; i < tokenParts.length; i++) {
                    l.add(tokenParts[i]);
                    if (i != tokenParts.length - 1) {
                      l.add("-");
                    }
                  }
                } else {
                  l.add(token);
                }
              } else {
                l.add(token);
              }
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }
        } else {
          l.add(token);
        }
      } else {
        l.add(token);
      }
    }
    return joinEMailsAndUrls(l);
  }

  /**
   * Set the tagger to use in tokenizing. This is called
   * in the constructor of Polish class, but if the class is used
   * separately, it has to be called after the constructor to use
   * the hybrid hyphen-tokenizing.
   * 
   * @param tagger The tagger to use (compatible only with the
   * Polish {@link BaseTagger} that uses the delivered PoliMorfologik 2.1
   * or later).
   * 
   * @since 2.5
   */
  public void setTagger(Tagger tagger) {
    this.tagger = tagger;
  }

}
