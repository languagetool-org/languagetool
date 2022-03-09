/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.JLanguageTool;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.de.GermanSpellerRule;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads a list of words (one per line) and the first part of those that are not accepted
 * by the speller and that are probably compounds.
 */
public class MissingGermanCompoundsFinder {

  private final GermanSpellerRule germanSpeller;

  public MissingGermanCompoundsFinder() {
    germanSpeller = new GermanSpellerRule(JLanguageTool.getMessageBundle(), new GermanyGerman());
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + MissingGermanCompoundsFinder.class.getSimpleName() + " <filename>");
      System.exit(1);
    }
    String filename = args[0];
    new MissingGermanCompoundsFinder().run(filename);
  }

  private void run(String filename) throws IOException {
    System.out.println("# compound words not accepted by LT speller");
    BufferedReader reader = getReaderForFilename(filename);
    String line;
    GermanWordSplitter splitter = new GermanWordSplitter(false);
    Map<String,Integer> firstPartCount = new HashMap<>();
    while ((line = reader.readLine()) != null) {
      String word;
      int count;
      if (line.contains("\t")) {
        count = Integer.parseInt(line.split("\t")[0]);
        word = line.split("\t")[1];
      } else {
        count = 1;
        word = line;
      }
      if (word.length() < 50 && StringTools.startsWithUppercase(word) && !isKnownByGermanSpeller(word)) {
        List<String> wordParts = splitter.splitWord(word);
        if (wordParts.size() > 1) {
          String key = wordParts.get(0);
          if (firstPartCount.containsKey(key)) {
            firstPartCount.put(key, firstPartCount.get(key)+count);
          } else {
            firstPartCount.put(key, count);
          }
        }
      }
    }
    for (Map.Entry<String, Integer> entry : firstPartCount.entrySet()) {
      if (entry.getValue() > 0) {
        boolean known = isKnownByGermanSpeller(entry.getKey() + "test");
        System.out.println(entry.getValue() + " " + entry.getKey() + " " + known);
      } else {
        System.out.println(entry.getValue() + " " + entry.getKey());
      }
    }
  }

  private boolean isKnownByGermanSpeller(String word) {
    return !germanSpeller.isMisspelled(StringTools.uppercaseFirstChar(word)) ||
      !germanSpeller.isMisspelled(StringTools.lowercaseFirstChar(word));
  }

  private BufferedReader getReaderForFilename(String filename) throws FileNotFoundException {
    FileInputStream fis = new FileInputStream(filename);
    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
    return new BufferedReader(isr);
  }

}
