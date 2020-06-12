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

import org.languagetool.languagemodel.LuceneLanguageModel;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Findet potentiell falsche Wortformen mit "zu" wie "verzukokeln", die entstehen, weil die "_"
 * Syntax benutzt wurde, ohne zu bedenken, dass diese Form mit "zu" nicht gültig ist.
 */
public class InvalidGermanVerbFinder {

  private static final String SPELLING_FILE = "/home/dnaber/lt/git/languagetool/languagetool-language-modules/de/src/main/resources/org/languagetool/resource/de/hunspell/spelling.txt";
  private static final String NGRAM_DIR = "/home/dnaber/data/google-ngram-index/de/";
  private static final int THRESHOLD = 20;

  public static void main(String[] args) throws IOException {
    LuceneLanguageModel lm = new LuceneLanguageModel(new File(NGRAM_DIR));
    List<String> lines = Files.readAllLines(Paths.get(SPELLING_FILE));
    for (String line : lines) {
      if (line.startsWith("#")) {
        continue;
      }
      if (line.contains("_")) {
        line = line.replaceFirst("#.*", "");
        String form = line.replace("_", "zu").trim();
        long count = lm.getCount(form);
        if (count < THRESHOLD) {
          System.out.println(count  + " " + form);
        }
      }
    }
  }
}
