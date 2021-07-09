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
import org.languagetool.tokenizers.Tokenizer;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Source of sentences to be checked/indexed. Sub classes provide access to XML files
 * or plain text sources.
 * @since 2.4
 */
public abstract class SentenceSource implements Iterator<Sentence> {

  static final int MIN_SENTENCE_LENGTH = 10;
  static final int MIN_SENTENCE_TOKEN_COUNT = 4;
  static final int MAX_SENTENCE_LENGTH = 300;

  private final Tokenizer wordTokenizer;
  private final Pattern acceptPattern;
  
  private int ignoreCount = 0;

  SentenceSource(Language language) {
    this(language, null);
  }

  /** @since 3.0 */
  SentenceSource(Language language, Pattern acceptPattern) {
    wordTokenizer = language.getWordTokenizer();
    this.acceptPattern = acceptPattern;
  }

  @Override
  public abstract boolean hasNext();

  /**
   * Return the next sentence. Sentences from the source are filtered by length
   * to remove very short and very long sentences.
   */
  @Override
  public abstract Sentence next();

  public abstract String getSource();

  @Override
  public void remove() {
    throw new UnsupportedOperationException("remove not supported");
  }

  @Override
  public String toString() {
    return getSource() + "-" + super.toString();
  }

  protected boolean acceptSentence(String sentence) {
    if (acceptPattern != null) {
      if (!acceptPattern.matcher(sentence).find()) {
        // useful speedup: we don't consider sentences that cannot match anyway
        ignoreCount++;
        return false;
      }
    }
    String trimSentence = sentence.trim();
    boolean accept = trimSentence.length() >= MIN_SENTENCE_LENGTH && trimSentence.length() <= MAX_SENTENCE_LENGTH
      && countTokens(trimSentence) >= MIN_SENTENCE_TOKEN_COUNT;
    if (accept) {
      return true;
    } else {
      ignoreCount++;
      return false;
    }
  }
  
  int getIgnoredCount() {
    return ignoreCount;
  }
  
  private int countTokens(String sentence) {
    int realTokens = 0;
    List<String> allTokens = wordTokenizer.tokenize(sentence);
    for (String token : allTokens) {
      if (!token.trim().isEmpty()) {
        realTokens++;
      }
    }
    return realTokens;
  }

}
