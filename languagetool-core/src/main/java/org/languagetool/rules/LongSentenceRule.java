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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Tag;
import org.languagetool.UserConfig;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

/**
 * A rule that warns on long sentences.
 * @since 3.9
 */
public class LongSentenceRule extends TextLevelRule {

  public static final String RULE_ID = "TOO_LONG_SENTENCE";
  
  private final ResourceBundle messages;
  private final int maxWords;

  public LongSentenceRule(ResourceBundle messages, UserConfig userConfig, int maxWords) {
    this.messages = messages;
    setCategory(Categories.STYLE.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Style);
    setTags(Collections.singletonList(Tag.picky));
    setUrl(Tools.getUrl("https://languagetool.org/insights/post/sentence-length/"));
    int tmpMaxWords = maxWords;
    if (userConfig != null) {
      int confWords = userConfig.getConfigValueByID(getId());
      if (confWords > 0) {
        tmpMaxWords = confWords;
      }
    }
    this.maxWords = tmpMaxWords;
  }

  @Override
  public String getDescription() {
    return "Finds long sentences";
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  private boolean isWordCount(String tokenText) {
    if (tokenText.length() > 0) {
      char firstChar = tokenText.charAt(0);
      if ((firstChar >= 'A' && firstChar <= 'Z') ||
        (firstChar >= 'a' && firstChar <= 'z')) {
        return true;
      }
    }
    return false;
  }

  public String getMessage() {
    return MessageFormat.format(messages.getString("long_sentence_rule_msg2"), maxWords);
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int pos = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      if (tokens.length < maxWords) {   // just a short-circuit
        pos += sentence.getCorrectedTextLength();
        continue;
      }
      String msg = getMessage();
      int i = 0;
      List<Integer> fromPos = new ArrayList<>();
      List<Integer> toPos = new ArrayList<>();

      AnalyzedTokenReadings fromPosToken = null;
      AnalyzedTokenReadings toPosToken = null;
      while (i < tokens.length) {
        int numWords = 0;
        while (i < tokens.length && !tokens[i].getToken().equals(":") && !tokens[i].getToken().equals(";")
          && !tokens[i].getToken().equals("\n") && !tokens[i].getToken().equals("\r\n")
          && !tokens[i].getToken().equals("\n\r")
        ) {
          if (isWordCount(tokens[i].getToken())) {
            //Get first word token
            if (fromPosToken == null) {
              fromPosToken = tokens[i];
            }
            if (numWords == maxWords) {

              //Get last word token
              if (toPosToken == null) {
                for (int j = tokens.length - 1; j >= 0; j--) {
                  if (isWordCount(tokens[j].getToken())) {
                    if (tokens.length > j + 1 && tokens[j+1].getToken().equals(".")) {
                      toPosToken = tokens[j + 1];
                    } else {
                      toPosToken = tokens[j];

                    }
                    break;
                  }
                }
              }

              if (fromPosToken != null && toPosToken != null) {
                fromPos.add(fromPosToken.getStartPos());
                toPos.add(toPosToken.getEndPos() - 1);
              } else {
                //keep old logic if we could not find word tokens
                fromPos.add(tokens[0].getStartPos());
                toPos.add(tokens[tokens.length - 1].getEndPos() - 1);
              }
              break;
            }
            numWords++;
          }
          i++;
        }
        i++;
      }
      for (int j = 0; j < fromPos.size(); j++) {
        RuleMatch ruleMatch = new RuleMatch(this, sentence, pos+fromPos.get(j), pos+toPos.get(j), msg);
        ruleMatches.add(ruleMatch);
      }
      pos += sentence.getCorrectedTextLength();
    }
    return toRuleMatchArray(ruleMatches);
  }

  @Override
  public int minToCheckParagraph() {
    return 0;
  }
  
// next functions give the user the possibility to configure the function
  @Override
  public int getDefaultValue() {
    return maxWords;
  }

  @Override
  public boolean hasConfigurableValue() {
    return true;
  }

  @Override
  public int getMinConfigurableValue() {
    return 5;
  }

  @Override
  public int getMaxConfigurableValue() {
    return 100;
  }

  @Override
  public String getConfigureText() {
    return messages.getString("guiLongSentencesText");
  }



}
