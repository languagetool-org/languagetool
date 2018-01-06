/* LanguageTool, a natural language style checker 
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.bigdata;

import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.rules.ngrams.Probability;

import java.io.File;
import java.util.Arrays;

/**
 * Simple ngram count lookup.
 */
final class NGramLookup {

  private NGramLookup() {
  }

  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Usage: " + NGramLookup.class.getName() + " <ngram...> <ngramDataIndex>");
      System.out.println("  Example: " + NGramLookup.class.getName() + " \"my house\" /data/ngram-index");
      System.exit(1);
    }
    String indexTopDir = args[args.length-1];
    try (LuceneLanguageModel lm = new LuceneLanguageModel(new File(indexTopDir))) {
      double totalP = 1;
      for (int i = 0; i < args.length -1; i++) {
        String[] lookup = args[i].split(" ");
        long count = lm.getCount(Arrays.asList(lookup));
        Probability p = lm.getPseudoProbability(Arrays.asList(lookup));
        System.out.println(Arrays.toString(lookup) + " -> count:" + count + ", " + p + ", log:" + Math.log(p.getProb()));
        totalP *= p.getProb();
      }
      System.out.printf("totalP=" + totalP);
    }
  }
}
