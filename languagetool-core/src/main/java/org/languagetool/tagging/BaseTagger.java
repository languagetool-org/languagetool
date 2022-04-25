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

import morfologik.stemming.Dictionary;
import morfologik.stemming.WordData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Base tagger using Morfologik binary dictionaries.
 *
 * @author Marcin Milkowski
 */
public abstract class BaseTagger implements Tagger {

  private static final String MANUAL_ADDITIONS_FILE = "/added.txt";
  private static final String CUSTOM_MANUAL_ADDITIONS_FILE = "/added_custom.txt";
  private static final String MANUAL_REMOVALS_FILE = "/removed.txt";
  private static final String CUSTOM_MANUAL_REMOVALS_FILE = "/removed_custom.txt";

  protected final WordTagger wordTagger;
  protected final Locale locale;

  private final boolean tagLowercaseWithUppercase;
  private final String dictionaryPath;
  private final Dictionary dictionary;

  /**
   * Get the filenames for manual additions, e.g., {@code /en/added.txt}.
   * @since 5.0
   */
  @NotNull
  public List<String> getManualAdditionsFileNames() {
    return Arrays.asList(locale.getLanguage() +  MANUAL_ADDITIONS_FILE, locale.getLanguage() + CUSTOM_MANUAL_ADDITIONS_FILE);
  }

  /**
   * Get the filenames for manual removals, e.g., {@code /en/removed.txt}.
   * @since 5.0
   */
  @NotNull
  public List<String> getManualRemovalsFileNames() {
    return Arrays.asList(locale.getLanguage() +  MANUAL_REMOVALS_FILE, locale.getLanguage() + CUSTOM_MANUAL_REMOVALS_FILE);
  }

  /** @since 2.9 */
  public BaseTagger(String filename, Locale locale) {
    this(filename, locale, true);
  }

  /** @since 2.9 */
  public BaseTagger(String filename, Locale locale, boolean tagLowercaseWithUppercase) {
    this(filename, locale, tagLowercaseWithUppercase, false);
  }

  /**
   * @param internTags true if string tags should be interned
   * @since 4.9
   */
  public BaseTagger(String filename, Locale locale, boolean tagLowercaseWithUppercase, boolean internTags) {
    this.dictionaryPath = filename;
    this.locale = locale;
    this.tagLowercaseWithUppercase = tagLowercaseWithUppercase;
    this.dictionary = MorfologikSpeller.getDictionaryWithCaching(filename);
    this.wordTagger = initWordTagger(internTags);
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

  private WordTagger initWordTagger(boolean internTags) {
    MorfologikTagger morfologikTagger = new MorfologikTagger(dictionary, internTags);
    try {
      ManualTagger removalTagger = null;
      InputStream stream = null;
      try {
        for (String file : getManualRemovalsFileNames()) {
          for (URL url : JLanguageTool.getDataBroker().getFromResourceDirAsUrls(file)) {
            if (stream == null) {
              stream = url.openStream();
            } else {
              stream = new SequenceInputStream(stream, url.openStream());
            }
          }
        }
        if (stream != null) {
          removalTagger = new ManualTagger(stream, internTags);
        }
      } finally {
        if (stream != null) {
          stream.close();
        }
      }

      stream = null;
      try {
        for (String file : getManualAdditionsFileNames()) {
          for (URL url : JLanguageTool.getDataBroker().getFromResourceDirAsUrls(file)) {
            if (stream == null) {
              stream = url.openStream();
            } else {
              stream = new SequenceInputStream(stream, url.openStream());
            }
          }
        }
        if (stream != null) {
          ManualTagger manualTagger = new ManualTagger(stream, internTags);
          return new CombiningTagger(morfologikTagger, manualTagger, removalTagger, overwriteWithManualTagger());
        } else {
          return morfologikTagger;
        }
      } finally {
        if (stream != null) {
          stream.close();
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not load manual tagger data from " + getManualAdditionsFileNames(), e);
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
    String lowerWord = word.toLowerCase(locale);
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
