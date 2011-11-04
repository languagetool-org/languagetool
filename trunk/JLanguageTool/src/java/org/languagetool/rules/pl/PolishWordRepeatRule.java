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
package de.danielnaber.languagetool.rules.pl;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.RuleMatch;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * @author Marcin Miłkowski
 * 
 *         Rule for detecting same words in the sentence but not just in a row
 * 
 */
public class PolishWordRepeatRule extends PolishRule {

  /**
   * Excluded dictionary words.
   */
  private static final Pattern EXC_WORDS = Pattern
      .compile("nie|tuż|aż|to|siebie|być|ani|ni|albo|"
          + "lub|czy|bądź|jako|zł|np|coraz"
          + "|bardzo|bardziej|proc|ten|jak|mln|tys|swój|mój|"
          + "twój|nasz|wasz|i|zbyt");

  /**
   * Excluded part of speech classes.
   */
  private static final Pattern EXC_POS = Pattern.compile("prep:.*|ppron.*");

  /**
   * Excluded non-words (special symbols, Roman numerals etc.
   */
  private static final Pattern EXC_NONWORDS = Pattern
      .compile("&quot|&gt|&lt|&amp|[0-9].*|"
          + "M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");

  public PolishWordRepeatRule(final ResourceBundle messages) {
    if (messages != null) {
      super.setCategory(new Category(messages.getString("category_misc")));
    }
    setDefaultOff();
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.danielnaber.languagetool.rules.Rule#getId()
   */
  @Override
  public final String getId() {
    return "PL_WORD_REPEAT";
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.danielnaber.languagetool.rules.Rule#getDescription()
   */
  @Override
  public final String getDescription() {
    return "Powtórzenia wyrazów w zdaniu (monotonia stylistyczna)";
  }

  /*
   * Tests if any word form is repeated in the sentence.
   */
  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    boolean repetition = false;
    final TreeSet<String> inflectedWords = new TreeSet<String>();
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
        final String msg = "Powtórzony wyraz w zdaniu";
        final int pos = tokens[i].getStartPos();
        final RuleMatch ruleMatch = new RuleMatch(this, pos, pos
            + token.length(), msg, "Powtórzenie wyrazu");        
        ruleMatches.add(ruleMatch);
        repetition = false;
      }

    }
    return toRuleMatchArray(ruleMatches);
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.danielnaber.languagetool.rules.Rule#reset()
   */
  @Override
  public void reset() {
    // nothing

  }

}
