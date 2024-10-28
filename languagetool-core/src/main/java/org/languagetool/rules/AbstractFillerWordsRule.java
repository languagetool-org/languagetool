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

import java.util.ResourceBundle;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.UserConfig;

/**
 * A rule that gives hints about the use of filler words.
 * The hints are only given when the percentage of filler words per paragraph exceeds the given limit.
 * A limit of 0 shows all used filler words. Direct speech or citation is excluded otherwise. 
 * This rule detects no grammar error but gives stylistic hints (default off).
 * @author Fred Kruse
 * @since 4.2
 */
public abstract class AbstractFillerWordsRule extends AbstractStatisticStyleRule {
  
  public static final String RULE_ID = "FILLER_WORDS";
  
  private static final int DEFAULT_MIN_PERCENT = 8;
  private static final boolean DEFAULT_ACTIVATION = false;

  private int minPercent = DEFAULT_MIN_PERCENT;

  /*
   * Override this to detect filler words in the specified language
   */
  protected abstract boolean isFillerWord(String token);
  
  public AbstractFillerWordsRule(ResourceBundle messages, Language lang, UserConfig userConfig, boolean defaultActive) {
    super(messages, lang, userConfig, DEFAULT_MIN_PERCENT);
  }

  public AbstractFillerWordsRule(ResourceBundle messages, Language lang, UserConfig userConfig) {
    this(messages, lang, userConfig, DEFAULT_ACTIVATION);
  }

  @Override
  public String getDescription() {
    return messages.getString("filler_words_rule_desc");
  }

  @Override
  public String getId() {
    return RULE_ID;
  }

  /**
   *  give the user the possibility to configure the function
   */
  @Override
  public RuleOption[] getRuleOptions() {
    RuleOption[] ruleOptions = { new RuleOption(minPercent, messages.getString("filler_words_rule_opt_text"), 0, 100) };
    return ruleOptions;
  }

  public String getMessage() {
    return messages.getString("filler_words_rule_msg");
  }
  
  protected boolean isException(AnalyzedTokenReadings[] tokens, int num) {
    return false;
  }

  @Override
  protected int conditionFulfilled(AnalyzedTokenReadings[] tokens, int nAnalysedToken) {
    if (isFillerWord(tokens[nAnalysedToken].getToken()) && !isException(tokens, nAnalysedToken)) {
     return nAnalysedToken;
   }
   return -1;
  }

  @Override
  protected boolean sentenceConditionFulfilled(AnalyzedTokenReadings[] tokens, int nAnalysedToken) {
    return false;
  }

  @Override
  protected boolean excludeDirectSpeech() {
    return true;
  }

  @Override
  protected String getLimitMessage(int limit, double percent) {
    return getMessage();
  }

  @Override
  protected String getSentenceMessage() {
    return null;
  }

  @Override
  public String getConfigurePercentText() {
    return null;
  }

  @Override
  public String getConfigureWithoutDirectSpeachText() {
    return null;
  }
  
  @Override
  public int minToCheckParagraph() {
    return 0;
  }
  
}
