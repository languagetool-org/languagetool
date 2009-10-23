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
import de.danielnaber.languagetool.tools.UnsyncStack;

/**
 * Rule that finds unpaired quotes, brackets etc.
 * 
 * @author Marcin Miłkowski
 */
public class UnpairedQuotesBracketsRule extends Rule {

  /**
   * Note that there must be equal length of both arrays, and the sequence of
   * starting symbols must match exactly the sequence of ending symbols.
   */
  private static final String[] START_SYMBOLS = { "[", "(", "{", "\"", "'" };
  private static final String[] END_SYMBOLS   = { "]", ")", "}", "\"", "'" };

  private final String[] startSymbols;
  private final String[] endSymbols;

  private static final String[] EN_START_SYMBOLS = { "[", "(", "{", "“", "\"", "'" };
  private static final String[] EN_END_SYMBOLS   = { "]", ")", "}", "”", "\"", "'" };

  private static final String[] PL_START_SYMBOLS = { "[", "(", "{", "„", "»", "\"" };
  private static final String[] PL_END_SYMBOLS   = { "]", ")", "}", "”", "«", "\"" };

  private static final String[] SK_START_SYMBOLS = { "[", "(", "{", "„", "»", "\"" };
  private static final String[] SK_END_SYMBOLS   = { "]", ")", "}", "“", "«", "\"" };

  private static final String[] RO_START_SYMBOLS = { "[", "(", "{", "„", "«" };
  private static final String[] RO_END_SYMBOLS   = { "]", ")", "}", "”", "»" };

  private static final String[] FR_START_SYMBOLS = { "[", "(", "{", "«", /*"‘"*/ };
  private static final String[] FR_END_SYMBOLS   = { "]", ")", "}", "»", /*"’" used in "d’arm" and many other words */ };

  private static final String[] DE_START_SYMBOLS = { "[", "(", "{", "„", "»", "‘" };
  private static final String[] DE_END_SYMBOLS   = { "]", ")", "}", "“", "«", "’" };

  private static final String[] GL_START_SYMBOLS = { "[", "(", "{", "“", "«", "‘", "\"", "'" };
  private static final String[] GL_END_SYMBOLS   = { "]", ")", "}", "”", "»", "’", "\"", "'" };

  private static final String[] ES_START_SYMBOLS = { "[", "(", "{", "“", "«", "¿", "¡" };
  private static final String[] ES_END_SYMBOLS   = { "]", ")", "}", "”", "»", "?", "!" };

  private static final String[] UK_START_SYMBOLS = { "[", "(", "{", "„", "«" };
  private static final String[] UK_END_SYMBOLS   = { "]", ")", "}", "“", "»" };

  private static final String[] RU_START_SYMBOLS = { "[", "(", "{", "„", "«", "\"", "'" };
  private static final String[] RU_END_SYMBOLS   = { "]", ")", "}", "“", "»", "\"", "'" };

  private static final String[] NL_START_SYMBOLS = { "[", "(", "{", "„", "“", "‘" };
  private static final String[] NL_END_SYMBOLS   = { "]", ")", "}", "”", "”", "’" };

  private static final String[] IT_START_SYMBOLS = { "[", "(", "{", "»", /*"‘"*/ };
  private static final String[] IT_END_SYMBOLS   = { "]", ")", "}", "«", /*"’"*/ };

  private static final String[] DK_START_SYMBOLS = { "[", "(", "{", "\"", "”" };
  private static final String[] DK_END_SYMBOLS   = { "]", ")", "}", "\"", "”" };

  /**
   * The stack for pairing symbols.
   */
  private final UnsyncStack<SymbolLocator> symbolStack = new UnsyncStack<SymbolLocator>();

  /**
   * Stack of rule matches.
   */
  private final UnsyncStack<RuleMatchLocator> ruleMatchStack = new UnsyncStack<RuleMatchLocator>();

  private boolean endOfParagraph;

  private final Language ruleLang;

  private static final Pattern PUNCTUATION = Pattern.compile("\\p{Punct}");
  private static final Pattern PUNCTUATION_NO_DOT = Pattern
      .compile("[\\p{Punct}&&[^\\.]]");
  private static final Pattern NUMBER = Pattern.compile("\\d+");
  private static final Pattern NUMERALS = Pattern
      .compile("(?i)\\d{1,2}?[a-z']*|M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");
  private int ruleMatchIndex;
  private List<RuleMatch> ruleMatches;

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
    } else if (language.equals(Language.GALICIAN)) {
      startSymbols = GL_START_SYMBOLS;
      endSymbols = GL_END_SYMBOLS;
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
    } else if (language.equals(Language.ROMANIAN)) {
      startSymbols = RO_START_SYMBOLS;
      endSymbols = RO_END_SYMBOLS;
    } else if (language.equals(Language.DANISH)) {
      startSymbols = DK_START_SYMBOLS;
      endSymbols = DK_END_SYMBOLS;
    } else {
      startSymbols = START_SYMBOLS;
      endSymbols = END_SYMBOLS;
    }

    ruleLang = language;
  }

  public final String getId() {
    return "UNPAIRED_BRACKETS";
  }

  public final String getDescription() {
    return messages.getString("desc_unpaired_brackets");
  }

// TODO: make this a generic rule, and extend for every language
//find a way to easily specify exceptions (abstract method similar
//  to isEnglishException?)
  public final RuleMatch[] match(final AnalyzedSentence text) {
    ruleMatches = new ArrayList<RuleMatch>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();

    if (endOfParagraph) {
      reset();
    }

    ruleMatchIndex = getMatchesIndex();

    for (int i = 1; i < tokens.length; i++) {
      for (int j = 0; j < startSymbols.length; j++) {

        final String token = tokens[i].getToken();
        if (token.equals(startSymbols[j]) || token.equals(endSymbols[j])) {
          boolean precededByWhitespace = true;
          if (startSymbols[j].equals(endSymbols[j])) {
            precededByWhitespace = tokens[i - 1].isSentStart()
                || tokens[i].isWhitespaceBefore()
                || PUNCTUATION_NO_DOT.matcher(tokens[i - 1].getToken())
                    .matches();
          }

          boolean followedByWhitespace = true;
          if (i < tokens.length - 1 && startSymbols[j].equals(endSymbols[j])) {
            followedByWhitespace = tokens[i + 1].isWhitespaceBefore()
                || PUNCTUATION.matcher(tokens[i + 1].getToken()).matches();
          }         

          boolean noException = true;
          if (ruleLang.equals(Language.ENGLISH) && i > 1) {
            noException = isEnglishException(token, tokens, i,
                precededByWhitespace, followedByWhitespace);
          }

          if (noException && precededByWhitespace
              && token.equals(startSymbols[j])) {
            symbolStack.push(new SymbolLocator(startSymbols[j], i));
          } else if (noException && followedByWhitespace
              && token.equals(endSymbols[j])) {
            if (i > 1 && endSymbols[j].equals(")")) {
              // exception for bullets: 1), 2), 3)...,
              // II), 2') and 1a).
              if ((NUMERALS.matcher(tokens[i - 1].getToken()).matches() && !(!symbolStack
                  .empty() && "(".equals(symbolStack.peek().symbol)))) {
                noException = false;
              }
            }

            if (noException)
              if (symbolStack.isEmpty()) {
                symbolStack.push(new SymbolLocator(endSymbols[j], i));
              } else {
                if (symbolStack.peek().symbol.equals(startSymbols[j])) {
                  symbolStack.pop();
                } else {
                  symbolStack.push(new SymbolLocator(endSymbols[j], i));
                }                
              }
          }
        }
      }
    }
    for (final SymbolLocator sLoc : symbolStack) {
      final RuleMatch rMatch = createMatch(tokens[sLoc.index].getStartPos(),
          sLoc.symbol);
      if (rMatch != null) {
        ruleMatches.add(rMatch);
      }
    }
    symbolStack.clear();
    if (tokens[tokens.length - 1].isParaEnd()) {
      endOfParagraph = true;
    }

    return toRuleMatchArray(ruleMatches);
  }

  private boolean isEnglishException(final String token,
      final AnalyzedTokenReadings[] tokens, final int i, final boolean precSpace,
      final boolean follSpace) {  
//TODO: add an', o', 'till, 'tain't, 'cept, 'fore in the disambiguator
//and mark up as contractions somehow
// add exception for dates like '52    
    
    if (!precSpace && follSpace) {
      // exception for English inches, e.g., 20"
      if ("\"".equals(token)
          && NUMBER.matcher(tokens[i - 1].getToken()).matches()) {
        return false;
      }
      // Exception for English plural Saxon genetive
      // current disambiguation scheme is a bit too greedy
      // for adjectives
      if ("'".equals(token) && tokens[i].hasPosTag("POS")) {
        return false;
      }
      // puttin' on the Ritz
      if ("'".equals(token) && tokens[i - 1].hasPosTag("VBG")
          && tokens[i - 1].getToken().endsWith("in")) {
        return false;
      }
    }
    if (precSpace && !follSpace) {
      // hold 'em!
      if ("'".equals(token) && i + 1 < tokens.length
          && "em".equals(tokens[i + 1].getToken())) {
        return false;
      }
    }
    return true;
  }

  private RuleMatch createMatch(final int startPos, final String symbol) {
    if (!ruleMatchStack.empty()) {
      final int index = findSymbolNum(symbol);
      if (index >= 0) {
        final RuleMatchLocator rLoc = ruleMatchStack.peek();
        if (rLoc.symbol.equals(startSymbols[index])) {
          if (ruleMatches.size() > rLoc.myIndex) {
            ruleMatches.remove(rLoc.myIndex);
            ruleMatchStack.pop();
            return null;
            // if (ruleMatches.get(rLoc.myIndex).getFromPos())
          }
          if (isInMatches(rLoc.index)) {
            setAsDeleted(rLoc.index);
            ruleMatchStack.pop();
            return null;
          }
        }
      }
    }
    ruleMatchStack.push(new RuleMatchLocator(symbol, ruleMatchIndex,
        ruleMatches.size()));
    ruleMatchIndex++;
    return new RuleMatch(this, startPos, startPos + symbol.length(), messages
        .getString("unpaired_brackets"));
  }

  private int findSymbolNum(final String ch) {
    for (int i = 0; i < endSymbols.length; i++) {
      if (ch.equals(endSymbols[i])) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Reset the state information for the rule, including paragraph-level
   * information.
   */
  public final void reset() {
    ruleMatchStack.clear();
    symbolStack.clear();
    if (!endOfParagraph) {
      clearMatches();
    }
    endOfParagraph = false;
  }

}

class SymbolLocator {
  public String symbol;
  public int index;

  SymbolLocator(final String sym, final int ind) {
    symbol = sym;
    index = ind;
  }
}

class RuleMatchLocator extends SymbolLocator {
  public int myIndex;

  RuleMatchLocator(final String sym, final int ind, final int myInd) {
    super(sym, ind);
    myIndex = myInd;
  }
}
