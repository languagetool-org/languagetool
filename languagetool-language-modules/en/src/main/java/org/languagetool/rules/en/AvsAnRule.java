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
package org.languagetool.rules.en;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.*;
import org.languagetool.tools.StringTools;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import static org.languagetool.rules.en.AvsAnData.getWordsRequiringA;
import static org.languagetool.rules.en.AvsAnData.getWordsRequiringAn;

/**
 * Check if the determiner (if any) preceding a word is:
 * <ul>
 *   <li><i>an</i> if the next word starts with a vowel
 *   <li><i>a</i> if the next word does not start with a vowel
 * </ul>
 *  This rule loads some exceptions from external files {@code det_a.txt} and
 *  {@code det_an.txt} (e.g. for <i>an hour</i>).
 * 
 * @author Daniel Naber
 */
public class AvsAnRule extends EnglishRule {

  enum Determiner {
    A, AN, A_OR_AN, UNKNOWN
  }

  private static final Pattern cleanupPattern = Pattern.compile("[^αa-zA-Z0-9\\.;,:']");

  public AvsAnRule(ResourceBundle messages) {
    super.setCategory(Categories.MISC.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Misspelling);
    addExamplePair(Example.wrong("The train arrived <marker>a hour</marker> ago."),
                   Example.fixed("The train arrived <marker>an hour</marker> ago."));
  }

  @Override
  public String getId() {
    return "EN_A_VS_AN";
  }

  @Override
  public String getDescription() {
    return "Use of 'a' vs. 'an'";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
    int prevTokenIndex = 0;
    for (int i = 1; i < tokens.length; i++) {  // ignoring token 0, i.e., SENT_START
      AnalyzedTokenReadings token = tokens[i];
      String prevTokenStr = prevTokenIndex > 0 ? tokens[prevTokenIndex].getToken() : null;

      boolean isSentenceStart = prevTokenIndex == 1;
      boolean equalsA = "a".equalsIgnoreCase(prevTokenStr);
      boolean equalsAn = "an".equalsIgnoreCase(prevTokenStr);

      if (!isSentenceStart) {
        equalsA = "a".equals(prevTokenStr);
        equalsAn = "an".equals(prevTokenStr);
      }

      if (equalsA || equalsAn) {
        Determiner determiner = getCorrectDeterminerFor(token);
        String msg = null;
        if (equalsA && determiner == Determiner.AN) {
          String replacement = StringTools.startsWithUppercase(prevTokenStr) ? "An" : "an";
          msg = "Use <suggestion>" + replacement + "</suggestion> instead of '" + prevTokenStr + "' if the following "+
                  "word starts with a vowel sound, e.g. 'an article', 'an hour'";
        } else if (equalsAn && determiner == Determiner.A) {
          String replacement = StringTools.startsWithUppercase(prevTokenStr) ? "A" : "a";
          msg = "Use <suggestion>" + replacement + "</suggestion> instead of '" + prevTokenStr + "' if the following "+
                  "word doesn't start with a vowel sound, e.g. 'a sentence', 'a university'";
        }
        if (msg != null) {
          RuleMatch match = new RuleMatch(
              this, tokens[prevTokenIndex].getStartPos(), tokens[prevTokenIndex].getEndPos(), msg, "Wrong article");
          ruleMatches.add(match);
        }
      }
      if (token.hasPosTag("DT")) {
        prevTokenIndex = i;
      } else if (token.getToken().matches("[-\"()\\[\\]]+")) {
        // skip e.g. the quote in >>an "industry party"<<
      } else {
        prevTokenIndex = 0;
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  /**
   * Adds "a" or "an" to the English noun. Used for suggesting the proper form of the indefinite article.
   * For the rare cases where both "a" and "an" are considered okay (e.g. for "historical"), "a" is returned.
   * @param origWord Word that needs an article.
   * @return String containing the word with a determiner, or just the word if the word is an abbreviation.
   */
  public String suggestAorAn(String origWord) {
    AnalyzedTokenReadings token = new AnalyzedTokenReadings(new AnalyzedToken(origWord, null, null), 0);
    Determiner determiner = getCorrectDeterminerFor(token);
    if (determiner == Determiner.A) {
      return "a " + origWord;
    } else if (determiner == Determiner.AN) {
      return "an " + origWord;
    } else if (determiner == Determiner.A_OR_AN) {
      return "a " + origWord;
    } else {
      return origWord;
    }
  }

  Determiner getCorrectDeterminerFor(AnalyzedTokenReadings token) {
    String word = token.getToken();
    Determiner determiner = Determiner.UNKNOWN;
    String[] parts = word.split("[-']");  // for example, in "one-way" only "one" is relevant
    if (parts.length >= 1 && !parts[0].equalsIgnoreCase("a")) {  // avoid false alarm on "A-levels are..."
      word = parts[0];
    }
    if (token.isWhitespaceBefore() || !"-".equals(word)) { // e.g., 'a- or anti- are prefixes'
      word = cleanupPattern.matcher(word).replaceAll("");         // e.g. >>an "industry party"<<
      if (StringTools.isEmpty(word)) {
        return Determiner.UNKNOWN;
      }
    }
    if (getWordsRequiringA().contains(word.toLowerCase()) || getWordsRequiringA().contains(word)) {
      determiner = Determiner.A;
    }
    if (getWordsRequiringAn().contains(word.toLowerCase()) || getWordsRequiringAn().contains(word)) {
      if (determiner == Determiner.A) {
        determiner = Determiner.A_OR_AN;   // e.g. for 'historical'
      } else {
        determiner = Determiner.AN;
      }
    }
    if (determiner == Determiner.UNKNOWN) {
      char tokenFirstChar = word.charAt(0);
      if (StringTools.isAllUppercase(word) || StringTools.isMixedCase(word)) {
        // we don't know how all-uppercase words (often abbreviations) are pronounced,
        // so never complain about these
        determiner = Determiner.UNKNOWN;
      } else if (isVowel(tokenFirstChar)) {
        determiner = Determiner.AN;
      } else {
        determiner = Determiner.A;
      }
    }
    return determiner;
  }

  private boolean isVowel(char c) {
    char lc = Character.toLowerCase(c);
    return lc == 'a' || lc == 'e' || lc == 'i' || lc == 'o' || lc == 'u';
  }

  @Override
  public void reset() {
    // nothing
  }

}
