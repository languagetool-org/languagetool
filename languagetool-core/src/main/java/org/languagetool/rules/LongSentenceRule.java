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
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

/**
 * A rule that warns on long sentences. Note that this rule is off by default.
 */
public class LongSentenceRule extends Rule {

  private static final int DEFAULT_MAX_WORDS = 50;
  private static final Pattern NON_WORD_REGEX = Pattern.compile("[.?!…:;,~’'\"„“”»«‚‘›‹()\\[\\]\\-–—*×∗·+÷/=]");
  private static final boolean DEFAULT_ACTIVATION = false;

  protected static int maxWords = DEFAULT_MAX_WORDS;

  /**
   * @since 3.7
   */
  public LongSentenceRule(ResourceBundle messages, boolean defaultActive) {
    super(messages);
    super.setCategory(Categories.STYLE.getCategory(messages));
    if (!defaultActive) {
      setDefaultOff();
    }
    setLocQualityIssueType(ITSIssueType.Style);
  }

  /**
   * Creates a rule with default inactive
   */
  public LongSentenceRule(ResourceBundle messages) {
    this(messages, DEFAULT_ACTIVATION);
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
    return "TOO_LONG_SENTENCE";
  }

  /*
   * set maximal Distance of words in number of sentences - note that this sets a static value
   * that affects all instances of this rule!
   * @since 4.1
   */
  @Override
  public void setDefaultValue(int numWords) {
    maxWords = numWords;
  }
  
  /*
   * get maximal Distance of words in number of sentences
   * @since 4.1
   */
  @Override
  public int getDefaultValue() {
    return maxWords;
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
        String token = aToken.getToken();
        if (!aToken.isSentenceStart() && !aToken.isSentenceEnd() && !NON_WORD_REGEX.matcher(token).matches()) {
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
