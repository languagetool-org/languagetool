/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.spelling.morfologik;

import morfologik.fsa.CFSA2Serializer;
import morfologik.fsa.FSA;
import morfologik.fsa.FSABuilder;
import morfologik.stemming.Dictionary;
import org.languagetool.JLanguageTool;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Morfologik speller that merges results from binary (.dict) and plain text (.txt) dictionaries.
 *
 * @since 2.9
 */
public class MorfologikMultiSpeller {

  private final List<MorfologikSpeller> spellers = new ArrayList<>();

  /**
   * @param paths paths in classpath to morfologik dictionaries, each one either a
   *              plain text {@code .txt} file or a {@code .dict} binary Morfologik file
   * @param maxEditDistance maximum edit distance for accepting suggestions
   */
  public MorfologikMultiSpeller(List<String> paths, int maxEditDistance) throws IOException {
    for (String path : paths) {
      MorfologikSpeller speller;
      if (path.endsWith(".txt")) {
        InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"))) {
          List<byte[]> lines = getLines(br);
          Dictionary dictionary = getDictionary(lines);
          speller = new MorfologikSpeller(dictionary, maxEditDistance);
        }
      } else if (path.endsWith(".dict")) {
        speller = new MorfologikSpeller(path, maxEditDistance);
      } else {
        throw new RuntimeException("Unsupported dictionary, file needs to have suffix .txt (plain text) or .dict (binary Morfologik file): " + path);
      }
      spellers.add(speller);
    }
  }

  private List<byte[]> getLines(BufferedReader br) throws IOException {
    List<byte[]> lines = new ArrayList<>();
    String line;
    while ((line = br.readLine()) != null) {
      lines.add(line.getBytes("utf-8"));
    }
    return lines;
  }

  private Dictionary getDictionary(List<byte[]> lines) throws IOException {
    Collections.sort(lines, FSABuilder.LEXICAL_ORDERING);
    FSA fsa = FSABuilder.build(lines);
    ByteArrayOutputStream fsaOutStream = new CFSA2Serializer().serialize(fsa, new ByteArrayOutputStream());
    ByteArrayInputStream fsaInStream = new ByteArrayInputStream(fsaOutStream.toByteArray());
    String metaData = "fsa.dict.separator=+\n" +
                      "fsa.dict.encoding=utf-8\n";
    ByteArrayInputStream metaDataStream = new ByteArrayInputStream(metaData.getBytes("utf-8"));
    return Dictionary.readAndClose(fsaInStream, metaDataStream);
  }

  /**
   * Accept the word if at least one of the dictionaries accepts it as not misspelled.
   */
  public boolean isMisspelled(String word) {
    for (MorfologikSpeller speller : spellers) {
      if (!speller.isMisspelled(word)) {
        return false;
      }
    }
    return true;
  }

  /**
   * The suggestions from all dictionaries (without duplicates).
   */
  public List<String> getSuggestions(String word) {
    List<String> result = new ArrayList<>();
    for (MorfologikSpeller speller : spellers) {
      List<String> suggestions = speller.getSuggestions(word);
      for (String suggestion : suggestions) {
        if (!result.contains(suggestion)) {
          result.add(suggestion);
        }
      }
    }
    return result;
  }
}
