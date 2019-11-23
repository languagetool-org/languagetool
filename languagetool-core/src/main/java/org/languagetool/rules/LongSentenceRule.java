/* LanguageTool, a natural language style checker
 * Copyright (C) 2011 Michael Bryant
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
import org.languagetool.UserConfig;

/**
 * A rule that warns on long sentences. Note that this rule is off by default.
 */
public class LongSentenceRule extends Rule {

  public static final String RULE_ID = "TOO_LONG_SENTENCE";
  
  private static final int DEFAULT_MAX_WORDS = 50;
  private static final boolean DEFAULT_ACTIVATION = false;

  protected int maxWords = DEFAULT_MAX_WORDS;

  /**
   * @since 4.2
   */
  public LongSentenceRule(ResourceBundle messages, UserConfig userConfig, int defaultWords, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    if (!defaultActive) {
      setDefaultOff();
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
    setLocQualityIssueType(ITSIssueType.Style);
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
    return MessageFormat.format(messages.getString("long_sentence_rule_desc"), maxWords);
  }

  /**
   * Override this ID by adding a language acronym (e.g. TOO_LONG_SENTENCE_DE)
   * to use adjustment of maxWords by option panel
   * @since 4.1
   */   
  @Override
  public String getId() {
    return RULE_ID;
  }

  /*
   * get maximal Distance of words in number of sentences
   * @since 4.1
   */
  @Override
  public int getDefaultValue() {
    return maxWords;
  }

  /**
   * @since 4.2
   */
  @Override
  public boolean hasConfigurableValue() {
    return true;
  }

  /**
   * @since 4.2
   */
  @Override
  public int getMinConfigurableValue() {
    return 5;
  }

  /**
   * @since 4.2
   */
  @Override
  public int getMaxConfigurableValue() {
    return 100;
  }

  /**
   * @since 4.2
   */
  @Override
  public String getConfigureText() {
    return messages.getString("guiLongSentencesText");
  }

  public String getMessage() {
		return MessageFormat.format(messages.getString("long_sentence_rule_msg2"), maxWords);
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    String msg = getMessage();
    if (tokens.length < maxWords + 1) {   // just a short-circuit
      return toRuleMatchArray(ruleMatches);
    } else {
      int numWords = 0;
      int startPos = 0;
      int prevStartPos;
      for (AnalyzedTokenReadings aToken : tokens) {
        if (!aToken.isSentenceStart() && !aToken.isSentenceEnd() && !aToken.isNonWord()) {
          numWords++;
          prevStartPos = startPos;
          startPos = aToken.getStartPos();
          if (numWords > maxWords) {
            RuleMatch ruleMatch = new RuleMatch(this, sentence, prevStartPos, aToken.getEndPos(), msg);
            ruleMatches.add(ruleMatch);
            break;
          }
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}
