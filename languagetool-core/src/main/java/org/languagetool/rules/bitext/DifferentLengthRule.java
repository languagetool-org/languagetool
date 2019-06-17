/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Marcin Miłkowski (www.languagetool.org)
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
package org.languagetool.rules.bitext;

import java.io.IOException;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;

/**
 * Checks if the translation has a really different length than the source
 * (smaller than 30% or longer by 250%).
 * 
 * @author Marcin Miłkowski
 */
public class DifferentLengthRule extends BitextRule {

  private static final int MAX_SKEW = 250;
  private static final int MIN_SKEW = 30;

  public DifferentLengthRule() {
    setLocQualityIssueType(ITSIssueType.Length);
  }

  @Override
  public String getDescription() { 
    return "Check if translation length is similar to source length";
  }
  
  @Override
  public String getId() {
    return "TRANSLATION_LENGTH";
  }

  @Override
  public String getMessage() {
    return "Source and target translation lengths are very different";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sourceText,
      AnalyzedSentence targetText) throws IOException {
   
    if (isLengthDifferent(sourceText.getText(), targetText.getText())) {
      AnalyzedTokenReadings[] tokens = targetText.getTokens();
      int endPos = tokens[tokens.length - 1].getStartPos() + tokens[tokens.length - 1].getToken().length();
      return new RuleMatch[] { new RuleMatch(this, targetText, 0, endPos, getMessage()) };
    }
    return new RuleMatch[0];
  }
  
  private boolean isLengthDifferent(String src, String trg) {
    double skew = ((double) src.length() / (double) trg.length()) * 100.00;
    return skew > MAX_SKEW || skew < MIN_SKEW;
  }
  
}
