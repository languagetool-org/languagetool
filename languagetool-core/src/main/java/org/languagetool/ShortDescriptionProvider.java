/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (www.danielnaber.de)
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
package org.languagetool;

import org.jetbrains.annotations.Nullable;
import org.languagetool.broker.ResourceDataBroker;

import java.util.*;

/**
 * Provide short (up to ~ 40 characters) descriptions for words.
 * Used to display as an additional hint when there are several suggestions.
 * @since 4.5
 */
public class ShortDescriptionProvider {

  private final static Map<Key,String> wordToDef = new HashMap<>();
  private final static Set<Language> initializedLangs = new HashSet<>();
  private final static List<String> filenames = Arrays.asList(
    "word_definitions.txt"
  );

  public ShortDescriptionProvider() {
  }

  @Nullable
  public String getShortDescription(String word, Language lang) {
    if (!initializedLangs.contains(lang)) {
      init(lang);
    }
    return wordToDef.get(new Key(word, lang));
  }

  /**
   * For testing only.
   */
  public Map<String, String> getAllDescriptions(Language lang) {
    if (!initializedLangs.contains(lang)) {
      init(lang);
    }
    Map<String,String> result = new HashMap<>();
    for (Map.Entry<Key, String> entry : wordToDef.entrySet()) {
      if (entry.getKey().lang.equals(lang)) {
        result.put(entry.getKey().word, entry.getValue());
      }
    }
    return result;
  }

  private void init(Language lang) {
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    for (String filename : filenames) {
      String path = "/" + lang.getShortCode() + "/" + filename;
      if (!dataBroker.resourceExists(path)) {
        continue;
      }
      List<String> lines = dataBroker.getFromResourceDirAsLines(path);
      for (String line : lines) {
        if (line.startsWith("#") || line.trim().isEmpty()) {
          continue;
        }
        String[] parts = line.split("\t");
        if (parts.length != 2) {
          throw new RuntimeException("Format in " + path + " not expected, expected 2 tab-separated columns: '" + line + "'");
        }
        wordToDef.put(new Key(parts[0], lang), parts[1]);
      }
    }
    initializedLangs.add(lang);
  }

  private static class Key {
    String word;
    Language lang;
    Key(String word, Language lang) {
      this.word = Objects.requireNonNull(word);
      this.lang = Objects.requireNonNull(lang);
    }
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Key key = (Key) o;
      return word.equals(key.word) &&
        lang.equals(key.lang);
    }
    @Override
    public int hashCode() {
      return Objects.hash(word, lang);
    }

    @Override
    public String toString() {
      return word + "@" + lang.getShortCodeWithCountryAndVariant();
    }
  }
}
