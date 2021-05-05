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

import org.languagetool.*;
//import org.languagetool.markup.AnnotatedText;
//import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Check performance per sentence. Not a unit test, for interactive use only.
 */
final class PerformanceTest {
  
  private static final long RUNS = 5;

  private PerformanceTest() {
  }

  private void run(JLanguageTool lt, File textFile) throws IOException {
    String text = StringTools.readStream(new FileInputStream(textFile), "utf-8");
    int sentenceCount = lt.sentenceTokenize(text).size();
    lt.activateLanguageModelRules(new File("data/ngrams"));
    //lt.activateLanguageModelRules(new File("/home/dnaber/data/google-ngram-index"));
    System.out.println("Language: " +  lt.getLanguage() +
                       ", Text length: " + text.length() + " chars, " + sentenceCount + " sentences");

    System.out.println("Warmup...");
    long startTime1 = System.currentTimeMillis();
    lt.check(text);
    long runTime1 = System.currentTimeMillis() - startTime1;
    float timePerSentence1 = (float)runTime1 / sentenceCount;
    System.out.printf("Check time on first run: " + runTime1 + "ms = %.1fms per sentence\n", timePerSentence1);

    System.out.println("Checking text...");
    float totalTime = 0;
    for (int i = 0; i < RUNS; i++) {
      long startTime2 = System.currentTimeMillis();
      lt.check(text);
      //lt.check(new AnnotatedTextBuilder().addText(text).build(), true, JLanguageTool.ParagraphHandling.NORMAL, null, JLanguageTool.Mode.ALL);
      long runTime2 = System.currentTimeMillis() - startTime2;
      float timePerSentence2 = (float)runTime2 / sentenceCount;
      System.out.printf("Check time after warmup: " + runTime2 + "ms = %.1fms per sentence\n", timePerSentence2);
      totalTime += timePerSentence2;
    }
    float avg = totalTime / (float)RUNS;
    System.out.printf("Average time per sentence = %.1fms\n", avg);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + PerformanceTest.class.getSimpleName() + " <languageCode> <text_file>");
      System.exit(1);
    }
    PerformanceTest test = new PerformanceTest();
    String languageCode = args[0];
    File textFile = new File(args[1]);
    //ResultCache cache = new ResultCache(1000, 5, TimeUnit.MINUTES);
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode(languageCode));
    //JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode(languageCode), null, cache);
    //MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(Languages.getLanguageForShortCode(languageCode));
    //MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(Languages.getLanguageForShortCode(languageCode), null, cache);
    test.run(lt, textFile);
    //lt.shutdown();
  }

}
