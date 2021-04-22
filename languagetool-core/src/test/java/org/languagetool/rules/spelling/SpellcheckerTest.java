/* LanguageTool, a natural language style checker
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling;

import org.languagetool.Language;
import org.languagetool.Languages;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SpellcheckerTest {

  /**
   * To be called from language modules. Languages.get() only knows the languages that are in the classpath,
   * and that's only the demo language for languagetool-core.
   */
  public void runLanguageSpecificTest() throws IOException {
    int totalProhibited = 0;
    if (Languages.get().isEmpty()) {
      //System.err.println("Warning: no languages found in classpath - cannot run any spell check tests");
      throw new RuntimeException("Warning: no languages found in classpath - cannot run any spell check tests");
    }
    for (Language lang : Languages.get()) {
      CachingWordListLoader loader = new CachingWordListLoader();
      List<String> prohibitedWords = loader.loadWords(lang.getShortCode() + "/hunspell/prohibit.txt");
      totalProhibited += prohibitedWords.size();
      List<String> spellingWords = loader.loadWords(lang.getShortCode() + "/hunspell/spelling.txt");
      System.out.println("Testing " + lang + ": " + prohibitedWords.size() + " words from prohibit.txt and " +
              spellingWords.size() + " words from spelling.txt");
      Set<String> intersection = prohibitedWords.stream()
              .distinct()
              .filter(spellingWords::contains)
              .collect(Collectors.toSet());
      if (intersection.size() > 0) {
        throw new RuntimeException("Word(s) appear in both spelling.txt and prohibit.txt - this doesn't make sense: " + intersection);
      }
    }
    if (Languages.get().size() > 5 && totalProhibited == 0) {
      throw new RuntimeException("No prohibited words at all for " + Languages.get().size() + " languages - is there a bug in this test?!");
    }
  }

}
