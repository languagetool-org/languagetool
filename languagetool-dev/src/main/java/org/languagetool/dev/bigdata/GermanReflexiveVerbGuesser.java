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

import org.languagetool.AnalyzedToken;
import org.languagetool.language.GermanyGerman;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.synthesis.Synthesizer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * Hackish attempt to have a corpus-based guess on whether a German verb is reflexive.
 */
final class GermanReflexiveVerbGuesser {

  private final Synthesizer synthesizer;
          
  private GermanReflexiveVerbGuesser() {
    synthesizer = new GermanyGerman().getSynthesizer();
  }
  
  private void run(File indexTopDir, File lemmaListFile) throws IOException {
    List<String> lemmas = Files.readAllLines(lemmaListFile.toPath());
    System.out.println("Durchschnitt Prozent | Anzahl Lemma | mich/uns/euch ... | ... mich/uns/euch | Lemma");
    try (LuceneLanguageModel lm = new LuceneLanguageModel(indexTopDir)) {
      for (String lemma : lemmas) {
        //if (!lemma.equals("reklamieren")) { continue; }
        //if (!lemma.equals("hertreiben")) { continue; }
        String[] firstPsSinArray = synthesizer.synthesize(new AnalyzedToken(lemma, "VER:INF:NON", lemma), "VER:1:SIN:PRÄ.*", true);
        String[] thirdPsSinArray = synthesizer.synthesize(new AnalyzedToken(lemma, "VER:INF:NON", lemma), "VER:3:SIN:PRÄ.*", true);
        String firstPsSin = firstPsSinArray.length > 0 ? firstPsSinArray[0] : null;
        String thirdPsSin = thirdPsSinArray.length > 0 ? thirdPsSinArray[0] : null;
        long reflexiveCount1 = count1(lm, lemma, firstPsSin, thirdPsSin) 
                               - counterExamples("für", lm, lemma, firstPsSin, thirdPsSin)
                               - counterExamples("vor", lm, lemma, firstPsSin, thirdPsSin);
        long reflexiveCount2 = count2(lm, lemma, firstPsSin, thirdPsSin);
        long lemmaCount = lm.getCount(lemma);
        float factor1 = ((float)reflexiveCount1 / lemmaCount) * 100.0f;
        float factor2 = ((float)reflexiveCount2 / lemmaCount) * 100.0f;
        float avgFactor = (factor1 + factor2) / 2;
        //System.out.printf("%.2f%% %.2f%% " + reflexiveCount1 + " " + reflexiveCount2 + " " + lemmaCount + " " + lemma + "\n", factor1, factor2);
        //System.out.printf("%.2f%% %.2f%% " + lemmaCount + " " + lemma + "\n", factor1, factor2);
        System.out.printf("%.2f %d %.2f%% %.2f%% %s\n", avgFactor, lemmaCount, factor1, factor2, lemma);
      }
    }
  }

  private long count1(LuceneLanguageModel lm, String lemma, String firstPsSin, String thirdPsSin) {
    return
      lm.getCount(asList("mich", firstPsSin))  // "wenn ich mich schäme"
      + lm.getCount(asList("mich", lemma))     // "ich muss mich schämen"
      //+ lm.getCount(asList("dich", sing2))
      + lm.getCount(asList("sich", thirdPsSin))
      + lm.getCount(asList("uns", lemma))
      + lm.getCount(asList("euch", lemma))
      + lm.getCount(asList("sich", lemma));
  }

  private long counterExamples(String term, LuceneLanguageModel lm, String lemma, String firstPsSin, String thirdPsSin) {
    return
      lm.getCount(asList(term, "mich", firstPsSin))  // "für mich reklamiere"
      + lm.getCount(asList(term, "mich", lemma))     // "... für mich reklamieren"
      + lm.getCount(asList(term, "sich", thirdPsSin))
      + lm.getCount(asList(term, "uns", lemma))
      + lm.getCount(asList(term, "euch", lemma))
      + lm.getCount(asList(term, "sich", lemma));
  }

  private long count2(LuceneLanguageModel lm, String lemma, String firstPsSin, String thirdPsSin) {
    return
      lm.getCount(asList(firstPsSin, "mich"))  // "schäme mich"
      //+ lm.getCount(asList(sing2, "dich"))
      + lm.getCount(asList(thirdPsSin, "sich"))
      + lm.getCount(asList(lemma, "uns"))
      //+ lm.getCount(asList(plu2, "euch"))
      + lm.getCount(asList(lemma, "sich"));
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + GermanReflexiveVerbGuesser.class.getName() + " <ngramDataIndex> <verbLemmaFile>");
      System.exit(1);
    }
    String indexTopDir = args[0];
    String lemmaListFile = args[1];
    new GermanReflexiveVerbGuesser().run(new File(indexTopDir), new File(lemmaListFile));
  }
}
