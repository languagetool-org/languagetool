/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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

import org.jetbrains.annotations.NotNull;
import org.languagetool.rules.de.LineExpander;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Compare two (mostly) expanded prohibit.txt files.
 */
public class ProhibitComparator {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + ProhibitComparator.class.getName() + " <oldFile> <newFile>");
      System.exit(1);
    }
    List<String> expanded1 = getExpandedLines(args[0]);
    List<String> expanded2 = getExpandedLines(args[1]);

    System.out.println("Words removed in " + args[1] + ":");
    System.out.println("*** NOTE: result might not be accurate for words with '.*'");
    for (String word : expanded1) {
      if (!expanded2.contains(word) && !hasPrefixLine(word, expanded2)) {
        System.out.println(word);
      }
    }
  }

  private static boolean hasPrefixLine(String word, List<String> expanded2) {
    for (String line : expanded2) {
      if (line.endsWith(".*") && word.startsWith(line.substring(0, line.length()-2))) {
        //System.out.println("!!"+line.substring(0, line.length()-2));
        return true;
      }
    }
    return false;
  }

  @NotNull
  private static List<String> getExpandedLines(String filename) throws IOException {
    LineExpander expander = new LineExpander();
    List<String> lines = Files.readAllLines(Paths.get(filename));
    List<String> expanded = new ArrayList<>();
    for (String line : lines) {
      if (line.startsWith("#")) {
        continue;
      }
      expanded.addAll(expander.expandLine(line));
    }
    return expanded;
  }
}
