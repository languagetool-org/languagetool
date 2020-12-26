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
package org.languagetool.dev.simulation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Small helper to aggregate request sizes grepped from the log files to see
 * how request sizes are distributed.
 * Input: no_of_occurrences chars, e.g.
 * <pre>
 * 13521 22
 * 13378 21
 * 9906 32
 * </pre>
 */
public class RequestSizeAggregator {

  public static void main(String[] args) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    Map<Integer,Integer> map = new TreeMap<>();
    for (String line : lines) {
      String[] parts = line.trim().split(" ");
      int occ = Integer.parseInt(parts[0]);
      int chars = Integer.parseInt(parts[1]);
      int roundedChars = chars - chars % 10;
      map.compute(roundedChars, (k, v) -> v == null ? occ : map.get(k) + occ);
    }
    for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
      System.out.println(entry.getKey() + " " + entry.getValue());
    }
  }

}
