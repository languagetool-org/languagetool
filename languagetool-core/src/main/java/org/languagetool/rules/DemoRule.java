/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple demo rule as an example for how to implement your own Java-based
 * rule in LanguageTool. Simple walks over the text and prints the words
 * and their analysis.
 * 
 * <p>To activate this rule, add it to {@code getRelevantRules()} in e.g. {@code English.java}.
 * 
 * <p>This rule works on sentences, extend {@link TextLevelRule} instead to work 
 * on the complete text.
 */
public class DemoRule extends Rule {
  
  @Override
  public String getId() {
    return "DEMO_RULE";  // a unique id that doesn't change over time
  }

  @Override
  public String getDescription() {
    return "A demo rule that just prints the text analysis";  // shown in the configuration dialog
  }

  // This is the method with the error detection logic that you need to implement:
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();

    // Let's get all the tokens (i.e. words) of this sentence, but not the spaces:
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    
    // No let's iterate over those - note that the first token will
    // be a special token that indicates the start of a sentence:
    for (AnalyzedTokenReadings token : tokens) {
      
      System.out.println("Token: " + token.getToken());  // the original word from the input text
      
      // A word can have more than one reading, e.g. 'dance' can be a verb or a noun,
      // so we iterate over the readings:
      for (AnalyzedToken analyzedToken : token.getReadings()) {
        System.out.println("  Lemma: " + analyzedToken.getLemma());
        System.out.println("  POS: " + analyzedToken.getPOSTag());
      }
      
      // You can add your own logic here to find errors. Here, we just consider
      // the word "demo" an error and create a rule match that LanguageTool will
      // then show to the user:
      if (token.getToken().equals("demo")) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, token.getStartPos(), token.getEndPos(), "The demo rule thinks this looks wrong");
        ruleMatch.setSuggestedReplacement("blablah");  // the user will see this as a suggested correction
        ruleMatches.add(ruleMatch);
      }
    }

    return toRuleMatchArray(ruleMatches);
  }

}
