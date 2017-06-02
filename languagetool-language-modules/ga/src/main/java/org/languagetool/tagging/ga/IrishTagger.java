/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.ga;

import java.util.ArrayList;
import java.util.List;
import java.lang.Character;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;
import java.util.Locale;

/**
 * Irish POS tagger.
 *
 * Based on IrishFST, using FSA.
 * 
 * @author Jim O'Regan
 */
public class IrishTagger extends BaseTagger {
  public IrishTagger() {
    super("/ga/irish.dict", new Locale("ga"));
  }

  private boolean isUpperVowel(char c) {
    switch(c) {
      case 'A':
      case 'E':
      case 'I':
      case 'O':
      case 'U':
      case '\u00c1':
      case '\u00c9':
      case '\u00cd':
      case '\u00d3':
      case '\u00da':
        return true;
      default:
        return false;
    }
  }
  private String toLowerCaseIrish(String s) {
    if(s.length() > 1 && (s.charAt(0) == 'n' || s.charAt(0) == 't') && isUpperVowel(s.charAt(1))) {
      return s.substring(0,1) + "-" + s.substring(1).toLowerCase();
    } else {
      return s.toLowerCase();
    }
  }

  // Not used
  @Override
  public String getManualAdditionsFileName() {
    return "/ga/added.txt";
  }

  @Override
  public final List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {
    List<AnalyzedToken> taggerTokens;
    List<AnalyzedToken> lowerTaggerTokens;
    List<AnalyzedToken> upperTaggerTokens;
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;

    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = toLowerCaseIrish(word);

      taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
      lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(lowerWord));
      final boolean isLowercase = word.equals(lowerWord);

      //normal case
      addTokens(taggerTokens, l);

      if (!isLowercase) {
        //lowercase
        addTokens(lowerTaggerTokens, l);
      }

      //uppercase
      if (lowerTaggerTokens.isEmpty() && taggerTokens.isEmpty()) {
        if (isLowercase) {
          upperTaggerTokens = asAnalyzedTokenListForTaggedWords(word,
              getWordTagger().tag(StringTools.uppercaseFirstChar(word)));
          if (!upperTaggerTokens.isEmpty()) {
            addTokens(upperTaggerTokens, l);
          } else {
            l.add(new AnalyzedToken(word, null, null));
          }
        } else {
          l.add(new AnalyzedToken(word, null, null));
        }
      }
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }

    return tokenReadings;
  }

  private void addTokens(List<AnalyzedToken> taggedTokens, List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      for (AnalyzedToken at : taggedTokens) {
        l.add(at);
      }
    }
  }
}
