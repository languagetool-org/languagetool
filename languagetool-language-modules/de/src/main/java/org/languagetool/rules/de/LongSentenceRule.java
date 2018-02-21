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
package org.languagetool.rules.de;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A rule that warns on long sentences.
 * @since 3.9
 */
public class LongSentenceRule extends org.languagetool.rules.LongSentenceRule {

  private static final boolean DEFAULT_INACTIVE = false;

  /**
   * @param defaultActive allows default granularity
   */
  public LongSentenceRule(ResourceBundle messages, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>Dies ist ein Bandwurmsatz, der immer weiter geht, obwohl das kein guter Stil ist, den man eigentlich berücksichtigen sollte, obwohl es auch andere Meinungen gibt, die aber in der Minderzahl sind, weil die meisten Autoren sich doch an die Stilvorgaben halten, wenn auch nicht alle, was aber letztendlich wiederum eine Sache des Geschmacks ist</marker>."),
            Example.fixed("<marker>Dies ist ein kurzer Satz.</marker>"));
    if (defaultActive) {
      setDefaultOn();
    }
  }

  /**
   * Creates a rule with the default Off
   */
  public LongSentenceRule(ResourceBundle messages) {
    this(messages, DEFAULT_INACTIVE);
  }

  @Override
  public String getDescription() {
    return "Sehr langer Satz";
  }

  @Override
  public String getMessage() {
    return "Dieser Satz ist sehr lang (mehr als " + maxWords + " Wörter).";
  }

  @Override
  public String getId() {
    return "TOO_LONG_SENTENCE_DE";
  }

  private boolean isWordCount(String tokenText) {
    if (tokenText.length() > 0) {
      char firstChar = tokenText.charAt(0);
      if (((firstChar >= 'A' && firstChar <= 'Z')
                || (firstChar >= 'a' && firstChar <= 'z')
                || firstChar == 'ä' || firstChar == 'ö' || firstChar == 'ü'
                || firstChar == 'Ä' || firstChar == 'Ö' || firstChar == 'Ü' 
                || firstChar == 'ß')) {
      return true;
      }
    } 
    return false;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    String msg = getMessage();
    if (tokens.length < maxWords + 1) {   // just a short-circuit
      return toRuleMatchArray(ruleMatches);
    }
    int i = 0;
    List<Integer> fromPos = new ArrayList<>();
    List<Integer> toPos = new ArrayList<>();
    while (i < tokens.length) {
      for (; i < tokens.length && !isWordCount(tokens[i].getToken()); i++) ;
      if (i < tokens.length) {
        fromPos.add(tokens[i].getStartPos());
        toPos.add(tokens[i].getEndPos());
      }
      int numWords = 1;
      //  Text before and after ':' and ';' is handled as separated sentences
      //  Direct speech is splitted 
      while (i < tokens.length && !tokens[i].getToken().equals(":") && !tokens[i].getToken().equals(";")
              && ((i < tokens.length - 1 && !tokens[i + 1].getToken().equals(","))
              || (!tokens[i].getToken().equals("“") && !tokens[i].getToken().equals("»")
              && !tokens[i].getToken().equals("«") && !tokens[i].getToken().equals("\"")))) {
        if (isWordCount(tokens[i].getToken())) {
          toPos.set(toPos.size() - 1, tokens[i].getEndPos());
          numWords++;
        } else if (tokens[i].getToken().equals("(") || tokens[i].getToken().equals("{")
                || tokens[i].getToken().equals("[")) {        //  The Text between brackets is handled as separate sentence
          String endChar;
          if (tokens[i].getToken().equals("(")) endChar = ")";
          else if (tokens[i].getToken().equals("{")) endChar = "}";
          else endChar = "]";
          int numWordsInt = 0;
          int fromPosInt = 0;
          int toPosInt = 0;
          int k;
          for (k = i + 1; k < tokens.length && !tokens[k].getToken().equals(endChar) && !isWordCount(tokens[k].getToken()); k++)
            ;
          if (k < tokens.length) {
            fromPosInt = tokens[k].getStartPos();
            toPosInt = tokens[k].getEndPos();
          }
          for (k++; k < tokens.length && !tokens[k].getToken().equals(endChar); k++) {
            if (isWordCount(tokens[k].getToken())) {
              toPosInt = tokens[k].getEndPos();
              numWordsInt++;
            }
          }
          if (k < tokens.length) {
            if (numWordsInt > maxWords) {
              RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPosInt, toPosInt, msg);
              ruleMatches.add(ruleMatch);
            }
            for (i = k; i < tokens.length && !isWordCount(tokens[i].getToken()); i++) ;
            if (i < tokens.length) {
              fromPos.add(tokens[i].getStartPos());
              toPos.add(tokens[i].getEndPos());
              numWords++;
            }
          }
        }
        i++;
      }
      if (numWords > maxWords) {
        for (int j = 0; j < fromPos.size(); j++) {
          RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos.get(j), toPos.get(j), msg);
          ruleMatches.add(ruleMatch);
        }
      } else {
        for (int j = fromPos.size() - 1; j >= 0; j--) {
          fromPos.remove(j);
          toPos.remove(j);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }
}
