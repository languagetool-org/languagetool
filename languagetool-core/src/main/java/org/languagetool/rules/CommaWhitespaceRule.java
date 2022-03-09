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

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.tools.StringTools;

import static org.languagetool.tools.StringTools.isEmpty;

/**
 * A rule that matches periods, commas and closing parenthesis preceded by whitespace and
 * opening parenthesis followed by whitespace.
 * 
 * @author Daniel Naber
 */
public class CommaWhitespaceRule extends Rule {

  private boolean quotesWhitespaceCheck;

  /** @since 3.3 */
  public CommaWhitespaceRule(ResourceBundle messages, IncorrectExample incorrectExample, CorrectExample correctExample) {
    super(messages);
    super.setCategory(Categories.TYPOGRAPHY.getCategory(messages));
    setLocQualityIssueType(ITSIssueType.Whitespace);
    if (incorrectExample != null && correctExample != null) {
      addExamplePair(incorrectExample, correctExample);
    }
    this.quotesWhitespaceCheck = true;
  }

  public CommaWhitespaceRule(ResourceBundle messages, boolean quotesWhitespace) { 
    this(messages, null, null);
    this.quotesWhitespaceCheck = quotesWhitespace;
  }

  /**
   * @deprecated use {@link #CommaWhitespaceRule(ResourceBundle, IncorrectExample, CorrectExample)} instead (deprecated since 3.3)
   */
  public CommaWhitespaceRule(ResourceBundle messages) {
    this(messages, null, null);
    this.quotesWhitespaceCheck = true;
  }

  @Override
  public String getId() {
    return "COMMA_PARENTHESIS_WHITESPACE";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_comma_whitespace");
  }
  
  public String getCommaCharacter() {
    return ",";
  }

  /**
   * @return Returns true if there exception to this rule
   * @since 5.3
   */
  protected boolean isException(AnalyzedTokenReadings[] tokens, int tokenIdx) {
    return false;
  }
  
  @Override
  public final RuleMatch[] match(AnalyzedSentence sentence) {
    List<RuleMatch> ruleMatches = new ArrayList<>();
    AnalyzedTokenReadings[] tokens = sentence.getTokens();
    String prevToken = "";
    String prevPrevToken = "";
    boolean prevWhite = false;
    for (int i = 0; i < tokens.length; i++) {
      String token = tokens[i].getToken();
      boolean isWhitespace = isWhitespaceToken(tokens[i]);
      boolean twoSuggestions = false;
      
      String msg = null;
      String suggestionText = null;
      if (isWhitespace && isLeftBracket(prevToken)) {
        boolean isException = i + 1 < tokens.length && prevToken.equals("[") && token.equals(" ") && tokens[i+1].getToken().equals("]");  // "- [ ]" syntax e.g. on GitHub
        if (!isException) {
          msg = messages.getString("no_space_after");
          suggestionText = prevToken;
        }
      } else if (isWhitespace && isQuote(prevToken) && this.quotesWhitespaceCheck && prevPrevToken.equals(" ")) {
          msg = messages.getString("no_space_around_quotes");
          suggestionText = prevToken;
          twoSuggestions = true;
      } else if (!isWhitespace && prevToken.equals(getCommaCharacter())
          && !isQuote(token)
          && !isHyphenOrComma(token)
          && !containsDigit(prevPrevToken)
          && !containsDigit(token)
          && !",".equals(prevPrevToken)) {
        msg = messages.getString("missing_space_after_comma");
        suggestionText = getCommaCharacter() + " " + tokens[i].getToken();
      } else if (prevWhite) {
        if (isRightBracket(token)) {
          boolean isException = token.equals("]") && prevToken.equals(" ") && prevPrevToken.equals("["); // "- [ ]" syntax e.g. on GitHub
          if (!isException) {
            msg = messages.getString("no_space_before");
            suggestionText = token;
          }
        } else if (token.equals(getCommaCharacter())) {
          msg = messages.getString("space_after_comma");
          suggestionText = getCommaCharacter();
          // exception for duplicated comma (we already have another rule for that)
          if (i + 1 < tokens.length && getCommaCharacter().equals(tokens[i+1].getToken())) {
            msg = null;
          }
          if (i + 1 < tokens.length && !tokens[i+1].isWhitespace()) {
            suggestionText = getCommaCharacter() + " ";
          }
        } else if (token.equals(".") && !isDomain(tokens, i+1) && !isFileExtension(tokens, i+1)) {
          msg = messages.getString("no_space_before_dot");
          suggestionText = ".";
          // exception case for figures such as ".5" and ellipsis
          if (i + 1 < tokens.length && isDigitOrDot(tokens[i+1].getToken())) {
            msg = null;
          } else if (i + 2 < tokens.length && tokens[i+1].getToken().equals("/") && tokens[i+2].getToken().matches("[a-zA-Z]+")) {
            // commands like "./validate.sh"
            msg = null;
          }
        }
      }
      if (msg != null && ! isException(tokens, i) ) {
        int fromPos = tokens[i - 1].getStartPos();
        if (twoSuggestions) {
          fromPos = tokens[i - 2].getStartPos();
        }
        int toPos = tokens[i].getEndPos();
        RuleMatch ruleMatch = new RuleMatch(this, sentence, fromPos, toPos, msg);
        if (twoSuggestions) {
          ruleMatch.addSuggestedReplacement(suggestionText + " ");
          ruleMatch.addSuggestedReplacement(" " + suggestionText);
        } else {
          ruleMatch.setSuggestedReplacement(suggestionText);  
        }
        ruleMatches.add(ruleMatch);
      }
      prevPrevToken = prevToken;
      prevToken = token;
      prevWhite = isWhitespace && !tokens[i].isFieldCode(); // LO/OO code before comma/dot
    }

    return toRuleMatchArray(ruleMatches);
  }

  private boolean isDomain(AnalyzedTokenReadings[] tokens, int i) {
    return i < tokens.length && tokens[i].getToken().matches("(com|org|net|int|edu|gov|mil|[a-z]{2})");
  }

  private boolean isFileExtension(AnalyzedTokenReadings[] tokens, int i) {
    return i < tokens.length && tokens[i].getToken().matches("[a-z]{3,4}|[A-Z]{3,4}|ai|mp[34]");
  }

  private static boolean isWhitespaceToken(AnalyzedTokenReadings token) {
    return (   token.isWhitespace()
        || StringTools.isNonBreakingWhitespace(token.getToken())
        || token.isFieldCode()) && !token.getToken().equals("\u200B");
  }

  private static boolean isQuote(String str) {
    if (str.length() == 1) {
      char c = str.charAt(0);
      if (c =='\'' || c == '"' || c =='’'
          || c == '”' || c == '“'
          || c == '«'|| c == '»') {
        return true;
      }
    }
    return false;
  }

  private static boolean isHyphenOrComma(String str) {
    if (str.length() == 1) {
      char c = str.charAt(0);
      if (c == '-' || c == ',') {
        return true;
      }
    }
    return false;
  }

  private static boolean isDigitOrDot(String str) {
    if (isEmpty(str)) {
      return false;
    }
    char c = str.charAt(0);
    return c == '.' || Character.isDigit(c);
  }

  private static boolean isLeftBracket(String str) {
    if (isEmpty(str)) {
      return false;
    }
    char c = str.charAt(0);
    return c == '(' || c == '[' || c == '{';
  }

  private static boolean isRightBracket(String str) {
    if (isEmpty(str)) {
      return false;
    }
    char c = str.charAt(0);
    return c == ')' || c == ']' || c == '}';
  }

  private static boolean containsDigit(String str) {
    for (int i = 0; i < str.length(); i++) {
      if (Character.isDigit(str.charAt(i))) {
        return true;
      }
    }
    return false;
  }

}
