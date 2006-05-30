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
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.Language;

/**
 * Check if a word is repeated twice, e.g. "the the". Knows about an
 * exception for German where "..., die die" is often okay.
 *   
 * @author Daniel Naber
 */
public class WordRepeatRule extends Rule {

  private Language language = null;
  
  public WordRepeatRule(ResourceBundle messages, Language language) {
    super(messages);
    this.language = language;
  }
  
  public String getId() {
    return "WORD_REPEAT_RULE";
  }

  public String getDescription() {
    return messages.getString("desc_repetition");
  }

  public Language[] getLanguages() {
    return new Language[] { Language.ENGLISH, Language.GERMAN, Language.POLISH};
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List ruleMatches = new ArrayList();
    AnalyzedToken[] tokens = text.getTokens();
    String prevToken = "";
    String prevPrevToken = "";
    int pos = 0;
    int prevPos = 0;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      if (token.trim().equals("")) {
        // ignore
      } else {
        // avoid "..." etc. to be matched:
        boolean isWord = true;
        if (token.length() == 1) {
          char c = token.charAt(0);
          // Polish '\u0347' is not classified as letter by isLetter
          // System.err.println(c);
          if (!Character.isLetter(c)) {
            isWord = false;
          }
        }
        boolean germanException = false;
        // Don't mark error for cases like:
        // "wie Honda und Samsung, die die Bezahlung ihrer Firmenchefs..."
        if (prevPrevToken.equals(",") && language == Language.GERMAN) {
          germanException = true;
        }
        if (isWord && prevToken.toLowerCase().equals(token.toLowerCase()) && !germanException) {
          String msg = messages.getString("repetition");
          RuleMatch ruleMatch = new RuleMatch(this, prevPos, pos+prevToken.length(), msg);
          ruleMatch.setSuggestedReplacement(prevToken);
          ruleMatches.add(ruleMatch);
        }
        prevPrevToken = prevToken;
        prevToken = token;
        prevPos = pos;
      }
      pos += token.length();
    }
    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
    // nothing
  }

}
