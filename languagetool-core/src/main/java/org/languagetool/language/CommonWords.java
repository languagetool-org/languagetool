/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.databroker.ResourceDataBroker;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Information about common words - use as a fallback if language identification yields low confidence.
 * @since 4.4
 */
public class CommonWords {

  private final static Map<String, List<Language>> word2langs = Collections.synchronizedMap(new HashMap<>());
  private final static Pattern numberPattern = Pattern.compile("[0-9-.,]+");

  public CommonWords() {
    if (word2langs.size() == 0) {
      for (Language lang : Languages.get()) {
        if (lang.isVariant()) {
          continue;
        }
        ResourceDataBroker dataBroker = JLanguageTool.getDataBroker();
        String path = lang.getShortCode() + "/common_words.txt";
        if (dataBroker.resourceExists(path)) {
          try (InputStream inputStream = dataBroker.getFromResourceDirAsStream(path);
               Scanner scanner = new Scanner(inputStream, "utf-8")) {
            while (scanner.hasNextLine()) {
              String line = scanner.nextLine();
              if (line.isEmpty() || line.startsWith("#")) {
                continue;
              }
              String key = line.toLowerCase();
              List<Language> languages = word2langs.get(key);
              if (languages == null) {
                // word2langs is static, so this can be accessed from multiple threads concurrently -> prevent exceptions
                List<Language> l = Collections.synchronizedList(new LinkedList<>());
                l.add(lang);
                word2langs.put(key, l);
              } else {
                languages.add(lang);
              }
            }
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
        } else {
          //System.err.println("No common_words.txt found for " + lang);
        }
      }
    }
  }

  public Map<Language, Integer> getKnownWordsPerLanguage(String text) {
    Map<Language,Integer> result = new HashMap<>();
    if (!text.endsWith(" ")) {
      // last word might not be finished yet, so ignore
      text = text.replaceFirst("\\p{L}+$", "");
    }
    // Proper per-language tokenizing might help, but then the common_words.txt
    // will also need to be tokenized the same way. Also, this is quite fast.
    String[] words = text.split("[(),.:;!?„“\"¡¿\\s-]");
    for (String word : words) {
      if (numberPattern.matcher(word).matches()) {
        continue;
      }
      List<Language> languages = word2langs.get(word.toLowerCase());
      if (languages != null) {
        for (Language lang : languages) {
          if (result.containsKey(lang)) {
            result.put(lang, result.get(lang) + 1);
          } else {
            result.put(lang, 1);
          }
        }
      }
    }
    return result;
  }
  
}
