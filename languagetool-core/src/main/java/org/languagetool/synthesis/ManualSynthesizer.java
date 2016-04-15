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

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.languagetool.tagging.ManualTagger;
import org.languagetool.tools.StringTools;

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

  /** a map with the key composed by the lemma and POS (separated by "|"). The values are lists of inflected forms. */ 
  private final Map<String, List<String>> mapping;
  private final Set<String> possibleTags;

  public ManualSynthesizer(InputStream inputStream) throws IOException {
    MappingAndTags mappingAndTags = loadMapping(inputStream, "utf8");
    mapping = mappingAndTags.mapping;
    possibleTags = Collections.unmodifiableSet(mappingAndTags.tags); // lock
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
  public List<String> lookup(String lemma, String posTag) {
    return mapping.get(lemma + "|" + posTag);
  }

  private MappingAndTags loadMapping(InputStream inputStream, String encoding) throws IOException {
    MappingAndTags result = new MappingAndTags();
    try (Scanner scanner = new Scanner(inputStream, encoding)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        if (StringTools.isEmpty(line) || line.charAt(0) == '#') {
          continue;
        }
        String[] parts = line.split("\t");
        if (parts.length != 3) {
          throw new IOException("Unknown line format when loading manual synthesizer dictionary: " + line);
        }
        String key = parts[1] + "|" + parts[2];
        if (!result.mapping.containsKey(key)) {
          result.mapping.put(key, new ArrayList<>());
        }
        result.mapping.get(key).add(parts[0]);
        result.tags.add(parts[2]); // POS
      }
    }
    return result;
  }

  static class MappingAndTags {
    Map<String, List<String>> mapping = new HashMap<>();
    Set<String> tags = new HashSet<>();
  }

}
