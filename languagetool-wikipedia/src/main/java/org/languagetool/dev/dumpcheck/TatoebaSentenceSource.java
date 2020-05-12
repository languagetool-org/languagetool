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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Provides access to the sentences of a Tatoeba (http://tatoeba.org) text
 * file (tab separated) that has already been filtered to contain only one language.
 * @since 2.4
 */
class TatoebaSentenceSource extends SentenceSource {

  private final List<TatoebaSentence> sentences;
  private final BufferedReader reader;

  // Each sentence is one article, but count anyway so it's coherent with what the Wikipedia code does:
  private int articleCount = 0;
  
  TatoebaSentenceSource(InputStream textInput, Language language) {
    this(textInput, language, null);
  }

  /** @since 3.0 */
  TatoebaSentenceSource(InputStream textInput, Language language, Pattern filter) {
    super(language, filter);
    reader = new BufferedReader(new InputStreamReader(textInput));
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
    TatoebaSentence sentence = sentences.remove(0);
    String title = "Tatoeba-" + sentence.id;
    return new Sentence(sentence.sentence, getSource(), title, "http://tatoeba.org", ++articleCount);
  }

  @Override
  public String getSource() {
    return "tatoeba";
  }

  private void fillSentences() {
    try {
      String line;
      while (sentences.isEmpty() && (line = reader.readLine()) != null) {
        if (line.isEmpty()) {
          continue;
        }
        String[] parts = line.split("\t");
        if (parts.length != 3) {
          System.err.println("Unexpected line format: expected three tab-separated columns: '" + line + "', skipping");
          continue;
        }
        long id = Long.parseLong(parts[0]);
        String sentence = parts[2];  // actually it's sometimes two (short) sentences, but anyway...
        if (acceptSentence(sentence)) {
          sentences.add(new TatoebaSentence(id, sentence));
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static class TatoebaSentence {
    long id;
    String sentence;
    private TatoebaSentence(long id, String sentence) {
      this.id = id;
      this.sentence = Objects.requireNonNull(sentence);
    }
  }
}
