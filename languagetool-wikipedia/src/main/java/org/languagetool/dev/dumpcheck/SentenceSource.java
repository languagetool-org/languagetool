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

import java.util.Iterator;

/**
 * Source of sentences to be checked/indexed. Sub classes provide access to XML files
 * or plain text sources.
 * @since 2.4
 */
abstract class SentenceSource implements Iterator<Sentence> {

  private static final int MIN_SENTENCE_SIZE = 10;
  private static final int MIN_SENTENCE_WORD_COUNT = 3;
  private static final int MAX_SENTENCE_LENGTH = 300;

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
    String trimSentence = sentence.trim();
    return trimSentence.length() >= MIN_SENTENCE_SIZE && trimSentence.length() <= MAX_SENTENCE_LENGTH
            && countWords(trimSentence) >= MIN_SENTENCE_WORD_COUNT;
  }

  private int countWords(String sentence) {
    return sentence.split("\\s+").length;
  }

}
