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
package org.languagetool.rules.ru;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.Category;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tools.StringTools;

/**
 * @author   -  Yakov Reztsov, based on code by Marcin Miłkowski
 * 
 *         Rule for detecting same words in the sentence but not just in a row
 * 
 */
public class RussianWordRepeatRule extends RussianRule {
  /**
   * Excluded dictionary words.
   */
  private static final Pattern EXC_WORDS = Pattern
      .compile("не|ни|а|"
          + "на|в");

  /**
   * Excluded part of speech classes.
   */
  private static final Pattern EXC_POS = Pattern.compile("INTERJECTION|PRDC|PNN:.*");

  /**
   * Excluded non-words (special symbols, Roman numerals etc.
   */
  private static final Pattern EXC_NONWORDS = Pattern
      .compile("&quot|&gt|&lt|&amp|[0-9].*|"
          + "M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");

  public RussianWordRepeatRule(final ResourceBundle messages) {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    setDefaultOff();   // set default off
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.languagetool.rules.Rule#getId()
   */
  @Override
  public final String getId() {
    return "RU_WORD_REPEAT";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.languagetool.rules.Rule#getDescription()
   */
  @Override
  public final String getDescription() {
    return "Повтор слов в предложении";
  }
  
  /*
   * Tests if any word form is repeated in the sentence.
   */
  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    boolean repetition = false;
    final TreeSet<String> inflectedWords = new TreeSet<>();
    String prevLemma, curLemma;
    // start from real token, 0 = SENT_START
    for (int i = 1; i < tokens.length; i++) {
      final String token = tokens[i].getToken();
      // avoid "..." etc. to be matched:
      boolean isWord = true;
      boolean hasLemma = true;

      if (token.length() < 2) {
        isWord = false;
      }

      final int readingsLen = tokens[i].getReadingsLength();
      for (int k = 0; k < readingsLen; k++) {
        final String posTag = tokens[i].getAnalyzedToken(k).getPOSTag();
        if (posTag != null) {
          if (StringTools.isEmpty(posTag)) {
            isWord = false;
            break;
          }
          // FIXME: too many false alarms here:
          final String lemma = tokens[i].getAnalyzedToken(k).getLemma();
          if (lemma == null) {
            hasLemma = false;
            break;
          }
          final Matcher m1 = EXC_WORDS.matcher(lemma);
          if (m1.matches()) {
            isWord = false;
            break;
          }

          final Matcher m2 = EXC_POS.matcher(posTag);
          if (m2.matches()) {
            isWord = false;
            break;
          }
        } else {
          hasLemma = false;
        }

      }

      final Matcher m1 = EXC_NONWORDS.matcher(tokens[i].getToken());
      if (m1.matches()) {
        isWord = false;
      }

      prevLemma = "";
      if (isWord) {
        boolean notSentEnd = false;
        for (int j = 0; j < readingsLen; j++) {
          final String pos = tokens[i].getAnalyzedToken(j).getPOSTag();
          if (pos != null) {
            notSentEnd |= "SENT_END".equals(pos);
          }
          if (hasLemma) {
            curLemma = tokens[i].getAnalyzedToken(j).getLemma();
            if (!prevLemma.equals(curLemma) && !notSentEnd) {
              if (inflectedWords.contains(curLemma)) {
                repetition = true;
              } else {
                inflectedWords.add(tokens[i].getAnalyzedToken(j).getLemma());
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
        final String msg = "Повтор слов в предложении";
        final int pos = tokens[i].getStartPos();
        final RuleMatch ruleMatch = new RuleMatch(this, pos, pos
            + token.length(), msg, "Повтор слов в предложении");        
  //////////
  //       ruleMatch.setSuggestedReplacement(tokens[i].getAnalyzedToken(0).getLemma());
  //       example how to correct word
  //////////
        
        ruleMatches.add(ruleMatch);
        repetition = false;
      }

    }
    return toRuleMatchArray(ruleMatches);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.languagetool.rules.Rule#reset()
   */
  @Override
  public void reset() {
    // nothing

  }

}
