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
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import morfologik.stemming.Dictionary;

import morfologik.stemming.WordData;
import org.jetbrains.annotations.Nullable;
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

  protected final WordTagger wordTagger;
  protected final Locale conversionLocale;

  private final boolean tagLowercaseWithUppercase;
  private final String dictionaryPath;
  private final Dictionary dictionary;

  /**
   * Get the filename for manual additions, e.g., {@code /en/added.txt}, or {@code null}.
   * @since 2.8
   */
  @Nullable
  public abstract String getManualAdditionsFileName();

  /**
   * Get the filename for manual removals, e.g., {@code /en/removed.txt}, or {@code null}.
   * @since 3.2
   */
  @Nullable
  public String getManualRemovalsFileName() {
    return null;
  }

  /** @since 2.9 */
  public BaseTagger(String filename) {
    this(filename, Locale.getDefault(), true);
  }

  /** @since 2.9 */
  public BaseTagger(String filename, Locale conversionLocale) {
    this(filename, conversionLocale, true);
  }

  /** @since 2.9 */
  public BaseTagger(String filename, Locale locale, boolean tagLowercaseWithUppercase) {
    this.dictionaryPath = filename;
    this.conversionLocale = locale;
    this.tagLowercaseWithUppercase = tagLowercaseWithUppercase;
    try {
      URL url = JLanguageTool.getDataBroker().getFromResourceDirAsUrl(filename);
      this.dictionary = Dictionary.read(url);
    } catch (IOException e) {
      throw new RuntimeException("Could not load dictionary from " + filename, e);
    }
    this.wordTagger = initWordTagger();
  }

  /**
   * @since 2.9
   */
  public String getDictionaryPath() {
    return dictionaryPath;
  }

  /**
   * If true, tags from the binary dictionary (*.dict) will be overwritten by manual tags
   * from the plain text dictionary.
   * @since 2.9
   */
  public boolean overwriteWithManualTagger() {
    return false;
  }

  protected WordTagger getWordTagger() {
    return wordTagger;
  }

  private WordTagger initWordTagger() {
    MorfologikTagger morfologikTagger = new MorfologikTagger(dictionary);
    try {
      String manualRemovalFileName = getManualRemovalsFileName();
      ManualTagger removalTagger = null;
      if (manualRemovalFileName != null) {
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(manualRemovalFileName)) {
          removalTagger = new ManualTagger(stream);
        }
      }
      String manualAdditionFileName = getManualAdditionsFileName();
      if (manualAdditionFileName != null) {
        try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(manualAdditionFileName)) {
          ManualTagger manualTagger = new ManualTagger(stream);
          return new CombiningTagger(morfologikTagger, manualTagger, removalTagger, overwriteWithManualTagger());
        }
      } else {
        return morfologikTagger;
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not load manual tagger data from " + getManualAdditionsFileName(), e);
    }
  }

  protected Dictionary getDictionary() {
    return dictionary;
  }

  @Override
  public List<AnalyzedTokenReadings> tag(List<String> sentenceTokens)
      throws IOException {
    List<AnalyzedTokenReadings> tokenReadings = new ArrayList<>();
    int pos = 0;
    for (String word : sentenceTokens) {
      List<AnalyzedToken> l = getAnalyzedTokens(word);
      tokenReadings.add(new AnalyzedTokenReadings(l, pos));
      pos += word.length();
    }
    return tokenReadings;
  }

  protected List<AnalyzedToken> getAnalyzedTokens(String word) {
    List<AnalyzedToken> result = new ArrayList<>();
    String lowerWord = word.toLowerCase(conversionLocale);
    boolean isLowercase = word.equals(lowerWord);
    boolean isMixedCase = StringTools.isMixedCase(word);
    List<AnalyzedToken> taggerTokens = asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(word));
    List<AnalyzedToken> lowerTaggerTokens =
        ! isLowercase
            ? asAnalyzedTokenListForTaggedWords(word, getWordTagger().tag(lowerWord))
            : taggerTokens;

    //normal case:
    addTokens(taggerTokens, result);
    //tag non-lowercase (alluppercase or startuppercase), but not mixedcase word with lowercase word tags:
    if (!isLowercase && !isMixedCase) {
      addTokens(lowerTaggerTokens, result);
    }
    //tag lowercase word with startuppercase word tags:
    if (tagLowercaseWithUppercase
        && lowerTaggerTokens.isEmpty()
        && taggerTokens.isEmpty()
        && isLowercase) {
      List<AnalyzedToken> upperTaggerTokens = asAnalyzedTokenListForTaggedWords(word,
            getWordTagger().tag(StringTools.uppercaseFirstChar(word)));
      if (!upperTaggerTokens.isEmpty()) {
        addTokens(upperTaggerTokens, result);
      }
    }
    // Additional language-dependent-tagging:
    if (result.isEmpty()) {
      List<AnalyzedToken> additionalTaggedTokens = additionalTags(word, getWordTagger());
      addTokens(additionalTaggedTokens, result);
    }
    if (result.isEmpty()) {
      result.add(new AnalyzedToken(word, null, null));
    }
    return result;
  }

  protected List<AnalyzedToken> asAnalyzedTokenList(String word, List<WordData> wdList) {
    List<AnalyzedToken> aTokenList = new ArrayList<>();
    for (WordData wd : wdList) {
      aTokenList.add(asAnalyzedToken(word, wd));
    }
    return aTokenList;
  }

  protected List<AnalyzedToken> asAnalyzedTokenListForTaggedWords(String word, List<TaggedWord> taggedWords) {
    List<AnalyzedToken> aTokenList = new ArrayList<>();
    for (TaggedWord taggedWord : taggedWords) {
      aTokenList.add(asAnalyzedToken(word, taggedWord));
    }
    return aTokenList;
  }

  protected AnalyzedToken asAnalyzedToken(String word, WordData wd) {
    String tag = StringTools.asString(wd.getTag());
    // Remove frequency data from tags (if exists)
    // The frequency data is in the last byte (without a separator)
    if (dictionary.metadata.isFrequencyIncluded() && tag.length() > 1) {
      tag = tag.substring(0, tag.length() - 1);
    }
    return new AnalyzedToken( word, tag, StringTools.asString(wd.getStem()));
  }

  private AnalyzedToken asAnalyzedToken(String word, TaggedWord taggedWord) {
    return new AnalyzedToken(word, taggedWord.getPosTag(), taggedWord.getLemma());
  }

  //please do not make protected, this breaks other languages
  private void addTokens(List<AnalyzedToken> taggedTokens, List<AnalyzedToken> l) {
    if (taggedTokens != null) {
      for (AnalyzedToken at : taggedTokens) {
        l.add(at);
      }
    }
  }

  @Override
  public final AnalyzedTokenReadings createNullToken(String token, int startPos) {
    return new AnalyzedTokenReadings(new AnalyzedToken(token, null, null), startPos);
  }

  @Override
  public AnalyzedToken createToken(String token, String posTag) {
    return new AnalyzedToken(token, posTag, null);
  }

  /**
   * Allows additional tagging in some language-dependent circumstances
   * @param word The word to tag
   * @return Returns list of analyzed tokens with additional tags, or {@code null}
   */
  @Nullable
  protected List<AnalyzedToken> additionalTags(String word, WordTagger wordTagger) {
    return null;
  }

}
