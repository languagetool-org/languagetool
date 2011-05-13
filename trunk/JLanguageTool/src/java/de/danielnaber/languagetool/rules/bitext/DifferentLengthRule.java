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
package de.danielnaber.languagetool.rules.bitext;

import java.io.IOException;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.rules.RuleMatch;

/**
 * Checks if the translation has a really different length than the source
 * (smaller than 30% or longer by 250%).
 * 
 * @author Marcin Miłkowski
 *
 */
public class DifferentLengthRule extends BitextRule {
  
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
    return "Source and target translation lengths are very different!";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sourceText,
      AnalyzedSentence targetText) throws IOException {
   
    if (isLengthDifferent(getPureText(sourceText), getPureText(targetText))) {
      final RuleMatch[] ruleMatch = new RuleMatch[1];
      final AnalyzedTokenReadings[] tokens = targetText.getTokens();
      final int len = tokens[tokens.length - 1].getStartPos() + tokens[tokens.length - 1].getToken().length();
      ruleMatch[0] = new RuleMatch(this, 1, len, getMessage());
      return ruleMatch;
    }
    return new RuleMatch[0];
  }
  
  private boolean isLengthDifferent(final String src, final String trg) {
    final double skew = (((double) src.length() / (double) trg.length()) * 100.00);
    return (skew > 250 || skew < 30);
  }
  
  @Override
  public void reset() {
  }

}
