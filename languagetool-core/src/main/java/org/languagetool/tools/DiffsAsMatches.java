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
    return matches;
  }

}
