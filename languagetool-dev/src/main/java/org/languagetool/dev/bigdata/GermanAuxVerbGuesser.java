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
        String pa2 = line.split("\t")[0];
        String lemma = line.split("\t")[1];
        long haben = countHaben(lm, pa2, lemma);
        long sein = countSein(lm, pa2, lemma);
        System.out.println(lemma + ": haben: " + haben + ", sein: " + sein);
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

  private static long countHaben(LuceneLanguageModel lm, String pa2, String lemma) {
    return 
        lm.getCount(asList("habe", pa2))
      + lm.getCount(asList("hast", pa2))
      + lm.getCount(asList("hat", pa2))
      + lm.getCount(asList("habt", pa2))
      + lm.getCount(asList("haben", pa2))

      + lm.getCount(asList("hatte", pa2))
      + lm.getCount(asList("hattest", pa2))
      + lm.getCount(asList("hatte", pa2))
      + lm.getCount(asList("hatten", pa2))
      + lm.getCount(asList("hattet", pa2))
      
      + lm.getCount(asList("werde", pa2, "haben"))
      + lm.getCount(asList("wirst", pa2, "haben"))
      + lm.getCount(asList("wird", pa2, "haben"))
      + lm.getCount(asList("werden", pa2, "haben"))
      + lm.getCount(asList("werdet", pa2, "haben"));
  }

  private static long countSein(LuceneLanguageModel lm, String pa2, String lemma) {
    return
        lm.getCount(asList("bin", pa2))
      + lm.getCount(asList("bist", pa2))
      + lm.getCount(asList("ist", pa2))
      + lm.getCount(asList("sind", pa2))
      + lm.getCount(asList("seid", pa2))

      + lm.getCount(asList("war", pa2))
      + lm.getCount(asList("warst", pa2))
      + lm.getCount(asList("war", pa2))
      + lm.getCount(asList("waren", pa2))
      + lm.getCount(asList("wart", pa2))

      + lm.getCount(asList("werde", pa2, "sein"))
      + lm.getCount(asList("wirst", pa2, "sein"))
      + lm.getCount(asList("wird", pa2, "sein"))
      + lm.getCount(asList("werden", pa2, "sein"))
      + lm.getCount(asList("werdet", pa2, "sein"));
  }
}
