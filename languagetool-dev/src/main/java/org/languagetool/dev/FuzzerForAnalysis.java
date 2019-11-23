/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.chunking.Chunker;

import java.io.IOException;
import java.util.Random;

/**
 * A very simple fuzzer to see if certain random input causes long processing times
 * when analyzing text.
 */
public class FuzzerForAnalysis extends Fuzzer {

  private void run() throws IOException {
    for (int i = 0; i < 10; i++) {
      System.out.println("-----------------------");
      Random rnd = new Random(i);
      for (Language language : Languages.get()) {
        JLanguageTool lt = new JLanguageTool(language);
        Chunker chunker = language.getChunker();
        if (chunker != null) {
          String text = fuzz(rnd, 2500);
          long t1 = System.currentTimeMillis();
          System.out.println(language.getShortCode() + " with text length of " + text.length() + "...");
          //System.out.println(">> " + text);
          lt.getAnalyzedSentence(text);
          long t2 = System.currentTimeMillis();
          long runtime = t2 - t1;
          float relRuntime = (float) runtime / text.length() * 1000;
          System.out.printf(language.getShortCode() + ": " + runtime + "ms = %.2f ms/1K chars\n", relRuntime);
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    new FuzzerForAnalysis().run();
  }

}
