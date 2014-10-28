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

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Simple command line tool to look up occurrence counts in an ngram index.
 * @since 2.7
 */
class NGramStats {

  private void lookup(File dir, String phrase) throws IOException {
    try (HomophoneOccurrenceDumper lm = new HomophoneOccurrenceDumper(dir)) {
      String[] tokens = phrase.split(" ");
      if (tokens.length == 1) {
        String[] parts = phrase.split("\\|");
        if (parts.length < 2) {
           throw new RuntimeException("Please specify at least two words like 'there|their': " + phrase);
        }
        System.out.println("Will iterate all terms to find the relevant pairs (this may take some minutes)...");
        Map<String,Long> contexts = lm.getContext(parts);
        showUsefulContexts(contexts, parts);
      } else if (tokens.length == 2) {
        long count = lm.getCount(tokens[0], tokens[1]);
        System.out.println(phrase + ": " + count);
      } else if (tokens.length == 3) {
        long count = lm.getCount(tokens[0], tokens[1], tokens[2]);
        System.out.println(phrase + ": " + count);
      } else {
        throw new RuntimeException("Phrases of length " + tokens.length + " are not yet supported: '" + phrase + "'");
      }
    }
  }

  private void showUsefulContexts(Map<String, Long> contexts, String[] similarWords) {
    List<PairRatio> ratios = getContexts(contexts, similarWords);
    Collections.sort(ratios);
    for (String word : similarWords) {
      printTopContexts(word, ratios);
    }
  }

  private List<PairRatio> getContexts(Map<String, Long> contexts, String[] similarWords) {
    List<PairRatio> ratios = new ArrayList<>();
    for (Map.Entry<String, Long> entry : contexts.entrySet()) {
      String wholeTerm = entry.getKey();
      String[] parts = wholeTerm.split(" ");
      String term = parts[1];
      long count = entry.getValue();
      for (String similarWord : similarWords) {
        if (similarWord.equals(term)) {
          continue;
        }
        Long alternativeCountObj = contexts.get(parts[0] + " " + similarWord + " " + parts[2]);
        long alternativeCount = alternativeCountObj == null ? 0 : alternativeCountObj;
        long ratio = count / Math.max(alternativeCount, 1);
        PairRatio pairRatio = new PairRatio();
        pairRatio.ngram = wholeTerm;
        pairRatio.term1 = term;
        pairRatio.term1Count = count;
        pairRatio.term2 = similarWord;
        pairRatio.term2Count = alternativeCount;
        pairRatio.ratio = ratio;
        ratios.add(pairRatio);
      }
    }
    return ratios;
  }

  private void printTopContexts(String word, List<PairRatio> ratios) {
    int max = 500;
    int i = 0;
    System.out.println("================================================");
    System.out.println("Top " + max + " contexts for '" + word + "':");
    for (PairRatio ratio : ratios) {
      if (!ratio.term1.equals(word)) {
        continue;
      }
      System.out.printf(Locale.ENGLISH,"%dx advantage (%d/%d) for '%s' in: %s\n",
                        ratio.ratio, ratio.term1Count, ratio.term2Count, ratio.term1, ratio.ngram);
      if (++i >= max) {
        break;
      }
    }
  }

  class PairRatio  implements Comparable<PairRatio> {
    String ngram;
    String term1;
    String term2;
    long ratio;
    long term1Count;
    long term2Count;
    @Override
    public int compareTo(PairRatio o) {
      if (ratio > o.ratio) {
        return -1;
      } else if (ratio < o.ratio) {
        return 1;
      } else {
        return 0;
      }
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1 && args.length != 2) {
      System.out.println("Usage: " + NGramStats.class.getSimpleName() + " <dir> <phrase>");
      System.out.println("  'dir' is a directory with e.g. a '3grams' sub directory with a Lucene index of ngrams");
      System.out.println("  'phrase' is one of:");
      System.out.println("      - a 2 or 3-word case-sensitive phrase, e.g. \"the tall boy\" (include the quotes) or");
      System.out.println("      - a pipe-separated list of similar words (e.g. 'calender|calendar') to be compared (slow!)");
      System.exit(1);
    }
    String dir = args[0];
    String phrase = args[1];
    NGramStats stats = new NGramStats();
    stats.lookup(new File(dir), phrase);
  }
}
