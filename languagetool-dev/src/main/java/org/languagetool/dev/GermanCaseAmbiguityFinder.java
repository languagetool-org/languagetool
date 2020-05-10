/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import morfologik.stemming.Dictionary;
import morfologik.stemming.DictionaryLookup;
import morfologik.stemming.WordData;
import org.languagetool.JLanguageTool;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.languagetool.tools.StringTools.*;

public class GermanCaseAmbiguityFinder {

  public static void main(String[] args) throws IOException {
    Dictionary dictionary = Dictionary.read(JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/de/german.dict"));
    DictionaryLookup dl = new DictionaryLookup(dictionary);
    Map<String,String> lc = new HashMap<>();
    Map<String,String> uc = new HashMap<>();
    System.out.println("Iterating...");
    for (WordData wd : dl) {
      String word = wd.getWord().toString();
      String base = wd.getStem().toString();
      if (startsWithLowercase(word) && startsWithUppercase(base) || startsWithUppercase(word) && startsWithLowercase(base)) {
        // e.g. "Feilbieten"
        continue;
      }
      String tag = wd.getTag().toString();
      if (tag.endsWith(":INF") || tag.endsWith(":ADJ")) {
        // "Das Laufen" etc.
        continue;
      }
      if (!tag.startsWith("VER:") && !tag.startsWith("SUB:")) {
        continue;
      }
      if (startsWithUppercase(word)) {
        uc.put(word, tag);
      } else if (startsWithLowercase(word)) {
        lc.put(word, tag);
      }
    }
    System.out.println("Done. lc=" + lc.size() + ", uc=" + uc.size());
    for (Map.Entry<String, String> entry : uc.entrySet()) {
      String key = lowercaseFirstChar(entry.getKey());
      if (lc.containsKey(key)) {
        //System.out.println(entry.getKey() + " " + entry.getValue() + " " + lc.get(key));
        System.out.println(entry.getKey());
      }
    }
  }

}
