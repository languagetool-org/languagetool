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

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tagging.BaseTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tools.StringTools;

import java.util.*;

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

  @Override
  public final List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens) {
    List<AnalyzedToken> taggerTokens;
    List<AnalyzedToken> lowerTaggerTokens;
    List<AnalyzedToken> upperTaggerTokens;
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;

    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = Utils.toLowerCaseIrish(word);

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
        List<AnalyzedToken> guessedTokens = asAnalyzedTokenListForTaggedWords(word, filterMorph(word));
        List<AnalyzedToken> lowerGuessedTokens = asAnalyzedTokenListForTaggedWords(word,
          filterMorph(Utils.toLowerCaseIrish(word)));
        if (!guessedTokens.isEmpty()) {
          addTokens(guessedTokens, l);
        }
        if (guessedTokens.isEmpty() && !lowerGuessedTokens.isEmpty()) {
          addTokens(lowerGuessedTokens, l);
        }
        if (isLowercase) {
          upperTaggerTokens = asAnalyzedTokenListForTaggedWords(word,
              getWordTagger().tag(StringTools.uppercaseFirstChar(word)));
          if (!upperTaggerTokens.isEmpty()) {
            addTokens(upperTaggerTokens, l);
          } else {
            List<AnalyzedToken> upperGuessedTokens = asAnalyzedTokenListForTaggedWords(word,
              filterMorph(StringTools.uppercaseFirstChar(word)));
            if(!upperGuessedTokens.isEmpty()) {
              addTokens(upperGuessedTokens, l);
            } else {
              l.add(new AnalyzedToken(word, null, null));
            }
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

  private List<TaggedWord> filterMorph(String in) {
    List<TaggedWord> tagged = new ArrayList<>();
    List<Retaggable> tocheck = Utils.morphWord(in);
    if (tocheck.isEmpty()) {
      return tagged;
    }
    for(Retaggable rt : tocheck) {
      boolean pfx = false;
      List<TaggedWord> cur = getWordTagger().tag(rt.getWord());
      if(rt.getPrefix() != null && !rt.getPrefix().equals("")) {
        pfx = true;
        String tryword = rt.getPrefix() + Utils.lenite(rt.getWord());
        List<TaggedWord> joined = getWordTagger().tag(tryword);
        String hyphword = rt.getPrefix() + "-" + Utils.lenite(rt.getWord());
        List<TaggedWord> hyphen = getWordTagger().tag(hyphword);

        if (!joined.isEmpty()) {
          cur = joined;
          pfx = false;
        } else if(!hyphen.isEmpty()) {
          pfx = false;
          cur = hyphen;
        } else {
          pfx = true;
        }
      }

      if (cur.isEmpty()) {
        continue;
      }
      for (TaggedWord tw : cur) {
        String append = (pfx) ? rt.getAppendTag() + ":NonStdCmpd" : rt.getAppendTag();
        if(tw.getPosTag().matches(rt.getRestrictToPos())) {
          String lemma = (pfx) ? rt.getPrefix() + Utils.lenite(tw.getLemma()) : tw.getLemma();
          tagged.add(new TaggedWord(lemma, tw.getPosTag() + append));
        }
      }
    }
    return tagged;
  }

  private void addTokens(List<AnalyzedToken> taggedTokens, List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      l.addAll(taggedTokens);
    }
  }
}
