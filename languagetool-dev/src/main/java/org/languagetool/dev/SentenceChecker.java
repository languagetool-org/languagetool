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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

/**
 * Run LT on a large file with one sentence per line.
 * Useful to see if performance gets worse over time.
 * @since 2.7
 */
class SentenceChecker {
  
  private static final int BATCH_SIZE = 1000;

  private void run(Language language, File file) throws IOException {
    JLanguageTool lt = new JLanguageTool(language);
    try (Scanner scanner = new Scanner(file)) {
      int count = 0;
      long startTime = System.currentTimeMillis();
      while (scanner.hasNextLine()) {
        String line = scanner.nextLine();
        lt.check(line);
        if (++count % BATCH_SIZE == 0) {
          long time = System.currentTimeMillis() - startTime;
          System.out.println(count + ". " + time + "ms per " + BATCH_SIZE + " sentences");
          startTime = System.currentTimeMillis();
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.err.println("Usage: " + SentenceChecker.class.getSimpleName() + " <langCode> <sentenceFile>");
      System.exit(1);
    }
    SentenceChecker checker = new SentenceChecker();
    checker.run(Languages.getLanguageForShortCode(args[0]), new File(args[1]));
  }
}
