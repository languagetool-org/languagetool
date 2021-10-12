/* LanguageTool, a natural language style checker 
 * Copyright (C) 2017 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.fail;

public class SuggestionRegressionTest {
  
  @Test
  @Ignore("a bit too slow to run every time")
  public void testSuggestions() throws IOException {
    String file = "src/test/resources/suggestions.txt";
    List<String> lines = Files.readAllLines(Paths.get(file));
    GermanyGerman german = (GermanyGerman) Languages.getLanguageForShortCode("de-DE");
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getEnglishMessages(), german);
    boolean different = false;
    StringBuilder result = new StringBuilder();
    JLanguageTool lt = new JLanguageTool(german);
    for (String line : lines) {
      if (line.startsWith("#")) {
        result.append(line).append('\n');
        continue;
      }
      String[] parts = line.split(" => ?");
      String word = parts[0];
      if (rule.match(lt.analyzeText(word).get(0)).length == 0) {
        System.out.println("No error, removing from file: " + word);
        continue;
      }
      List<String> oldSuggestions = parts.length > 1 ? Arrays.asList(parts[1].split(", ")) : Collections.emptyList();
      oldSuggestions = oldSuggestions.subList(0, Math.min(5, oldSuggestions.size()));
      List<String> newSuggestions = rule.getSuggestions(word);
      newSuggestions = newSuggestions.subList(0, Math.min(5, newSuggestions.size()));
      String thisResult = word + " => " + String.join(", ", newSuggestions);
      result.append(thisResult).append('\n');
      if (!oldSuggestions.equals(newSuggestions)) {
        System.err.println("Input   : " + word);
        System.err.println("Expected: " + oldSuggestions);
        System.err.println("Got     : " + newSuggestions);
        different = true;
      }
    }
    try (FileWriter fw = new FileWriter(file)) {
      fw.write(result.toString());
    }
    if (different) {
      fail("There were differences between expected and real suggestions, please check them. If they are okay, commit " +
              file + ", otherwise roll back the changes.");
    }
  }

  @Test
  @Ignore("interactive use to find words not yet accepted")
  public void testGetExamples() throws IOException {
    GermanyGerman german = (GermanyGerman) Languages.getLanguageForShortCode("de-DE");
    GermanSpellerRule rule = new GermanSpellerRule(TestTools.getEnglishMessages(), german);
    List<String> lines = Files.readAllLines(Paths.get("/home/dnaber/data/corpus/jan_schreiber/german.dic"));
    JLanguageTool lt = new JLanguageTool(german);
    int i = 0;
    for (String line : lines) {
      RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(line));
      if (matches.length >= 1) {
        System.out.println(line);
      }
      if (i % 100 == 0) {
        System.err.println(i + "...");
      }
      i++;
    }
  }
  
}
