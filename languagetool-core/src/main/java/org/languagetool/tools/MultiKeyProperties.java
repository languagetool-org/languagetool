/* LanguageTool, a natural language style checker 
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.*;

/**
 * A very simple property-style configuration. If a key occurs more than once, the
 * values will be merged to a list with the other properties of that key. This
 * is useful if property files get merged by Maven.
 * 
 * Note: this is not a full replacement for {@link Properties}, e.g. it does
 * not support values that span multiple lines
 */
public class MultiKeyProperties {

  private final Map<String, List<String>> properties = new HashMap<>();

  public MultiKeyProperties(InputStream inStream) {
    try (Scanner scanner = new Scanner(inStream)) {
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine().trim();
        if (line.startsWith("#") || line.isEmpty()) {
          continue;
        }
        String[] parts = line.split("\\s*=\\s*");
        if (parts.length != 2) {
          continue;
        }
        String key = parts[0];
        String value = parts[1];
        List<String> list = properties.get(key);
        if (list == null) {
          list = new ArrayList<>();
        }
        list.add(value);
        properties.put(key, list);
      }
    }
  }

  /**
   * @return a list of values or {@code null} if there's no such key
   */
  @Nullable
  public List<String> getProperty(String key) {
    return properties.get(key);
  }

}
