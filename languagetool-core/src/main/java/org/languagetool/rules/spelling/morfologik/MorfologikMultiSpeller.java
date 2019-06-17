/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling.morfologik;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import morfologik.fsa.FSA;
import morfologik.fsa.builders.FSABuilder;
import morfologik.fsa.builders.CFSA2Serializer;
import morfologik.stemming.Dictionary;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.Experimental;
import org.languagetool.JLanguageTool;
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.*;

/**
 * Morfologik speller that merges results from binary (.dict) and plain text (.txt) dictionaries.
 *
 * @since 2.9
 */
public class MorfologikMultiSpeller {

  private static final LoadingCache<BufferedReaderWithSource, List<byte[]>> dictCache = CacheBuilder.newBuilder()
          //.maximumSize(0)
          .expireAfterWrite(10, TimeUnit.MINUTES)
          .build(new CacheLoader<BufferedReaderWithSource, List<byte[]>>() {
            @Override
            public List<byte[]> load(@NotNull BufferedReaderWithSource reader) throws IOException {
              List<byte[]> lines = getLines(reader.reader);
              if (reader.languageVariantReader != null) {
                lines.addAll(getLines(reader.languageVariantReader));
                lines.add(SpellingCheckRule.LANGUAGETOOL.getBytes());  // adding here so it's also used for suggestions
              }
              return lines;
            }
          });
  private static final Map<String,Dictionary> dicPathToDict = new HashMap<>();

  private final List<MorfologikSpeller> spellers;
  private final List<MorfologikSpeller> defaultDictSpellers;
  private final List<MorfologikSpeller> userDictSpellers;
  private final boolean convertsCase;

  public MorfologikMultiSpeller(String binaryDictPath, String plainTextPath, String languageVariantPlainTextPath, int maxEditDistance) throws IOException {
    this(binaryDictPath, plainTextPath, languageVariantPlainTextPath, null, maxEditDistance);
  }

  /**
   * @param binaryDictPath path in classpath to a {@code .dict} binary Morfologik file
   * @param plainTextPath path in classpath to a plain text {@code .txt} file (like spelling.txt)
   * @param maxEditDistance maximum edit distance for accepting suggestions
   * @since 4.2
   */
  @Experimental
  public MorfologikMultiSpeller(String binaryDictPath, String plainTextPath, String languageVariantPlainTextPath,
    UserConfig userConfig, int maxEditDistance) throws IOException {
    this(binaryDictPath,
         plainTextPath != null ? new BufferedReader(new InputStreamReader(JLanguageTool.getDataBroker().getFromResourceDirAsStream(plainTextPath), UTF_8)) : null,
         plainTextPath,
         languageVariantPlainTextPath == null ? null : new BufferedReader(new InputStreamReader(JLanguageTool.getDataBroker().getFromResourceDirAsStream(languageVariantPlainTextPath), UTF_8)),
         languageVariantPlainTextPath,
         userConfig != null ? userConfig.getAcceptedWords(): Collections.emptyList(),
         maxEditDistance);
    if (plainTextPath != null &&
        (!plainTextPath.endsWith(".txt") ||
          (languageVariantPlainTextPath != null && !languageVariantPlainTextPath.endsWith(".txt")))) {
      throw new IllegalArgumentException("Unsupported dictionary, plain text file needs to have suffix .txt: " + plainTextPath);
    }
  }

  /**
   * @param binaryDictPath path in classpath to a {@code .dict} binary Morfologik file
   * @param plainTextReader reader with to a plain text {@code .txt} file (like from spelling.txt)
   * @param maxEditDistance maximum edit distance for accepting suggestions
   * @since 3.0
   */
  public MorfologikMultiSpeller(String binaryDictPath, BufferedReader plainTextReader, String plainTextReaderPath,
       BufferedReader languageVariantPlainTextReader, String languageVariantPlainTextPath, List<String> userWords,
       int maxEditDistance) throws IOException {
    MorfologikSpeller speller = getBinaryDict(binaryDictPath, maxEditDistance);
    List<MorfologikSpeller> spellers = new ArrayList<>();
    MorfologikSpeller userDictSpeller = getUserDictSpellerOrNull(userWords, binaryDictPath, maxEditDistance);
    if (userDictSpeller != null) {
      // add this first, as otherwise suggestions from user's won dictionary might drown in the mass of other suggestions
      spellers.add(userDictSpeller);
      userDictSpellers = Collections.singletonList(userDictSpeller);
    } else {
      userDictSpellers = Collections.emptyList();
    }
    spellers.add(speller);
    convertsCase = speller.convertsCase();
    if (plainTextReader != null) {
      MorfologikSpeller plainTextSpeller = getPlainTextDictSpellerOrNull(plainTextReader, plainTextReaderPath,
        languageVariantPlainTextReader, languageVariantPlainTextPath, binaryDictPath, maxEditDistance);
      if (plainTextSpeller != null) {
        spellers.add(plainTextSpeller);
        defaultDictSpellers = Arrays.asList(speller, plainTextSpeller);
      } else {
        defaultDictSpellers = Collections.singletonList(speller);
      }
    } else {
      defaultDictSpellers = Collections.singletonList(speller);
    }
    this.spellers = Collections.unmodifiableList(spellers);
  }

  private MorfologikSpeller getUserDictSpellerOrNull(List<String> userWords, String dictPath, int maxEditDistance) throws IOException {
    if (userWords.isEmpty()) {
      return null;
    }
    List<byte[]> byteLines = new ArrayList<>();
    for (String line : userWords) {
      byteLines.add(line.getBytes(UTF_8));
    }
    Dictionary dictionary = getDictionary(byteLines, dictPath, dictPath.replace(".dict", ".info"), false);
    return new MorfologikSpeller(dictionary, maxEditDistance);
  }

  private MorfologikSpeller getBinaryDict(String binaryDictPath, int maxEditDistance) {
    if (binaryDictPath.endsWith(".dict")) {
      return new MorfologikSpeller(binaryDictPath, maxEditDistance);
    } else {
      throw new RuntimeException("Unsupported dictionary, binary Morfologik file needs to have suffix .dict: " + binaryDictPath);
    }
  }

  @Nullable
  private MorfologikSpeller getPlainTextDictSpellerOrNull(BufferedReader plainTextReader, String plainTextReaderPath,
      BufferedReader languageVariantPlainTextReader, String languageVariantPlainTextPath, String dictPath, int maxEditDistance) throws IOException {
    List<byte[]> lines = dictCache.getUnchecked(new BufferedReaderWithSource(plainTextReader, plainTextReaderPath, languageVariantPlainTextReader, languageVariantPlainTextPath));
    if (lines.isEmpty()) {
      return null;
    }
    Dictionary dictionary = getDictionary(lines, plainTextReaderPath, dictPath.replace(".dict", ".info"), true);
    return new MorfologikSpeller(dictionary, maxEditDistance);
  }

  private static List<byte[]> getLines(BufferedReader br) throws IOException {
    List<byte[]> lines = new ArrayList<>();
    String line;
    while ((line = br.readLine()) != null) {
      if (!line.startsWith("#")) {
        lines.add(StringUtils.substringBefore(line,"#").trim().getBytes(UTF_8));
      }
    }
    return lines;
  }

  private Dictionary getDictionary(List<byte[]> lines, String dictPath, String infoPath, boolean allowCache) throws IOException {
    String cacheKey = dictPath + "|" + infoPath;
    Dictionary dictFromCache = dicPathToDict.get(cacheKey);
    if (allowCache && dictFromCache != null) {
      return dictFromCache;
    } else {
      // Creating the dictionary at runtime can easily take 50ms for spelling.txt files
      // that are ~50KB. We don't want that overhead for every check of a short sentence,
      // so we cache the result:
      List<byte[]> linesCopy = new ArrayList<>(lines);
      Collections.sort(linesCopy, FSABuilder.LEXICAL_ORDERING);
      FSA fsa = FSABuilder.build(linesCopy);
      ByteArrayOutputStream fsaOutStream = new CFSA2Serializer().serialize(fsa, new ByteArrayOutputStream());
      ByteArrayInputStream fsaInStream = new ByteArrayInputStream(fsaOutStream.toByteArray());
      Dictionary dict = Dictionary.read(fsaInStream, JLanguageTool.getDataBroker().getFromResourceDirAsStream(infoPath));
      dicPathToDict.put(cacheKey, dict);
      return dict;
    }
  }

  /**
   * Accept the word if at least one of the dictionaries accepts it as not misspelled.
   */
  public boolean isMisspelled(String word) {
    for (MorfologikSpeller speller : spellers) {
      if (!speller.isMisspelled(word)) {
        return false;
      }
    }
    return true;
  }

  @NotNull
  private List<String> getSuggestionsFromSpellers(String word, List<MorfologikSpeller> spellerList) {
    List<String> result = new ArrayList<>();
    for (MorfologikSpeller speller : spellerList) {
      List<String> suggestions = speller.getSuggestions(word);
      for (String suggestion : suggestions) {
        if (!result.contains(suggestion) && !suggestion.equals(word)) {
          result.add(suggestion);
        }
      }
    }
    return result;
  }

  /**
   * The suggestions from all dictionaries (without duplicates).
   */
  public List<String> getSuggestions(String word) {
    return getSuggestionsFromSpellers(word, spellers);
  }

  /**
   * @since 4.5
   * @param word misspelled word
   * @return suggestions from users personal dictionary
   */
  @Experimental
  public List<String> getSuggestionsFromUserDicts(String word) {
    return getSuggestionsFromSpellers(word, userDictSpellers);
  }

  /**
   * @since 4.5
   * @param word misspelled word
   * @return suggestions from built-in dictionaries
   */
  @Experimental
  public List<String> getSuggestionsFromDefaultDicts(String word) {
    return getSuggestionsFromSpellers(word, defaultDictSpellers);
  }

  /**
   * Determines whether the dictionary uses case conversions.
   * @return True when the speller uses spell conversions.
   * @since 2.5
   */
  public boolean convertsCase() {
    return convertsCase;
  }

  static class BufferedReaderWithSource {
    private BufferedReader reader;
    private String readerPath;
    private BufferedReader languageVariantReader;
    private String languageVariantPath;

    BufferedReaderWithSource(BufferedReader reader, String readerPath, BufferedReader languageVariantReader, String languageVariantPath) {
      this.reader = Objects.requireNonNull(reader);
      this.readerPath = Objects.requireNonNull(readerPath);
      this.languageVariantReader = languageVariantReader;
      this.languageVariantPath = languageVariantPath;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      } else if (o == null || getClass() != o.getClass()) {
        return false;
      }
      BufferedReaderWithSource that = (BufferedReaderWithSource) o;
      return Objects.equals(readerPath, that.readerPath) && Objects.equals(languageVariantPath, that.languageVariantPath);
    }

    @Override
    public int hashCode() {
      return Objects.hash(readerPath, languageVariantPath);
    }
  }
}
