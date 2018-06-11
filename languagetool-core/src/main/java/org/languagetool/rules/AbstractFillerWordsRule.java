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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.UserConfig;
import org.languagetool.rules.Category.Location;

/**
 * A rule that warns on long paragraphs. Note that this rule is off by default.
 */
public abstract class AbstractFillerWordsRule extends TextLevelRule {


  public static final String RULE_ID = "FILLER_WORDS";
  
  private static final int DEFAULT_MIN_PERCENT = 6;
  private static final Pattern NON_WORD_REGEX = Pattern.compile("[.?!…:;,~’'\"„“”»«‚‘›‹()\\[\\]\\-–—*×∗·+÷/=]");
  private static final boolean DEFAULT_ACTIVATION = false;

  private int minPercent = DEFAULT_MIN_PERCENT;


  /* Override this to detect filler words in the specified language
   * 
   */
  protected abstract boolean isFillerWord(String token);
  
  /**
   * @since 4.2
   */
  public AbstractFillerWordsRule(ResourceBundle messages, UserConfig userConfig, boolean defaultActive) {
    super(messages);
    super.setCategory(new Category(new CategoryId("CREATIV_WRITING"), 
        messages.getString("category_creativ_writing"), Location.INTERNAL, false));
    if (!defaultActive) {
      setDefaultOff();
    }
    if (userConfig != null) {
      int confPercent = userConfig.getConfigValueByID(getId());
      if(confPercent > 0) {
        this.minPercent = confPercent;
      }
    }
    setLocQualityIssueType(ITSIssueType.Style);
  }

  /**
   * Creates a rule with default inactive
   * @since 4.2
   */
  public AbstractFillerWordsRule(ResourceBundle messages, UserConfig userConfig) {
    this(messages, userConfig, DEFAULT_ACTIVATION);
  }

  @Override
  public String getDescription() {
    return messages.getString("filler_words_rule_desc");
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
    return minPercent;
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
    return 0;
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
  public String getConfigureText() {
    return messages.getString("filler_words_rule_opt_text");
  }

  public String getMessage() {
    return messages.getString("filler_words_rule_msg");
  }
  
  public boolean isException(AnalyzedTokenReadings[] tokens, int num) {
    return false;
  }
  
  @Override
  public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    String msg = getMessage();
    List<Integer> startPos = new ArrayList<Integer>();
    List<Integer> endPos = new ArrayList<Integer>();
    double percent = 0;
    int pos = 0;
    int wordCount = 0;
    AnalyzedTokenReadings lastToken = null;
    for(AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokens();
      for(int n = 0; n < tokens.length; n++) {
        AnalyzedTokenReadings token = tokens[n];
        String sToken = token.getToken();
        if(!token.isWhitespace() && !token.isSentenceStart() 
            && !token.isSentenceEnd() && !NON_WORD_REGEX.matcher(sToken).matches()) {
          wordCount++;
          if(isFillerWord(sToken) && !isException(tokens, n)) {
            startPos.add(token.getStartPos() + pos);
            endPos.add(token.getEndPos() + pos);
          }
        } else if ("\n".equals(sToken) || "\r\n".equals(sToken) || "\n\r".equals(sToken)) {
          percent = startPos.size() * 100.0 / wordCount;
          if (percent > minPercent) {
            for (int i = 0; i < startPos.size(); i++) {
              RuleMatch ruleMatch = new RuleMatch(this, startPos.get(i), endPos.get(i), msg);
              ruleMatches.add(ruleMatch);
            }
          }
          wordCount = 0;
          startPos = new ArrayList<Integer>();
          endPos = new ArrayList<Integer>();
        }
      }
      pos += sentence.getText().length();
    }
    percent = startPos.size() * 100.0 / wordCount;
    if (percent > minPercent) {
      for (int i = 0; i < startPos.size(); i++) {
        RuleMatch ruleMatch = new RuleMatch(this, startPos.get(i), endPos.get(i), msg);
        ruleMatches.add(ruleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

}
