/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

/**
 * Simple command line tool to look up occurrence counts in an ngram index.
 * @since 2.7
 */
class NGramStats {

  private void lookup(HomophoneOccurrenceDumper lm, File phraseFile) throws IOException {
    List<String> lines = Files.readAllLines(phraseFile.toPath());
    for (String line : lines) {
      lookup(lm, line);
    }
  }

  private void lookup(HomophoneOccurrenceDumper lm, String phrase) throws IOException {
    String[] tokens = phrase.split(" ");
    if (tokens.length > 3) {
      throw new RuntimeException("Phrases of length " + tokens.length + " are not yet supported: '" + phrase + "'");
    } else {
      long count = lm.getCount(Arrays.asList(tokens));
      System.out.println(count + " " + phrase);
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1 && args.length != 2) {
      System.out.println("Usage: " + NGramStats.class.getSimpleName() + " <dir> <phrase>");
      System.out.println("  'dir' is a directory with '1grams' etc sub directories with a Lucene index of ngrams");
      System.out.println("  'phrase' is a 1 to 3 word case-sensitive phrase, e.g. \"the tall boy\" (include the quotes) or a file");
      System.exit(1);
    }
    String dir = args[0];
    String phraseOrFile = args[1];
    NGramStats stats = new NGramStats();
    File file = new File(phraseOrFile);
    try (HomophoneOccurrenceDumper lm = new HomophoneOccurrenceDumper(new File(dir))) {
      if (file.exists()) {
        stats.lookup(lm, file);
      } else {
        stats.lookup(lm, phraseOrFile);
      }
    }
  }

}
