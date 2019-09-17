/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà i Font
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
package org.languagetool.rules.es;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Load data for {@link AccentuationCheckRule}.
 * @since 3.3
 */
class AccentuationDataLoader {

  private static final String FILE_ENCODING = "utf-8";

  Map<String, AnalyzedTokenReadings> loadWords(String path) {
    final Map<String, AnalyzedTokenReadings> map = new HashMap<>();
    final InputStream inputStream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
    try (Scanner scanner = new Scanner(inputStream, FILE_ENCODING)) {
      while (scanner.hasNextLine()) {
        final String line = scanner.nextLine().trim();
        if (line.isEmpty() || line.charAt(0) == '#') {  // ignore comments
          continue;
        }
        final String[] parts = line.split(";");
        if (parts.length != 3) {
          throw new RuntimeException("Format error in file " + path + ", line: "
                  + line + ", " + "expected 3 semicolon-separated parts, got "
                  + parts.length);
        }
        final AnalyzedToken analyzedToken = new AnalyzedToken(parts[1], parts[2], null);
        map.put(parts[0], new AnalyzedTokenReadings(analyzedToken, 0));
      }
    }
    return map;
  }
  
}
