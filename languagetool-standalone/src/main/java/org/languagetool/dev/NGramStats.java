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

import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.languagemodel.LuceneLanguageModel;

import java.io.File;
import java.io.IOException;

/**
 * Simple command line tool to look up occurrence counts in an ngram index.
 * @since 2.7
 */
class NGramStats {

  private void lookup(File dir, String phrase) throws IOException {
    try (LanguageModel lm = new LuceneLanguageModel(dir)) {
      String[] tokens = phrase.split(" ");
      long count;
      if (tokens.length == 2) {
        count = lm.getCount(tokens[0], tokens[1]);
      } else if (tokens.length == 3) {
        count = lm.getCount(tokens[0], tokens[1], tokens[2]);
      } else {
        throw new RuntimeException("Phrases of length " + tokens.length + " are not yet supported: '" + phrase + "'");
      }
      System.out.println(phrase + ": " + count);
    }
  }
  
  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + NGramStats.class.getSimpleName() + " <dir> <phrase>");
      System.out.println("  'dir' is a directory with e.g. a '3grams' sub directory with a Lucene index of ngrams");
      System.out.println("  'phrase' is a 2 or 3-word case-sensitive phrase, e.g. \"the tall boy\" (include the quotes)");
      System.exit(1);
    }
    String dir = args[0];
    String phrase = args[1];
    NGramStats stats = new NGramStats();
    stats.lookup(new File(dir), phrase);
  }
}
