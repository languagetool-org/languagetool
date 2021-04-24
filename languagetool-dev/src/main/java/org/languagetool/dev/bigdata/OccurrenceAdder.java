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
package org.languagetool.dev.bigdata;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

/**
 * Get occurrence counts for words by iterating compressed Google ngram files.
 */
public class OccurrenceAdder {

  private static final int BUFFER_SIZE = 16384;

  private void run(Map<String, Integer> map, File dir) throws IOException {
    File[] files = dir.listFiles();
    for (File file : files) {
      runOnFile(map, file);
    }
  }

  private void runOnFile(Map<String, Integer> map, File file) throws IOException {
    System.out.println("Working on " + file);
    try (
      InputStream fileStream = new FileInputStream(file);
      InputStream gzipStream = new GZIPInputStream(fileStream, BUFFER_SIZE);
      Reader decoder = new InputStreamReader(gzipStream, "utf-8");
      BufferedReader buffered = new BufferedReader(decoder, BUFFER_SIZE)
    ) {
      String line;
      while ((line = buffered.readLine()) != null) {
        String[] parts = line.split("\t");
        String word = parts[0];
        int occurrences = Integer.parseInt(parts[2]);
        Integer val = map.get(word);
        if (val != null) {
          map.put(word, val + occurrences);
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + OccurrenceAdder.class.getName() + " <wordfile> <dir>");
      System.exit(1);
    }
    OccurrenceAdder occurrenceAdder = new OccurrenceAdder();
    Map<String, Integer> map = new HashMap<>();
    List<String> words = IOUtils.readLines(new FileInputStream(args[0]));
    for (String word : words) {
      map.put(word, 0);
    }
    occurrenceAdder.run(map, new File(args[1]));
    System.out.println("-------------------------");
    for (Map.Entry<String, Integer> entry : map.entrySet()) {
      System.out.println(entry.getValue() + "\t" + entry.getKey());
    }
  }

}
