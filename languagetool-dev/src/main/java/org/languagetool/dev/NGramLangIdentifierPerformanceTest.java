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
package org.languagetool.dev;

import org.languagetool.language.identifier.detector.NGramDetector;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NGramLangIdentifierPerformanceTest {

  private static final File ngramZip = new File("/home/languagetool/ngram-lang-id/model_ml50_new.zip");
  private static final Path input = Paths.get("/home/dnaber/data/corpus/tatoeba/20191014/sentences_shuf.txt");
  private static final int limit = 10_000;

  public void testPerformance() throws IOException {
    System.out.println("Loading ngrams...");
    NGramDetector ngram = new NGramDetector(ngramZip, 50);
    System.out.println("Loaded.");
    int i = 0;
    double totalMillis = 0;
    long totalLength = 0;
    long totalRuns = 0;
    System.out.println("Loading input...");
    List<String> lines = Files.readAllLines(input);
    System.out.println("Loaded " + lines.size() + " lines.");
    for (String line : lines) {
      long startTime = System.nanoTime();
      Map<String, Double> detectLanguages = ngram.detectLanguages(line, null);
      long endTime = System.nanoTime();
      double runTimeMillis = (endTime - startTime) / 1000.0f / 1000.0f;
      if (i > 10) {
        totalMillis += runTimeMillis;
        totalLength += line.length();
        totalRuns++;
        //System.out.println(line.length() + " chars took " + runTimeMillis + "ms -> " + detectLanguages);
        //System.out.println(line.length() + " chars took " + runTimeMillis + "ms");
        if (runTimeMillis > 5) {
          System.out.println(line.length() + " chars took " + runTimeMillis + "ms for text: " + line);
        }
      } else {
        System.out.println("Skipping early run " + i);
      }
      if (i > limit) {
        System.out.println("Stopping test at limit " + limit);
        break;
      }
      i++;
    }
    System.out.println("Runs: " + totalRuns);
    System.out.printf(Locale.ENGLISH, "Avg. length: %.2f chars\n", (double)totalLength/totalRuns);
    System.out.printf(Locale.ENGLISH, "Avg: %.2fms\n", totalMillis/totalRuns);
  }

  public static void main(String[] args) throws IOException {
    new NGramLangIdentifierPerformanceTest().testPerformance();
  }

}