/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.rules.ar;


import gnu.trove.THashSet;
import org.languagetool.JLanguageTool;

import java.io.InputStream;
import java.util.Collections;
import java.util.Scanner;
import java.util.Set;

/**
 * Data for {@link ArabicTransVerbRule}.
 *
 * @author Taha Zerrouki
 */
public class ArabicTransVerbData {
  private static final Set<String> requiresA = loadWords("/ar/verb_trans_to_untrans.txt");
  private static final Set<String> requiresAn = loadWords("/ar/verb_untrans_to_trans.txt");

  private ArabicTransVerbData() {
  }

  static Set<String> getWordsRequiringA() {
    return requiresA;
  }

  static Set<String> getWordsRequiringAn() {
    return requiresAn;
  }

  /**
   * Load words, normalized to lowercase unless starting with '*'.
   */
  private static Set<String> loadWords(String path) {
    Set<String> set = new THashSet<>();
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
