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
package org.languagetool.rules.de;

import java.util.ArrayList;
import java.util.List;

/**
 * Expand lines according to their suffix, e.g. {@code foo/S} becomes {@code [foo, foos]}.
 * @since 3.0
 */
public class LineExpander implements org.languagetool.rules.LineExpander {

  @Override
  public List<String> expandLine(String line) {
    List<String> result = new ArrayList<>();
    if (!line.startsWith("#") && line.contains("/")) {
      String[] parts = cleanTags(line).split("/");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Unexpected line format, expected at most one slash: " + line);
      }
      String word = parts[0];
      String suffix = parts[1];
      for (int i = 0; i < suffix.length(); i++) {
        char c = suffix.charAt(i);
        if (c == 'S') {
          add(result, word);
          result.add(word + "s");
        } else if (c == 'N') {
          add(result, word);
          result.add(word + "n");
        } else if (c == 'E') {
          add(result, word);
          result.add(word + "e");
        } else if (c == 'F') {
          add(result, word);
          result.add(word + "in"); // (m/f)
        } else if (c == 'A') { // Adjektiv
          add(result, word);
          if (word.endsWith("e")) {
            result.add(word + "r");
            result.add(word + "s");
            result.add(word + "n");
            result.add(word + "m");
          } else {
            result.add(word + "e");
            result.add(word + "er");
            result.add(word + "es");
            result.add(word + "en");
            result.add(word + "em");
         }
        } else if (c == 'V') { // Verb
          result.add(word + "n");
          result.add(word + "e");
          result.add(word + "st");
          result.add(word + "t");
          result.add(word + "te");
          result.add(word + "test");
          result.add(word + "ten");
        } else {
          throw new IllegalArgumentException("Unknown suffix: " + suffix + " in line: " + line);
        }
      }
    } else {
      result.add(cleanTags(line));
    }
    return result;
  }

  private void add(List<String> result, String word) {
    if (!result.contains(word)) {
      result.add(word);
    }
  }

  // ignore "#..." so it can be used as a tag:
  private String cleanTags(String s) {
    int idx = s.indexOf('#');
    if (idx != -1) {
      s = s.substring(0, idx);
    }
    return s.trim();
  }
}
