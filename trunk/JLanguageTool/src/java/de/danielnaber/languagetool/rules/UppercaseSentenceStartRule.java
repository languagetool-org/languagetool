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
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;

/**
 * Checks that a sentence starts with an uppercase letter.
 * 
 * @author Daniel Naber
 */
public class UppercaseSentenceStartRule extends Rule {

  private final Language language;

  private String lastParagraphString = "";

  public UppercaseSentenceStartRule(final ResourceBundle messages,
      final Language language) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_case")));
    this.language = language;
  }

  @Override
  public final String getId() {
    return "UPPERCASE_SENTENCE_START";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_uppercase_sentence");
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    if (tokens.length < 2) {
      return toRuleMatchArray(ruleMatches);
    }
    int matchTokenPos = 1; // 0 = SENT_START
    final String firstToken = tokens[matchTokenPos].getToken();
    String secondToken = null;
    String thirdToken = null;
    // ignore quote characters:
    if (tokens.length >= 3
        && ("'".equals(firstToken) || "\"".equals(firstToken) || "„"
            .equals(firstToken))) {
      matchTokenPos = 2;
      secondToken = tokens[matchTokenPos].getToken();
    }
    final String firstDutchToken = dutchSpecialCase(firstToken, secondToken,
        tokens);
    if (firstDutchToken != null) {
      thirdToken = firstDutchToken;
      matchTokenPos = 3;
    }

    String checkToken = firstToken;
    if (thirdToken != null) {
      checkToken = thirdToken;
    } else if (secondToken != null) {
      checkToken = secondToken;
    }

    final String lastToken = tokens[tokens.length - 1].getToken();

    boolean noException = false;
    //fix for lists; note - this will not always work for the last point in OOo,
    //as OOo might serve paragraphs in any order.
    if ((language == Language.RUSSIAN || language == Language.POLISH)
        && (";".equals(lastParagraphString) || ";".equals(lastToken)
            || ",".equals(lastParagraphString) || ",".equals(lastToken))) {
      noException = true;
    }
    //fix for comma in last paragraph; note - this will not always work for the last point in OOo,
    //as OOo might serve paragraphs in any order.
    if ((language == Language.RUSSIAN || language == Language.ITALIAN 
        || language == Language.POLISH || language == Language.GERMAN)
        && (",".equals(lastParagraphString))) {
      noException = true;
    }
    
    lastParagraphString = lastToken;

    if (checkToken.length() > 0) {
        final char firstChar = checkToken.charAt(0);
        if (Character.isLowerCase(firstChar) && (!noException)) {
          final RuleMatch ruleMatch = new RuleMatch(this, tokens[matchTokenPos]
              .getStartPos(), tokens[matchTokenPos].getStartPos()
              + tokens[matchTokenPos].getToken().length(), messages
              .getString("incorrect_case"));
          ruleMatch.setSuggestedReplacement(Character.toUpperCase(firstChar)
              + checkToken.substring(1));
          ruleMatches.add(ruleMatch);
        }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private String dutchSpecialCase(final String firstToken,
      final String secondToken, final AnalyzedTokenReadings[] tokens) {
    if (language != Language.DUTCH) {
      return null;
    }
    if (tokens.length >= 3 && firstToken.equals("'")
        && secondToken.matches("k|m|n|r|s|t")) {
      return tokens[3].getToken();
    }
    return null;
  }

  @Override
  public void reset() {
  }

}
