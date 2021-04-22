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
package org.languagetool.languagemodel;

import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.List;

public class LanguageModelTest {

  private static final int SKIP_FIRST_ITEMS = 5;
  private static final String FILE = "/lt/performance-test/en.txt";

  protected void testPerformance(LuceneLanguageModel model, int ngramLength) throws Exception {
    try (FileInputStream fis = new FileInputStream(FILE)) {
      String content = StringTools.readStream(fis, "UTF-8");
      WordTokenizer wordTokenizer = new WordTokenizer();
      List<String> words = wordTokenizer.tokenize(content);
      String prevPrevWord = null;
      String prevWord = null;
      int i = 0;
      long totalMicros = 0;
      for (String word : words) {
        if (word.trim().isEmpty()) {
          continue;
        }
        if (prevWord != null) {
          long t1 = System.nanoTime()/1000;
          long count = 0;
          if (ngramLength == 2) {
            count = model.getCount(Arrays.asList(prevWord, word));
          } else if (ngramLength == 3) {
            if (prevPrevWord != null) {
              count = model.getCount(Arrays.asList(prevPrevWord, prevWord, word));
            }
          } else {
            throw new IllegalArgumentException("ngram length not supported: " + ngramLength);
          }
          long timeMicros = (System.nanoTime()/1000) - t1;
          long timeMillis = timeMicros/1000;
          if (ngramLength == 2) {
            System.out.println(count + "\t\t" + prevWord + " " + word + ": " + timeMicros + "µs = " + timeMillis + "ms");
          } else {
            System.out.println(count + "\t\t" + prevPrevWord + " " + prevWord + " " + word + ": " + timeMicros + "µs = " + timeMillis + "ms");
          }
          if (i > SKIP_FIRST_ITEMS) {
            totalMicros += timeMicros;
          }
          if (++i % 25 == 0) {
            printStats(i, totalMicros);
          }
        }
        prevPrevWord = prevWord;
        prevWord = word;
      }
      printStats(i, totalMicros);
    }
  }

  private void printStats(int i, long totalMicros) {
    long averageMicros = totalMicros/i;
    System.out.println("*** Average: " + averageMicros + "µs = " + (averageMicros/1000) + "ms");
  }

}
