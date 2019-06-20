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
package org.languagetool.rules;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;
import org.languagetool.rules.Category.Location;

/**
 * A rule that warns on long paragraphs. Note that this rule is off by default.
 * @since 4.2
 */
public class LongParagraphRule extends TextLevelRule {

  public static final String RULE_ID = "TOO_LONG_PARAGRAPH";
  
  private static final int DEFAULT_MAX_WORDS = 80;
  private static final boolean DEFAULT_ACTIVATION = false;

  protected int maxWords = DEFAULT_MAX_WORDS;
  private final Language lang;

  public LongParagraphRule(ResourceBundle messages, Language lang, UserConfig userConfig, int defaultWords, boolean defaultActive) {
    super(messages);
    super.setCategory(new Category(new CategoryId("CREATIVE_WRITING"), 
        messages.getString("category_creative_writing"), Location.INTERNAL, false));
    this.lang = lang;
    if (!defaultActive) {
      setDefaultOff();
    }
    if (defaultWords > 0) {
      this.maxWords = defaultWords;
    }
    if (userConfig != null) {
      int confWords = userConfig.getConfigValueByID(getId());
      if (confWords > 0) {
        this.maxWords = confWords;
      }
    }
    setLocQualityIssueType(ITSIssueType.Style);
  }

  public LongParagraphRule(ResourceBundle messages, Language lang, UserConfig userConfig, int defaultWords) {
    this(messages, lang, userConfig, defaultWords, DEFAULT_ACTIVATION);
  }

  public LongParagraphRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    this(messages, lang, userConfig, -1, DEFAULT_ACTIVATION);
  }

  @Override
  public String getDescription() {
    return MessageFormat.format(messages.getString("long_paragraph_rule_desc"), maxWords);
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

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
    return 200;
  }

  public String getConfigureText() {
    return messages.getString("guiLongParagraphsText");
  }

  public String getMessage() {
    return MessageFormat.format(messages.getString("long_paragraph_rule_msg"), maxWords);
  }

  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    String msg = getMessage();
    int pos = 0;
    int startPos = 0;
    int endPos = 0;
    int wordCount = 0;
    for(AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for(AnalyzedTokenReadings token : tokens) {
        if(!token.isWhitespace() && !token.isSentenceStart() && !token.isNonWord()) {
          wordCount++;
          if(wordCount == 1) {
            startPos = token.getStartPos() + pos;
          } else if(wordCount == maxWords) {
            endPos = token.getEndPos() + pos;
          }
        }
      }
      if (sentence.hasParagraphEndMark(lang)) {
        if (wordCount > maxWords) {
          RuleMatch ruleMatch = new RuleMatch(this, sentence, startPos, endPos, msg);
          ruleMatches.add(ruleMatch);
        }
        wordCount = 0;
      }
      pos += sentence.getText().length();
    }
    if (wordCount > maxWords) {
      RuleMatch ruleMatch = new RuleMatch(this, startPos, endPos, msg);
      ruleMatches.add(ruleMatch);
    }
    return toRuleMatchArray(ruleMatches);
  }

}
