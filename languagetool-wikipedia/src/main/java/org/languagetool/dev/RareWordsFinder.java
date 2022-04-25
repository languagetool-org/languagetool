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
package org.languagetool.dev;

import org.languagetool.rules.spelling.hunspell.Hunspell;
import org.languagetool.rules.spelling.hunspell.HunspellDictionary;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Scanner;

/**
 * A hacky attempt to find rare words which are considered correct
 * by the spell checker, but which might actually be too rare,
 * so a rule with a warning might be advisable.
 * @since 3.6
 */
final class RareWordsFinder {

  private static final String dictInClassPath = "/en/hunspell/en_US.dict";
  
  private final HunspellDictionary hunspell;
  
  private RareWordsFinder(String hunspellBase) throws IOException {
    hunspell = Hunspell.getDictionary(Paths.get(hunspellBase + ".dic"), Paths.get(hunspellBase + ".aff"));
  }
  
  private void run(File input, int minimum) throws FileNotFoundException, CharacterCodingException {
    MorfologikSpeller speller = new MorfologikSpeller(dictInClassPath, 1);
    int lineCount = 0;
    int wordCount = 0;
    try (Scanner s = new Scanner(input)) {
      while (s.hasNextLine()) {
        String line = s.nextLine();
        String[] parts = line.split("\t");
        String word = parts[0];
        long count = Long.parseLong(parts[1]);
        if (count <= minimum) {
          if (word.matches("[a-zA-Z]+") && !word.matches("[A-Z]+") && !word.matches("[a-zA-Z]+[A-Z]+[a-zA-Z]*") && !word.matches("[A-Z].*")) {
            boolean isMisspelled = speller.isMisspelled(word);
            if (!isMisspelled) {
              //List<String> suggestions = speller.getSuggestions(word);  // seems to work only for words that are actually misspellings
              List<String> suggestions = hunspell.suggest(word);
              suggestions.remove(word);
              if (suggestionsMightBeUseful(word, suggestions)) {
                System.out.println(word + "\t" + count + " -> " + String.join(", ", suggestions));
                wordCount++;
              }
            }
          }
        }
        lineCount++;
        if (lineCount % 1_000_000 == 0) {
          System.out.println("lineCount: " + lineCount + ", words found: " + wordCount);
        }
      }
      System.out.println("Done. lineCount: " + lineCount + ", words found: " + wordCount);
    }
  }

  private boolean suggestionsMightBeUseful(String word, List<String> suggestions) {
    return suggestions.size() > 0 &&
            !suggestions.get(0).contains(" ") &&
            !suggestions.get(0).equals(word + "s") &&
            !suggestions.get(0).equals(word.replaceFirst("s$", ""));
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("Usage: " + RareWordsFinder.class.getSimpleName() + " <wordFile> <hunspellBase> <limit>");
      System.out.println("    <wordFile> is a word file with occurrence counts, separated by tabs");
      System.out.println("    <hunspellBase> is the hunspell file without suffix, e.g. '/path/to/en_US'");
      System.out.println("    <limit> only words with this many or less occurrences are considered");
      System.exit(1);
    }
    RareWordsFinder finder = new RareWordsFinder(args[1]);
    File input = new File(args[0]);
    int minimum = Integer.parseInt(args[2]);
    finder.run(input, minimum);
  }
  
}
