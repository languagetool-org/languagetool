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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.JLanguageTool;
import org.languagetool.tools.StringTools;

/**
 * Rule for detecting same words in the sentence but not just in a row. This rule is off by default.
 *
 * @author Marcin Miłkowski
 */
public abstract class AdvancedWordRepeatRule extends Rule {

  public AdvancedWordRepeatRule(ResourceBundle messages) {
    super.setCategory(Categories.MISC.getCategory(messages));
    setDefaultOff();
    setLocQualityIssueType(ITSIssueType.Style);
  }

  protected abstract Set<String> getExcludedWordsPattern();
  protected abstract Pattern getExcludedNonWordsPattern();
  protected abstract Pattern getExcludedPos();
  protected abstract String getMessage();
  protected abstract String getShortMessage();

  /*
   * Tests if any word form is repeated in the sentence.
   */
  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    boolean repetition = false;
    Set<String> inflectedWords = new TreeSet<>();
    String prevLemma;
    int curToken = 0;
    // start from real token, 0 = SENT_START
    for (int i = 1; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      // avoid "..." etc. to be matched:
      boolean isWord = true;
      boolean hasLemma = true;

      if (token.length() < 2) {
        isWord = false;
      }

      for (AnalyzedToken analyzedToken : tokens[i]) {
        String posTag = analyzedToken.getPOSTag();
        if (posTag != null) {
          if (StringTools.isEmpty(posTag)) {
            isWord = false;
            break;
          }
          String lemma = analyzedToken.getLemma();
          if (lemma == null) {
            hasLemma = false;
            break;
          }

          if (getExcludedWordsPattern().contains(lemma)) {
            isWord = false;
            break;
          }

          Matcher m2 = getExcludedPos().matcher(posTag);
          if (m2.matches()) {
            isWord = false;
            break;
          }
        } else {
          hasLemma = false;
        }

      }

      Matcher m1 = getExcludedNonWordsPattern().matcher(tokens[i].getToken());
      if (isWord && m1.matches()) {
        isWord = false;
      }

      prevLemma = "";
      if (isWord) {
        boolean notSentEnd = false;
        for (AnalyzedToken analyzedToken : tokens[i]) {
          String pos = analyzedToken.getPOSTag();
          if (pos != null) {
            notSentEnd |= JLanguageTool.SENTENCE_END_TAGNAME.equals(pos);
          }
          if (hasLemma) {
            String curLemma = analyzedToken.getLemma();
            if (!prevLemma.equals(curLemma) && !notSentEnd) {
              if (inflectedWords.contains(curLemma) && curToken != i) {
                repetition = true;
              } else {
                inflectedWords.add(analyzedToken.getLemma());
                curToken = i;
              }
            }
            prevLemma = curLemma;
          } else {
            if (inflectedWords.contains(tokens[i].getToken()) && !notSentEnd) {
              repetition = true;
            } else {
              inflectedWords.add(tokens[i].getToken());
            }
          }
        }
      }

      if (repetition) {
        int pos = tokens[i].getStartPos();
        RuleMatch ruleMatch = new RuleMatch(this, sentence, pos, pos
            + token.length(), getMessage(), getShortMessage());
        ruleMatches.add(ruleMatch);
        repetition = false;
      }

    }
    return toRuleMatchArray(ruleMatches);
  }

}
