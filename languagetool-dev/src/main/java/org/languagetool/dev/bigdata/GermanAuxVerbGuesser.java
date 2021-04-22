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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Locale;

import static java.util.Arrays.asList;

/**
 * Hackish attempt to have a corpus-based guess on whether a German verb uses "haben"
 * or "sein" as auxiliary verb. E.g. it's "getrunken haben" and not "getrunken sein".
 * Requires Google ngram index or similar in Lucene format as used by LT and
 * a list of "participle2 \t lemma" per line (i.e. tab-separated), e.g. "getrunken \t trinken".
 */
final class GermanAuxVerbGuesser {

  private GermanAuxVerbGuesser() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + GermanAuxVerbGuesser.class.getName() + " <ngramDataIndex> <lemmaFile>");
      System.out.println("   <lemmaFile> is a text file with 'participle2 \\t lemma' per line, e.g. 'getrunken \t trinken'");
      System.exit(1);
    }
    String indexTopDir = args[0];
    List<String> lines = Files.readAllLines(Paths.get(args[1]));
    int match = 0;
    int noMatch = 0;
    int unambiguous = 0;
    try (LuceneLanguageModel lm = new LuceneLanguageModel(new File(indexTopDir))) {
      for (String line : lines) {
        if (line.startsWith("#")) {
          continue;
        }
        String pa2 = line.split("\t")[0];
        String lemma = line.split("\t")[1];
        long haben = countHaben(lm, pa2);
        long sein = countSein(lm, pa2);
        float ratio = (float)haben/sein;
        System.out.printf(Locale.ENGLISH, "%.2f " + lemma + ": haben: " + haben + ", sein: " + sein + "\n", ratio);
        if (haben == 0 && sein == 0) {
          noMatch++;
        } else {
          if (haben == 0 && sein > 0 || haben > 0 && sein == 0) {
            unambiguous++;
          }
          match++;
        }
      }
    }
    System.out.println("match: " + match);
    System.out.println("noMatch: " + noMatch);
    System.out.println("----");
    System.out.println("unambiguous: " + unambiguous);
  }

  private static long countHaben(LuceneLanguageModel lm, String pa2) {
    return 
        count(lm, pa2, "habe")
      + count(lm, pa2, "hast")
      + count(lm, pa2, "hat")
      + count(lm, pa2, "habt")
      + count(lm, pa2, "haben")

      + count(lm, pa2, "hatte")
      + count(lm, pa2, "hattest")
      + count(lm, pa2, "hatte")
      + count(lm, pa2, "hatten")
      + count(lm, pa2, "hattet")
      
      + count2(lm, pa2, "werde", "haben")
      + count2(lm, pa2, "wirst", "haben")
      + count2(lm, pa2, "wird", "haben")
      + count2(lm, pa2, "werden", "haben")
      + count2(lm, pa2, "werdet", "haben");
  }

  private static long countSein(LuceneLanguageModel lm, String pa2) {
    return
        count(lm, pa2, "bin")
      + count(lm, pa2, "bist")
      + count(lm, pa2, "ist")
      + count(lm, pa2, "sind")
      + count(lm, pa2, "seid")

      + count(lm, pa2, "war")
      + count(lm, pa2, "warst")
      + count(lm, pa2, "war")
      + count(lm, pa2, "waren")
      + count(lm, pa2, "wart")

      + count2(lm, pa2, "werde", "sein")
      + count2(lm, pa2, "wirst", "sein")
      + count2(lm, pa2, "wird", "sein")
      + count2(lm, pa2, "werden", "sein")
      + count2(lm, pa2, "werdet", "sein");
  }

  private static long count(LuceneLanguageModel lm, String pa2, String verb) {
    long count = lm.getCount(asList(verb, pa2));
    if (count > 0) {
      System.out.println(verb + " " + pa2 + ": " + count);
      //long count2 = lm.getCount(asList(verb, pa2, "worden"));
      //System.out.println("  BUT: " + verb + " " + pa2 + " worden: " + count2);
    }
    return count;
  }

  private static long count2(LuceneLanguageModel lm, String pa2, String werde, String sein) {
    long count = lm.getCount(asList(werde, pa2, sein));
    if (count > 0) {
      System.out.println(werde + " " + pa2 + " " + sein + ": " + count);
    }
    return count;
  }
}
