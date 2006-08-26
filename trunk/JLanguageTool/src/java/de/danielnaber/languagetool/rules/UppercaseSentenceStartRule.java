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
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;

/**
 * Checks that a sentence starts with an uppercase letter.
 * 
 * @author Daniel Naber
 */
public class UppercaseSentenceStartRule extends Rule {

  public UppercaseSentenceStartRule(ResourceBundle messages) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_case")));
  }

  public String getId() {
    return "UPPERCASE_SENTENCE_START";
  }

  public String getDescription() {
    return messages.getString("desc_uppercase_sentence");
  }

  public Language[] getLanguages() {
    return new Language[] { Language.ENGLISH, Language.GERMAN, Language.POLISH, Language.FRENCH, Language.SPANISH };
  }

  public RuleMatch[] match(AnalyzedSentence text) {
    List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    if (tokens.length < 2)
      return toRuleMatchArray(ruleMatches);
    //the case should be the same in all readings
    //discarding the rest of the possible lemmas and POS tags
    AnalyzedToken token = tokens[1].getAnalyzedToken(0);        // 0 is the artifical sentence start token
    String firstToken = token.getToken();
    char firstChar = firstToken.charAt(0);
    if (Character.isLowerCase(firstChar)) {
      String msg = messages.getString("incorrect_case");
      RuleMatch ruleMatch = new RuleMatch(this, token.getStartPos(), 
          token.getStartPos()+token.getToken().length(), msg);
      ruleMatch.setSuggestedReplacement(Character.toUpperCase(firstChar) +  firstToken.substring(1));
      ruleMatches.add(ruleMatch);
    }
    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
  }

}
