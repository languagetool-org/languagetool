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
class LineExpander {

  List<String> expandLine(String line) {
    List<String> result = new ArrayList<>();
    if (!line.startsWith("#") && line.contains("/")) {
      String[] parts = cleanTags(line).split("/");
      if (parts.length != 2) {
        throw new IllegalArgumentException("Unexpected line format, expected at most one slash: " + line);
      }
      String word = parts[0];
      String suffix = parts[1];
      result.add(word);
      for (int i = 0; i < suffix.length(); i++) {
        char c = suffix.charAt(i);
        if (c == 'S') {
          result.add(word + "s");
        } else if (c == 'N') {
          result.add(word + "n");
        } else if (c == 'E') {
          result.add(word + "e");
        } else if (c == 'F') {
          result.add(word + "in"); // (m/f)
        } else if (c == 'A') { // Adjektiv
          result.add(word + "e");
          result.add(word + "er");
          result.add(word + "es");
          result.add(word + "en");
          result.add(word + "em");
        } else {
          throw new IllegalArgumentException("Unknown suffix: " + suffix + " in line: " + line);
        }
      }
    } else {
      result.add(cleanTags(line));
    }
    return result;
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
