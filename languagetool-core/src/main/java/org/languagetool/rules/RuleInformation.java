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
package org.languagetool.rules;

import org.languagetool.Experimental;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;

/**
 * @since 4.4
 */
@Experimental
public class RuleInformation {
  
  private final static Set<Key> rules = loadRules();

  private static Set<Key> loadRules() {
    Set<Key> result = new HashSet<>();
    String path = "incomplete_sentence_ignore_rules.txt";
    InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
    try (Scanner sc = new Scanner(stream)) {
      while (sc.hasNext()) {
        String line = sc.nextLine();
        if (!line.startsWith("#") && !line.isEmpty()) {
          String[] parts = line.split("\t");
          if (parts.length != 2) {
            throw new RuntimeException("Unexpected format in " + path + ", expected two tabulator-separated parts: '" + line + "'");
          }
          result.add(new Key(parts[0], parts[1]));
        }
      }
    }
    return result;
  }

  private RuleInformation() {
  }

  /**
   * Whether this rule should be ignored when the sentence isn't finished yet.
   * @since 4.4
   */
  public static boolean ignoreForIncompleteSentences(String ruleId, Language lang) {
    return rules.contains(new Key(lang.getShortCode(), ruleId));
  }
  
  static class Key {
    private String langCode;
    private String ruleId;

    Key(String ruleId, String langCode) {
      this.ruleId = Objects.requireNonNull(ruleId);
      this.langCode = Objects.requireNonNull(langCode);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Key key = (Key) o;
      return Objects.equals(langCode, key.langCode) && Objects.equals(ruleId, key.ruleId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(langCode, ruleId);
    }
  }
}
