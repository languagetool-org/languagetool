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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.tagging.ManualTagger;
import org.languagetool.tagging.TaggedWord;
import org.languagetool.tools.MostlySingularMultiMap;
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

  /** a map with the key composed by the lemma and POS. The values are inflected forms. */
  private final MostlySingularMultiMap<TaggedWord, String> mapping;
  private final Set<String> possibleTags;
  
  private final static String DEFAULT_SEPARATOR = "\t";
  private static String separator;

  public ManualSynthesizer(InputStream inputStream) throws IOException {
    THashSet<String> tags = new THashSet<>();
    mapping = new MostlySingularMultiMap<>(loadMapping(inputStream, tags));
    tags.trimToSize();
    possibleTags = Collections.unmodifiableSet(tags);
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
    return mapping.getList(new TaggedWord(lemma, posTag));
  }

  private static Map<TaggedWord, List<String>> loadMapping(InputStream inputStream, Set<String> outTags) throws IOException {
    Map<String, String> internedStrings = new HashMap<>();
    Map<TaggedWord, List<String>> mapping = new HashMap<>();
    Map<String, String> interned = new HashMap<>();
    try (Scanner scanner = new Scanner(inputStream, "utf8")) {
      separator = DEFAULT_SEPARATOR;
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
          throw new IOException("Unknown line format when loading manual synthesizer dictionary: " + line);
        }

        String form = parts[0];

        String lemma = parts[1];
        if (form.equals(lemma)) form = lemma;
        lemma = interned.computeIfAbsent(lemma, Function.identity());

        String posTag = internedStrings.computeIfAbsent(parts[2], Function.identity());

        mapping.computeIfAbsent(new TaggedWord(lemma, posTag), __ -> new ArrayList<>()).add(form);
        outTags.add(posTag);
      }
    }
    return mapping;
  }

}
