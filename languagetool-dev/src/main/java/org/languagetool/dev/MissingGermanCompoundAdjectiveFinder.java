/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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

import de.danielnaber.jwordsplitter.GermanWordSplitter;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.GermanyGerman;
import org.languagetool.tagging.Tagger;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Find German compounds like "verwaltungstechnisch" that are not tagged yet.
 */
public class MissingGermanCompoundAdjectiveFinder {

  public static void main(String[] args) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    Tagger tagger = new GermanyGerman().getTagger();
    Set<String> printed = new HashSet<>();
    int j = 0;
    int splitCount = 0;
    GermanWordSplitter splitter = new GermanWordSplitter(false);
    for (String line : lines) {
      if (!StringTools.startsWithLowercase(line)) {
        continue;
      }
      String clean = line.replaceFirst("e[rmns]?$", "");
      List<AnalyzedTokenReadings> tags = tagger.tag(Collections.singletonList(clean));
      boolean isTagged = tags.stream().anyMatch(k -> k.isTagged());
      if (isTagged) {
        continue;
      }
      for (int i = clean.length()-2; i > 2; i--) {
        String part1 = clean.substring(0, i);
        String part2 = clean.substring(i);
        List<AnalyzedTokenReadings> part1Tags = tagger.tag(Collections.singletonList(StringTools.uppercaseFirstChar(part1)));
        List<AnalyzedTokenReadings> part2Tags = tagger.tag(Collections.singletonList(part2));
        boolean part1isNoun = part1Tags.stream().anyMatch(k -> k.hasAnyPartialPosTag("SUB"));
        boolean part2isAdj = part2Tags.stream().anyMatch(k -> k.hasAnyPartialPosTag("ADJ", "PA1", "PA2"));
        if (part1isNoun && part2isAdj && !printed.contains(clean)) {
          //System.out.println(part1 + " / " + part2 + " " + part2isAdj);
          List<String> split = splitter.splitWord(clean);
          if (split.size() > 1) {
            splitCount++;
          }
          System.out.println(j + ". " + clean);
          printed.add(clean);
          j++;
        }
      }
    }
    System.out.println("splitCount: " + splitCount);
  }

  /*public static void main2(String[] args) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get("/tmp/x"));
    GermanSpellerRule speller = new GermanSpellerRule(JLanguageTool.getMessageBundle(), new GermanyGerman());
    for (String line : lines) {
      if (!speller.isMisspelled(line)) {
        System.out.println(line);
      }
    }
  }*/
}
