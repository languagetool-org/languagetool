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
package org.languagetool.dev.dumpcheck;

import org.languagetool.Language;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

/**
 * Provides access to the relevant sentences of a plain text file
 * with one sentence per line.
 * @since 3.0
 */
public class PlainTextSentenceSource extends SentenceSource {

  private final List<String> sentences;
  private final BufferedReader reader;

  // Each sentence is one article, but count anyway so it's coherent with what the Wikipedia code does:
  private int articleCount = 0;
  private String currentUrl = null;

  public PlainTextSentenceSource(InputStream textInput, Language language) {
    this(textInput, language, null);
  }
  
  /** @since 3.0 */
  public PlainTextSentenceSource(InputStream textInput, Language language, Pattern filter) {
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
    return new Sentence(sentences.remove(0), getSource(), "<plaintext>", null, ++articleCount);
  }

  @Override
  public String getSource() {
    return currentUrl;
  }

  private void fillSentences() {
    try {
      String line;
      while (sentences.isEmpty() && (line = reader.readLine()) != null) {
        if (line.isEmpty()) {
          continue;
        }
        if (line.startsWith("# source:")) {
          currentUrl = line.substring("# source: ".length());
          continue;
        }
        if (acceptSentence(line)) {
          sentences.add(line);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

}
