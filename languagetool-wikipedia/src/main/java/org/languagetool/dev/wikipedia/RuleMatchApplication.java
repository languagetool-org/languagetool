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

/**
 * Original text with a potential error and (one of) its applied corrections.
 */
public class RuleMatchApplication {

  private final RuleMatch ruleMatch;
  private final String text;
  private final String textWithCorrection;
  private final ErrorMarker errorMarker;
  private final boolean hasRealReplacement;

  static RuleMatchApplication forMatchWithReplacement(RuleMatch ruleMatch, String text, String textWithCorrection, ErrorMarker errorMarker) {
    return new RuleMatchApplication(ruleMatch, text, textWithCorrection, errorMarker, true);
  }

  static RuleMatchApplication forMatchWithoutReplacement(RuleMatch ruleMatch, String text, String textWithCorrection, ErrorMarker errorMarker) {
    return new RuleMatchApplication(ruleMatch, text, textWithCorrection, errorMarker, false);
  }

  private RuleMatchApplication(RuleMatch ruleMatch, String text, String textWithCorrection, ErrorMarker errorMarker, boolean hasRealReplacement) {
    if (!textWithCorrection.contains(errorMarker.getStartMarker())) {
      throw new IllegalArgumentException("No start error marker (" + errorMarker.getStartMarker() + ") found in text with correction");
    }
    if (!textWithCorrection.contains(errorMarker.getEndMarker())) {
      throw new IllegalArgumentException("No end error marker (" + errorMarker.getEndMarker() + ") found in text with correction");
    }
    this.ruleMatch = ruleMatch;
    this.text = text;
    this.textWithCorrection = textWithCorrection;
    this.errorMarker = errorMarker;
    this.hasRealReplacement = hasRealReplacement;
  }

  public String getOriginalErrorContext(int contextSize) {
    return getContext(text, contextSize);
  }

  public String getCorrectedErrorContext(int contextSize) {
    return getContext(textWithCorrection, contextSize);
  }

  private String getContext(String text, int contextSize) {
    int errorStart = textWithCorrection.indexOf(errorMarker.getStartMarker());
    int errorEnd = textWithCorrection.indexOf(errorMarker.getEndMarker());
    int errorContextStart = Math.max(errorStart - contextSize, 0);
    int errorContentEnd = Math.min(errorEnd + contextSize, text.length());
    return text.substring(errorContextStart, errorContentEnd);
  }

  public RuleMatch getRuleMatch() {
    return ruleMatch;
  }

  public String getOriginalText() {
    return text;
  }

  public String getTextWithCorrection() {
    return textWithCorrection;
  }

  /** @since 2.6 */
  public ErrorMarker getErrorMarker() {
    return errorMarker;
  }
  
  public boolean hasRealReplacement() {
    return hasRealReplacement;
  }

  @Override
  public String toString() {
    return ruleMatch.toString();
  }
}
