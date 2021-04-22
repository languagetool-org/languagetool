/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.archive;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;

/**
 * Used for tokenizing word lists for the MorfologikSpeller.
 *
 * @author Marcin Miłkowski
 */
public final class WordTokenizer {

  public static void main(final String[] args) throws IOException {
    final WordTokenizer prg = new WordTokenizer();
    if (args.length != 1) {
      System.err.println("Please supply the language code as the only argument.");
      System.exit(-1);
    }
    prg.run(args[0]);
  }

  private void run(final String lang) throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode(lang));
    BufferedWriter out = null;
    try (BufferedReader in = new BufferedReader(new InputStreamReader(System.in))) {
      out = new BufferedWriter(new OutputStreamWriter(System.out));
      String line;
      while ((line = in.readLine()) != null) {
        AnalyzedTokenReadings[] atr = lt.getRawAnalyzedSentence(line).
          getTokensWithoutWhitespace();
        for (AnalyzedTokenReadings a : atr) {
          out.write(a.getToken());
          out.write('\n');
        }
      }
    } finally {
      if (out != null) {
        out.flush();
        out.close();
      }
    }
  }

}
