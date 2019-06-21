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
package org.languagetool.tagging.pl;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Polish POS tagger based on FSA morphological dictionaries.
 * 
 * @author Marcin Milkowski
 */
public class PolishTagger extends BaseTagger {

  private final Locale plLocale = new Locale("pl");

  public PolishTagger() {
    super("/pl/polish.dict");
  }

  @Override
  public String getManualAdditionsFileName() {
    return "/pl/added.txt";
  }

  @Override
  public final List<AnalyzedTokenReadings> tag(List<String> sentenceTokens) {
    List<AnalyzedToken> taggerTokens;
    List<AnalyzedToken> lowerTaggerTokens;
    List<AnalyzedToken> upperTaggerTokens;    
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;

    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = new ArrayList<>();
      String lowerWord = word.toLowerCase(plLocale);
      taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
      lowerTaggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(lowerWord));
      boolean isLowercase = word.equals(lowerWord);

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
        String[] tagsArr = StringTools.asString(at.getPOSTag()).split("\\+");
        for (String currTag : tagsArr) {
          l.add(new AnalyzedToken(at.getToken(), currTag, at.getLemma()));
        }
      }
    }
  }

}