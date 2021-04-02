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
package org.languagetool.rules.patterns;

import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.MultiThreadedJLanguageTool;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

/**
 * Check startup performance by "checking" the empty string for
 * each language.
 * Not a unit test, for interactive use only.
 */
final class StartupTimePerformanceTest {

  private static final int RUNS = 100;
  private static final int SKIP = 5;
  
  private void run() throws IOException {
    List<Language> languages = Languages.get();
    for (Language language : languages) {
      run(language);
    }
  }

  private void run(Language language) throws IOException {
    long totalTime = 0;
    for (int i = 0; i < RUNS; i++) {
      long startTime = System.currentTimeMillis();
      MultiThreadedJLanguageTool lt = new MultiThreadedJLanguageTool(language);
      List<RuleMatch> matches = lt.check("");
      if (matches.size() > 0) {
        throw new RuntimeException("Got matches on empty input for " + language + ": " + matches);
      }
      long runTime = System.currentTimeMillis() - startTime;
      lt.shutdown();
      if (i >= SKIP) {
        totalTime += runTime;
      }
      //System.out.println(runTime + "ms");
    }
    System.out.println(language.getShortCodeWithCountryAndVariant() + ": avg. Time: " + (float)totalTime/RUNS + "ms");
  }

  public static void main(String[] args) throws IOException {
    StartupTimePerformanceTest test = new StartupTimePerformanceTest();
    test.run();
    //test.run(new GermanyGerman());
  }

}
