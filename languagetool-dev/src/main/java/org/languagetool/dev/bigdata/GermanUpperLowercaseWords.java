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

import org.jetbrains.annotations.NotNull;
import org.languagetool.tools.StringTools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Walks a list of verbs and print those that appear in upper and lowercase (e.g. Teil, teil).
 * Use {@link AutomaticConfusionRuleEvaluator} on the result.
 */
final class GermanUpperLowercaseWords {

  private GermanUpperLowercaseWords() {
  }

  @NotNull
  private static Set<String> getUppercaseWords(List<String> lines) {
    Set<String> uppercaseWords = new HashSet<>();
    for (String line : lines) {
      if (StringTools.startsWithUppercase(line)) {
        uppercaseWords.add(line);
      }
    }
    return uppercaseWords;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + GermanUpperLowercaseWords.class.getSimpleName() + " <wordList>");
      System.exit(1);
    }
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    Set<String> uppercaseWords = getUppercaseWords(lines);
    for (String line : lines) {
      String uppercased = StringTools.uppercaseFirstChar(line);
      if (!StringTools.startsWithUppercase(line) && uppercaseWords.contains(uppercased)) {
        System.out.println(line + "; " + uppercased);
      }
    }
  }
  
}
