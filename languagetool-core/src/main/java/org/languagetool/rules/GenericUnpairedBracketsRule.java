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

package org.languagetool.rules;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

/**
 * Rule that finds unpaired quotes, brackets etc.
 * 
 * @author Marcin Miłkowski
 */
public class GenericUnpairedBracketsRule extends TextLevelRule {

  private static final Pattern NUMERALS_EN =
          Pattern.compile("(?i)\\d{1,2}?[a-z']*|M*(D?C{0,3}|C[DM])(L?X{0,3}|X[LC])(V?I{0,3}|I[VX])$");
  private static final Pattern PUNCTUATION = Pattern.compile("[\\p{Punct}…–—]");
  private static final Pattern PUNCTUATION_NO_DOT =
          Pattern.compile("[ldmnstLDMNST]'|[–—\\p{Punct}&&[^\\.]]");
  // "[ldmnst]'" allows dealing with apostrophed words in Catalan (i.e. l'«home) 

  private final String[] startSymbols;
  private final String[] endSymbols;
  private final Map<String,Boolean> uniqueMap;
  private final String ruleId;
  private final Pattern numerals;

  public GenericUnpairedBracketsRule(String ruleId, ResourceBundle messages, List<String> startSymbols, List<String> endSymbols) {
    this(ruleId, messages, startSymbols, endSymbols, NUMERALS_EN);
  }

  /**
   * @since 3.7
   */
  public GenericUnpairedBracketsRule(String ruleId, ResourceBundle messages, List<String> startSymbols, List<String> endSymbols, Pattern numerals) {
    super(messages);
    this.ruleId = ruleId != null ? ruleId : "UNPAIRED_BRACKETS";
    super.setCategory(Categories.PUNCTUATION.getCategory(messages));
    if (startSymbols.size() != endSymbols.size()) {
      throw new IllegalArgumentException("Different number of start and end symbols: " + startSymbols + " vs. " + endSymbols);
    }
    this.startSymbols = startSymbols.toArray(new String[0]);
    this.endSymbols = endSymbols.toArray(new String[0]);
    this.numerals = Objects.requireNonNull(numerals);
    this.uniqueMap = uniqueMapInit();
    setLocQualityIssueType(ITSIssueType.Typographical);
  }

  /**
   * @param startSymbols start symbols like "(" - note that the array must be of equal length as the next parameter
   *                     and the sequence of starting symbols must match exactly the sequence of ending symbols.
   * @param endSymbols end symbols like ")"
   */
  public GenericUnpairedBracketsRule(ResourceBundle messages, List<String> startSymbols, List<String> endSymbols) {
    this(null, messages, startSymbols, endSymbols);
  }

  /**
   * @since 3.7
   */
  public GenericUnpairedBracketsRule(ResourceBundle messages, List<String> startSymbols, List<String> endSymbols, Pattern numerals) {
    this(null, messages, startSymbols, endSymbols, numerals);
  }

  /**
   * Construct rule with a set of default start and end symbols: <code>[] () {} "" ''</code>
   */
  public GenericUnpairedBracketsRule(ResourceBundle messages) {
    this(null, messages, Arrays.asList("[", "(", "{", "\"", "'"), Arrays.asList("]", ")", "}", "\"", "'"));
  }

  @Override
  public String getId() {
    return ruleId;
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_unpaired_brackets");
  }

  /**
   * Generic method to specify an exception. For unspecified
   * language, it simply returns true (which means no exception) unless
   * there's a common smiley like :-) or ;-).
   * @param token String token
   * @param tokens Sentence tokens
   * @param i Current token index
   * @param precSpace is preceded with space
   * @param follSpace is followed with space
   */
  protected boolean isNoException(String token,
                                  AnalyzedTokenReadings[] tokens, int i, int j,
                                  boolean precSpace,
                                  boolean follSpace, UnsyncStack<SymbolLocator> symbolStack) {
    String tokenStr = tokens[i].getToken();
    if (i >= 2) {
      String prevPrevToken = tokens[i - 2].getToken();
      String prevToken = tokens[i - 1].getToken();
      // Smiley ":-)" and ":-("
      if (prevPrevToken.equals(":") && prevToken.equals("-") && (tokenStr.equals(")") || tokenStr.equals("("))) {
        return false;
      }
      // Smiley ";-)" and ";-("
      if (prevPrevToken.equals(";") && prevToken.equals("-") && (tokenStr.equals(")") || tokenStr.equals("("))) {
        return false;
      }
    }
    if (i >= 1) {
      String prevToken = tokens[i - 1].getToken();
      // Smiley ":)" and  ":("
      if (prevToken.equals(":") && !tokens[i].isWhitespaceBefore() && (tokenStr.equals(")") || tokenStr.equals("("))) {
        return false;
      }
      // Smiley ";)" and  ";("
      if (prevToken.equals(";") && !tokens[i].isWhitespaceBefore() && (tokenStr.equals(")") || tokenStr.equals("("))) {
        return false;
      }
    }
    return true;
  }

  @Override
  public final RuleMatch[] match(List<AnalyzedSentence> sentences) {
    UnsyncStack<SymbolLocator> symbolStack = new UnsyncStack<>();   // the stack for pairing symbols
    UnsyncStack<SymbolLocator> ruleMatchStack = new UnsyncStack<>();
    List<RuleMatch> ruleMatches = new ArrayList<>();
    int startPosBase = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int i = 1; i < tokens.length; i++) {
        for (int j = 0; j < startSymbols.length; j++) {
          if (fillSymbolStack(startPosBase, tokens, i, j, symbolStack, sentence)) {
            break;
          }
        }
      }
      for (AnalyzedTokenReadings readings : sentence.getTokens()) {
        startPosBase += readings.getToken().length();
      }
    }
    for (SymbolLocator sLoc : symbolStack) {
      RuleMatch rMatch = createMatch(ruleMatches, ruleMatchStack, sLoc.getStartPos(), sLoc.getSymbol(), sLoc.getSentence());
      if (rMatch != null) {
        ruleMatches.add(rMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private Map<String, Boolean> uniqueMapInit() {
    Map<String,Boolean> uniqueMap = new HashMap<>();
    for (String endSymbol : endSymbols) {
      int found = 0;
      for (String endSymbol1 : endSymbols) {
        if (endSymbol1.equals(endSymbol)) {
          found++;
        }
      }
      uniqueMap.put(endSymbol, found == 1);
    }
    return Collections.unmodifiableMap(uniqueMap);
  }

  private boolean fillSymbolStack(int startPosBase, AnalyzedTokenReadings[] tokens, int i, int j, UnsyncStack<SymbolLocator> symbolStack, AnalyzedSentence sentence) {
    String token = tokens[i].getToken();
    int startPos = startPosBase + tokens[i].getStartPos();
    if (token.equals(startSymbols[j]) || token.equals(endSymbols[j])) {
      boolean precededByWhitespace = getPrecededByWhitespace(tokens, i, j);
      boolean followedByWhitespace = getFollowedByWhitespace(tokens, i, j);
      boolean noException = isNoException(token, tokens, i, j,
              precededByWhitespace, followedByWhitespace, symbolStack);

      if (noException && precededByWhitespace && token.equals(startSymbols[j])) {
        symbolStack.push(new SymbolLocator(startSymbols[j], i, startPos, sentence));
        return true;
      } else if (noException && (followedByWhitespace || tokens[i].isSentenceEnd())
              && token.equals(endSymbols[j])) {
        if (i > 1 && endSymbols[j].equals(")")
                && (numerals.matcher(tokens[i - 1].getToken()).matches()
                && !(!symbolStack.empty()
                && "(".equals(symbolStack.peek().getSymbol())))) {
        } else {
          if (symbolStack.empty()) {
            symbolStack.push(new SymbolLocator(endSymbols[j], i, startPos, sentence));
            return true;
          } else {
            if (symbolStack.peek().getSymbol().equals(startSymbols[j])) {
              symbolStack.pop();
              return true;
            } else {
              if (isEndSymbolUnique(endSymbols[j])) {
                symbolStack.push(new SymbolLocator(endSymbols[j], i, startPos, sentence));
                return true;
              } else {
                if (j == endSymbols.length - 1) {
                  symbolStack.push(new SymbolLocator(endSymbols[j], i, startPos, sentence));
                  return true;
                }
              }
            }
          }
        }
      }
    }
    return false;
  }

  private boolean getPrecededByWhitespace(AnalyzedTokenReadings[] tokens, int i, int j) {
    boolean precededByWhitespace = true;
    if (startSymbols[j].equals(endSymbols[j])) {
      precededByWhitespace = tokens[i - 1].isSentenceStart()
          || tokens[i].isWhitespaceBefore()
          || PUNCTUATION_NO_DOT.matcher(tokens[i - 1].getToken()).matches()
          || Arrays.asList(startSymbols).contains(tokens[i - 1].getToken());
    }
    return precededByWhitespace;
  }

  private boolean getFollowedByWhitespace(AnalyzedTokenReadings[] tokens, int i, int j) {
    boolean followedByWhitespace = true;
    if (i < tokens.length - 1 && startSymbols[j].equals(endSymbols[j])) {
      followedByWhitespace = tokens[i + 1].isWhitespaceBefore()
              || PUNCTUATION.matcher(tokens[i + 1].getToken()).matches()
              || Arrays.asList(endSymbols).contains(tokens[i + 1].getToken());
    }
    return followedByWhitespace;
  }

  private boolean isEndSymbolUnique(String str) {
    return uniqueMap.get(str);
  }

  @Nullable
  private RuleMatch createMatch(List<RuleMatch> ruleMatches, UnsyncStack<SymbolLocator> ruleMatchStack, int startPos, String symbol, AnalyzedSentence sentence) {
    if (!ruleMatchStack.empty()) {
      int index = findSymbolNum(symbol, endSymbols);
      if (index >= 0) {
        SymbolLocator rLoc = ruleMatchStack.peek();
        if (rLoc.getSymbol().equals(startSymbols[index])) {
          if (ruleMatches.size() > rLoc.getIndex()) {
            ruleMatches.remove(rLoc.getIndex());
            ruleMatchStack.pop();
            return null;
          }
        }
      }
    }
    ruleMatchStack.push(new SymbolLocator(symbol, ruleMatches.size(), startPos, sentence));
    String otherSymbol = findCorrespondingSymbol(symbol);
    String message = MessageFormat.format(messages.getString("unpaired_brackets"), otherSymbol);
    return new RuleMatch(this, sentence, startPos, startPos + symbol.length(), message);
  }

  private int findSymbolNum(String ch, String[] symbols) {
    for (int i = 0; i < symbols.length; i++) {
      if (ch.equals(symbols[i])) {
        return i;
      }
    }
    return -1;
  }

  private String findCorrespondingSymbol(String symbol) {
    int idx1 = findSymbolNum(symbol, startSymbols);
    if (idx1 >= 0) {
      return endSymbols[idx1];
    } else {
      int idx2 = findSymbolNum(symbol, endSymbols);
      return startSymbols[idx2];
    }
  }

}
