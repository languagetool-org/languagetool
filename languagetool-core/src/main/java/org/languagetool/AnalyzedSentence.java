/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber, Marcin Miłkowski (http://www.languagetool.org)
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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.tools.StringTools;

import java.util.*;

/**
 * A sentence that has been tokenized and analyzed.
 * 
 * @author Daniel Naber
 */
public final class AnalyzedSentence {

  private final AnalyzedTokenReadings[] tokens;
  private final AnalyzedTokenReadings[] preDisambigTokens;
  private final AnalyzedTokenReadings[] nonBlankTokens;
  private final AnalyzedTokenReadings[] nonBlankPreDisambigTokens;
  private final int[] whPositions;  // maps positions without whitespace to positions that include whitespaces
  private final Set<String> tokenSet;
  private final Set<String> lemmaSet;

  /**
   * Creates an AnalyzedSentence from the given {@link AnalyzedTokenReadings}. Whitespace is also a token.
   */
  public AnalyzedSentence(AnalyzedTokenReadings[] tokens) {
    this(tokens, tokens);
  }
  
  public AnalyzedSentence(AnalyzedTokenReadings[] tokens, AnalyzedTokenReadings[] preDisambigTokens) {
    this.tokens = tokens;
    this.preDisambigTokens = preDisambigTokens;
    int whCounter = 0;
    int nonWhCounter = 0;
    int[] mapping = new int[tokens.length + 1];
    this.whPositions = mapping;
    this.nonBlankTokens = getNonBlankReadings(tokens, whCounter, nonWhCounter, mapping).toArray(new AnalyzedTokenReadings[0]);
    this.nonBlankPreDisambigTokens = getNonBlankReadings(preDisambigTokens, whCounter, nonWhCounter, mapping).toArray(new AnalyzedTokenReadings[0]);
    this.tokenSet = getTokenSet(tokens);
    this.lemmaSet = getLemmaSet(tokens);
  }

  @NotNull
  private List<AnalyzedTokenReadings> getNonBlankReadings(AnalyzedTokenReadings[] tokens, int whCounter, int nonWhCounter, int[] mapping) {
    List<AnalyzedTokenReadings> l = new ArrayList<>();
    for (AnalyzedTokenReadings token : tokens) {
      if (!token.isWhitespace() || token.isSentenceStart() || token.isSentenceEnd() || token.isParagraphEnd()) {
        l.add(token);
        mapping[nonWhCounter] = whCounter;
        nonWhCounter++;
      }
      whCounter++;
    }
    return l;
  }

  private AnalyzedSentence(AnalyzedTokenReadings[] tokens, int[] mapping, AnalyzedTokenReadings[] nonBlankTokens, AnalyzedTokenReadings[] nonBlankPreDisambigTokens) {
    this.tokens = tokens;
    this.preDisambigTokens = tokens;
    this.whPositions = mapping;
    this.nonBlankTokens = nonBlankTokens;
    this.nonBlankPreDisambigTokens = nonBlankPreDisambigTokens;
    this.tokenSet = getTokenSet(tokens);
    this.lemmaSet = getLemmaSet(tokens);
  }

  private Set<String> getTokenSet(AnalyzedTokenReadings[] tokens) {
    Set<String> tokenSet = new HashSet<>();
    for (AnalyzedTokenReadings token : tokens) {
      tokenSet.add(token.getToken().toLowerCase());
    }
    return Collections.unmodifiableSet(tokenSet);
  }

  private Set<String> getLemmaSet(AnalyzedTokenReadings[] tokens) {
    Set<String> lemmaSet = new HashSet<>();
    for (AnalyzedTokenReadings token : tokens) {
      for (AnalyzedToken lemmaTok : token.getReadings()) {
        if (lemmaTok.getLemma() != null) {
          lemmaSet.add(lemmaTok.getLemma().toLowerCase());
        } else {
          lemmaSet.add(lemmaTok.getToken().toLowerCase());
        }
      }
    }
    return Collections.unmodifiableSet(lemmaSet);
  }

  /**
   * The method copies {@link AnalyzedSentence} and returns the copy.
   * Useful for performing local immunization (for example).
   *
   * @param sentence {@link AnalyzedSentence} to be copied
   * @return a new object which is a copy
   * @since  2.5
   */
  public AnalyzedSentence copy(AnalyzedSentence sentence) {
    AnalyzedTokenReadings[] copyTokens = new AnalyzedTokenReadings[sentence.getTokens().length];
    for (int i = 0; i < copyTokens.length; i++) {
      AnalyzedTokenReadings analyzedTokens = sentence.getTokens()[i];
      copyTokens[i] = new AnalyzedTokenReadings(analyzedTokens.getReadings(), analyzedTokens.getStartPos());
      copyTokens[i].setHistoricalAnnotations(analyzedTokens.getHistoricalAnnotations());
      copyTokens[i].setChunkTags(analyzedTokens.getChunkTags());
      if (analyzedTokens.isImmunized()) {
        copyTokens[i].immunize();
      }
      if (analyzedTokens.isIgnoredBySpeller()) {
        copyTokens[i].ignoreSpelling();
      }
      copyTokens[i].setWhitespaceBefore(sentence.getTokens()[i].isWhitespaceBefore());
    }
    return new AnalyzedSentence(copyTokens, sentence.whPositions, sentence.getTokensWithoutWhitespace(), sentence.getPreDisambigTokensWithoutWhitespace());
  }

  /**
   * Returns the {@link AnalyzedTokenReadings} of the analyzed text. Whitespace
   * is also a token.
   */
  public AnalyzedTokenReadings[] getTokens() {
    // It would be better to return a clone here to make this object immutable,
    // but this would be bad for performance:
    return tokens;
  }

  /**
   * @since 4.5
   */
  @Experimental
  public AnalyzedTokenReadings[] getPreDisambigTokens() {
    // It would be better to return a clone here to make this object immutable,
    // but this would be bad for performance:
    return preDisambigTokens;
  }

  /**
   * Returns the {@link AnalyzedTokenReadings} of the analyzed text, with
   * whitespace tokens removed but with the artificial <code>SENT_START</code>
   * token included.
   */
  public AnalyzedTokenReadings[] getTokensWithoutWhitespace() {
    return nonBlankTokens.clone();
  }

  /**
   * @since 4.5
   */
  @Experimental
  public AnalyzedTokenReadings[] getPreDisambigTokensWithoutWhitespace() {
    return nonBlankPreDisambigTokens.clone();
  }

  /**
   * Get a position of a non-whitespace token in the original sentence with
   * whitespace.
   *
   * @param nonWhPosition position of a non-whitespace token
   * @return position in the original sentence.
   */
  public int getOriginalPosition(int nonWhPosition) {
    return whPositions[nonWhPosition];
  }

  @Override
  public String toString() {
    return toString(",");
  }

  /**
   * Return string representation without chunk information.
   * @since 2.3
   */
  public String toShortString(String readingDelimiter) {
    return toString(readingDelimiter, false);
  }

  /**
   * Return the original text.
   * @since 2.7
   */
  public String getText() {
    StringBuilder sb = new StringBuilder();
    for (AnalyzedTokenReadings element : tokens) {
      sb.append(element.getToken());
    }
    return sb.toString();
  }

  /**
   * Return string representation without any analysis information, just the original text.
   * @since 2.6
   */
  String toTextString() {
    return getText();
  }

  /**
   * Return string representation with chunk information.
   */
  public String toString(String readingDelimiter) {
    return toString(readingDelimiter, true);
  }

  private String toString(String readingDelimiter, boolean includeChunks) {
    StringBuilder sb = new StringBuilder();
    for (AnalyzedTokenReadings element : tokens) {
      if (!element.isWhitespace()) {
        sb.append(element.getToken());
        sb.append('[');
      }
      Iterator<AnalyzedToken> iterator = element.iterator();
      while (iterator.hasNext()) {
        AnalyzedToken token = iterator.next();
        String posTag = token.getPOSTag();
        if (element.isSentenceStart()) {
          sb.append("<S>");
        } else if (JLanguageTool.SENTENCE_END_TAGNAME.equals(posTag)) {
          sb.append("</S>");
        } else if (JLanguageTool.PARAGRAPH_END_TAGNAME.equals(posTag)) {
          sb.append("<P/>");
        } else if (posTag == null && !includeChunks) {
          sb.append(token.getToken());
        } else {
          if (!element.isWhitespace()) {
            sb.append(token);
            if (iterator.hasNext()) {
              sb.append(readingDelimiter);
            }
          }
        }
      }
      if (!element.isWhitespace()) {
        if (includeChunks && element.getChunkTags().size() > 0) {
          sb.append(',');
          sb.append(StringUtils.join(element.getChunkTags(), "|"));
        }
        if (element.isImmunized()) {
          sb.append("{!}");
        }
        sb.append(']');
      } else {
        sb.append(' ');
      }

    }
    return sb.toString();
  }

  /**
   * Get disambiguator actions log.
   */
  public String getAnnotations() {
    StringBuilder sb = new StringBuilder(40);
    sb.append("Disambiguator log: \n");
    for (AnalyzedTokenReadings element : tokens) {
      if (!element.isWhitespace() &&
              !"".equals(element.getHistoricalAnnotations())) {
        sb.append(element.getHistoricalAnnotations());
        sb.append('\n');
      }
    }
    return sb.toString();
  }

  /**
   * Get the lowercase tokens of this sentence in a set.
   * Used internally for performance optimization.
   * @since 2.4
   */
  public Set<String> getTokenSet() {
    return tokenSet;
  }

  /**
   * Get the lowercase lemmas of this sentence in a set.
   * Used internally for performance optimization.
   * @since 2.5
   */
  public Set<String> getLemmaSet() {
    return lemmaSet;
  }

  @SuppressWarnings("ControlFlowStatementWithoutBraces")
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AnalyzedSentence other = (AnalyzedSentence) o;
    // tokenSet and lemmaSet are a subset of tokens and don't need to be included
    return Arrays.equals(nonBlankTokens, other.nonBlankTokens) 
        && Arrays.equals(tokens, other.tokens)
        && Arrays.equals(whPositions, other.whPositions);
  }

  @Override
  public int hashCode() {
    // tokenSet and lemmaSet are a subset of tokens and don't need to be included
    return Objects.hash(nonBlankTokens, tokens, whPositions);
  }

  /**
   * Returns true if sentences ends with a paragraph break.
   * @since 4.3
   */
  public boolean hasParagraphEndMark(Language lang) {
    return StringTools.isParagraphEnd(getText(), lang.getSentenceTokenizer().singleLineBreaksMarksPara());
  }

}
