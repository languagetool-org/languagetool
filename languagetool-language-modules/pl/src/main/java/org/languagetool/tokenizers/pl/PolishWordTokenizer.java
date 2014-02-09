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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.Tagger;
import org.languagetool.tokenizers.WordTokenizer;

/**
 * @author Marcin Milkowski
 * @since 2.5
 */
public class PolishWordTokenizer extends WordTokenizer {

  private final String plTokenizing;

  private Tagger tagger;

  /**
   * The set of prefixes that are not allowed to be split.
   */
  private final static Set<String> prefixes;

  //Polish prefixes that should never be used to
  //split parts of words
  static {
    final Set<String> tempSet = new HashSet<String>();
    tempSet.add("arcy");  tempSet.add("neo");
    tempSet.add("pre");   tempSet.add("anty");
    tempSet.add("eks");   tempSet.add("bez");
    tempSet.add("beze");  tempSet.add("ekstra");
    tempSet.add("hiper"); tempSet.add("infra");
    tempSet.add("kontr"); tempSet.add("maksi");
    tempSet.add("midi");  tempSet.add("między");
    tempSet.add("mini");  tempSet.add("nad");
    tempSet.add("nade");  tempSet.add("około");
    tempSet.add("ponad"); tempSet.add("post");
    tempSet.add("pro");   tempSet.add("przeciw");
    tempSet.add("pseudo"); tempSet.add("super");
    tempSet.add("śród");  tempSet.add("ultra");
    tempSet.add("wice");  tempSet.add("wokół");
    tempSet.add("wokoło");
    prefixes = Collections.unmodifiableSet(tempSet);
  }


  public PolishWordTokenizer() {
    plTokenizing = super.getTokenizingCharacters() + "–—";   // n-dash, m-dash
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
   * such as <em>dziecko-geniusz</em> (two nouns) or
   * <em>polsko-indonezyjski</em> (a post-positional adjective and adjective),
   * but not words in which the hyphen occurs before a morphological ending
   * (such as <em>SMS-y</em>).
   * </ol>
   * 
   * @param text String of words to tokenize.
   */
  @Override
  public List<String> tokenize(final String text) {
    final List<String> l = new ArrayList<>();
    final StringTokenizer st = new StringTokenizer(text,
        plTokenizing, true);
    while (st.hasMoreElements()) {
      final String token = st.nextToken();
      if (token.length() > 1) {
        if (token.endsWith("-")) {
          l.add(token.substring(0, token.length() - 1));
          l.add("-");
        } else if (token.startsWith("-")) {
          l.add("-");
          l.add(token.substring(1, token.length()));
        } else if (token.contains("-")) {
          String[] tokenParts = token.split("-");
          if (tokenParts.length == 2) {
            List<String> testedTokens = new ArrayList<>(3);
            testedTokens.add(tokenParts[0]);
            testedTokens.add(tokenParts[1]);
            testedTokens.add(token);
            if (prefixes.contains(tokenParts[0])
                || tagger == null) {
              l.add(token);
            } else {
              try { // we rely on the tagger here,
                // but since the tagger cannot be setup
                // in the constructor, there is a soft-fail
                // here: we simply don't split these words
                // for which the tagger information is needed.
                List<AnalyzedTokenReadings> taggedToks = tagger.tag(testedTokens);
                if (taggedToks.size() == 3
                    && !taggedToks.get(2).isTagged()
                    // "niemiecko-indonezyjski"
                    && (taggedToks.get(0).hasPosTag("adja")
                        && taggedToks.get(1).hasPartialPosTag("adj:")
                        // "kobieta-wojownik"
                        || taggedToks.get(0).hasPartialPosTag("subst:")
                        && taggedToks.get(1).hasPartialPosTag("subst:"))) {
                  l.add(tokenParts[0]);
                  l.add("-");
                  l.add(tokenParts[1]);
                } else {
                  l.add(token);
                }
              } catch (IOException e) { // fail gracefully
                l.add(token);
              }
            }
          }
          else {
            l.add(token);
          }
        }
        else {
          l.add(token);
        }
      } else {
        l.add(token);
      }
    }
    return joinUrls(l);
  }

  public void setupTagger(Tagger tagger) {
    this.tagger = tagger;
  }

}
