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
package org.languagetool.rules.ga;

import org.languagetool.JLanguageTool;

import java.io.InputStream;
import java.util.*;

/**
 * Data for {@link DhaNoBeirtRule}.
 * Loads list of humans from external files.
 * Based on AvsAnData
 */
final class DhaNoBeirtData {

  private static final Set<String> daoine = loadWords("/ga/people.txt");

  private DhaNoBeirtData() {
  }

  static Set<String> getDaoine() {
    return daoine;
  }

  private static final Map<String, String> numbers = new HashMap<String, String>();
  static {
    numbers.put("dhá", "beirt");
    numbers.put("trí", "triúr");
    numbers.put("ceathair", "ceathrar");
    numbers.put("cúig", "cúigear");
    numbers.put("sé", "seisear");
    numbers.put("seacht", "seachtar");
    numbers.put("ocht", "ochtar");
    numbers.put("naoi", "naonúr");
    numbers.put("deich", "deichniúr");
  }

  static Map<String, String> getNumberReplacements() {
    return numbers;
  }

  /**
   * Load words, normalized to lowercase unless starting with '*'.
   */
  private static Set<String> loadWords(String path) {
    Set<String> set = new HashSet<>();
    InputStream stream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
    try (Scanner scanner = new Scanner(stream, "utf-8")) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.isEmpty() || line.charAt(0) == '#') {
          continue;
        }
        if (line.charAt(0) == '*') {
          set.add(line.substring(1));
        } else {
          set.add(line.toLowerCase());
        }
      }
    }
    return Collections.unmodifiableSet(set);
  }

}
