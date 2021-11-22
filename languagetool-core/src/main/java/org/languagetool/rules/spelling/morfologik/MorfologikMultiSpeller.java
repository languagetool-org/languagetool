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

import com.google.common.cache.*;
import morfologik.fsa.FSA;
import morfologik.fsa.builders.CFSA2Serializer;
import morfologik.fsa.builders.FSABuilder;
import morfologik.stemming.Dictionary;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.UserConfig;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.languagetool.JLanguageTool.DICTIONARY_FILENAME_EXTENSION;
import static org.languagetool.JLanguageTool.getDataBroker;
import morfologik.fsa.FSA;
import morfologik.fsa.builders.CFSA2Serializer;
import morfologik.fsa.builders.FSABuilder;
import morfologik.stemming.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Morfologik speller that merges results from binary (.dict) and plain text (.txt) dictionaries.
 *
 * @since 2.9
 */
public class MorfologikMultiSpeller {

  private static class UserDictCacheKey {

    private final long userId;
    private final String binaryDictPath;

    UserDictCacheKey(long userId, String binaryDictPath) {
      this.userId = userId;
      this.binaryDictPath = binaryDictPath;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      UserDictCacheKey that = (UserDictCacheKey) o;

      return new EqualsBuilder()
        .append(userId, that.userId)
        .append(binaryDictPath, that.binaryDictPath)
        .isEquals();
    }

    @Override
    public int hashCode() {
      return new HashCodeBuilder(43, 57)
        .append(userId)
        .append(binaryDictPath)
        .toHashCode();
    }
  }

  private static final Logger logger = LoggerFactory.getLogger(MorfologikMultiSpeller.class);

  private static final LoadingCache<BufferedReaderWithSource, List<byte[]>> dictCache = CacheBuilder.newBuilder()
          //.maximumSize(0)
          .expireAfterWrite(10, TimeUnit.MINUTES)
          .build(new CacheLoader<BufferedReaderWithSource, List<byte[]>>() {
            @Override
            public List<byte[]> load(@NotNull BufferedReaderWithSource reader) throws IOException {
              List<byte[]> lines = getLines(reader.reader, reader.readerPath);
              if (reader.languageVariantReader != null) {
                lines.addAll(getLines(reader.languageVariantReader, reader.readerPath));
                lines.add(SpellingCheckRule.LANGUAGETOOL.getBytes());  // adding here so it's also used for suggestions
              }
              return lines;
            }

            private List<byte[]> getLines(BufferedReader br, String path) throws IOException {
              List<byte[]> lines = new ArrayList<>();
              String line;
              while ((line = br.readLine()) != null) {
                if (!line.startsWith("#")) {
                  lines.add(StringUtils.substringBefore(line,"#").trim().getBytes(UTF_8));
                }
              }
              return lines;
            }
          });
  private static final Map<String,Dictionary> dicPathToDict = new HashMap<>();
  private static final Map<UserDictCacheKey, Cache<String, Dictionary>> userDictCaches = new HashMap<>();
  private static final Map<UserDictCacheKey, Map<String, Integer>> userDictSizes = new HashMap<>();
  private final List<MorfologikSpeller> spellers;
  private final List<MorfologikSpeller> defaultDictSpellers;
  private final List<MorfologikSpeller> userDictSpellers;
  private final boolean convertsCase;
  private final Long premiumUid;
  private final Long userDictCacheSize;
  private final String userDictName;
  private final UserDictCacheKey userDictCacheKey;
  private static final int MAX_SUGGESTIONS = 20;

  public MorfologikMultiSpeller(String binaryDictPath, List<String> plainTextPaths, String languageVariantPlainTextPath, int maxEditDistance) throws IOException {
    this(binaryDictPath, plainTextPaths, languageVariantPlainTextPath, null, maxEditDistance);
  }

  /**
   * @param binaryDictPath path in classpath to a {@code .dict} binary Morfologik file
   * @param plainTextPaths paths in classpath to plain text {@code .txt} files (like spelling.txt)
   * @param maxEditDistance maximum edit distance for accepting suggestions
   * @since 4.2
   */
  public MorfologikMultiSpeller(String binaryDictPath, List<String> plainTextPaths, String languageVariantPlainTextPath,
    UserConfig userConfig, int maxEditDistance) throws IOException {
    this(binaryDictPath,
         getBufferedReader(plainTextPaths),
         plainTextPaths,
         languageVariantPlainTextPath == null ? null : new BufferedReader(new InputStreamReader(getDataBroker().getFromResourceDirAsStream(languageVariantPlainTextPath), UTF_8)),
         languageVariantPlainTextPath,
         userConfig, maxEditDistance);
    for (String plainTextPath : plainTextPaths) {
      if (plainTextPath != null &&
        (!plainTextPath.endsWith(".txt") ||
          (languageVariantPlainTextPath != null && !languageVariantPlainTextPath.endsWith(".txt")))) {
        throw new IllegalArgumentException("Unsupported dictionary, plain text file needs to have suffix .txt: " + plainTextPath);
      }
    }
  }

  @NotNull
  private static BufferedReader getBufferedReader(List<String> plainTextPaths) {
    List<InputStream> streams = new ArrayList<>();
    for (String plainTextPath : plainTextPaths) {
      streams.add(getDataBroker().getFromResourceDirAsStream(plainTextPath));
    }
    return new BufferedReader(new InputStreamReader(new SequenceInputStream(Collections.enumeration(streams)), UTF_8));
  }

  /**
   * @param binaryDictPath path in classpath to a {@code .dict} binary Morfologik file
   * @param plainTextReader reader with to a plain text {@code .txt} file (like from spelling.txt)
   * @param userConfig
   * @param maxEditDistance maximum edit distance for accepting suggestions
   * @since 3.0
   */
  public MorfologikMultiSpeller(String binaryDictPath, BufferedReader plainTextReader, List<String> plainTextReaderPath,
                                BufferedReader languageVariantPlainTextReader, String languageVariantPlainTextPath, UserConfig userConfig,
                                int maxEditDistance) throws IOException {
    if (userConfig == null || userConfig.getAcceptedWords() == null || userConfig.getAcceptedWords().isEmpty()) {
      premiumUid = null;
      userDictName = null;
      userDictCacheSize = null;
    } else {
      premiumUid = userConfig.getPremiumUid();
      userDictName = userConfig.getUserDictName();
      userDictCacheSize = userConfig.getUserDictCacheSize();
    }
    if (premiumUid != null && userDictCacheSize != null) {
      userDictCacheKey = new UserDictCacheKey(premiumUid, binaryDictPath);
    } else {
      userDictCacheKey = null;
    }
    MorfologikSpeller speller = getBinaryDict(binaryDictPath, maxEditDistance);
    List<MorfologikSpeller> spellers = new ArrayList<>();
    MorfologikSpeller userDictSpeller = getUserDictSpellerOrNull(userConfig, binaryDictPath, maxEditDistance);
    if (userDictSpeller != null) {
      // add this first, as otherwise suggestions from user's own dictionary might drown in the mass of other suggestions
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

  private static List<byte[]> getLines(BufferedReader br) throws IOException {
    List<byte[]> lines = new ArrayList<>();
    String line;
    while ((line = br.readLine()) != null) {
      if (!line.startsWith("#")) {
        lines.add(line.replaceFirst("#.*", "").trim().getBytes("utf-8"));
      }
    }
    return lines;
  }

  private Cache<String, Dictionary> getUserDictCache() {
    if (premiumUid == null || userDictCacheSize == null || userDictName == null) {
      throw new IllegalStateException("No user / dict caching / dict name configured: "
        + "user = " + premiumUid +  ", cache size = " + userDictCacheSize + ", dict name = " + userDictName);
    }
    if (userDictCaches.containsKey(userDictCacheKey)) {
      return userDictCaches.get(userDictCacheKey);
    } else {
      Cache<String, Dictionary> cache = CacheBuilder.newBuilder()
        .concurrencyLevel(1) // makes eviction behavior easier to understand, makes choosing maximumWeight easier
        .expireAfterWrite(10, TimeUnit.MINUTES)
        .maximumWeight(userDictCacheSize)
        .weigher((Weigher<String, Dictionary>) (k, v) -> userDictSizes.get(userDictCacheKey).get(k))
        .build();
      userDictCaches.put(userDictCacheKey, cache);
      return cache;
    }
  }

  private MorfologikSpeller getUserDictSpellerOrNull(UserConfig userConfig, String dictPath, int maxEditDistance) throws IOException {
    if (premiumUid == null) {
      return null;
    }
    List<byte[]> byteLines = new ArrayList<>();
    for (String line : userConfig.getAcceptedWords()) {
      byteLines.add(line.getBytes(UTF_8));
    }
    Dictionary dictionary = getDictionary(byteLines, dictPath, dictPath.replace(DICTIONARY_FILENAME_EXTENSION, ".info"), true);
    return new MorfologikSpeller(dictionary, maxEditDistance);
  }

  private MorfologikSpeller getBinaryDict(String binaryDictPath, int maxEditDistance) {
    if (binaryDictPath.endsWith(DICTIONARY_FILENAME_EXTENSION)) {
      return new MorfologikSpeller(binaryDictPath, maxEditDistance);
    } else {
      throw new IllegalArgumentException("Unsupported dictionary, binary Morfologik file needs to have suffix .dict: " + binaryDictPath);
    }
  }

  @Nullable
  private MorfologikSpeller getPlainTextDictSpellerOrNull(BufferedReader plainTextReader, List<String> plainTextReaderPaths,
      BufferedReader languageVariantPlainTextReader, String languageVariantPlainTextPath, String dictPath, int maxEditDistance) throws IOException {
    List<byte[]> lines = new ArrayList<>();
    for (String plainTextReaderPath : plainTextReaderPaths) {
      List<byte[]> l = dictCache.getUnchecked(new BufferedReaderWithSource(plainTextReader, plainTextReaderPath, languageVariantPlainTextReader, languageVariantPlainTextPath));
      lines.addAll(l);
    }
    if (lines.isEmpty()) {
      return null;
    }
    Dictionary dictionary = getDictionary(lines, plainTextReaderPaths.toString(), dictPath.replace(DICTIONARY_FILENAME_EXTENSION, ".info"), false);
    return new MorfologikSpeller(dictionary, maxEditDistance);
  }

  private Dictionary getDictionary(List<byte[]> lines, String dictPath, String infoPath, boolean isUserDict) throws IOException {
    String cacheKey = dictPath + "|" + infoPath;
    Dictionary dictFromCache = dicPathToDict.get(cacheKey);

    if (!isUserDict && dictFromCache != null) {
      return dictFromCache;
    } else {
      if (isUserDict && userDictCacheSize != null) {
        // for cache weigher, save dictionary sizes
        userDictSizes.putIfAbsent(userDictCacheKey, new HashMap<>());
        userDictSizes.get(userDictCacheKey).put(userDictName, lines.size());
        Cache<String, Dictionary> userDictCache = getUserDictCache();
        Dictionary userDict = userDictCache.getIfPresent(userDictName);
        if (userDict != null) {
          return userDict;
        }
      }
      // Creating the dictionary at runtime can easily take 50ms for spelling.txt files
      // that are ~50KB. We don't want that overhead for every check of a short sentence,
      // so we cache the result
      // Two caches exist: One for the standard dictionaries, which are static and in dicPathToDict
      // Another one for user dictionaries, this is only enabled for selected users with huge and relatively static dictionaries

      List<byte[]> linesCopy = new ArrayList<>(lines);
      linesCopy.sort(FSABuilder.LEXICAL_ORDERING);
      FSA fsa = FSABuilder.build(linesCopy);
      ByteArrayOutputStream fsaOutStream = new CFSA2Serializer().serialize(fsa, new ByteArrayOutputStream());
      ByteArrayInputStream fsaInStream = new ByteArrayInputStream(fsaOutStream.toByteArray());
      InputStream metadata;

      if (new File(infoPath).exists()) {
        metadata = new FileInputStream(infoPath);
      } else {
        metadata = getDataBroker().getFromResourceDirAsStream(infoPath);
      }
      Dictionary dict = Dictionary.read(fsaInStream, metadata);

      if (!isUserDict) {
        dicPathToDict.put(cacheKey, dict);
      } else if(userDictCacheSize != null){
        getUserDictCache().put(userDictName, dict);
      }

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
  
  /**
   * Get the frequency of use of a word (0-27) form the dictionary
   */
  public int getFrequency(String word) {
    for (MorfologikSpeller speller : spellers) {
      int freq = speller.getFrequency(word);
      if (freq > 0) {
        return freq;
      }
    }
    return 0;
  }

  @NotNull
  private List<String> getSuggestionsFromSpellers(String word, List<MorfologikSpeller> spellerList) {
    List<WeightedSuggestion> result = new ArrayList<>();
    Set<String> seenWords = new HashSet<>();
    for (MorfologikSpeller speller : spellerList) {
      List<WeightedSuggestion> suggestions = speller.getSuggestions(word);
      for (WeightedSuggestion suggestion : suggestions) {
        if (!seenWords.contains(suggestion.getWord()) && !suggestion.getWord().equals(word)) {
          result.add(suggestion);
        }
        seenWords.add(suggestion.getWord());
      }
    }
    Collections.sort(result);
    List<String> wordResults = new ArrayList<>();
    for (WeightedSuggestion weightedSuggestion : result) {
      wordResults.add(weightedSuggestion.getWord());
    }
    return wordResults;
  }

  /**
   * The suggestions from all dictionaries (without duplicates).
   */
  public List<String> getSuggestions(String word) {
    return getSuggestionsFromSpellers(word, spellers);
  }

  /**
   * @param word misspelled word
   * @return suggestions from users personal dictionary
   * @since 4.5
   */
  public List<String> getSuggestionsFromUserDicts(String word) {
    return getSuggestionsFromSpellers(word, userDictSpellers);
  }

  /**
   * @param word misspelled word
   * @return suggestions from built-in dictionaries
   * @since 4.5
   */
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
    private final BufferedReader reader;
    private final String readerPath;
    private final BufferedReader languageVariantReader;
    private final String languageVariantPath;

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

  /**
   * for tests only
   */
  public static void clearUserDictCache() {
    userDictCaches.clear();
    userDictSizes.clear();
  }
}
