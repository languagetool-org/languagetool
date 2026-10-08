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
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * A sentence that has been tokenized and analyzed.
 * 
 * @author Daniel Naber
 */
public final class AnalyzedSentence {

  // objects of this type are cached, so everything needs to be immutable
  private final AnalyzedTokenReadings[] tokens;
  private final AnalyzedTokenReadings[] preDisambigTokens;
  private final AnalyzedTokenReadings[] nonBlankTokens;
  private final AnalyzedTokenReadings[] nonBlankPreDisambigTokens;
  private final int[] whPositions;  // maps positions without whitespace to positions that include whitespaces
  private final Map<String, List<Integer>> tokenOffsets;
  private final Map<String, List<Integer>> lemmaOffsets;
  private final Set<String> posTagSet;

  /**
   * Creates an AnalyzedSentence from the given {@link AnalyzedTokenReadings}. Whitespace is also a token.
   */
  public AnalyzedSentence(AnalyzedTokenReadings[] tokens) {
    this(tokens, tokens);
  }
  
  public AnalyzedSentence(AnalyzedTokenReadings[] tokens, AnalyzedTokenReadings[] preDisambigTokens) {
    this(tokens, preDisambigTokens, null, false);
  }

  /**
   * Like {@link #AnalyzedSentence(AnalyzedTokenReadings[], AnalyzedTokenReadings[])}, but reuses the token/lemma/POS
   * indexes of {@code previous} where that's known to be safe, to avoid re-indexing the whole sentence each time a
   * disambiguation rule modifies a few tokens.
   * @param previous the sentence the given tokens were derived from, or {@code null}
   * @param readingsUnchanged {@code true} if the caller guarantees the readings (lemmas, POS tags) of all tokens are the
   *                          same as in {@code previous} (e.g. only immunization or ignore-spelling flags were set)
   */
  @ApiStatus.Internal
  public AnalyzedSentence(AnalyzedTokenReadings[] tokens, AnalyzedTokenReadings[] preDisambigTokens,
                          @Nullable AnalyzedSentence previous, boolean readingsUnchanged) {
    this.tokens = tokens;
    this.preDisambigTokens = preDisambigTokens;
    int whCounter = 0;
    int nonWhCounter = 0;
    int[] mapping = new int[tokens.length + 1];
    this.whPositions = mapping;
    this.nonBlankTokens = getNonBlankReadings(tokens, whCounter, nonWhCounter, mapping).toArray(new AnalyzedTokenReadings[0]);
    this.nonBlankPreDisambigTokens = getNonBlankReadings(preDisambigTokens, whCounter, nonWhCounter, mapping).toArray(new AnalyzedTokenReadings[0]);
    boolean sameTokens = previous != null && haveSameTokenStrings(previous.nonBlankTokens, nonBlankTokens);
    tokenOffsets = sameTokens ? previous.tokenOffsets : indexTokens(nonBlankTokens);
    if (sameTokens && readingsUnchanged) {
      lemmaOffsets = previous.lemmaOffsets;
      posTagSet = previous.posTagSet;
    } else {
      lemmaOffsets = indexLemmas(nonBlankTokens);
      posTagSet = indexPosTags(nonBlankTokens);
    }
  }

  private static boolean haveSameTokenStrings(AnalyzedTokenReadings[] a, AnalyzedTokenReadings[] b) {
    if (a.length != b.length) {
      return false;
    }
    for (int i = 0; i < a.length; i++) {
      if (!a[i].getToken().equals(b[i].getToken())) {
        return false;
      }
    }
    return true;
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

  private AnalyzedSentence(AnalyzedTokenReadings[] tokens, AnalyzedSentence source) {
    this.tokens = tokens;
    this.preDisambigTokens = tokens;
    this.whPositions = source.whPositions;
    // same non-blank tokens as the source, so the (unmodifiable) indexes would be identical: share them
    this.nonBlankTokens = source.nonBlankTokens.clone();
    this.nonBlankPreDisambigTokens = source.nonBlankPreDisambigTokens.clone();
    tokenOffsets = source.tokenOffsets;
    lemmaOffsets = source.lemmaOffsets;
    posTagSet = source.posTagSet;
  }

  private static Map<String, List<Integer>> indexTokens(AnalyzedTokenReadings[] tokens) {
    Map<String, List<Integer>> result = new HashMap<>(tokens.length);
    for (int i = 0; i < tokens.length; i++) {
      result.computeIfAbsent(tokens[i].getTokenLowerCase(), __ -> new ArrayList<>(1)).add(i);
    }
    return makeUnmodifiable(result);
  }

  private static Map<String, List<Integer>> indexLemmas(AnalyzedTokenReadings[] tokens) {
    Map<String, List<Integer>> result = new HashMap<>(tokens.length);
    for (int i = 0; i < tokens.length; i++) {
      AnalyzedTokenReadings tr = tokens[i];
      int readingsLength = tr.getReadingsLength();
      for (int j = 0; j < readingsLength; j++) {
        AnalyzedToken token = tr.getAnalyzedToken(j);
        String key = token.getLemmaOrTokenLowerCase();
        List<Integer> list = result.computeIfAbsent(key, __ -> new ArrayList<>(1));
        if (list.isEmpty() || list.get(list.size() - 1) != i) {
          list.add(i);
        }
      }
    }
    return makeUnmodifiable(result);
  }

  private static Set<String> indexPosTags(AnalyzedTokenReadings[] tokens) {
    Set<String> result = new HashSet<>();
    for (AnalyzedTokenReadings tokenReadings : tokens) {
      for (AnalyzedToken token : tokenReadings) {
        String posTag = token.getPOSTag();
        if (posTag != null) {
          result.add(posTag);
        }
      }
    }
    return result.isEmpty() ? Collections.emptySet() : Collections.unmodifiableSet(result);
  }

  private static Map<String, List<Integer>> makeUnmodifiable(Map<String, List<Integer>> result) {
    for (Map.Entry<String, List<Integer>> entry : result.entrySet()) {
      entry.setValue(Collections.unmodifiableList(entry.getValue()));
    }
    return Collections.unmodifiableMap(result);
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
      copyTokens[i] = new AnalyzedTokenReadings(analyzedTokens, analyzedTokens.getReadings(), "");
    }
    return new AnalyzedSentence(copyTokens, sentence);
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
   * Get the length of the array returned by {@link #getTokensWithoutWhitespace()} without additional allocations.
   */
  @ApiStatus.Internal
  public int getNonWhitespaceTokenCount() {
    return nonBlankTokens.length;
  }

  /**
   * @since 4.5
   */
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

  private volatile String text;

  /**
   * Return the original text.
   * @since 2.7
   */
  public String getText() {
    String result = text;
    if (result == null) {
      text = result = calcText();
    }
    return result;
  }

  private String calcText() {
    StringBuilder sb = new StringBuilder();
    for (AnalyzedTokenReadings element : tokens) {
      sb.append(element.getToken());
    }
    return sb.toString();
  }

  /** Text length taking position fixes (for removed soft hyphens etc.) into account, so
   * this is _not_ always equal to {@code getText()}.
   * @since 5.1
   */
  public int getCorrectedTextLength() {
    int len = 0;
    for (int i = 0; i < tokens.length; i++) {
      AnalyzedTokenReadings element = tokens[i];
      len += element.getCleanToken().length();
      if (i == tokens.length - 1) {  // only apply at end, so the position fix at every token doesn't add up
        len += element.getPosFix();
      }
    }
    return len;
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
    return tokenOffsets.keySet();
  }

  /**
   * Get the lowercase lemmas of this sentence in a set.
   * Used internally for performance optimization.
   * @since 2.5
   */
  public Set<String> getLemmaSet() {
    return lemmaOffsets.keySet();
  }

  /**
   * Get all POS tags occurring in this sentence.
   * Used internally for performance optimization.
   * @since 6.9
   */
  @ApiStatus.Internal
  public Set<String> getPosTagSet() {
    return posTagSet;
  }

  /**
   * @return all offsets in {@link #getTokensWithoutWhitespace()} where tokens with the given text occur (case-insensitive),
   * or {@code null} if there are no such occurrences
   * @since 5.3
   */
  @Nullable
  @ApiStatus.Internal
  public List<Integer> getTokenOffsets(String token) {
    return tokenOffsets.get(token);
  }

  /**
   * @return all offsets in {@link #getTokensWithoutWhitespace()} where tokens with the given lemma occur (case-insensitive),
   * or {@code null} if there are no such occurrences
   * @since 5.3
   */
  @Nullable
  @ApiStatus.Internal
  public List<Integer> getLemmaOffsets(String token) {
    return lemmaOffsets.get(token);
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

}
