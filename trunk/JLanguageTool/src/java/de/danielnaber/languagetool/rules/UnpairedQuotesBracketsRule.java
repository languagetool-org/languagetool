/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Daniel Naber (http://www.danielnaber.de)
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

package de.danielnaber.languagetool.rules;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;

/** Rule that finds unpaired quotes, brackets etc. 
 * @author Marcin Miłkowski
 * **/
public class UnpairedQuotesBracketsRule extends Rule {

  /**
   * Note that there must be equal length of both arrays, and the sequence of
   * starting symbols must match exactly the sequence of ending symbols.
   */
  private static final String[] START_SYMBOLS = { "[", "(", "{", "\"", "'" };
  private static final String[] END_SYMBOLS = { "]", ")", "}", "\"", "'" };

  private final String[] startSymbols;
  private final String[] endSymbols;

  private static final String[] EN_START_SYMBOLS = { "[", "(", "{", "“", "\"",
  "'" };
  private static final String[] EN_END_SYMBOLS = { "]", ")", "}", "”", "\"",
  "'" };

  private static final String[] PL_START_SYMBOLS = { "[", "(", "{", "„", "»",
  "\"" };
  private static final String[] PL_END_SYMBOLS = { "]", ")", "}", "”", "«",
  "\"" };
  
  private static final String[] SK_START_SYMBOLS = { "[", "(", "{", "„", "»",
  "\"" };
  private static final String[] SK_END_SYMBOLS = { "]", ")", "}", "“", "«",
  "\"" };

  private static final String[] FR_START_SYMBOLS = { "[", "(", "{", "»", "‘" };
  private static final String[] FR_END_SYMBOLS = { "]", ")", "}", "«", "’" };

  private static final String[] DE_START_SYMBOLS = { "[", "(", "{", "„", "»",
  "‘" };
  private static final String[] DE_END_SYMBOLS = { "]", ")", "}", "“", "«", "’" };

  private static final String[] ES_START_SYMBOLS = { "[", "(", "{", "“", "«",
    "¿", "¡" };
  private static final String[] ES_END_SYMBOLS = { "]", ")", "}", "”", "»",
    "?", "!" };

  private static final String[] UK_START_SYMBOLS = { "[", "(", "{", "„", "«" };
  private static final String[] UK_END_SYMBOLS = { "]", ")", "}", "“", "»" };

  private static final String[] RU_START_SYMBOLS = { "[", "(", "{", "„", "«",
    "\"", "'" };
  private static final String[] RU_END_SYMBOLS = { "]", ")", "}", "“", "»",
    "\"", "'" };

  private static final String[] NL_START_SYMBOLS = { "[", "(", "{", "„", "“",
  "‘" };
  private static final String[] NL_END_SYMBOLS = { "]", ")", "}", "”", "”", "’" };

  private static final String[] IT_START_SYMBOLS = { "[", "(", "{", "»", "‘" };
  private static final String[] IT_END_SYMBOLS = { "]", ")", "}", "«", "’" };

  /**
   * The counter used for pairing symbols.
   */
  private int[] symbolCounter;

  private int[] ruleMatchArray;

  private boolean reachedEndOfParagraph;

  private final Language ruleLang;

  private static final Pattern PUNCTUATION = Pattern.compile("\\p{Punct}");
  private static final Pattern PUNCTUATION_NO_DOT = Pattern
  .compile("\\p{Punct}(?<!\\.)");
  private static final Pattern NUMBER = Pattern.compile("\\d+");
  private static final Pattern NUMERALS = Pattern
  .compile("(?i)\\d{1,2}?[a-z']*|M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");

  public UnpairedQuotesBracketsRule(final ResourceBundle messages,
      final Language language) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));

    setParagraphBackTrack(true);
    if (language.equals(Language.POLISH)) {
      startSymbols = PL_START_SYMBOLS;
      endSymbols = PL_END_SYMBOLS;
    } else if (language.equals(Language.SLOVAK)) {
      startSymbols = SK_START_SYMBOLS;
      endSymbols = SK_END_SYMBOLS;
    } else if (language.equals(Language.FRENCH)) {
      startSymbols = FR_START_SYMBOLS;
      endSymbols = FR_END_SYMBOLS;
    } else if (language.equals(Language.ENGLISH)) {
      startSymbols = EN_START_SYMBOLS;
      endSymbols = EN_END_SYMBOLS;
    } else if (language.equals(Language.GERMAN)) {
      startSymbols = DE_START_SYMBOLS;
      endSymbols = DE_END_SYMBOLS;
    } else if (language.equals(Language.DUTCH)) {
      startSymbols = NL_START_SYMBOLS;
      endSymbols = NL_END_SYMBOLS;
    } else if (language.equals(Language.SPANISH)) {
      startSymbols = ES_START_SYMBOLS;
      endSymbols = ES_END_SYMBOLS;
    } else if (language.equals(Language.UKRAINIAN)) {
      startSymbols = UK_START_SYMBOLS;
      endSymbols = UK_END_SYMBOLS;
    } else if (language.equals(Language.RUSSIAN)) {
      startSymbols = RU_START_SYMBOLS;
      endSymbols = RU_END_SYMBOLS;
    } else if (language.equals(Language.ITALIAN)) {
      startSymbols = IT_START_SYMBOLS;
      endSymbols = IT_END_SYMBOLS;
    } else {
      startSymbols = START_SYMBOLS;
      endSymbols = END_SYMBOLS;
    }

    symbolCounter = new int[startSymbols.length];
    ruleMatchArray = new int[startSymbols.length];

    for (int i = 0; i < startSymbols.length; i++) {
      symbolCounter[i] = 0;
      ruleMatchArray[i] = 0;
    }
    ruleLang = language;
  }

  public final String getId() {
    return "UNPAIRED_BRACKETS";
  }

  public final String getDescription() {
    return messages.getString("desc_unpaired_brackets");
  }

  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();

    if (reachedEndOfParagraph) {
      reset();
    }

    int ruleMatchIndex = getMatchesIndex();

    int pos = 0;
    for (int j = 0; j < startSymbols.length; j++) {
      for (int i = 1; i < tokens.length; i++) {
        final String token = tokens[i].getToken().trim();
        boolean precededByWhitespace = true;
        if (startSymbols[j].equals(endSymbols[j])) {
          precededByWhitespace = tokens[i].isWhitespaceBefore()
          || PUNCTUATION_NO_DOT.matcher(tokens[i - 1].getToken()).matches();
        }

        boolean followedByWhitespace = true;
        if (i < tokens.length - 1 && startSymbols[j].equals(endSymbols[j])) {
          followedByWhitespace = tokens[i + 1].isWhitespace()
          || PUNCTUATION.matcher(tokens[i + 1].getToken()).matches();
        }

        if (followedByWhitespace && precededByWhitespace) {
          if (i == tokens.length) {
            precededByWhitespace = false;
          } else if (startSymbols[j].equals(endSymbols[j])) {
            if (symbolCounter[j] > 0) {
              precededByWhitespace = false;
            } else {
              followedByWhitespace = false;
            }
          }
        }

        boolean noException = true;

        if (ruleLang.equals(Language.ENGLISH) && i > 1) {

          // exception for English inches, e.g., 20"
          if ((precededByWhitespace || followedByWhitespace)
              && "\"".equals(token)
              && NUMBER.matcher(tokens[i - 1].getToken()).matches()) {
            noException = false;
          }

          // Exception for English plural saxon genetive
          if ((precededByWhitespace || followedByWhitespace)
              && "'".equals(token)
              && noException
              && (tokens[i - 1].getToken().charAt(
                  tokens[i - 1].getToken().length() - 1) == 's')
                  && (tokens[i - 1].hasPosTag("NNS") || tokens[i - 1].hasPosTag("NNPS"))) {
            noException = false;
          }
        }

        if (noException && precededByWhitespace
            && token.equals(startSymbols[j])) {
          symbolCounter[j]++;
          pos = i;
        } else if (noException && followedByWhitespace
            && token.equals(endSymbols[j])) {
          if (i > 1 && endSymbols[j].equals(")") && symbolCounter[j] == 0) {
            // exception for bullets: 1), 2), 3)...,
            // II), 2') and 1a).
            if (!NUMERALS.matcher(tokens[i - 1].getToken()).matches()) {
              symbolCounter[j]--;
              pos = i;
            }
          } else {
            symbolCounter[j]--;
            pos = i;
          }
        }
      }

      for (int i = 0; i < symbolCounter.length; i++) {
        if (symbolCounter[i] != 0) {
          if (ruleMatchArray[i] != 0 && isInMatches(ruleMatchArray[i] - 1)) {
            setAsDeleted(ruleMatchArray[i] - 1);
            ruleMatchArray[i] = 0;
          } else {
            ruleMatchIndex++;
            ruleMatchArray[i] = ruleMatchIndex;
            final int startPos = tokens[pos].getStartPos();
            final RuleMatch ruleMatch = new RuleMatch(this, startPos, 
                startPos + 1, messages.getString("unpaired_brackets"));
            ruleMatches.add(ruleMatch);
          }

         symbolCounter[i] = 0;

        }
      }
    }

    if (tokens[tokens.length - 1].isParaEnd()) {
      reachedEndOfParagraph = true;
    }

    return toRuleMatchArray(ruleMatches);
  }

  /**
   * Reset the state information for the rule, including paragraph-level
   * information.
   */
  public final void reset() {
    for (int i = 0; i < symbolCounter.length; i++) {
      symbolCounter[i] = 0;
      ruleMatchArray[i] = 0;
    }
    if (!reachedEndOfParagraph) {
      clearMatches();
    }
    reachedEndOfParagraph = false;
  }

}
