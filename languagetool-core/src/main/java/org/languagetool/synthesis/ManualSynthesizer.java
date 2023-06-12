/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.synthesis;

import gnu.trove.THashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectProcedure;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.function.Function;

/**
 * A synthesizer that reads the inflected form and POS information from a plain (UTF-8) text file.
 * This makes it possible for the user to edit the text file to let the system know
 * about new words or missing readings in the synthesizer *.dict file.
 * <p>
 * File Format: <tt>fullform baseform postags</tt> (tab separated)
 * 
 * @author Ionuț Păduraru
 * @see ManualTagger  
 * @see BaseSynthesizer
 */
public final class ManualSynthesizer {
  private static final String SUFFIX_MARKER = "+";
  private final Set<String> possibleTags;

  private static final int OFFSET_SHIFT = 8;
  private static final int MAX_LENGTH = (1 << OFFSET_SHIFT) - 1;
  private static final int MAX_OFFSET = (1 << 32 - OFFSET_SHIFT) - 1;
  private static final int ENTRY_SIZE = 3;
  private final String[] data;

  /** A map from lemma+POS hashes to encoded lemma+POS+word tuple offsets in {@link #data} */
  private final TIntIntHashMap map;

  private final static String DEFAULT_SEPARATOR = "\t";

  public ManualSynthesizer(InputStream inputStream) throws IOException {
    Map<TaggedWord, List<String>> mapping = loadMapping(inputStream);
    TIntObjectHashMap<List<Triple<String, String, String>>> byHash = groupByHash(mapping);

    map = new TIntIntHashMap(byHash.size());
    int valueCount = mapping.values().stream().mapToInt(v -> v.size()).sum();
    int firstIndex = ENTRY_SIZE; // skip an entry, as 0 means an absent value in TObjectIntHashMap
    data = new String[valueCount * ENTRY_SIZE + firstIndex];
    if (valueCount > MAX_OFFSET) {
      throw new UnsupportedOperationException("Too many values (" + valueCount + "), the storage needs adjusting");
    }
    byHash.forEachEntry(new TIntObjectProcedure<List<Triple<String, String, String>>>() {
      int index = firstIndex;

      @Override
      public boolean execute(int hash, List<Triple<String, String, String>> value) {
        if (value.size() > MAX_LENGTH) {
          throw new UnsupportedOperationException(
            "Too many lemmas (" + value.size() + " for the same hash " + value + ", the storage needs adjusting");
        }
        map.put(hash, ((index / ENTRY_SIZE) << OFFSET_SHIFT) | value.size());
        for (Triple<String, String, String> triple : value) {
          data[index++] = triple.getLeft();
          data[index++] = triple.getMiddle();
          data[index++] = triple.getRight();
        }
        return true;
      }
    });

    possibleTags = Collections.unmodifiableSet(collectTags(mapping));
  }

  private static TIntObjectHashMap<List<Triple<String, String, String>>> groupByHash(Map<TaggedWord, List<String>> mapping) {
    TIntObjectHashMap<List<Triple<String, String, String>>> byHash = new TIntObjectHashMap<>(mapping.size());
    Map<String, String> internedStrings = new HashMap<>();
    for (Map.Entry<TaggedWord, List<String>> entry : mapping.entrySet()) {
      TaggedWord tw = entry.getKey();
      String lemma = tw.getLemma();
      int hash = hashCode(lemma, tw.getPosTag());
      List<Triple<String, String, String>> list = byHash.get(hash);
      if (list == null) {
        byHash.put(hash, list = new ArrayList<>());
      }
      for (String word : entry.getValue()) {
        if (word.startsWith(SUFFIX_MARKER)) {
          throw new UnsupportedOperationException("Words can't start with " + SUFFIX_MARKER);
        }
        String value = internedStrings.computeIfAbsent(encodeForm(lemma, word), Function.identity());
        list.add(new ImmutableTriple<>(lemma, tw.getPosTag(), value));
      }
    }
    return byHash;
  }

  private static String encodeForm(String lemma, String word) {
    if (word.length() > lemma.length() && word.startsWith(lemma)) {
      return SUFFIX_MARKER + word.substring(lemma.length());
    }
    if (word.length() >= lemma.length() && word.startsWith(lemma.substring(0, lemma.length() - 1))) {
      return SUFFIX_MARKER + SUFFIX_MARKER + word.substring(lemma.length() - 1);
    }
    return word;
  }

  private static String decodeForm(String lemma, String word) {
    if (word.startsWith(SUFFIX_MARKER)) {
      if (word.startsWith(SUFFIX_MARKER, SUFFIX_MARKER.length())) {
        return lemma.substring(0, lemma.length() - 1) + word.substring(SUFFIX_MARKER.length() * 2);
      }
      return lemma + word.substring(SUFFIX_MARKER.length());
    }
    return word;
  }

  private static THashSet<String> collectTags(Map<TaggedWord, List<String>> mapping) {
    THashSet<String> tags = new THashSet<>();
    for (TaggedWord tw : mapping.keySet()) {
      tags.add(tw.getPosTag());
    }
    tags.trimToSize();
    return tags;
  }

  private static int hashCode(String lemma, String posTag) {
    return lemma.hashCode() * 31 + posTag.hashCode();
  }

  /**
   * Retrieve all the possible POS values.
   */
  public Set<String> getPossibleTags() {
    return possibleTags;
  }
  
  /**
   * Look up a word's inflected form as specified by the lemma and POS tag.
   * 
   * @param lemma the lemma to inflect.
   * @param posTag the required POS tag.
   * @return a list with all the inflected forms of the specified lemma having the specified POS tag.
   * If no inflected form is found, the function returns <code>null</code>.
   */
  @Nullable
  public List<String> lookup(String lemma, String posTag) {
    if (lemma == null || posTag == null) {
      return null;
    }

    int value = map.get(hashCode(lemma, posTag));
    if (value == 0) return null;

    int offset = (value >>> OFFSET_SHIFT) * ENTRY_SIZE;
    int length = value & MAX_LENGTH;
    List<String> result = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      if (lemma.equals(data[offset + i * ENTRY_SIZE]) && posTag.equals(data[offset + i * ENTRY_SIZE + 1])) {
        String word = data[offset + i * ENTRY_SIZE + 2];
        result.add(decodeForm(lemma, word));
      }
    }
    return result;
  }

  private static Map<TaggedWord, List<String>> loadMapping(InputStream inputStream) throws IOException {
    Map<String, String> internedStrings = new HashMap<>();
    Map<TaggedWord, List<String>> mapping = new HashMap<>();
    Map<String, String> interned = new HashMap<>();
    try (Scanner scanner = new Scanner(inputStream, "utf8")) {
      String separator = DEFAULT_SEPARATOR;
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        line = line.trim();
        if (line.startsWith("#separatorRegExp=")) {
          separator = line.replace("#separatorRegExp=", "");
        }
        if (StringTools.isEmpty(line) || line.charAt(0) == '#') {
          continue;
        }
        line = StringUtils.substringBefore(line, "#").trim();
        String[] parts = line.split(separator);
        if (parts.length != 3) {
          throw new IOException("Unknown line format when loading manual synthesizer dictionary, " +
            "expected 3 parts separated by '" + separator + "', found " + parts.length + ": '" + line + "'");
        }
        String form = parts[0];
        String lemma = parts[1];
        if (form.equals(lemma)) {
          form = lemma;
        }
        lemma = interned.computeIfAbsent(lemma, Function.identity());
        String posTag = internedStrings.computeIfAbsent(parts[2], Function.identity());
        mapping.computeIfAbsent(new TaggedWord(lemma, posTag), __ -> new ArrayList<>()).add(form);
      }
    }
    return mapping;
  }

}
