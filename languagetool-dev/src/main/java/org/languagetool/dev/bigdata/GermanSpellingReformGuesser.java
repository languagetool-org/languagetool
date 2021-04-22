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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Hack: Reads a list of German lemmas and guesses which ones are affected
 * by the spelling reform.
 */
class GermanSpellingReformGuesser {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + GermanSpellingReformGuesser.class.getSimpleName() + " <lemmaList>");
      System.exit(1);
    }
    new GermanSpellingReformGuesser().run(args[0]);
  }

  private void run(String lemmaFile) throws IOException {
    List<String> lines = Files.readAllLines(Paths.get(lemmaFile));
    Set<String> lemmas = new HashSet<>(lines);
    Set<String> result = new HashSet<>();
    for (String line : lines) {
      String oldSpelling1 = line.replace("ss", "ß");
      /*if (!oldSpelling1.equals(line) && lemmas.contains(oldSpelling1)) {
        result.add(line + ";" + oldSpelling1);
      }
      String newSpelling1 = line.replace("ß", "ss");
      if (!newSpelling1.equals(line) && lemmas.contains(newSpelling1)) {
        result.add(newSpelling1 + ";" + line);
      }*/
      String oldSpelling2 = line.replace("f", "ph");
      if (!oldSpelling2.equals(line) && lemmas.contains(oldSpelling2)) {
        result.add(line + ";" + oldSpelling2);
      }
      String newSpelling1 = line.replace("ph", "f");
      if (!newSpelling1.equals(line) && lemmas.contains(newSpelling1)) {
        result.add(newSpelling1 + ";" + line);
      }
    }
    for (String s : result) {
      System.out.println(s);
    }
    System.err.println(result.size() + " Paare gefunden");
  }
  
}
