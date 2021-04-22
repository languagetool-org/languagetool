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
 * Hack to guess whether a German verb requires Akkusativ or Dativ, e.g. "ich reserviere mir"
 * vs. "ich schäme mich".
 * Requires Google ngram index or similar in Lucene format as used by LT and
 * a list of "participle2 \t lemma" per line (i.e. tab-separated), e.g. "getrunken \t trinken".
 */
final class GermanAuxVerbGuesser2 {

  private GermanAuxVerbGuesser2() {
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + GermanAuxVerbGuesser2.class.getName() + " <ngramDataIndex> <lemmaFile>");
      System.out.println("   <lemmaFile> is a text file with 'participle2 \\t lemma' per line, e.g. 'getrunken \t trinken'");
      System.exit(1);
    }
    String indexTopDir = args[0];
    List<String> lines = Files.readAllLines(Paths.get(args[1]));
    System.out.println("# factor lemma Dativ/mir Akkusativ/mich");
    try (LuceneLanguageModel lm = new LuceneLanguageModel(new File(indexTopDir))) {
      for (String line : lines) {
        String pa2 = line.split("\t")[0];
        String lemma = line.split("\t")[1];
        long mir = count(lm, pa2, lemma, "mir");
        long mich = count(lm, pa2, lemma, "mich");
        long dir = count(lm, pa2, lemma, "dir");
        long dich = count(lm, pa2, lemma, "dich");
        float factor = ((float)mir + dir) / ((float)mich + dich);
        System.out.println(factor + " " + lemma + " " + mir + " " + mich);
      }
    }
  }

  private static long count(LuceneLanguageModel lm, String pa2, String lemma, String reflexivePronoun) {
    return 
        lm.getCount(asList(reflexivePronoun, pa2)) 
      + lm.getCount(asList(reflexivePronoun, lemma));
  }

}
