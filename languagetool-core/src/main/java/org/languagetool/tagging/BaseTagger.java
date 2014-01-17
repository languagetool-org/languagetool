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
package org.languagetool.tagging;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.IStemmer;
import morfologik.stemming.WordData;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

/**
 * Base tagger using Morfologik binary dictionaries.
 *
 * @author Marcin Milkowski
 */
public abstract class BaseTagger implements Tagger {

  protected Locale conversionLocale = Locale.getDefault();

  private boolean tagLowercaseWithUppercase = true;
  private Dictionary dictionary;

  /**
   * Get the filename, e.g., {@code /en/english.dict}.
   */
  public abstract String getFileName();

  public void setLocale(Locale locale) {
    conversionLocale = locale;
  }

  protected Dictionary getDictionary() throws IOException {
    if (dictionary == null) {
      synchronized (this) {
        if (dictionary == null) {
          final URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(getFileName());
          dictionary = Dictionary.read(url);
        }
      }
    }
    return dictionary;
  }

  @Override
  public List<AnalyzedTokenReadings> tag(final List<String> sentenceTokens)
          throws IOException {
    List<AnalyzedToken> taggerTokens;
    List<AnalyzedToken> lowerTaggerTokens;
    List<AnalyzedToken> upperTaggerTokens;
    final List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    final IStemmer dictLookup = new DictionaryLookup(getDictionary());

    for (String word : sentenceTokens) {
      final List<AnalyzedToken> l = new ArrayList<>();
      final String lowerWord = word.toLowerCase(conversionLocale);
      taggerTokens = asAnalyzedTokenList(word, dictLookup.lookup(word));
      lowerTaggerTokens = asAnalyzedTokenList(word, dictLookup.lookup(lowerWord));
      final boolean isLowercase = word.equals(lowerWord);
      final boolean isMixedCase = StringTools.isMixedCase(word);

      //normal case
      addTokens(taggerTokens, l);

      //tag non-lowercase (alluppercase or startuppercase), but not mixedcase word with lowercase word tags
      if (!isLowercase && !isMixedCase) {
        addTokens(lowerTaggerTokens, l);
      }

      //tag lowercase word with startuppercase word tags
      if (tagLowercaseWithUppercase) {
        if (lowerTaggerTokens.isEmpty() && taggerTokens.isEmpty()) {
          if (isLowercase) {
            upperTaggerTokens = asAnalyzedTokenList(word,
                dictLookup.lookup(StringTools.uppercaseFirstChar(word)));
            if (!upperTaggerTokens.isEmpty()) {
              addTokens(upperTaggerTokens, l);
            }
          }
        }
      }

      // Additional language-dependent-tagging 
      if (l.isEmpty()) {
        List<AnalyzedToken> additionalTaggedTokens = additionalTags(word);
        addTokens(additionalTaggedTokens, l);
      }

      if (l.isEmpty()) {
        l.add(new AnalyzedToken(word, null, null));
      }

      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }

    return tokenReadings;
  }

  protected List<AnalyzedToken> asAnalyzedTokenList(final String word, final List<WordData> wdList) {
    final List<AnalyzedToken> aTokenList = new ArrayList<>();
    for (WordData wd : wdList) {
      aTokenList.add(asAnalyzedToken(word, wd));
    }
    return aTokenList;
  }

  protected AnalyzedToken asAnalyzedToken(final String word, final WordData wd) {
    String tag = StringTools.asString(wd.getTag());
    // Remove frequency data from tags (if exists)
    // The frequency data is in the last byte after a separator
    if (dictionary.metadata.isFrequencyIncluded() && tag.length()>2) {
      tag = tag.substring(0, tag.length()-2);
    }
    return new AnalyzedToken(
        word,
        tag,
        StringTools.asString(wd.getStem()));
  }

  protected void addTokens(final List<AnalyzedToken> taggedTokens, final List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      for (AnalyzedToken at : taggedTokens) {
        l.add(at);
      }
    }
  }

  @Override
  public final AnalyzedTokenReadings createNullToken(final String token, final int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), startPos);
  }

  @Override
  public AnalyzedToken createToken(String token, String posTag) {
    return new AnalyzedToken(token, posTag, null);
  }

  public void dontTagLowercaseWithUppercase() {
    tagLowercaseWithUppercase = false;
  }

  /*
   *  Additional tagging in some language-dependent circumstances
   */
  public List<AnalyzedToken> additionalTags(String word) {
    return null;
  }

}
