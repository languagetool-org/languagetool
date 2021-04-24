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

package org.languagetool.tokenizers.en;

import org.junit.Test;
import org.languagetool.JLanguageTool;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TokenizeMultiwordsTest {

  private final String MULTIWORDS_FILE = "/en/multiwords.txt";

  private final List<String> files_to_test = Arrays.asList("spelling_global.txt", "/en/added.txt", "/en/removed.txt",
      "/en/hunspell/ignore.txt", "/en/hunspell/prohibit.txt", "/en/hunspell/prohibit_custom.txt",
      "/en/hunspell/spelling.txt", "/en/hunspell/spelling_custom.txt", "/en/hunspell/spelling_en-AU.txt",
      "/en/hunspell/spelling_en-CA.txt", "/en/hunspell/spelling_en-GB.txt", "/en/hunspell/spelling_en-NZ.txt",
      "/en/hunspell/spelling_en-US.txt", "/en/hunspell/spelling_en-ZA.txt", "/en/hunspell/spelling_merged.txt");

  @Test
  public void testTokenize() {
    final EnglishWordTokenizer wordTokenizer = new EnglishWordTokenizer();
    List<String> multiwords;
    
    System.out.println("Checking multi-token words in spelling files...");

    // read multiwords.txt
    try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(MULTIWORDS_FILE)) {
      multiwords = loadWords(stream);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // read spelling.txt
    for (String fileName : files_to_test) {
      List<String> wordList;
      try (InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(fileName)) {
        wordList = loadWords(stream);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      for (String word : wordList) {
        if (!multiwords.contains(word.replaceAll("’", "'"))) {
          List<String> tokens = wordTokenizer.tokenize(word);
          if (tokens.size() > 1) {
            System.out.println("WARNING: '" + word + "' in '" + fileName
                + "' is multi-token and useless here for English spelling. Add it to multiwords.txt or disambiguation.xml.");
          }
        }

      }
    }
  }

  private List<String> loadWords(InputStream stream) {
    List<String> lines = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        line = line.replaceFirst("#.*", "").trim();
        if (line.isEmpty() || line.charAt(0) == '#') { // ignore comments
          continue;
        }
        //
        String[] parts = line.split("\t");
        lines.add(parts[0]);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return lines;
  }
}
