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

import morfologik.fsa.FSA;
import morfologik.fsa.builders.FSABuilder;
import morfologik.fsa.builders.CFSA2Serializer;
import morfologik.stemming.Dictionary;

import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;

import java.io.*;
import java.util.*;

/**
 * Morfologik speller that merges results from binary (.dict) and plain text (.txt) dictionaries.
 *
 * @since 2.9
 */
public class MorfologikMultiSpeller {

  private static final Map<String,Dictionary> dicPathToDict = new HashMap<>();

  private final List<MorfologikSpeller> spellers;
  private final boolean convertsCase;

  /**
   * @param binaryDictPath path in classpath to a {@code .dict} binary Morfologik file
   * @param plainTextPath path in classpath to a plain text {@code .txt} file (like spelling.txt)
   * @param maxEditDistance maximum edit distance for accepting suggestions
   */
  public MorfologikMultiSpeller(String binaryDictPath, String plainTextPath, int maxEditDistance) throws IOException {
    this(binaryDictPath,
         new BufferedReader(new InputStreamReader(JLanguageTool.getDataBroker().getFromResourceDirAsStream(plainTextPath), "utf-8")),
         maxEditDistance);
    if (!plainTextPath.endsWith(".txt")) {
      throw new RuntimeException("Unsupported dictionary, plain text file needs to have suffix .txt: " + plainTextPath);
    }
  }

  /**
   * @param binaryDictPath path in classpath to a {@code .dict} binary Morfologik file
   * @param plainTextReader reader with to a plain text {@code .txt} file (like from spelling.txt)
   * @param maxEditDistance maximum edit distance for accepting suggestions
   * @since 3.0
   */
  public MorfologikMultiSpeller(String binaryDictPath, BufferedReader plainTextReader, int maxEditDistance) throws IOException {
    MorfologikSpeller speller = getBinaryDict(binaryDictPath, maxEditDistance);
    List<MorfologikSpeller> spellers = new ArrayList<>();
    spellers.add(speller);
    convertsCase = speller.convertsCase();
    MorfologikSpeller plainTextSpeller = getPlainTextDictSpellerOrNull(plainTextReader, binaryDictPath, maxEditDistance);
    if (plainTextSpeller != null) {
      spellers.add(plainTextSpeller);
    }
    this.spellers = Collections.unmodifiableList(spellers);
  }

  private MorfologikSpeller getBinaryDict(String binaryDictPath, int maxEditDistance) throws IOException {
    if (binaryDictPath.endsWith(".dict")) {
      return new MorfologikSpeller(binaryDictPath, maxEditDistance);
    } else {
      throw new RuntimeException("Unsupported dictionary, binary Morfologik file needs to have suffix .dict: " + binaryDictPath);
    }
  }

  @Nullable
  private MorfologikSpeller getPlainTextDictSpellerOrNull(BufferedReader plainTextReader, String dictPath, int maxEditDistance) throws IOException {
    List<byte[]> lines = getLines(plainTextReader);
    if (lines.size() == 0) {
      return null;
    }
    Dictionary dictionary = getDictionary(lines, dictPath);
    return new MorfologikSpeller(dictionary, maxEditDistance);
  }

  private List<byte[]> getLines(BufferedReader br) throws IOException {
    List<byte[]> lines = new ArrayList<>();
    String line;
    while ((line = br.readLine()) != null) {
      if (!line.startsWith("#")) {
        lines.add(line.getBytes("utf-8"));
      }
    }
    return lines;
  }

  private Dictionary getDictionary(List<byte[]> lines, String dictPath) throws IOException {
    Dictionary dictFromCache = dicPathToDict.get(dictPath);
    if (dictFromCache != null) {
      return dictFromCache;
    } else {
      // Creating the dictionary at runtime can easily take 50ms for spelling.txt files
      // that are ~50KB. We don't want that overhead for every check of a short sentence,
      // so we cache the result:
      Collections.sort(lines, FSABuilder.LEXICAL_ORDERING);
      FSA fsa = FSABuilder.build(lines);
      ByteArrayOutputStream fsaOutStream = new CFSA2Serializer().serialize(fsa, new ByteArrayOutputStream());
      ByteArrayInputStream fsaInStream = new ByteArrayInputStream(fsaOutStream.toByteArray());
      String infoFile = dictPath.replace(".dict", ".info");
      Dictionary dict = Dictionary.read(fsaInStream, JLanguageTool.getDataBroker().getFromResourceDirAsStream(infoFile));
      dicPathToDict.put(dictPath, dict);
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
   * The suggestions from all dictionaries (without duplicates).
   */
  public List<String> getSuggestions(String word) {
    List<String> result = new ArrayList<>();
    for (MorfologikSpeller speller : spellers) {
      List<String> suggestions = speller.getSuggestions(word);
      for (String suggestion : suggestions) {
        if (!result.contains(suggestion)) {
          result.add(suggestion);
        }
      }
    }
    return result;
  }

  /**
   * Determines whether the dictionary uses case conversions.
   * @return True when the speller uses spell conversions.
   * @since 2.5
   */
  public boolean convertsCase() {
    return convertsCase;
  }

}
