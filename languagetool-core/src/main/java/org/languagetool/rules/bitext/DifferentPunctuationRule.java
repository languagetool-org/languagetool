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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

/**
 * Checks if the translation has the same ending punctuation as the source.
 * 
 * @author Marcin Miłkowski
 */
public class DifferentPunctuationRule extends BitextRule {

  public DifferentPunctuationRule() {
    setLocQualityIssueType(ITSIssueType.Typographical);
  }

  @Override
  public String getDescription() { 
    return "Check if translation has ending punctuation different from the source";
  }
  
  @Override
  public String getId() {
    return "DIFFERENT_PUNCTUATION";
  }

  @Override
  public String getMessage() {
    return "Source and target translation have different ending punctuation";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sourceText,
      AnalyzedSentence targetText) throws IOException {

      final AnalyzedTokenReadings[] translationTokens = targetText.getTokens();
      final AnalyzedTokenReadings[] sourceTokens = sourceText.getTokens();
      int lastTok = translationTokens.length - 1;
      if ((".".equals(translationTokens[lastTok].getToken()) ||
          "?".equals(translationTokens[lastTok].getToken()) ||
          "!".equals(translationTokens[lastTok].getToken())) &&
            !translationTokens[lastTok].getToken().equals
              (sourceTokens[sourceTokens.length - 1].getToken())) {
      final int len = translationTokens[lastTok].getStartPos() +
          translationTokens[lastTok].getToken().length();
      return new RuleMatch[] { new RuleMatch(this, 1, len, getMessage()) };
    }
    return new RuleMatch[0];
  }

  @Override
  public void reset() {
  }

}
