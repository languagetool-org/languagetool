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
package org.languagetool.rules.translation;

import java.util.ArrayList;
import java.util.List;

/**
 * @since 4.9
 */
public class TranslationTools {

  private TranslationTools() {
  }

  public static String cleanTranslationForReplace(String s, String prevWord) {
    String clean = s
      .replaceAll("\\[.*?\\]", "")   // e.g. "[coll.]", "[Br.]"
      .replaceAll("\\{.*?\\}", "")   // e.g. "to go {went; gone}"
      .replaceAll("\\(.*?\\)", "")   // e.g. "icebox (old-fashioned)"
      .replaceAll("/[A-Z]+/", "")    // e.g. "heavy goods vehicle /HGV/"
      .trim();
    if ("to".equals(prevWord) && clean.startsWith("to ")) {
      return clean.substring(3);
    }
    return clean;
  }

  public static String cleanTranslationForSuffix(String s) {
    StringBuilder sb = new StringBuilder();
    List<String> lookingFor = new ArrayList<>();
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '[') {
        lookingFor.add("]");
      } else if (c == ']' && lookingFor.contains("]")) {
        sb.append(c);
        sb.append(' ');
        lookingFor.remove("]");
      } else if (c == '(') {
        lookingFor.add(")");
      } else if (c == ')') {
        sb.append(c);
        sb.append(' ');
        lookingFor.remove(")");
      } else if (c == '{') {
        lookingFor.add("}");
      } else if (c == '}') {
        sb.append(c);
        sb.append(' ');
        lookingFor.remove("}");
      }
      if (lookingFor.size() > 0) {
        sb.append(c);
      }
    }
    return sb.toString().trim();
  }

}
