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
 * Checks if the translation for segments that have more than two words
 * is different.
 * 
 * @author Marcin Miłkowski
 */
public class SameTranslationRule extends BitextRule {

  public SameTranslationRule() {
    setLocQualityIssueType(ITSIssueType.Untranslated);
  }

  @Override
  public String getDescription() { 
    return "Check if translation is the same as source";
  }
  
  @Override
  public String getId() {
    return "SAME_TRANSLATION";
  }

  @Override
  public String getMessage() {
    return "Source and target translation are the same";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sourceText,
      AnalyzedSentence targetText) throws IOException {

    //This is just heuristics, checking word count
    if (sourceText.getTokensWithoutWhitespace().length > 3 
        && sourceText.getText().equals(targetText.getText())) {
      AnalyzedTokenReadings[] tokens = targetText.getTokens();
      int endPos = tokens[tokens.length - 1].getEndPos();
      return new RuleMatch[] { new RuleMatch(this, targetText, 1, endPos, getMessage()) };
    }
    return new RuleMatch[0];
  }

}
