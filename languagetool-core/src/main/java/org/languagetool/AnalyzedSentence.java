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

import org.apache.commons.lang.StringUtils;

import java.util.*;

/**
 * A sentence that has been tokenized and analyzed.
 * 
 * @author Daniel Naber
 */
public final class AnalyzedSentence {

  private final AnalyzedTokenReadings[] tokens;
  private final AnalyzedTokenReadings[] nonBlankTokens;
  private final int[] whPositions;
  private final Set<String> tokenSet;
  private final Set<String> lemmaSet;

  /**
   * Creates an AnalyzedSentence from the given {@link AnalyzedTokenReadings}. Whitespace is also a token.
   */
  public AnalyzedSentence(AnalyzedTokenReadings[] tokens) {
    this.tokens = tokens;
    int whCounter = 0;
    int nonWhCounter = 0;
    final int[] mapping = new int[tokens.length + 1];
    final List<AnalyzedTokenReadings> l = new ArrayList<>();
    for (final AnalyzedTokenReadings token : tokens) {
      if (!token.isWhitespace() || token.isSentenceStart() || token.isSentenceEnd() || token.isParagraphEnd()) {
        l.add(token);
        mapping[nonWhCounter] = whCounter;
        nonWhCounter++;
      }
      whCounter++;
    }
    this.whPositions = mapping;
    this.nonBlankTokens = l.toArray(new AnalyzedTokenReadings[l.size()]);
    this.tokenSet = getTokenSet(tokens);
    this.lemmaSet = getLemmaSet(tokens);
  }

  private AnalyzedSentence(AnalyzedTokenReadings[] tokens, int[] mapping, AnalyzedTokenReadings[] nonBlankTokens) {
    this.tokens = tokens;
    this.whPositions = mapping;
    this.nonBlankTokens = nonBlankTokens;
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
   * The method copies {@link org.languagetool.AnalyzedSentence} and returns the copy.
   * Useful for performing local immunization (for example).
   *
   * @param sentence {@link org.languagetool.AnalyzedSentence} to be copied
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
    return new AnalyzedSentence(copyTokens, sentence.whPositions, sentence.getTokensWithoutWhitespace());
  }

  /**
   * Returns the {@link AnalyzedTokenReadings} of the analyzed text. Whitespace
   * is also a token.
   */
  public final AnalyzedTokenReadings[] getTokens() {
    return tokens;
  }

  /**
   * Returns the {@link AnalyzedTokenReadings} of the analyzed text, with
   * whitespace tokens removed but with the artificial <code>SENT_START</code>
   * token included.
   */
  public final AnalyzedTokenReadings[] getTokensWithoutWhitespace() {
    return nonBlankTokens.clone();
  }

  /**
   * Get a position of a non-whitespace token in the original sentence with
   * whitespace.
   *
   * @param nonWhPosition position of a non-whitespace token
   * @return position in the original sentence.
   */
  public final int getOriginalPosition(final int nonWhPosition) {
    return whPositions[nonWhPosition];
  }

  @Override
  public final String toString() {
    return toString(",");
  }

  /**
   * Return string representation without chunk information.
   * @since 2.3
   */
  public final String toShortString(String readingDelimiter) {
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
  final String toTextString() {
    return getText();
  }

  /**
   * Return string representation with chunk information.
   */
  public final String toString(String readingDelimiter) {
    return toString(readingDelimiter, true);
  }

  private String toString(String readingDelimiter, boolean includeChunks) {
    final StringBuilder sb = new StringBuilder();
    for (final AnalyzedTokenReadings element : tokens) {
      if (!element.isWhitespace()) {
        sb.append(element.getToken());
        sb.append('[');
      }
      Iterator<AnalyzedToken> iterator = element.iterator();
      while (iterator.hasNext()) {
        final AnalyzedToken token = iterator.next();
        final String posTag = token.getPOSTag();
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
            sb.append(token.toString());
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
  public final String getAnnotations() {
    final StringBuilder sb = new StringBuilder(40);
    sb.append("Disambiguator log: \n");
    for (final AnalyzedTokenReadings element : tokens) {
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
  public synchronized boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final AnalyzedSentence other = (AnalyzedSentence) obj;
    if (!Arrays.equals(nonBlankTokens, other.nonBlankTokens))
      return false;
    if (!Arrays.equals(tokens, other.tokens))
      return false;
    if (!Arrays.equals(whPositions, other.whPositions))
      return false;
    if (tokenSet != null && !tokenSet.equals(other.tokenSet))
      return false;
    if (lemmaSet != null && !lemmaSet.equals(other.lemmaSet))
      return false;
    return true;
  }

  @Override
  public synchronized int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(nonBlankTokens);
    result = prime * result + Arrays.hashCode(tokens);
    result = prime * result + Arrays.hashCode(whPositions);
    result = prime * result + tokenSet.hashCode();
    result = prime * result + lemmaSet.hashCode();
    return result;
  }

}
