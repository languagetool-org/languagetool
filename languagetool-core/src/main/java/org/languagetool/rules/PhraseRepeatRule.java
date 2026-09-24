/* LanguageTool, a natural language style checker
 * Copyright (C) 2026 Jaume Ortolà
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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.tools.StringTools;

/**
 * Check if a phrase of two or three words is repeated directly after itself,
 * e.g. "the house the house is ..." or "the house is the house is green".
 *
 * <p>This is a generalisation of {@link WordRepeatRule}, which only detects
 * single-word repetitions such as "the the".
 *
 * @author Daniel Naber
 */
public class PhraseRepeatRule extends Rule {

  // longest phrase length checked first, so a 3-word repetition isn't
  // reported twice as a shorter 2-word repetition
  private static final int MAX_PHRASE_LENGTH = 3;
  private static final int MIN_PHRASE_LENGTH = 2;

  public PhraseRepeatRule(ResourceBundle messages, Language language) {
    super(messages);
    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Duplication);
  }

  /**
   * Implement this method to return <code>true</code> if there's
   * a potential phrase repetition of the given length starting at the current
   * position that should be ignored, i.e. if no error should be created.
   * @param tokens the tokens of the sentence currently being checked
   * @param position the position of the first token of the (first occurrence of the) phrase
   * @param phraseLength the number of words in the repeated phrase (2 or 3)
   * @return this implementation always returns false
   */
  public boolean ignore(AnalyzedTokenReadings[] tokens, int position, int phraseLength) {
    return false;
  }

  @Override
  public String getId() {
    return "PHRASE_REPEAT_RULE";
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_repetition");
  }

  @Override
  public int estimateContextForSureMatch() {
    return 1;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = getSentenceWithImmunization(sentence).getTokensWithoutWhitespace();

    // we start from token 1, token no. 0 is guaranteed to be SENT_START
    int i = 1;
    while (i < tokens.length) {
      int matchedLength = 0;
      // check longer phrases first so we don't report a 3-word repetition as a 2-word one
      for (int phraseLength = MAX_PHRASE_LENGTH; phraseLength >= MIN_PHRASE_LENGTH; phraseLength--) {
        if (i + 2 * phraseLength <= tokens.length && phraseRepeatedAt(tokens, i, phraseLength) && !ignore(tokens, i, phraseLength)) {
          matchedLength = phraseLength;
          break;
        }
      }
      if (matchedLength > 0) {
        int firstStart = tokens[i].getStartPos();
        int secondEnd = tokens[i + 2 * matchedLength - 1].getEndPos();
        String phrase = phraseToString(tokens, i, matchedLength);
        String msg = messages.getString("desc_repetition"); //TODO update message
        RuleMatch ruleMatch = createRuleMatch(phrase, firstStart, secondEnd, msg, sentence);
        ruleMatches.add(ruleMatch);
        // skip past the whole repeated span to avoid overlapping matches
        i += 2 * matchedLength;
      } else {
        i++;
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  protected RuleMatch createRuleMatch(String phrase, int fromPos, int toPos, String msg, AnalyzedSentence sentence) {
    RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, msg, messages.getString("desc_repetition_short"));
    ruleMatch.setSuggestedReplacement(phrase);
    return ruleMatch;
  }

  /**
   * Checks whether the {@code phraseLength} tokens starting at {@code position}
   * are immediately repeated, i.e. whether tokens[position..position+phraseLength-1]
   * equal (case-insensitively) tokens[position+phraseLength..position+2*phraseLength-1].
   */
  protected boolean phraseRepeatedAt(AnalyzedTokenReadings[] tokens, int position, int phraseLength) {
    boolean sawWord = false;
    for (int j = 0; j < phraseLength; j++) {
      AnalyzedTokenReadings first = tokens[position + j];
      AnalyzedTokenReadings second = tokens[position + phraseLength + j];
      if (first.isImmunized() || second.isImmunized()) {
        return false;
      }
      String firstToken = first.getToken();
      String secondToken = second.getToken();
      if (!firstToken.equalsIgnoreCase(secondToken)) {
        return false;
      }
      if (isWord(firstToken)) {
        sawWord = true;
      }
    }
    // require at least one actual word in the phrase, so e.g. ", , , ," isn't flagged
    return sawWord;
  }

  private String phraseToString(AnalyzedTokenReadings[] tokens, int position, int phraseLength) {
    StringBuilder sb = new StringBuilder();
    for (int j = 0; j < phraseLength; j++) {
      if (j > 0 && tokens[position + j].isWhitespaceBefore()) {
        sb.append(' ');
      }
      sb.append(tokens[position + j].getToken());
    }
    return sb.toString();
  }

  // avoid "..." etc. to be matched:
  private boolean isWord(String token) {
    if (StringTools.isEmoji(token)) {
      return false;
    }
    if (StringUtils.isNumericSpace(token)) {
      return false;
    } else if (token.length() == 1) {
      char c = token.charAt(0);
      if (!Character.isLetter(c)) {
        return false;
      }
    }
    return true;
  }

}