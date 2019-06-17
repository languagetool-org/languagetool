/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;

/**
 * An abstract rule that checks if there is a missing space before some conjunctions.
 *
 * @since 2.7
 */
public abstract class AbstractSpaceBeforeRule extends Rule {

  protected abstract Pattern getConjunctions();

  public AbstractSpaceBeforeRule(ResourceBundle messages, Language language) {
    super.setCategory(Categories.MISC.getCategory(messages));
  }

  @Override
  public String getId() {
    return "SPACE_BEFORE_CONJUNCTION";
  }

  @Override
  public String getDescription() {
    return "Checks for missing space before some conjunctions";
  }

  protected String getShort() {
    return "Missing white space";
  }

  protected String getSuggestion() {
    return "Missing white space before conjunction";
  }

  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokens();

    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      Matcher matcher = getConjunctions().matcher(token);
      if (matcher.matches()) {
        String previousToken = tokens[i - 1].getToken();
        if (!(previousToken.equals(" ") || previousToken.equals("("))) {
          String replacement = " " + token;
          String msg = getSuggestion();
          int pos = tokens[i].getStartPos();
          RuleMatch potentialRuleMatch = new RuleMatch(this, sentence, pos, pos
              + token.length(), msg, getShort());
          potentialRuleMatch.setSuggestedReplacement(replacement);
          ruleMatches.add(potentialRuleMatch);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}
