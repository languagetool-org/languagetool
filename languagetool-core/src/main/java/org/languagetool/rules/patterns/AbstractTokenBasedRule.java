/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;

import java.util.*;

/**
 * A base class for {@link PatternToken}-based rules.
 * It's public for implementation reasons and should not be used outside LanguageTool.
 */
@ApiStatus.Internal
public abstract class AbstractTokenBasedRule extends AbstractPatternRule {
  // Tokens used for fast checking whether a rule can ever match.
  @Nullable
  final TokenHint[] tokenHints;

  // Token/lemma hints together with offset of a matching token from any match start
  @Nullable
  final TokenHint anchorHint;

  protected AbstractTokenBasedRule(String id, String description, Language language, List<PatternToken> patternTokens, boolean getUnified) {
    super(id, description, language, patternTokens, getUnified);

    Set<TokenHint> tokenHints = new HashSet<>();
    TokenHint anchorHint = null;

    boolean fixedOffset = true;
    for (int i = 0; i < patternTokens.size(); i++) {
      PatternToken token = patternTokens.get(i);

      boolean inflected = false;
      Set<String> hints = token.calcFormHints();
      if (hints == null) {
        inflected = true;
        hints = token.calcLemmaHints();
      }
      if (hints != null) {
        TokenHint hint = new TokenHint(inflected, hints, i);
        tokenHints.add(hint);
        if (fixedOffset && anchorHint == null) {
          anchorHint = hint;
        }
      }

      if (fixedOffset && (token.getMinOccurrence() != 1 || token.getSkipNext() != 0 || token.getMaxOccurrence() != 1)) {
        fixedOffset = false;
      }
    }

    this.tokenHints = tokenHints.isEmpty() ? null : tokenHints.stream()
      .sorted(Comparator
        .comparing((TokenHint th) -> th.lowerCaseValues.length)
        .thenComparing(th -> -Arrays.stream(th.lowerCaseValues).mapToInt(String::length).min().orElse(0))
      ).toArray(TokenHint[]::new);
    this.anchorHint = anchorHint;
  }

  /**
   * A fast check whether this rule can be ignored for the given sentence
   * because it can never match. Used for performance optimization.
   */
  @ApiStatus.Internal
  public boolean canBeIgnoredFor(AnalyzedSentence sentence) {
    if (tokenHints == null) return false;
    for (TokenHint th : tokenHints) {
      if (th.canBeIgnoredFor(sentence)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Represents possible values of a {@link PatternToken}'s lemma or text.
   */
  static class TokenHint {
    final boolean inflected;
    final String[] lowerCaseValues;
    final int tokenIndex;

    private TokenHint(boolean inflected, Set<String> possibleValues, int tokenIndex) {
      this.inflected = inflected;
      this.tokenIndex = tokenIndex;
      lowerCaseValues = possibleValues.stream().map(String::toLowerCase).distinct().toArray(String[]::new);
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof TokenHint)) return false;
      TokenHint tokenHint = (TokenHint) o;
      return inflected == tokenHint.inflected &&
             tokenIndex == tokenHint.tokenIndex &&
             Arrays.equals(lowerCaseValues, tokenHint.lowerCaseValues);
    }

    @Override
    public int hashCode() {
      return Objects.hash(inflected, tokenIndex, Arrays.hashCode(lowerCaseValues));
    }

    /**
     * @return all indices inside sentence's non-blank tokens where this token could possibly match
     */
    List<Integer> getPossibleIndices(AnalyzedSentence sentence) {
      boolean needMerge = false;
      List<Integer> result = null;
      for (String hint : lowerCaseValues) {
        List<Integer> hintIndices = getHintIndices(sentence, hint);
        if (hintIndices != null) {
          if (result == null) {
            result = hintIndices;
          } else {
            if (!needMerge) {
              result = new ArrayList<>(result);
              needMerge = true;
            }
            result.addAll(hintIndices);
          }
        }
      }
      if (result == null) return Collections.emptyList();
      return needMerge ? new ArrayList<>(new TreeSet<>(result)) : result;
    }

    private boolean canBeIgnoredFor(AnalyzedSentence sentence) {
      for (String hint : lowerCaseValues) {
        if (getHintIndices(sentence, hint) != null) {
          return false;
        }
      }
      return true;
    }

    @Nullable
    private List<Integer> getHintIndices(AnalyzedSentence sentence, String hint) {
      return inflected ? sentence.getLemmaOffsets(hint) : sentence.getTokenOffsets(hint);
    }
  }

}
