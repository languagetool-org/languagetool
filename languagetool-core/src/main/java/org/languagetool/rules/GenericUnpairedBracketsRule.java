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

import com.google.common.base.Suppliers;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Pattern;

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
          Pattern.compile("[ldmnstLDMNST]'|[–—\\p{Punct}&&[^.]]");
  // "[ldmnst]'" allows dealing with apostrophed words in Catalan (i.e. l'«home) 

  private final List<String> startSymbols;
  private final List<String> endSymbols;
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
    this.startSymbols = startSymbols;
    this.endSymbols = endSymbols;
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
    if (i > 0 && tokens[i-1].getToken().matches("https?://.+") && tokens[i-1].getToken().contains("(")) {
      return false;
    }
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
    int sentenceIdx = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int i = 1; i < tokens.length; i++) {
        for (int j = 0; j < startSymbols.size(); j++) {
          if (fillSymbolStack(startPosBase, tokens, i, j, symbolStack, sentence, sentenceIdx)) {
            break;
          }
        }
      }
      startPosBase += sentence.getCorrectedTextLength();
      sentenceIdx++;
    }
    boolean isSymmetric = false;
    //if the stack is odd and symmetric match only the symbol in the middle, e. g. ({"})
    int ssSize = symbolStack.size();
    if (ssSize > 2 && ssSize % 2 == 1) {
      isSymmetric = true;
      for (int i = 0; i < ssSize / 2; i++) {
        if (startSymbols.indexOf(symbolStack.get(i).getSymbol().symbol) !=
            endSymbols.indexOf(symbolStack.get(ssSize - 1).getSymbol().symbol)) {
          isSymmetric = false;
          break;
        }
      }
    }
    Supplier<String> lazyFullText = Suppliers.memoize(() -> {
      StringBuilder fullText = new StringBuilder();
      for (AnalyzedSentence aSentence : sentences) {
        fullText.append(aSentence.getText());
      }
      return fullText.toString();
    });
    if (isSymmetric) {
      SymbolLocator loc = symbolStack.get(ssSize / 2);
      int sentenceIndex = loc.getSentenceIndex();
      RuleMatch rMatch = createMatch(ruleMatches, ruleMatchStack, loc.getStartPos(),
              loc.getSymbol(), loc.getSentence(), sentenceIndex, lazyFullText);
      if (rMatch != null) {
        ruleMatches.add(rMatch);
      }
    } else {
      for (SymbolLocator sLoc : symbolStack) {
        RuleMatch rMatch = createMatch(ruleMatches, ruleMatchStack, sLoc.getStartPos(), sLoc.getSymbol(),
            sLoc.getSentence(), sLoc.getSentenceIndex(), lazyFullText);
        if (rMatch != null && (sLoc.getSymbol().symbolType == GenericUnpairedBracketsRule.Symbol.Type.Closing ||
                endsLikeRealSentence(sLoc.getSentence().getText()) || sentences.size()-1 > sLoc.getSentenceIndex())) {
          ruleMatches.add(rMatch);
        }
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private boolean endsLikeRealSentence(String s) {
    return s.endsWith(".") || s.endsWith("?") || s.endsWith("!");
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

  private boolean fillSymbolStack(int startPosBase, AnalyzedTokenReadings[] tokens, int i, int j, UnsyncStack<SymbolLocator> symbolStack, AnalyzedSentence sentence, int sentenceIdx) {
    String token = tokens[i].getToken();
    int startPos = startPosBase + tokens[i].getStartPos();
    if (token.equals(startSymbols.get(j)) || token.equals(endSymbols.get(j))) {
      boolean precededByWhitespace = getPrecededByWhitespace(tokens, i, j);
      boolean isSpecialCase = getSpecialCase(tokens, i, j);
      boolean noException = isNoException(token, tokens, i, j,
              precededByWhitespace, isSpecialCase, symbolStack);

      if (noException && precededByWhitespace && token.equals(startSymbols.get(j))) {
        symbolStack.push(new SymbolLocator(new Symbol(startSymbols.get(j), Symbol.Type.Opening), i, startPos, sentence, sentenceIdx));
        return true;
      } else if (noException && (isSpecialCase || tokens[i].isSentenceEnd())
              && token.equals(endSymbols.get(j))) {
        if (i > 1 && endSymbols.get(j).equals(")")
                && (numerals.matcher(tokens[i - 1].getToken()).matches()
                && !(!symbolStack.empty()
                && "(".equals(symbolStack.peek().getSymbol().symbol)))) {
        } else {
          if (symbolStack.empty()) {
            symbolStack.push(new SymbolLocator(new Symbol(endSymbols.get(j), Symbol.Type.Closing), i, startPos, sentence, sentenceIdx));
            return true;
          } else {
            if (symbolStack.peek().getSymbol().symbol.equals(startSymbols.get(j))) {
              symbolStack.pop();
              return true;
            } else {
              if (isEndSymbolUnique(endSymbols.get(j))) {
                symbolStack.push(new SymbolLocator(new Symbol(endSymbols.get(j), Symbol.Type.Closing), i, startPos, sentence, sentenceIdx));
                return true;
              } else {
                if (j == endSymbols.size() - 1) {
                  symbolStack.push(new SymbolLocator(new Symbol(endSymbols.get(j), Symbol.Type.Closing), i, startPos, sentence, sentenceIdx));
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
    if (startSymbols.get(j).equals(endSymbols.get(j))) {
      precededByWhitespace = tokens[i - 1].isSentenceStart()
          || tokens[i].isWhitespaceBefore()
          || PUNCTUATION_NO_DOT.matcher(tokens[i - 1].getToken()).matches()
          || startSymbols.contains(tokens[i - 1].getToken());
    }
    return precededByWhitespace;
  }

  private boolean getSpecialCase(AnalyzedTokenReadings[] tokens, int i, int j) {
    boolean isException = true;
    if (i < tokens.length - 1 && startSymbols.get(j).equals(endSymbols.get(j))) {
      isException = tokens[i + 1].isWhitespaceBefore()
              || PUNCTUATION.matcher(tokens[i + 1].getToken()).matches()
              || endSymbols.contains(tokens[i + 1].getToken())
              || (i >= 1 && tokens[i - 1].getToken().endsWith("-")) // e.g. >>xxx-"yyy yyy"-zzz<<
              || tokens[i + 1].getToken().startsWith("-") // e.g. >>"Go"-button<<
              || "s".equals(tokens[i + 1].getToken());// e.g. >>"I"s<< has and needs no space
    }
    return isException;
  }

  private boolean isEndSymbolUnique(String str) {
    return uniqueMap.get(str);
  }

  @Nullable
  private RuleMatch createMatch(List<RuleMatch> ruleMatches, UnsyncStack<SymbolLocator> ruleMatchStack, int startPos, Symbol symbol, AnalyzedSentence sentence, int sentenceIdx, Supplier<String> lazyFullText) {
    if (!ruleMatchStack.empty()) {
      int index = endSymbols.indexOf(symbol.symbol);
      if (index >= 0) {
        SymbolLocator rLoc = ruleMatchStack.peek();
        if (rLoc.getSymbol().symbol.equals(startSymbols.get(index))) {
          if (ruleMatches.size() > rLoc.getIndex()) {
            ruleMatches.remove(rLoc.getIndex());
            ruleMatchStack.pop();
            return null;
          }
        }
      }
    }
    ruleMatchStack.push(new SymbolLocator(symbol, ruleMatches.size(), startPos, sentence, sentenceIdx));
    String otherSymbol = findCorrespondingSymbol(symbol);
    String message = MessageFormat.format(messages.getString("unpaired_brackets"), otherSymbol);
    String fullText = lazyFullText.get();
    if (startPos + symbol.symbol.length() < fullText.length()) {
      if (startPos >= 2 && startPos + symbol.symbol.length() < fullText.length()) {
        String context = fullText.substring(startPos - 2, startPos + symbol.symbol.length());
        if (context.matches("\n[a-zA-Z]\\)")) {  // prevent error for "b) foo item"
          return null;
        }
      } else if (startPos >= 1) {
        String context = fullText.substring(startPos - 1, startPos + symbol.symbol.length());
        if (context.matches("[a-zA-Z]\\)")) {   // prevent error for "a) foo item" at text start
          return null;
        }
      }
    }
    if (preventMatch(sentence)) {
      return null;
    }
    RuleMatch match = new RuleMatch(this, sentence, startPos, startPos + symbol.symbol.length(), message);
    List<String> repl = getSuggestions(lazyFullText, startPos, startPos + symbol.symbol.length());
    if (repl != null) {
      match.setSuggestedReplacements(repl);
    }
    return match;
  }

  protected boolean preventMatch(AnalyzedSentence sentence) {
    return false;
  }

  protected List<String> getSuggestions(Supplier<String> text, int startPos, int endPos) {
    return null;
  }

  private String findCorrespondingSymbol(Symbol symbol) {
    int idx1 = startSymbols.indexOf(symbol.symbol);
    if (idx1 >= 0) {
      return endSymbols.get(idx1);
    } else {
      int idx2 = endSymbols.indexOf(symbol.symbol);
      return startSymbols.get(idx2);
    }
  }

  @Override
  public int minToCheckParagraph() {
    return -1;
  }

  static class Symbol {
    enum Type {Opening, Closing}
    String symbol;
    Type symbolType;

    public Symbol(String symbol, Type symbolType) {
      this.symbol = symbol;
      this.symbolType = symbolType;
    }

    @Override
    public String toString() {
      return symbol;
    }
  }
}
