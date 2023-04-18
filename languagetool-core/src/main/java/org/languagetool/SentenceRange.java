/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.languagetool.markup.AnnotatedText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A range in a text that makes up a sentence.
 * @since 5.8
 */
public class SentenceRange {

  private final int fromPos;
  private final int toPos;

  SentenceRange(int fromPos, int toPos) {
    this.fromPos = fromPos;
    this.toPos = toPos;
  }

  public static List<SentenceRange> getRangesFromSentences(AnnotatedText annotatedText, List<String> sentences) {
    List<SentenceRange> sentenceRanges = new ArrayList<>();
    int pos = 0;
    int diff = annotatedText.getTextWithMarkup().length() - annotatedText.getPlainText().length();
    for (String sentence : sentences) {
      if (sentence.trim().isEmpty()) {
        //No content no sentence
        pos += sentence.length();
        continue;
      }
      //trim whitespaces
      String sentenceNoBeginnWhitespace = sentence.replaceFirst("^\\s*", "");
      String sentenceNoEndWhitespace = sentence.replaceFirst("\\s++$", "");
      //Get position without tailing and leading whitespace
      int fromPos = pos + (sentence.length() - sentenceNoBeginnWhitespace.length());
      int toPos = pos + sentenceNoEndWhitespace.length();

      int fromPosOrig = fromPos + diff;
      int toPosOrig = toPos + diff;
      if (fromPosOrig != annotatedText.getTextWithMarkup().length()) {
        fromPosOrig = annotatedText.getOriginalTextPositionFor(fromPos, false);
      }
      if (toPosOrig != annotatedText.getTextWithMarkup().length()) {
        toPosOrig = annotatedText.getOriginalTextPositionFor(toPos, true);
      }
      sentenceRanges.add(new SentenceRange(fromPosOrig, toPosOrig));
      pos += sentence.length();
    }
    return sentenceRanges;
  }

  public int getFromPos() {
    return fromPos;
  }

  public int getToPos() {
    return toPos;
  }

  @Override
  public String toString() {
    return fromPos + "-" + toPos;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    SentenceRange range = (SentenceRange) o;
    return fromPos == range.fromPos && toPos == range.toPos;
  }

  @Override
  public int hashCode() {
    return Objects.hash(fromPos, toPos);
  }
}
