/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.wikipedia;

import org.languagetool.rules.RuleMatch;
import xtc.tree.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies the rule suggestions to a text, considering a mapping so the
 * suggestion can be applied to the original markup of a text.
 */
public class SuggestionReplacer {

  private final PlainTextMapping textMapping;
  private final String originalText;

  /**
   * @param originalText the original text that includes markup
   */
  public SuggestionReplacer(PlainTextMapping textMapping, String originalText) {
    this.textMapping = textMapping;
    this.originalText = originalText;
  }

  public List<String> applySuggestionsToOriginalText(RuleMatch match) {
    final List<String> replacements = match.getSuggestedReplacements();
    final List<String> newTexts = new ArrayList<String>();
    final Location fromPosLocation = textMapping.getOriginalTextPositionFor(match.getFromPos() + 1);  // not zero-based!
    final Location toPosLocation = textMapping.getOriginalTextPositionFor(match.getToPos() + 1);

    final int fromPos = LocationHelper.absolutePositionFor(fromPosLocation, originalText);
    final int toPos = LocationHelper.absolutePositionFor(toPosLocation, originalText);
    final int contextSize = 5;  // that's a guessed value so we don't have to be 100% exact in our mapping
    for (String replacement : replacements) {
      final String errorText = textMapping.getPlainText().substring(match.getFromPos(), match.getToPos());
      // it's too dangerous to replace the error text everywhere, so only replace is at the error position:
      final int contextFrom = Math.max(fromPos - contextSize, 0);
      final int contextTo = Math.min(toPos + contextSize, originalText.length());
      final String newText = originalText.substring(0, contextFrom)
              // we do a simple string replacement as that works even if our mapping if off a bit:
              + originalText.substring(contextFrom, contextTo).replace(errorText, replacement)
              + originalText.substring(contextTo);
      newTexts.add(newText);
    }
    return newTexts;
  }

}
