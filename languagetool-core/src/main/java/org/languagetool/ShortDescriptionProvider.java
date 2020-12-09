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

import com.google.common.annotations.VisibleForTesting;
import org.jetbrains.annotations.Nullable;
import org.languagetool.broker.ResourceDataBroker;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provide short (up to ~ 40 characters) descriptions for words.
 * Used to display as an additional hint when there are several suggestions.
 * @since 4.5
 */
public class ShortDescriptionProvider {
  private final static Map<Language, Map<String, String>> descriptions = new ConcurrentHashMap<>();

  @Nullable
  public String getShortDescription(String word, Language lang) {
    return getAllDescriptions(lang).get(word);
  }

  @VisibleForTesting
  static Map<String, String> getAllDescriptions(Language lang) {
    return descriptions.computeIfAbsent(lang, ShortDescriptionProvider::init);
  }

  private static Map<String, String> init(Language lang) {
    ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
    String path = "/" + lang.getShortCode() + "/word_definitions.txt";
    if (!dataBroker.resourceExists(path)) {
      return Collections.emptyMap();
    }
    Map<String, String> wordToDef = new HashMap<>();
    List<String> lines = dataBroker.getFromResourceDirAsLines(path);
    for (String line : lines) {
      if (line.startsWith("#") || line.trim().isEmpty()) {
        continue;
      }
      String[] parts = line.split("\t");
      if (parts.length != 2) {
        throw new RuntimeException("Format in " + path + " not expected, expected 2 tab-separated columns: '" + line + "'");
      }
      wordToDef.put(parts[0], parts[1]);
    }
    return wordToDef;
  }

}
