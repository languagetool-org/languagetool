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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;

import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Rule that finds unpaired quotes
 * 
 * @author Fred Kruse
 * @since 6.4
 */
public class GenericUnpairedQuotesRule extends TextLevelRule {

//  private static final Pattern OPENING_BRACKETS = Pattern.compile("[(\\[{]");
  private static final Pattern POSSIBLE_APOSTROPHE = Pattern.compile("[‘’']");
  private static final Pattern INCH_PATTERN = Pattern.compile(".*\\d\".*", Pattern.DOTALL);
  private static final Pattern PUNCTUATION = Pattern.compile("[\\p{Punct}…–—&&[^\"'_]]");
  private static final Pattern PUNCT_MARKS = Pattern.compile("[\\?\\.!,]");

  private final List<String> startSymbols;
  private final List<String> endSymbols;
  private final String ruleId;
//  private final Pattern numerals;

  public GenericUnpairedQuotesRule(String ruleId, ResourceBundle messages, List<String> startSymbols, List<String> endSymbols) {
    super(messages);
    this.ruleId = ruleId != null ? ruleId : "UNPAIRED_QUOTES";
    super.setCategory(Categories.PUNCTUATION.getCategory(messages));
    if (startSymbols.size() != endSymbols.size()) {
      throw new IllegalArgumentException("Different number of start and end symbols: " + startSymbols + " vs. " + endSymbols);
    }
    this.startSymbols = startSymbols;
    this.endSymbols = endSymbols;
    setLocQualityIssueType(ITSIssueType.Typographical);
  }

  /**
   * @param startSymbols start symbols like "(" - note that the array must be of equal length as the next parameter
   *                     and the sequence of starting symbols must match exactly the sequence of ending symbols.
   * @param endSymbols end symbols like ")"
   */
  public GenericUnpairedQuotesRule(ResourceBundle messages, List<String> startSymbols, List<String> endSymbols) {
    this(null, messages, startSymbols, endSymbols);
  }

  /**
   * Construct rule with a set of default start and end symbols: <code>“” "" ‘’ ''</code>
   */
  public GenericUnpairedQuotesRule(ResourceBundle messages) {
    this(null, messages, Arrays.asList("“", "\"", "‘", "'"), Arrays.asList("”", "\"", "’", "'"));
  }

  @Override
  public String getId() {
    return ruleId;
  }

  @Override
  public String getDescription() {
    return messages.getString("desc_unpaired_quotes");
  }

  @Override
  public final RuleMatch[] match(List<AnalyzedSentence> sentences) {
    List<SymbolLocator> openingQuotes = new ArrayList<>();
    List<RuleMatch> ruleMatches = new ArrayList<>();
    String lastApostropheSymbol = null;
    boolean wasInch = false;
    int startPosBase = 0;
    for (AnalyzedSentence sentence : sentences) {
      AnalyzedTokenReadings[] tokens = sentence.getTokensWithoutWhitespace();
      for (int i = 1; i < tokens.length; i++) {
        if (isOpeningQuote(tokens, i)) {
          String symbol = tokens[i].getToken();
          if (!isNotBeginningApostrophe(tokens, i)) {
            lastApostropheSymbol = symbol;
            continue;
          }
          if ("\"".equals(symbol)) {
            wasInch = false;
          }
          if (lastApostropheSymbol != null && lastApostropheSymbol.equals(symbol)) {
            lastApostropheSymbol = null;;
          }
          int index = indexOfOpeningQuote(openingQuotes, symbol);
          if (index >= 0) {
            removeAllOpenInnerQuotes(index - 1, openingQuotes, ruleMatches);
          }
          openingQuotes.add(new SymbolLocator(symbol, tokens[i].getStartPos() + startPosBase, sentence));
        } else if (isClosingQuote(tokens, i, openingQuotes)) {
          String symbol = tokens[i].getToken();
          if (!isNotBeginningApostrophe(tokens, i)) {
            lastApostropheSymbol = symbol;
            continue;
          }
          boolean isInchSymb = "\"".equals(symbol);
          boolean isInch = isInchSymb ? isInchQuote(sentence.getText()) : false;
          String startSymbol = findCorrespondingSymbol(symbol);
          int index = indexOfOpeningQuote(openingQuotes, startSymbol);
          if (index >= 0) {
            removeAllOpenInnerQuotes(index, openingQuotes, ruleMatches);
            openingQuotes.remove(index);
            if (lastApostropheSymbol != null && lastApostropheSymbol.equals(startSymbol)) {
              lastApostropheSymbol = null;;
            }
            if (isInch) {
              wasInch = true;
            }
          } else if (isNotEndingApostrophe(tokens, i)){
            if (!isInch && (!isInchSymb || !wasInch)) {
              if (lastApostropheSymbol == null || !lastApostropheSymbol.equals(symbol)) {
                addMatch(new SymbolLocator(symbol, tokens[i].getStartPos() + startPosBase, sentence), ruleMatches);
              } else {
                lastApostropheSymbol = null;
              }
            } else {
              wasInch = false;
            }
          }
        }
      }
      startPosBase += sentence.getCorrectedTextLength();
    }
    removeAllOpenInnerQuotes(-1, openingQuotes, ruleMatches);
    return toRuleMatchArray(ruleMatches);
  }
  
  private boolean isStartSymbolbefore(AnalyzedTokenReadings[] tokens, int i) {
    for (int j = i - 1; j > 0; j--) {
      if (!tokens[i].getToken().equals(tokens[j].getToken()) && startSymbols.contains(tokens[j].getToken())) {
        if (tokens[j - 1].isSentenceStart() || tokens[j].isWhitespaceBefore()) {
          return true;
        }
      } else {
        return false;
      }
    }
    return true;
  }
  
  private boolean isNotOpenSymbol(int j, List<SymbolLocator> openingQuotes) {
    if (endSymbols.get(j).equals(startSymbols.get(j))) {
      for (SymbolLocator openingQuote : openingQuotes) {
        if (endSymbols.get(j).equals(openingQuote.getSymbol())) {
          return false;
        }
      }
    }
    return true;
  }
  
  private boolean isNotQuote (AnalyzedTokenReadings[] tokens, int i, int j) {
    if ((tokens[i - 1].isSentenceStart() || tokens[i].isWhitespaceBefore())
        && (i >= tokens.length -1 || tokens[i + 1].isWhitespaceBefore())) {
      return true;
    }
    if (endSymbols.get(j).equals(startSymbols.get(j))) {
      if (i < tokens.length - 1 
          && !tokens[i].isWhitespaceBefore()
          && !tokens[i + 1].isWhitespaceBefore()
          && PUNCTUATION.matcher(tokens[i - 1].getToken()).matches()
          && !".".equals(tokens[i + 1].getToken())
          && PUNCTUATION.matcher(tokens[i + 1].getToken()).matches()) {
        return true;
      }
    }
    return false;
  }

  private boolean isOpeningQuote(AnalyzedTokenReadings[] tokens, int i) {
    for (int j = 0; j < startSymbols.size(); j++) {
      if (startSymbols.get(j).equals(tokens[i].getToken())) {
        if (isNotQuote (tokens, i, j)) {
          return false;
        }
        if (endSymbols.contains(startSymbols.get(j))) {
          return (tokens[i - 1].isSentenceStart()
              || tokens[i].isWhitespaceBefore()
              || (i < tokens.length - 1 && !tokens[i + 1].isWhitespaceBefore()
                  && ((!PUNCT_MARKS.matcher(tokens[i + 1].getToken()).matches()
                      && PUNCTUATION.matcher(tokens[i - 1].getToken()).matches())
                      || (tokens[i - 1].getToken().endsWith("-"))))
              || isStartSymbolbefore(tokens, i));
        }
        return true;
      }
    }
    return false;
  }

  private boolean isClosingQuote(AnalyzedTokenReadings[] tokens, int i, List<SymbolLocator> openingQuotes) {
    for (int j = 0; j < endSymbols.size(); j++) {
      if (endSymbols.get(j).equals(tokens[i].getToken())) {
        if (isNotQuote (tokens, i, j) && isNotOpenSymbol(j, openingQuotes)) {
          return false;
        }
        return true;
      }
    }
    return false;
  }

  private boolean isInchQuote(String text) {
    return INCH_PATTERN.matcher(text).matches();
  }
  
  protected boolean isNotBeginningApostrophe(AnalyzedTokenReadings[] tokens, int i) {
    return !POSSIBLE_APOSTROPHE.matcher(tokens[i].getToken()).matches()
        || i >= tokens.length - 1  || tokens[i + 1].isNonWord() || tokens[i + 1].isWhitespaceBefore();
            
  }

  protected boolean isNotEndingApostrophe(AnalyzedTokenReadings[] tokens, int i) {
    return !POSSIBLE_APOSTROPHE.matcher(tokens[i].getToken()).matches()
            || tokens[i].isWhitespaceBefore()
            || tokens[i - 1].isNonWord();
  }
  
  private int indexOfOpeningQuote(List<SymbolLocator> openingQuotes, String symbol) {
    for(int i = 0; i < openingQuotes.size(); i++) {
      if (symbol.equals(openingQuotes.get(i).getSymbol())) {
        return i;
      }
    }
    return -1;
  }
  
  private void addMatch(SymbolLocator openingQuote, List<RuleMatch> ruleMatches) {
    String message = MessageFormat.format(messages.getString("unpaired_brackets"), findCorrespondingSymbol(openingQuote.getSymbol()));
    RuleMatch match = new RuleMatch(this, openingQuote.getSentence(), openingQuote.getStartPos(), 
        openingQuote.getStartPos() + openingQuote.getSymbol().length(), message);
    ruleMatches.add(match);
  }
  
  private void removeAllOpenInnerQuotes(int index, List<SymbolLocator> openingQuotes, List<RuleMatch> ruleMatches) {
    for (int i = openingQuotes.size() - 1; i > index; i--) {
      addMatch(openingQuotes.get(i), ruleMatches);
      openingQuotes.remove(i);
    }
  }

  protected String findCorrespondingSymbol(String symbol) {
    int idx1 = startSymbols.indexOf(symbol);
    if (idx1 >= 0) {
      return endSymbols.get(idx1);
    } else {
      int idx2 = endSymbols.indexOf(symbol);
      return startSymbols.get(idx2);
    }
  }

  @Override
  public int minToCheckParagraph() {
    return -1;
  }

  protected class SymbolLocator {

    private final String symbol;
    private final int startPos;
    private final AnalyzedSentence sentence;

    SymbolLocator(String symbol, int startPos, AnalyzedSentence sentence) {
      this.symbol = symbol;
      this.startPos = startPos;
      this.sentence = sentence;
    }

    public String getSymbol() {
      return symbol;
    }

    int getStartPos() {
      return startPos;
    }

    AnalyzedSentence getSentence() {
      return sentence;
    }

  }
}
