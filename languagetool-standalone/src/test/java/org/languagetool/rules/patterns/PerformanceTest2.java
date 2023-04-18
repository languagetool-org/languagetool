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
package org.languagetool.rules.patterns;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.spelling.morfologik.suggestions_ordering.SuggestionsOrdererConfig;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

/**
 * Check performance for shorts text, like in some multi-threaded use-cases,
 * where the language object is created once and the JLanguageTool object is
 * re-created for every request.
 * Not a unit test, for interactive use only.
 */
final class PerformanceTest2 {

  private static final int RUNS = 100;
  private static final int SKIP = 3;
  private static final int MAX_TEXT_LENGTH = 150;
  
  private void run(String languageCode, File textFile) throws IOException {
    String text = StringTools.readStream(new FileInputStream(textFile), "utf-8");
    System.out.println("Text length: " + text.length());
    Random rnd = new Random(42);
    Language language = Languages.getLanguageForShortCode(languageCode);
    long totalTime = 0;
    for (int i = 0; i < RUNS; i++) {
      int beginIndex = rnd.nextInt(text.length());
      int endIndex = Math.min(beginIndex + MAX_TEXT_LENGTH, text.length()-1);
      String subText = text.substring(beginIndex, endIndex);
      long startTime = System.currentTimeMillis();
      MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(language);
      //String ngramPath = "/home/dnaber/data/google-ngram-index";
      //lt.activateLanguageModelRules(new File(ngramPath));
      //SuggestionsOrdererConfig.setNgramsPath(ngramPath);
      List<RuleMatch> matches = lt.check(subText);
      //System.out.println(matches);
      long runTime = System.currentTimeMillis() - startTime;
      lt.shutdown();
      if (i >= SKIP) {
        totalTime += runTime;
        System.out.println("Time: " + runTime + "ms (" + matches.size() + " matches)");
      } else {
        System.out.println("Time: " + runTime + "ms (" + matches.size() + " matches) - skipped because of warm-up");
      }
    }
    System.out.println("Avg. Time: " + (float)totalTime/RUNS);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + PerformanceTest2.class.getSimpleName() + " <languageCode> <text_file>");
      System.exit(1);
    }
    PerformanceTest2 test = new PerformanceTest2();
    File textFile = new File(args[1]);
    test.run(args[0], textFile);
  }

}
