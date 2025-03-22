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
package org.languagetool.rules;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.languagetool.JLanguageTool;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Loads word variations words from a UTF-8 file. One entry per line,
 * word variations separated by a semicolon.
 * @since 3.0
 */
public class WordCoherencyDataLoader {

  public Map<String, Set<String>> loadWords(String path) {
    InputStream stream = JLanguageTool.getDataBroker().getFromRulesDirAsStream(path);
    Map<String, Set<String>> map = new Object2ObjectOpenHashMap<>();
    try (
      InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.isEmpty() || line.charAt(0) == '#') {   // ignore comments
          continue;
        }
        String[] parts = line.split(";");
        if (parts.length != 2) {
          throw new IOException("Format error in file " + path + ", line: " + line);
        }
        if(map.containsKey(parts[0])) {
          map.get(parts[0]).add(parts[1]);
        } else {
          map.put(parts[0], Stream.of(parts[1]).collect(Collectors.toCollection(ObjectOpenHashSet::new)));
        }
        if(map.containsKey(parts[1])) {
          map.get(parts[1]).add(parts[0]);
        } else {
          map.put(parts[1], Stream.of(parts[0]).collect(Collectors.toCollection(ObjectOpenHashSet::new)));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("Could not load coherency data from " + path, e);
    }
    return Collections.unmodifiableMap(map);
  }

}
