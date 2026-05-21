/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Jaume Ortolà
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
package org.languagetool.tools;

import java.util.List;
import java.util.ArrayList;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.AbstractDelta;
import com.github.difflib.patch.DeltaType;
import com.github.difflib.text.DiffRowGenerator;

/**
 * Converts the diffs between two texts into PseudoMatch objects
 */
public class DiffsAsMatches {

  private static final int MAX_CONTIGUOUS_DISTANCE = 3;
  private static final int INSERT_LOOKBACK_MAX = 2;

  private boolean filterOutApostropheDiffs = true;
  private boolean joinContiguousMatches = true;

  /**
   * Generate a list of PseudoMatches comparing original and revised text.
   *
   * @param original Original text
   * @param revised Revised text
   * @return List of PseudoMatches that represent the diffs
   */
  public List<PseudoMatch> getPseudoMatches(String original, String revised) {
    List<String> originalTokens = DiffRowGenerator.SPLITTER_BY_WORD.apply(original);
    List<String> revisedTokens = DiffRowGenerator.SPLITTER_BY_WORD.apply(revised);
    List<AbstractDelta<String>> deltas = DiffUtils.diff(originalTokens, revisedTokens, DiffRowGenerator.DEFAULT_EQUALIZER).getDeltas();
    List<PseudoMatch> matches = processDeltasIntoMatches(deltas, originalTokens, original);
    if (filterOutApostropheDiffs) {
      matches = filterOutApostropheDiffs(matches, original);
    }
    if (joinContiguousMatches) {
      matches = joinContiguousMatches(matches, original);
    }
    return matches;
  }

  /**
   * Process all deltas and convert them into PseudoMatches.
   */
  private List<PseudoMatch> processDeltasIntoMatches(List<AbstractDelta<String>> deltas,
                                                     List<String> originalTokens,
                                                     String originalText) {
    List<PseudoMatch> matches = new ArrayList<>();
    PseudoMatch lastMatch = null;
    AbstractDelta<String> lastDelta = null;

    for (AbstractDelta<String> delta : deltas) {
      // Get the baseline replacement
      String replacement = String.join("", delta.getTarget().getLines());

      // Compute initial position and adjustment for INSERTs (INSERTs need to underline the previous word)
      int errorIndex = delta.getSource().getPosition();
      int indexCorrection = getIndexCorrectionForInserts(delta, errorIndex);
      int fromPos = getPositionFromTokenIndex(originalTokens, errorIndex - indexCorrection);

      // Information about previous token
      boolean wasPreviousTokenWhitespace = isPreviousTokenWhitespace(originalTokens, errorIndex);
      String lastPunctuationStr = getPreviousPunctuationIfAny(originalTokens, errorIndex);

      // Compute underlined text and final position
      String underlinedError = String.join("", delta.getSource().getLines());
      int toPos = fromPos + underlinedError.length();

      // Add prefix for INSERTs
      String prefixReplacement = buildPrefixForInsert(originalTokens, errorIndex, indexCorrection);
      toPos += prefixReplacement.length();
      replacement = prefixReplacement + replacement;

      // Get text from original
      underlinedError = originalText.substring(fromPos, toPos);

      // Remove leading white spaces
      while (underlinedError.length() > 0 && replacement.length() > 0
        && StringTools.isWhitespace(underlinedError.substring(0, 1))
        && StringTools.isWhitespace(replacement.substring(0, 1))) {
        fromPos++;
        underlinedError = underlinedError.substring(1);
        replacement = replacement.substring(1);
      }

      // Special case: INSERT at the sentence start
      if (fromPos == 0 && toPos == 0) {
        toPos = originalTokens.get(0).length();
        replacement = replacement + originalTokens.get(0);
      }

      // Remove trailing whitespace in INSERTs
      while (underlinedError.length() > 0 && replacement.length() > 0
        && StringTools.isWhitespace(underlinedError.substring(underlinedError.length() - 1))
        && StringTools.isWhitespace(replacement.substring(replacement.length() - 1))) {
        toPos--;
        underlinedError = underlinedError.substring(0, underlinedError.length() - 1);
        replacement = replacement.substring(0, replacement.length() - 1);
      }

      // Check whether a merge with previous delta is needed
      PseudoMatch match;
      if (shouldMergeChangeWithInsert(delta, lastDelta, lastMatch, wasPreviousTokenWhitespace, lastPunctuationStr)) {
        // Merge CHANGE + INSERT: esrealitza -> es realitza (1 match)
        String newReplacement = lastMatch.getReplacements().get(0) +
          lastPunctuationStr +
          replacement.substring(toPos - fromPos);
        match = new PseudoMatch(newReplacement, lastMatch.getFromPos(), toPos);
        matches.remove(matches.size() - 1);
      } else if (shouldMergeWithDelete(delta, lastMatch, fromPos, wasPreviousTokenWhitespace)) {
        // Merge CHANGE + DELETE: Que el -> El (1 match)
        String newReplacement = lastMatch.getReplacements().get(0);
        match = new PseudoMatch(newReplacement, lastMatch.getFromPos(), toPos - 1);
        matches.remove(matches.size() - 1);
      } else {
        // independent match
        match = new PseudoMatch(replacement, fromPos, toPos);
      }
      matches.add(match);
      lastMatch = match;
      lastDelta = delta;
    }
    return matches;
  }

  /**
   * Compute index adjustment for INSERTs (lookback tokens):
   * two tokens before (INSERT_LOOKBACK_MAX = 2): 1 whitespace + 1 previous words
   */
  private int getIndexCorrectionForInserts(AbstractDelta<String> delta, int errorIndex) {
    int correction = 0;
    if (delta.getType() == DeltaType.INSERT) {
      correction = INSERT_LOOKBACK_MAX;
      while (errorIndex - correction < 0 && correction > 0) {
        correction--;
      }
    }
    return correction;
  }

  private boolean isPreviousTokenWhitespace(List<String> tokens, int errorIndex) {
    if (errorIndex - 1 >= 0 && errorIndex - 1 < tokens.size()) {
      return StringTools.isWhitespace(tokens.get(errorIndex - 1));
    }
    return false;
  }

  private String getPreviousPunctuationIfAny(List<String> tokens, int errorIndex) {
    if (errorIndex - 1 >= 0 && errorIndex - 1 < tokens.size()) {
      String token = tokens.get(errorIndex - 1);
      if (StringTools.isPunctuationMark(token)) {
        return token;
      }
    }
    return "";
  }

  private String buildPrefixForInsert(List<String> tokens, int errorIndex, int indexCorrection) {
    StringBuilder prefix = new StringBuilder();
    for (int i = errorIndex - indexCorrection; i < errorIndex; i++) {
      prefix.append(tokens.get(i));
    }
    return prefix.toString();
  }

  private int getPositionFromTokenIndex(List<String> tokens, int tokenIndex) {
    int position = 0;
    for (int i = 0; i < tokenIndex; i++) {
      position += tokens.get(i).length();
    }
    return position;
  }

  private boolean shouldMergeChangeWithInsert(AbstractDelta<String> currentDelta,
                                              AbstractDelta<String> previousDelta,
                                              PseudoMatch previousMatch,
                                              boolean wasPreviousWhitespace,
                                              String lastPunctuationStr) {
    if (previousMatch == null || previousDelta == null) {
      return false;
    }
    if (previousDelta.getType() != DeltaType.CHANGE || currentDelta.getType() != DeltaType.INSERT) {
      return false;
    }
    if (!wasPreviousWhitespace && lastPunctuationStr.isEmpty()) {
      return false;
    }
    int currentPosition = currentDelta.getSource().getPosition();
    int previousEndPosition = previousDelta.getSource().getPosition() +
      previousDelta.getSource().getLines().size();
    return currentPosition - 1 == previousEndPosition;
  }

  private boolean shouldMergeWithDelete(AbstractDelta<String> currentDelta,
                                        PseudoMatch previousMatch,
                                        int currentFromPos,
                                        boolean wasPreviousWhitespace) {
    if (previousMatch == null) {
      return false;
    }
    if (currentDelta.getType() != DeltaType.DELETE) {
      return false;
    }
    if (!wasPreviousWhitespace) {
      return false;
    }
    return previousMatch.getToPos() + 1 == currentFromPos;
  }

  /**
   * Join all matches inside a specific segment
   */
  public PseudoMatch getJoinedMatch(List<PseudoMatch> pseudoMatches, String originalSentence,
                                    int patternFrom, int patternTo) {
    if (pseudoMatches.isEmpty()) {
      return null;
    }
    int minFrom = -1;
    int maxTo = -1;
    int previousTo = -1;
    StringBuilder suggestion = new StringBuilder();
    for (PseudoMatch match : pseudoMatches) {
      if ((minFrom != -1 && maxTo == -1) || isMatchInRange(match, patternFrom, patternTo)) {
        if (minFrom == -1) {
          minFrom = match.getFromPos();
        } else if (match.getFromPos() > previousTo) {
          // add text between matches
          suggestion.append(originalSentence.substring(previousTo, match.getFromPos()));
        }
        suggestion.append(match.getReplacements().get(0));
        maxTo = match.getToPos();
      }
      previousTo = match.getToPos();
    }
    if (minFrom > -1 && maxTo > 0) {
      return new PseudoMatch(suggestion.toString(), minFrom, maxTo);
    }
    return null;
  }

  private boolean isMatchInRange(PseudoMatch match, int patternFrom, int patternTo) {
    return (match.getFromPos() >= patternFrom && match.getFromPos() <= patternTo)
      || (match.getToPos() >= patternFrom && match.getToPos() <= patternTo);
  }

  private List<PseudoMatch> filterOutApostropheDiffs(List<PseudoMatch> pseudoMatches, String original) {
    List<PseudoMatch> results = new ArrayList<>();

    for (PseudoMatch match : pseudoMatches) {
      String originalPart = original.substring(match.getFromPos(), match.getToPos());
      String normalizedOriginal = normalizeApostrophes(originalPart);
      String normalizedReplacement = normalizeApostrophes(match.getReplacement());

      if (!normalizedOriginal.equals(normalizedReplacement)) {
        results.add(match);
      }
    }
    return results;
  }

  private String normalizeApostrophes(String text) {
    return text.replace("’", "'");
  }

  /**
   * Join contiguous matches (less than 3 chars of separation).
   */
  private List<PseudoMatch> joinContiguousMatches(List<PseudoMatch> pseudoMatches, String original) {
    List<PseudoMatch> results = new ArrayList<>();
    int previousEndPosition = -1;
    for (PseudoMatch match : pseudoMatches) {
      if (previousEndPosition > -1 && match.getFromPos() - previousEndPosition < MAX_CONTIGUOUS_DISTANCE) {
        PseudoMatch joinedMatch = joinWithPreviousMatch(match, results.get(results.size() - 1), original);
        results.set(results.size() - 1, joinedMatch);
      } else {
        results.add(match);
      }
      previousEndPosition = match.getToPos();
    }
    return results;
  }

  private PseudoMatch joinWithPreviousMatch(PseudoMatch currentMatch, PseudoMatch previousMatch,
                                            String original) {
    StringBuilder replacementBuilder = new StringBuilder(previousMatch.getReplacement());
    //text between matches
    if (previousMatch.getToPos() < currentMatch.getFromPos()) {
      replacementBuilder.append(original.substring(previousMatch.getToPos(), currentMatch.getFromPos()));
    }
    replacementBuilder.append(currentMatch.getReplacement());
    return new PseudoMatch(replacementBuilder.toString(),
      previousMatch.getFromPos(),
      currentMatch.getToPos());
  }

}