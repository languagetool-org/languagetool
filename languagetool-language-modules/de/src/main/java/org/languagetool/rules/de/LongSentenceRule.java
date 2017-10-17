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
 * A rule that warns on long sentences. Note that this rule is off by default.
 *
 * @since 3.9
 */

public class LongSentenceRule extends org.languagetool.rules.LongSentenceRule {

    private static final int DEFAULT_MAX_WORDS = 40;
    private static final boolean DEFAULT_INACTIVE = true;

    /**
     * @param defaultActive allows default granularity
     * @since 3.7
     */
    public LongSentenceRule(ResourceBundle messages, int maxSentenceLength, boolean defaultActive) {
      super(messages);
      super.setCategory(Categories.STYLE.getCategory(messages));
      setLocQualityIssueType(ITSIssueType.Style);
      addExamplePair(Example.wrong("<marker>Dies ist ein Bandwurmsatz, der immer weiter geht, obwohl das kein guter Stil ist, den man eigentlich berücksichtigen sollte, obwohl es auch andere Meinungen gibt, die aber in der Minderzahl sind, weil die meisten Autoren sich doch an die Stilvorgaben halten, wenn auch nicht alle, was aber letztendlich wiederum eine Sache des Geschmacks ist</marker>."),
                Example.fixed("<marker>Dies ist ein kurzer Satz.</marker>"));
      if (defaultActive) {
          setDefaultOn();
        }
      maxWords = maxSentenceLength;
    }

    /**
     * @param maxSentenceLength the maximum sentence length that does not yet trigger a match
     * @since 2.4
     */
    public LongSentenceRule(ResourceBundle messages, int maxSentenceLength) {
      this(messages, maxSentenceLength, DEFAULT_INACTIVE);
    }

    /**
     * Creates a rule with the default maximum sentence length (40 words).
     */
    public LongSentenceRule(ResourceBundle messages) {
      this(messages, DEFAULT_MAX_WORDS, DEFAULT_INACTIVE);
      setDefaultOn();
    }


  @Override
    public String getId() {
      return "DE_TOO_LONG_SENTENCE_" + maxWords;
    }

    @Override
    public String getDescription() {
      return "Sehr langer Satz (mehr als " + maxWords + " Worte)";
    }

    @Override
    public String getMessage() {
        return "Dieser Satz ist sehr lang (mehr als " + maxWords + " Worte).";
    }

    private boolean isWordCount (String tokenText) {
        if (tokenText.length() > 0 && 
          ((tokenText.charAt(0) >= 'A' && tokenText.charAt(0) <= 'Z') 
            || (tokenText.charAt(0) >= 'a' && tokenText.charAt(0) <= 'z')
            || tokenText.charAt(0) == 'ä' || tokenText.charAt(0) == 'ö'  || tokenText.charAt(0) == 'ü'
            || tokenText.charAt(0) == 'Ä' || tokenText.charAt(0) == 'Ö'  || tokenText.charAt(0) == 'Ü')) {
          return true; }
        else return false;
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
      ArrayList<Integer> fromPos = new ArrayList<Integer>();
      ArrayList<Integer> toPos = new ArrayList<Integer>();
      while (i < tokens.length) {
        for(;i < tokens.length && !isWordCount(tokens[i].getToken()); i++);
        if(i < tokens.length) {
          fromPos.add(tokens[i].getStartPos()); 
          toPos.add(tokens[i].getEndPos());
        }
        int numWords = 1;
        //  Text before and after ':' and ';' is handled as separated sentences
        //  Direct speech is splitted 
        while (i < tokens.length && !tokens[i].getToken().equals(":") && !tokens[i].getToken().equals(";") 
                     && ((i < tokens.length - 1  && !tokens[i + 1].getToken().equals(","))
                     || (!tokens[i].getToken().equals("“") && !tokens[i].getToken().equals("»") 
                     && !tokens[i].getToken().equals("«") && !tokens[i].getToken().equals("\"")))) {
          if (isWordCount(tokens[i].getToken())) {
            toPos.set(toPos.size()-1, tokens[i].getEndPos()); 
            numWords++;
          }
          else if(tokens[i].getToken().equals("(") || tokens[i].getToken().equals("{") 
                    || tokens[i].getToken().equals("[")) {        //  The Text between brackets is handled as separate sentence
            String endChar;                             
            if(tokens[i].getToken().equals("(")) endChar = ")";
            else if(tokens[i].getToken().equals("{")) endChar = "}";
            else endChar = "]";
            int numWordsInt = 0;
            int fromPosInt = 0;
            int toPosInt = 0;
            int k;
            for(k = i + 1; k < tokens.length && !tokens[k].getToken().equals(endChar) && !isWordCount(tokens[k].getToken()); k++);
            if(k < tokens.length) {
                fromPosInt = tokens[k].getStartPos(); 
                toPosInt = tokens[k].getEndPos();
            }
            for(k++; k < tokens.length && !tokens[k].getToken().equals(endChar); k++) {
                if (isWordCount(tokens[k].getToken())) {
                  toPosInt = tokens[k].getEndPos(); 
                  numWordsInt++;
                }
            }
            if(k < tokens.length) {
              if(numWordsInt > maxWords) {
                RuleMatch ruleMatch = new RuleMatch(this, fromPosInt, toPosInt, msg);
                ruleMatches.add(ruleMatch);
              }
              for(i = k; i < tokens.length && !isWordCount(tokens[i].getToken()); i++);
              if(i < tokens.length) {
                fromPos.add(tokens[i].getStartPos()); 
                toPos.add(tokens[i].getEndPos());
                numWords++;
              }
            }
          }
          i++;
        }
        if(numWords > maxWords) {
          for(int j = 0; j < fromPos.size(); j++) {
            RuleMatch ruleMatch = new RuleMatch(this, fromPos.get(j), toPos.get(j), msg);
            ruleMatches.add(ruleMatch);
          }
        }
        else {
          for(int j = fromPos.size() - 1; j >= 0; j--) {
            fromPos.remove(j);
            toPos.remove(j);
          }
        }
      }
      return toRuleMatchArray(ruleMatches);
    }
}
