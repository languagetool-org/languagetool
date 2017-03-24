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

import org.apache.commons.lang3.StringUtils;
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
  private final ErrorMarker errorMarker;

  /**
   * @param originalText the original text that includes markup
   */
  public SuggestionReplacer(PlainTextMapping textMapping, String originalText) {
    // use <<span>> to avoid clashes with <span> in original markup:
    this(textMapping, originalText, new ErrorMarker("<<span class=\"error\">>", "<</span>>"));
  }

  /**
   * @since 2.6
   */
  public SuggestionReplacer(PlainTextMapping textMapping, String originalText, ErrorMarker errorMarker) {
    this.textMapping = textMapping;
    this.originalText = originalText;
    this.errorMarker = errorMarker;
  }

  /**
   * Applies the suggestions from the rule to the original text. For rules that
   * have no suggestion, a pseudo-correction is generated that contains the same
   * text as before.
   */
  public List<RuleMatchApplication> applySuggestionsToOriginalText(RuleMatch match) {
    List<String> replacements = new ArrayList<>(match.getSuggestedReplacements());
    boolean hasRealReplacements = replacements.size() > 0;
    if (!hasRealReplacements) {
      // create a pseudo replacement with the error text itself
      String plainText = textMapping.getPlainText();
      replacements.add(plainText.substring(match.getFromPos(), match.getToPos()));
    }

    List<RuleMatchApplication> ruleMatchApplications = new ArrayList<>();
    Location fromPosLocation = textMapping.getOriginalTextPositionFor(match.getFromPos() + 1);  // not zero-based!
    Location toPosLocation = textMapping.getOriginalTextPositionFor(match.getToPos() + 1);

    /*System.out.println("=========");
    System.out.println(textMapping.getMapping());
    System.out.println("=========");
    System.out.println(textMapping.getPlainText());
    System.out.println("=========");
    System.out.println(originalText);
    System.out.println("=========");*/

    int fromPos = LocationHelper.absolutePositionFor(fromPosLocation, originalText);
    int toPos = LocationHelper.absolutePositionFor(toPosLocation, originalText);
    for (String replacement : replacements) {
      String errorText = textMapping.getPlainText().substring(match.getFromPos(), match.getToPos());
      // the algorithm is off a bit sometimes due to the complex syntax, so consider the next whitespace:
      int contextFrom = findNextWhitespaceToTheLeft(originalText, fromPos);
      int contextTo = findNextWhitespaceToTheRight(originalText, toPos);

      /*System.out.println(match + ":");
      System.out.println("match.getFrom/ToPos(): " + match.getFromPos() + "/" + match.getToPos());
      System.out.println("from/toPosLocation: " + fromPosLocation + "/" + toPosLocation);
      System.out.println("from/toPos: " + fromPos + "/" + toPos);
      System.out.println("contextFrom/To: " + contextFrom + "/" + contextTo);*/

      String context = originalText.substring(contextFrom, contextTo);
      String text = originalText.substring(0, contextFrom)
              + errorMarker.getStartMarker()
              + context
              + errorMarker.getEndMarker()
              + originalText.substring(contextTo);
      String newContext;
      if (StringUtils.countMatches(context, errorText) == 1) {
        newContext = context.replace(errorText, replacement);
      } else {
        // This may happen especially for very short strings. As this is an
        // error, we don't claim to have real replacements:
        newContext = context;
        hasRealReplacements = false;
      }
      String newText = originalText.substring(0, contextFrom)
              // we do a simple string replacement as that works even if our mapping is off a bit:
              + errorMarker.getStartMarker()
              + newContext
              + errorMarker.getEndMarker()
              + originalText.substring(contextTo);
      RuleMatchApplication application;
      if (hasRealReplacements) {
        application = RuleMatchApplication.forMatchWithReplacement(match, text, newText, errorMarker);
      } else {
        application = RuleMatchApplication.forMatchWithoutReplacement(match, text, newText, errorMarker);
      }
      ruleMatchApplications.add(application);
    }
    return ruleMatchApplications;
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
    return 0;
  }

}
