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

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.languagemodel.LuceneLanguageModel;
import org.languagetool.tagging.de.GermanTagger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Fast hack to find words which have no POS tagging.
 */
public class MissingGermanPosFinder {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + MissingGermanPosFinder.class.getSimpleName() + " <file> <ngram_dir>");
      System.exit(1);
    }
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    //List<String> lines = Arrays.asList("Bundesrepublik", "Landwirtschaft", "Perl", "Haus", "Drücke", "Wischdsda", "gut", "schönxxx");
    LuceneLanguageModel lm = new LuceneLanguageModel(new File(args[1]));
    GermanTagger tagger = new GermanTagger();
    for (String word : lines) {
      word = word.trim();
      AnalyzedTokenReadings matches = tagger.lookup(word);
      if (matches == null || !matches.isTagged()) {
        long count = lm.getCount(word);
        System.out.println(count + "\t" + word);
      }
    }
  }
}
