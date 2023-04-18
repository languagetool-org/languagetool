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
import org.languagetool.tagging.en.EnglishTagger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Fast hack to find words which have no POS tagging.
 */
public class MissingEnglishPosFinder {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + MissingEnglishPosFinder.class.getSimpleName() + " <file> <ngram_dir>");
      System.exit(1);
    }
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    //List<String> lines = Arrays.asList("bike", "nicexxx");
    LuceneLanguageModel lm = new LuceneLanguageModel(new File(args[1]));
    EnglishTagger tagger = new EnglishTagger();
    for (String word : lines) {
      int origCount = -1;
      if (word.matches("\\d+ .*")) {
        String[] parts = word.split(" ");
        origCount = Integer.parseInt(parts[0]);
        word = parts[1];
      }
      word = word.trim();
      if (word.endsWith(".")) {
        word = word.substring(0, word.length()-1);
      }
      List<AnalyzedTokenReadings> matches = tagger.tag(Collections.singletonList(word));
      List<AnalyzedTokenReadings> lcMatches = tagger.tag(Collections.singletonList(word.toLowerCase()));
      if (matches.size() == 1 && noTag(matches.get(0)) && lcMatches.size() == 1 && noTag(lcMatches.get(0))) {
        long count = origCount == -1 ? lm.getCount(word) : origCount;
        System.out.println(count + "\t" + word);
      }
    }
  }

  private static boolean noTag(AnalyzedTokenReadings atr) {
    return !atr.isTagged();
  }
}
