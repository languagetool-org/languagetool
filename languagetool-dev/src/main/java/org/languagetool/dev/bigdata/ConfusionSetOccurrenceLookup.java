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

import org.languagetool.languagemodel.LuceneLanguageModel;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Take simple confusion set file (one set per line, separated by semicolons) and print out
 * occurrence information for all items.
 * @since 3.1
 */
final class ConfusionSetOccurrenceLookup {

  private ConfusionSetOccurrenceLookup() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + ConfusionSetOccurrenceLookup.class.getName() + " <confusion-file> <ngram-data-dir>");
      System.exit(1);
    }
    try (Scanner sc = new Scanner(new File(args[0]));
         LuceneLanguageModel lm = new LuceneLanguageModel(new File(args[1]))
    ) {
      while (sc.hasNextLine()) {
        String line = sc.nextLine();
        String[] words = line.split(";\\s*");
        long total = 0;
        List<Long> counts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        for (String word : words) {
          long count = lm.getCount(word);
          total += count;
          sb.append(word).append(":").append(count).append(" ");
          counts.add(count);
        }
        float factor = (float)Collections.max(counts) / Collections.min(counts);
        System.out.printf(Locale.ENGLISH, total + " " + line + "    " + sb.toString().trim() + " factor:%.1f\n", factor);
      }
    }
  }
  
}
