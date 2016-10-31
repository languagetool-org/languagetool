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

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.tools.StringTools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Check performance per sentence. Not a unit test, for interactive use only.
 */
final class PerformanceTest {

  private PerformanceTest() {
  }

  private void run(JLanguageTool langTool, File textFile) throws IOException {
    String text = StringTools.readStream(new FileInputStream(textFile), "utf-8");
    int sentenceCount = langTool.sentenceTokenize(text).size();
    //langTool.activateLanguageModelRules(new File("/data/google-ngram-index/"));
    System.out.println("Language: " +  langTool.getLanguage() +
                       ", Text length: " + text.length() + " chars, " + sentenceCount + " sentences");

    System.out.println("Warmup...");
    long startTime1 = System.currentTimeMillis();
    langTool.check(text);
    long runTime1 = System.currentTimeMillis() - startTime1;
    float timePerSentence1 = (float)runTime1 / sentenceCount;
    System.out.printf("Check time on first run: " + runTime1 + "ms = %.1fms per sentence\n", timePerSentence1);

    System.out.println("Checking text...");
    long startTime2 = System.currentTimeMillis();
    langTool.check(text);
    long runTime2 = System.currentTimeMillis() - startTime2;
    float timePerSentence2 = (float)runTime2 / sentenceCount;
    System.out.printf("Check time after warmup: " + runTime2 + "ms = %.1fms per sentence\n", timePerSentence2);
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + PerformanceTest.class.getSimpleName() + " <languageCode> <text_file>");
      System.exit(1);
    }
    PerformanceTest test = new PerformanceTest();
    String languageCode = args[0];
    File textFile = new File(args[1]);
    //JLanguageTool langTool = new JLanguageTool(Languages.getLanguageForShortName(languageCode));
    MultiThreadedJLanguageTool langTool = new MultiThreadedJLanguageTool(Languages.getLanguageForShortCode(languageCode));
    test.run(langTool, textFile);
    langTool.shutdown();
  }

}
