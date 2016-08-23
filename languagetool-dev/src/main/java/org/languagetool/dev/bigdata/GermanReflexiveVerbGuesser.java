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
 * Hackish attempt to have a corpus-based guess on whether a German verb is reflexive.
 */
final class GermanReflexiveVerbGuesser {

  private GermanReflexiveVerbGuesser() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + GermanReflexiveVerbGuesser.class.getName() + " <ngramDataIndex> <verbLemmaFile>");
      System.exit(1);
    }
    String indexTopDir = args[0];
    List<String> lemmas = Files.readAllLines(Paths.get(args[1]));
    //System.out.println("mich ... | ... mich | Anzahl Lemma | Lemma");
    System.out.println("Anzahl Lemma | mich/uns/euch ... | ... mich/uns/euch | Lemma");
    try (LuceneLanguageModel lm = new LuceneLanguageModel(new File(indexTopDir))) {
      for (String lemma : lemmas) {
        long reflexiveCount1 = count1(lm, lemma);
        long reflexiveCount2 = count2(lm, lemma);
        long lemmaCount = lm.getCount(lemma);
        float factor1 = ((float)reflexiveCount1 / lemmaCount) * 100.0f;
        float factor2 = ((float)reflexiveCount2 / lemmaCount) * 100.0f;
        //System.out.printf("%.2f%% %.2f%% " + reflexiveCount1 + " " + reflexiveCount2 + " " + lemmaCount + " " + lemma + "\n", factor1, factor2);
        //System.out.printf("%.2f%% %.2f%% " + lemmaCount + " " + lemma + "\n", factor1, factor2);
        System.out.printf("%d %.2f%% %.2f%% %s\n", lemmaCount, factor1, factor2, lemma);
      }
    }
  }

  private static long count1(LuceneLanguageModel lm, String lemma) {
    return
      lm.getCount(asList("mich", lemma))
      //+ lm.getCount(asList("dich", sing2))
      //+ lm.getCount(asList("sich", sing3))
      + lm.getCount(asList("uns", lemma))
      //+ lm.getCount(asList("euch", plu2))
      + lm.getCount(asList("sich", lemma));
  }
  
  private static long count2(LuceneLanguageModel lm, String lemma) {
    return
      lm.getCount(asList(lemma, "mich"))
      //+ lm.getCount(asList(sing2, "dich"))
      //+ lm.getCount(asList(sing3, "sich"))
      + lm.getCount(asList(lemma, "uns"))
      //+ lm.getCount(asList(plu2, "euch"))
      + lm.getCount(asList(lemma, "sich"));
  }
}
