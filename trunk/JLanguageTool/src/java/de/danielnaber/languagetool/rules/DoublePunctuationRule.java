/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules;

import java.util.ArrayList;
import java.util.List;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.Language;

/**
 * A rule that matches ".." (but not "..." etc) and ",,".
 * 
 * @author Daniel Naber
 */
public class DoublePunctuationRule extends Rule {

  public String getId() {
    return "DOUBLE_PUNCTUATION";
  }

  public String getDescription() {
    return "Use of two consecutive dots or commas";
  }

  public Language[] getLanguages() {
    return new Language[] { Language.ENGLISH, Language.GERMAN };
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList();
    AnalyzedToken[] tokens = text.getTokens();
    AnalyzedToken matchToken = null;
    int dotCount = 0;
    int commaCount = 0;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      String nextToken = null;
      if (i < tokens.length-1)
        nextToken = tokens[i+1].getToken();
      if (token.trim().equals(".")) {
        dotCount++;
        commaCount = 0;
        matchToken = tokens[i];
      } else if (token.trim().equals(",")) {
        commaCount++;
        dotCount = 0;
        matchToken = tokens[i];
      }
      if (dotCount == 2 && !nextToken.equals(".")) {
        String msg = "Two consecutive dots.";
        RuleMatch ruleMatch = new RuleMatch(this, matchToken.getStartPos(), matchToken.getStartPos()+1, msg);
        ruleMatches.add(ruleMatch);
        dotCount = 0;
      } else if (commaCount == 2 && !nextToken.equals(",")) {
        String msg = "Two consecutive commas.";
        RuleMatch ruleMatch = new RuleMatch(this, matchToken.getStartPos(), matchToken.getStartPos()+1, msg);
        ruleMatches.add(ruleMatch);
        commaCount = 0;
      }
      if (!token.trim().equals(".") && !token.trim().equals(",")) {
        dotCount = 0;
        commaCount = 0;
      }        
    }
    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
    // nothing
  }

}
