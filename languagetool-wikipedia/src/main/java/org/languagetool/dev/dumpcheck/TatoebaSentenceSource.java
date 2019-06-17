/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.dumpcheck;

import org.languagetool.Language;

import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Provides access to the sentences of a Tatoeba (http://tatoeba.org) text
 * file (tab separated) that has already been filtered to contain only one language.
 * @since 2.4
 */
class TatoebaSentenceSource extends SentenceSource {

  private final List<String> sentences;
  private final Scanner scanner;

  // Each sentence is one article, but count anyway so it's coherent with what the Wikipedia code does:
  private int articleCount = 0;
  
  TatoebaSentenceSource(InputStream textInput, Language language) {
    this(textInput, language, null);
  }

  /** @since 3.0 */
  TatoebaSentenceSource(InputStream textInput, Language language, Pattern filter) {
    super(language, filter);
    scanner = new Scanner(textInput);
    sentences = new ArrayList<>();
  }

  @Override
  public boolean hasNext() {
    fillSentences();
    return sentences.size() > 0;
  }

  @Override
  public Sentence next() {
    fillSentences();
    if (sentences.isEmpty()) {
      throw new NoSuchElementException();
    }
    return new Sentence(sentences.remove(0), getSource(), "<Tatoeba>", "http://tatoeba.org", ++articleCount);
  }

  @Override
  public String getSource() {
    return "tatoeba";
  }

  private void fillSentences() {
    while (sentences.isEmpty() && scanner.hasNextLine()) {
      String line = scanner.nextLine();
      if (line.isEmpty()) {
        continue;
      }
      String[] parts = line.split("\t");
      if (parts.length != 3) {
        throw new RuntimeException("Unexpected line format: expected three tab-separated columns: '" + line  + "'");
      }
      String sentence = parts[2];  // actually it's sometimes two (short) sentences, but anyway...
      if (acceptSentence(sentence)) {
        sentences.add(sentence);
      }
    }
  }

}
