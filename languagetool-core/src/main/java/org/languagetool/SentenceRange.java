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

import org.jetbrains.annotations.NotNull;
import org.languagetool.markup.AnnotatedText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A range in a text that makes up a sentence.
 * @since 5.8
 */
public class SentenceRange implements Comparable<SentenceRange>{

  private static final Pattern BEGINS_WITH_SPACE = Pattern.compile("^\\s*");
  private static final Pattern ENDS_WITH_SPACE = Pattern.compile("\\s+$");

  private final int fromPos;
  private final int toPos;

  SentenceRange(int fromPos, int toPos) {
    this.fromPos = fromPos;
    this.toPos = toPos;
  }

  public static List<SentenceRange> getRangesFromSentences(AnnotatedText annotatedText, List<String> sentences) {
    List<SentenceRange> sentenceRanges = new ArrayList<>();
    int pos = 0;
    int markupTextLength = annotatedText.getTextWithMarkup().length();
    int diff = markupTextLength - annotatedText.getPlainText().length();
    for (String sentence : sentences) {
      if (sentence.trim().isEmpty()) {
        //No content no sentence
        pos += sentence.length();
        continue;
      }
      //trim whitespaces
      String sentenceNoBeginWhitespace = BEGINS_WITH_SPACE.matcher(sentence).replaceFirst("");
      String sentenceNoEndWhitespace = ENDS_WITH_SPACE.matcher(sentence).replaceFirst("");
      //Get position without tailing and leading whitespace
      int fromPos = pos + (sentence.length() - sentenceNoBeginWhitespace.length());
      int toPos = pos + sentenceNoEndWhitespace.length();

      int fromPosOrig = fromPos + diff;
      int toPosOrig = toPos + diff;
      if (fromPosOrig != markupTextLength) {
        fromPosOrig = annotatedText.getOriginalTextPositionFor(fromPos, false);
      }
      if (toPosOrig != markupTextLength) {
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

  @Override
  public int compareTo(@NotNull SentenceRange o) {
    return Integer.compare(this.fromPos, o.fromPos);
  }
}
