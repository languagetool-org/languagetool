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
import java.util.Map;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.AnalyzedTokenReadings;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.tools.UnsyncStack;
import de.danielnaber.languagetool.tools.SymbolLocator;

/**
 * Rule that finds unpaired quotes, brackets etc.
 * 
 * @author Marcin Miłkowski
 */
public class GenericUnpairedBracketsRule extends Rule {

  protected String[] startSymbols;
  protected String[] endSymbols;
  
  /**
   * The stack for pairing symbols.
   */
  protected final UnsyncStack<SymbolLocator> symbolStack = new UnsyncStack<SymbolLocator>();

  /**
   * Stack of rule matches.
   */
  private final UnsyncStack<RuleMatchLocator> ruleMatchStack = new UnsyncStack<RuleMatchLocator>();

  private boolean endOfParagraph;

  private static final Pattern PUNCTUATION = Pattern.compile("\\p{Punct}");
  private static final Pattern PUNCTUATION_NO_DOT = Pattern
      .compile("[\\p{Punct}&&[^\\.]]");
  
  public static  Pattern NUMERALS;
  
  private static final Pattern NUMERALS_EN = Pattern
      .compile("(?i)\\d{1,2}?[a-z']*|M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");
  
  private int ruleMatchIndex;
  private List<RuleMatch> ruleMatches;
  
  private Map<String,Boolean> uniqueMap;

  public GenericUnpairedBracketsRule(final ResourceBundle messages,
      final Language language) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_misc")));

    setParagraphBackTrack(true);
    startSymbols = language.getUnpairedRuleStartSymbols();
    endSymbols = language.getUnpairedRuleEndSymbols();
    
    NUMERALS=NUMERALS_EN;
    uniqueMapInit();
  }

  
  @Override
  public String getId() {
    return "UNPAIRED_BRACKETS";
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_unpaired_brackets");
  }

  public void uniqueMapInit() {
    uniqueMap = new HashMap<String, Boolean>();
    for (String endSymbol : endSymbols) {
      int found = 0;
      for (String endSymbol1 : endSymbols) {
        if (endSymbol1.equals(endSymbol)) {
            found++;
        }
      }
      uniqueMap.put(endSymbol, found == 1);
    }
  }
  
  /**
   * Generic method to specify an exception. For unspecified
   * language, it simply returns true, which means no exception.
   * @param token
   *        String token
   * @param tokens
   *        Sentence tokens
   * @param i
   *        Current token index
   * @param precSpace
   *        boolean: is preceded with space
   * @param follSpace
   *        boolean: is followed with space
   * @return
   */
  protected boolean isNoException(final String token,
      final AnalyzedTokenReadings[] tokens, final int i, final int j,
      final boolean precSpace,
      final boolean follSpace) {
    return true;
  }

  @Override
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

          final boolean noException = isNoException(token, tokens, i, j,
                precededByWhitespace, followedByWhitespace);          

          if (noException && precededByWhitespace
              && token.equals(startSymbols[j])) {
            symbolStack.push(new SymbolLocator(startSymbols[j], i));
            break;
          } else if (noException && followedByWhitespace
              && token.equals(endSymbols[j])) {            
            if (i > 1 && endSymbols[j].equals(")") 
                && (NUMERALS.matcher(tokens[i - 1].getToken()).matches() 
                && !(!symbolStack.empty() 
               && "(".equals(symbolStack.peek().symbol)))) {        
            } else {
              if (symbolStack.empty()) {
                symbolStack.push(new SymbolLocator(endSymbols[j], i));
                break;
              } else {
                if (symbolStack.peek().symbol.equals(startSymbols[j])) {
                  symbolStack.pop();
                  break;
                } else {
                  if (isEndSymbolUnique(endSymbols[j])) {
                    symbolStack.push(new SymbolLocator(endSymbols[j], i));
                    break;
                  } else {
                    if (j == endSymbols.length - 1) {
                    symbolStack.push(new SymbolLocator(endSymbols[j], i));
                    break;
                    }
                  }
                }
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

  private boolean isEndSymbolUnique(final String str) {
    return uniqueMap.get(str);
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
  @Override
  public final void reset() {
    ruleMatchStack.clear();
    symbolStack.clear();
    if (!endOfParagraph) {
      clearMatches();
    }
    endOfParagraph = false;
  }

}

class RuleMatchLocator extends SymbolLocator {
  public int myIndex;

  RuleMatchLocator(final String symbol, final int index, final int myIndex) {
    super(symbol, index);
    this.myIndex = myIndex;
  }
}
