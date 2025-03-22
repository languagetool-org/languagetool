/* LanguageTool, a natural language style checker
 * Copyright (C) 2024 Jaume Ortolà
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

import org.junit.Test;
import org.languagetool.rules.spelling.SpellingCheckRule;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.languagetool.JLanguageTool.getDataBroker;

public class GlobalSpellingTest {

  private List<String> prohibitedExpressions = Arrays.asList("Dnipro", "Dnepr");
  private List<String> prohibitedTokens = Arrays.asList("Tolstoi", "Tolstoy", "Dostoevsky");

  @Test
  public void avoidSomeWords() throws IOException {
    List<String> lines = getDataBroker().getFromResourceDirAsLines((SpellingCheckRule.GLOBAL_SPELLING_FILE));
    for (String line : lines) {
      String[] parts = line.split("#");
      if (parts.length == 0) {
        continue;
      }
      String entry = parts[0].trim();
      if (prohibitedExpressions.contains(entry)) {
        throw new IllegalStateException("Do not use '" + entry + "' in global_spelling.txt. It is not a valid spelling for all languages.");
      }
      String[ ] tokens = entry.split(" ");
      for (String token : tokens) {
        if (prohibitedTokens.contains(token)) {
          throw new IllegalStateException("Do not use '" + token + "' in global_spelling.txt. It is not a valid spelling for all languages.");
        }
      }
    }
  }
}
