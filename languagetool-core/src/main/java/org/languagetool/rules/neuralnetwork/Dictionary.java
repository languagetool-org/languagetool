/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Markus Brenneis
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
package org.languagetool.rules.neuralnetwork;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

public class Dictionary extends HashMap<String, Integer> {

  public Dictionary(InputStream filePath) {
    List<String> rows = ResourceReader.readAllLines(filePath);
    fromString(rows.get(0));
  }

  public Dictionary(String dict) {
    fromString(dict);
  }

  private void fromString(String maps) {
    maps = maps.substring(1, maps.length() - 1);
    for (String entry : maps.split(", ")) {
      String[] kv = entry.split(": ");
      put(kv[0].substring(1, kv[0].length() - 1), Integer.parseInt(kv[1]));
    }
  }

  Integer safeGet(String key) {
    if (containsKey(key)) {
      return get(key);
    } else {
      return get("UNK");
    }
  }

}
