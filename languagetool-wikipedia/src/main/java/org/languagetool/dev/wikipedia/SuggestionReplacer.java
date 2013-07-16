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
  private final String errorMarkerStart;
  private final String errorMarkerEnd;

  /**
   * @param originalText the original text that includes markup
   */
  public SuggestionReplacer(PlainTextMapping textMapping, String originalText) {
    this.textMapping = textMapping;
    this.originalText = originalText;
    this.errorMarkerStart = "<span class=\"error\">";
    this.errorMarkerEnd = "</span>";
  }

  public SuggestionReplacer(PlainTextMapping textMapping, String originalText, String errorMarkerStart, String errorMarkerEnd) {
    this.textMapping = textMapping;
    this.originalText = originalText;
    this.errorMarkerStart = errorMarkerStart;
    this.errorMarkerEnd = errorMarkerEnd;
  }

  /**
   * Applies the suggestions from the rule to the original text. For rules that
   * have no suggestion, a pseudo-correction is generated that contains the same
   * text as before.
   */
  public List<RuleApplication> applySuggestionsToOriginalText(RuleMatch match) {
    final List<String> replacements = match.getSuggestedReplacements();
    final boolean hasRealReplacements = replacements.size() > 0;
    if (!hasRealReplacements) {
      // create a pseudo replacement with the error text itself
      String plainText = textMapping.getPlainText();
      replacements.add(plainText.substring(match.getFromPos(), match.getToPos()));
    }

    final List<RuleApplication> ruleApplications = new ArrayList<RuleApplication>();
    final Location fromPosLocation = textMapping.getOriginalTextPositionFor(match.getFromPos() + 1);  // not zero-based!
    final Location toPosLocation = textMapping.getOriginalTextPositionFor(match.getToPos() + 1);

    /*System.out.println("=========");
    System.out.println(textMapping.getMapping());
    System.out.println("=========");
    System.out.println(textMapping.getPlainText());
    System.out.println("=========");
    System.out.println(originalText);
    System.out.println("=========");*/

    final int fromPos = LocationHelper.absolutePositionFor(fromPosLocation, originalText);
    final int toPos = LocationHelper.absolutePositionFor(toPosLocation, originalText);
    for (String replacement : replacements) {
      final String errorText = textMapping.getPlainText().substring(match.getFromPos(), match.getToPos());
      // the algorithm is off a bit sometimes due to the complex syntax, so consider the next whitespace:
      final int contextFrom = findNextWhitespaceToTheLeft(originalText, fromPos);
      final int contextTo = findNextWhitespaceToTheRight(originalText, toPos);

      /*System.out.println(match + ":");
      System.out.println(match.getFromPos() + "/" + match.getToPos());
      System.out.println(fromPosLocation + "/" + toPosLocation);
      System.out.println(fromPos + "/" + toPos);
      System.out.println(contextFrom + "/" + contextTo + " @ " + originalText.length());*/

      final String text = originalText.substring(0, contextFrom)
              + errorMarkerStart
              + originalText.substring(contextFrom, contextTo)
              + errorMarkerEnd
              + originalText.substring(contextTo);
      final String newText = originalText.substring(0, contextFrom)
              // we do a simple string replacement as that works even if our mapping if off a bit:
              + errorMarkerStart
              + originalText.substring(contextFrom, contextTo).replace(errorText, replacement)
              + errorMarkerEnd
              + originalText.substring(contextTo);
      final RuleApplication application;
      if (hasRealReplacements) {
        application = RuleApplication.forMatchWithReplacement(match, text, newText, errorMarkerStart, errorMarkerEnd);
      } else {
        application = RuleApplication.forMatchWithoutReplacement(match, text, newText, errorMarkerStart, errorMarkerEnd);
      }
      ruleApplications.add(application);
    }
    return ruleApplications;
  }

  int findNextWhitespaceToTheRight(String text, int position) {
    for (int i = position; i < text.length(); i++) {
      if (Character.isWhitespace(text.charAt(i))) {
        return i;
      }
    }
    return text.length();
  }

  int findNextWhitespaceToTheLeft(String text, int position) {
    for (int i = position; i >= 0; i--) {
      if (Character.isWhitespace(text.charAt(i))) {
        return i + 1;
      }
    }
    return 1;
  }

}
