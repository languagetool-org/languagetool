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

public class DiffsAsMatches {

  private boolean filterOutApostropheDiffs = true;
  private boolean joinContiguousMatches = true;

  public List<PseudoMatch> getPseudoMatches(String original, String revised) {
    List<PseudoMatch> matches = new ArrayList<>();
    List<String> origList = DiffRowGenerator.SPLITTER_BY_WORD.apply(original);
    List<String> revList = DiffRowGenerator.SPLITTER_BY_WORD.apply(revised);
    List<AbstractDelta<String>> inlineDeltas = DiffUtils.diff(origList, revList, DiffRowGenerator.DEFAULT_EQUALIZER)
        .getDeltas();
    PseudoMatch lastMatch = null;
    AbstractDelta<String> lastInlineDelta = null;
    for (AbstractDelta<String> inlineDelta : inlineDeltas) {
      String replacement = String.join("", inlineDelta.getTarget().getLines());
      int fromPos = 0;
      int errorIndex = inlineDelta.getSource().getPosition();
      int indexCorrection = 0; // in case of INSERT, underline the 2 previous tokens (including a whitespace)
      if (inlineDelta.getType() == DeltaType.INSERT) {
        indexCorrection = 2;
        if (errorIndex - indexCorrection < 0) {
          indexCorrection = 1;
        }
        if (errorIndex - indexCorrection < 0) {
          indexCorrection = 0;
        }
      }
      for (int i = 0; i < errorIndex - indexCorrection; i++) {
        fromPos += origList.get(i).length();
      }
      boolean wasLastWhitespace = false;
      String lastPunctuationStr = "";
      if (errorIndex - 1 < origList.size() && errorIndex - 1 > -1) {
        wasLastWhitespace = StringTools.isWhitespace(origList.get(errorIndex - 1));
        if (StringTools.isPunctuationMark(origList.get(errorIndex - 1))) {
          lastPunctuationStr = origList.get(errorIndex - 1);
        };
      }

      String underlinedError = String.join("", inlineDelta.getSource().getLines());
      int toPos = fromPos + underlinedError.length();

      String prefixReplacement = "";
      for (int i = errorIndex - indexCorrection; i < errorIndex; i++) {
        toPos += origList.get(i).length();
        prefixReplacement = prefixReplacement + origList.get(i);
      }
      replacement = prefixReplacement + replacement;
      underlinedError = original.substring(fromPos, toPos);
      while (underlinedError.length()>0 && replacement.length()>0
        && underlinedError.substring(0,1).equals(" ") && replacement.substring(0,1).equals(" ")) {
        fromPos++;
        underlinedError = underlinedError.substring(1);
        replacement = replacement.substring(1);
      }
      // INSERT at the sentence start
      if (fromPos == 0 && toPos == 0) {
        toPos = origList.get(0).length();
        replacement = replacement + origList.get(0);
      }
      // remove unnecessary whitespace at the end in INSERT
      if (inlineDelta.getType() == DeltaType.INSERT && replacement.endsWith(" ") && replacement.length() > 2
          && wasLastWhitespace) {
        replacement = replacement.substring(0, replacement.length() - 1);
        toPos--;
      }
      PseudoMatch match;
      // serealiza -> se realiza CHANGE + INSERT -> 1 match
      if (lastMatch != null && lastInlineDelta.getType() == DeltaType.CHANGE
          && inlineDelta.getType() == DeltaType.INSERT
          //&& origList.get(inlineDelta.getSource().getPosition() - 1).equals(" ")
          && (wasLastWhitespace || !lastPunctuationStr.isEmpty())
          && inlineDelta.getSource().getPosition() - 1 == lastInlineDelta.getSource().getPosition()
              + lastInlineDelta.getSource().getLines().size()) {
        String newReplacement = lastMatch.getReplacements().get(0) + lastPunctuationStr + replacement.substring(toPos - fromPos);
        match = new PseudoMatch(newReplacement, lastMatch.getFromPos(), toPos);
        matches.remove(matches.size() - 1);
        // CHANGE + DELETE
      } else if (lastMatch != null && inlineDelta.getType() == DeltaType.DELETE && wasLastWhitespace
          && lastMatch.getToPos() + 1 == fromPos) {
        String newReplacement = lastMatch.getReplacements().get(0);
        match = new PseudoMatch(newReplacement, lastMatch.getFromPos(), toPos - 1);
        matches.remove(matches.size() - 1);
      } else {
        match = new PseudoMatch(replacement, fromPos, toPos);
      }
      matches.add(match);
      lastMatch = match;
      lastInlineDelta = inlineDelta;

    }
    if (filterOutApostropheDiffs) {
      matches = fiterOutApostropheDiffs(matches, original);
    }
    if (joinContiguousMatches) {
      matches = joinContiguousMatches(matches, original);
    }
    return matches;
  }

  /*
   * Join all matches inside a segment
   */
  public PseudoMatch getJoinedMatch(List<PseudoMatch> pseudoMatches, String originalSentence, int patternFrom,
                                     int patternTo) {
    int minFrom = -1;
    int maxTo = -1;
    int previousTo = -1;
    StringBuilder suggestion = new StringBuilder();
    for (PseudoMatch pm : pseudoMatches) {
      if ((minFrom != -1) && (maxTo == -1)
        || (pm.getFromPos() >= patternFrom && pm.getToPos() <= patternTo)) {
        maxTo = pm.getToPos();
        if (minFrom == -1) {
          minFrom = pm.getFromPos();
        } else if (pm.getFromPos() > previousTo) {
          suggestion.append(originalSentence.substring(previousTo, pm.getFromPos()));
        }
        suggestion.append(pm.getReplacements().get(0));
      }
      previousTo = pm.getToPos();
    }
    if (minFrom > -1 && maxTo > 0) {
      return new PseudoMatch(suggestion.toString(), minFrom, maxTo);
    }
    return null;
  }

  private List<PseudoMatch> fiterOutApostropheDiffs(List<PseudoMatch> pseudoMatches, String original) {
    List<PseudoMatch> results = new ArrayList<>();
    for (PseudoMatch pm : pseudoMatches) {
      if (!original.substring(pm.getFromPos(), pm.getToPos()).replace("’", "'")
        .equals(pm.getReplacement().replace("’", "'"))) {
        results.add(pm);
      }
    }
    return results;
  }

  private List<PseudoMatch> joinContiguousMatches(List<PseudoMatch> pseudoMatches, String original) {
    List<PseudoMatch> results = new ArrayList<>();
    int previousToPos = -1;
    for (PseudoMatch pm : pseudoMatches) {
      if (previousToPos > -1 && pm.getFromPos() - previousToPos < 3) {
        PseudoMatch previousPm = results.get(results.size() - 1);
        StringBuilder replacementBuilder = new StringBuilder(previousPm.getReplacement());
        if (previousPm.getToPos() < pm.getFromPos()) {
          replacementBuilder.append(original.substring(previousPm.getToPos(), pm.getFromPos()));
        }
        replacementBuilder.append(pm.getReplacement());
        PseudoMatch newPseudoPmatch = new PseudoMatch(replacementBuilder.toString(), previousPm.getFromPos(),
          pm.getToPos());
        results.set(results.size() - 1, newPseudoPmatch);
      } else {
        results.add(pm);
      }
      previousToPos = pm.getToPos();
    }
    return results;
  }

}
