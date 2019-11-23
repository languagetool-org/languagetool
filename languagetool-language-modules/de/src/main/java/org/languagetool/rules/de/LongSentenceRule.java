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
import org.languagetool.UserConfig;
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

  private static final boolean DEFAULT_ACTIVATION = false;

  /**
   * @param defaultActive allows default granularity
   */
  public LongSentenceRule(ResourceBundle messages, UserConfig userConfig, int defaultWords, boolean defaultActive) {
    super(messages, userConfig, defaultWords);
    super.setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    addExamplePair(Example.wrong("<marker>Dies ist ein Bandwurmsatz, der immer weiter geht, obwohl das kein guter Stil ist, den man eigentlich berücksichtigen sollte, obwohl es auch andere Meinungen gibt, die aber in der Minderzahl sind, weil die meisten Autoren sich doch an die Stilvorgaben halten, wenn auch nicht alle, was aber letztendlich wiederum eine Sache des Geschmacks ist</marker>."),
                   Example.fixed("<marker>Dies ist ein kurzer Satz.</marker>"));
    if (defaultActive) {
      setDefaultOn();
    }
    if(defaultWords > 0) {
      this.maxWords = defaultWords;
    }
    if (userConfig != null) {
      int confWords = userConfig.getConfigValueByID(getId());
      if(confWords > 0) {
        this.maxWords = confWords;
      }
    }
  }

  /**
   * Creates a rule with default inactive
   * @since 4.2
   */
  public LongSentenceRule(ResourceBundle messages, UserConfig userConfig, int defaultWords) {
    this(messages, userConfig, defaultWords, DEFAULT_ACTIVATION);
  }


  /**
   * Creates a rule with default values can be overwritten by configuration settings
   * @since 4.2
   */
  public LongSentenceRule(ResourceBundle messages, UserConfig userConfig) {
    this(messages, userConfig, -1, DEFAULT_ACTIVATION);
  }

  @Override
  public String getDescription() {
    return "Sehr langer Satz";
  }

  @Override
  public String getMessage() {
    return "Der Satz hat an der markierten Stelle mehr als " + maxWords + " Wörter.";
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
    AnalyzedTokenReadings[] tokens = sentence.getTokens();
    if (tokens.length < maxWords) {   // just a short-circuit
      return toRuleMatchArray(ruleMatches);
    }
    String msg = getMessage();
    int i = 0;
    List<Integer> fromPos = new ArrayList<>();
    List<Integer> toPos = new ArrayList<>();
    while (i < tokens.length) {
      int numWords = 0;
      while (i < tokens.length && !tokens[i].getToken().equals(":") && !tokens[i].getToken().equals(";")
              && !tokens[i].getToken().equals("\n") && !tokens[i].getToken().equals("\r\n") 
              && !tokens[i].getToken().equals("\n\r")
              ) {
        if (isWordCount(tokens[i].getToken())) {
          if(numWords == maxWords) {
            fromPos.add(tokens[i].getStartPos());
            toPos.add(tokens[i].getEndPos());
          }
          numWords++;
        }
        i++;
      }
      i++;
    }
    for (int j = 0; j < fromPos.size(); j++) {
      RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos.get(j), toPos.get(j), msg);
      ruleMatches.add(ruleMatch);
    }
    return toRuleMatchArray(ruleMatches);
  }

}
