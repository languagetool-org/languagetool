/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Jaume Ortolà
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
package org.languagetool.tagging.disambiguation.ca;

import org.jetbrains.annotations.Nullable;
import org.languagetool.*;
import org.languagetool.rules.ca.CatalanMorfologikMultitokenSpeller;
import org.languagetool.rules.spelling.morfologik.MorfologikSpeller;
import org.languagetool.tagging.disambiguation.AbstractDisambiguator;

import java.io.IOException;

public class CatalanMultitokenDisambiguator extends AbstractDisambiguator {

  private static MorfologikSpeller speller;
  private static final int WINDOW_FORWARD = 10;
  private static final int WINDOW_BACKWARD = 4;

  public CatalanMultitokenDisambiguator() {
    this.speller = CatalanMorfologikMultitokenSpeller.getSpeller();
  }

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException {
    return disambiguate(input, null);
  }

  @Override
  public AnalyzedSentence disambiguate(AnalyzedSentence input,
                                       @Nullable JLanguageTool.CheckCancelledCallback checkCanceled) throws IOException {
    if (speller == null) {
      return input;
    }
    AnalyzedTokenReadings[] anTokens = input.getTokens();
    for (int i = 1; i < anTokens.length; i++) {
      if (!anTokens[i].isWhitespace() && !anTokens[i].isTagged() && !anTokens[i].isIgnoredBySpeller()) {
        boolean found = false;
        // Forward
        if (Character.isUpperCase(anTokens[i].getToken().charAt(0))) {
          int fromFwd = i;
          int toFwd = Math.min(i + WINDOW_FORWARD, anTokens.length - 1);
          found = searchInDictAndTag(anTokens, fromFwd, toFwd, true);
        }
        // Backward
        if (!found) {
          int fromBwd = Math.max(1, i - WINDOW_BACKWARD);
          int toBwd = i;
          searchInDictAndTag(anTokens, fromBwd, toBwd, false);
        }
      }
    }
    return new AnalyzedSentence(anTokens);
  }

  private boolean searchInDictAndTag(AnalyzedTokenReadings[] tokens, int from, int to, boolean shrinkFromEnd) {
    int currentFrom = from;
    int currentTo = to;
    while (currentTo > currentFrom) {
      String textToCheck = getTextFromTo(tokens, currentFrom, currentTo);
      if (!textToCheck.endsWith(" ") && !textToCheck.isEmpty() && !speller.isMisspelled(textToCheck)) {
        for (int j = currentFrom; j <= currentTo; j++) {
          if (!tokens[j].isWhitespace()) {
            tokens[j].addReading(
              new AnalyzedToken(tokens[j].getToken(), "NPCNM00", textToCheck),"HybridDisamb");
            tokens[j].isPosTagUnknown();
          }
        }
        return true;
      }
      if (shrinkFromEnd) {
        currentTo--;
      } else {
        currentFrom++;
      }
    }
    return false;
  }

  private String getTextFromTo(AnalyzedTokenReadings[] anTokens, int indexFrom, int indexTo) {
    StringBuilder sb = new StringBuilder();
    for (int i = indexFrom; i <= indexTo; i++) {
      if (i > anTokens.length - 1) {
       return "";
      }
      sb.append(anTokens[i].getToken());
    }
    return sb.toString();
  }

}

