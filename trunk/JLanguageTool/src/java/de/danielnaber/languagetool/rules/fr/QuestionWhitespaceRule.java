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
package de.danielnaber.languagetool.rules.fr;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * A rule that matches spaces before ?,:,; and ! (required for 
 * correct French punctuation).
 * 
 * @author Marcin Miłkowski
 */
public class QuestionWhitespaceRule extends FrenchRule {

  public QuestionWhitespaceRule(final ResourceBundle messages) {
    //super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));
  }
  
  public String getId() {
    return "FRENCH_WHITESPACE";
  }

  public String getDescription() {
    return ("Insertion des espaces fines insécables");
  }
  

  public RuleMatch[] match(final AnalyzedSentence text) {
    List < RuleMatch > ruleMatches = new ArrayList<RuleMatch>();
    AnalyzedTokenReadings[] tokens = text.getTokens();
    String prevToken = "";
    int pos = 0;
    for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i].getToken().trim();
            boolean isWhiteBefore = tokens[i].isWhitespaceBefore();
            pos += token.length();
            String msg = null;
            int fixPos = 0;
            int fixLen = 0;
        String suggestionText = null;
            if (token.equals("?") 
                && isWhiteBefore) {
                msg = "Point d'interrogation est précédé d'une espace fine insécable.";
                // non-breaking space
                suggestionText = " ?";
                fixLen = 1;
            } else if (token.equals("!") 
                && isWhiteBefore) {
              msg = "Point d'exclamation est précédé d'une espace fine insécable.";
                // non-breaking space
                suggestionText = " !";
                fixLen = 1;
            } else if (token.equals("»") 
                && isWhiteBefore) {
              msg = "Le guillemet fermant est précédé d'une espace fine insécable.";
                // non-breaking space
                suggestionText = " »";
                fixLen = 1;
            } else if (StringTools.isEmpty(token) 
                && prevToken.equals("«")) {
              msg = "Le guillemet ouvrant est suivi d'une espace fine insécable.";
                // non-breaking space
                suggestionText = "« ";
                fixLen = 1;
            } else if (token.equals(";") 
                && isWhiteBefore) {
              msg = "Point-virgule est précédé d'une espace fine insécable.";
                // non-breaking space
                suggestionText = " ;";
                fixLen = 1;
            } else if (token.equals(":") 
                && isWhiteBefore) {
              msg = "Deux-points sont précédé d'une espace fine insécable.";
                // non-breaking space
                suggestionText = " :";
                fixLen = 1;
            } else if (token.equals("?") 
                && !isWhiteBefore
                && !prevToken.equals("!")
                && !(prevToken.equals("\u00a0"))) {
                msg = "Point d'interrogation est précédé d'une espace fine insécable.";
                  // non-breaking space
                  suggestionText = prevToken + " ?";
                  fixLen = 1;              
          } else if (token.equals("!") 
              && !isWhiteBefore
              && !prevToken.equals("?")
              && !(prevToken.equals("\u00a0"))) {
            msg = "Point d'exclamation est précédé d'une espace fine insécable.";
                // non-breaking space
                suggestionText = prevToken + " !";
                fixLen = 1;
          } else if (token.equals(";") 
              && !isWhiteBefore
              && !(prevToken.equals("\u00a0"))) {
              msg = "Point-virgule est précédé d'une espace fine insécable.";
                // non-breaking space
                suggestionText = prevToken + " ;";
                fixLen = 1;
          } else if (token.equals(":") 
              && !isWhiteBefore
              && !(prevToken.equals("\u00a0"))) {
              msg = "Deux-points précédés d'une espace fine insécable.";
              // non-breaking space
              suggestionText = prevToken + " :";
              fixLen = 1;
        } else if (!StringTools.isEmpty(token)
            && !token.equals("\u00a0")                 
            && prevToken.equals("«")) {
          msg = "Le guillemet ouvrant est suivi d'une espace fine insécable.";
            // non-breaking space
            suggestionText = "« ";
            fixLen = 0;
        } else if (!isWhiteBefore
            && !prevToken.equals("\u00a0")                 
            && token.equals("»")) {
          msg = "Le guillemet fermant est précédé d'une espace fine insécable.";
            // non-breaking space
            suggestionText = prevToken + " »";
            fixLen = 1;            
        } 
            
          if (msg != null) {
                int fromPos = tokens[i - 1].getStartPos() + fixPos;
                int toPos = tokens[i - 1].getStartPos() + fixPos 
                  + fixLen + tokens[i - 1].getToken().length();
                RuleMatch ruleMatch = new RuleMatch(this, fromPos, toPos, msg, "Insérer un espace insécable");
          if (suggestionText != null)
            ruleMatch.setSuggestedReplacement(suggestionText);
                ruleMatches.add(ruleMatch);
            }
            prevToken = token;
        }
    
    return toRuleMatchArray(ruleMatches);
  }

  public void reset() {
    // nothing
  }

}
