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
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.tokenizers.WordTokenizer;
import org.languagetool.tools.StringTools;

/**
 * Checks that a sentence starts with an uppercase letter.
 * 
 * @author Daniel Naber
 */
public class UppercaseSentenceStartRule extends Rule {

  private static final Pattern NUMERALS_EN =
          Pattern.compile("[a-z]|(m{0,4}(cm|cd|d?c{0,3})(xc|xl|l?x{0,3})(ix|iv|v?i{0,3}))$");
  private static final Pattern WHITESPACE_OR_QUOTE = Pattern.compile("[ \"'„»«“\\n]");
  private static final Pattern SENTENCE_END1 = Pattern.compile("[.?!…]|");
  private static final Pattern SENTENCE_END2 = Pattern.compile("[.?!…]");
  private static final Pattern DUTCH_SPECIAL_CASE = Pattern.compile("k|m|n|r|s|t");

  private final Language language;

  private String lastParagraphString = "";
  
  public UppercaseSentenceStartRule(final ResourceBundle messages,
      final Language language) {
    super(messages);
    super.setCategory(new Category(messages.getString("category_case")));
    this.language = language;
    setLocQualityIssueType(ITSIssueType.Typographical);
  }

  @Override
  public final String getId() {
    return "UPPERCASE_SENTENCE_START";
  }

  @Override
  public final String getDescription() {
    return messages.getString("desc_uppercase_sentence");
  }

  @Override
  public final RuleMatch[] match(final AnalyzedSentence text) {
    final List<RuleMatch> ruleMatches = new ArrayList<>();
    final AnalyzedTokenReadings[] tokens = text.getTokensWithoutWhitespace();
    if (tokens.length < 2) {
      return toRuleMatchArray(ruleMatches);
    }
    int matchTokenPos = 1; // 0 = SENT_START
    final String firstToken = tokens[matchTokenPos].getToken();
    String secondToken = null;
    String thirdToken = null;
    // ignore quote characters:
    if (tokens.length >= 3
        && ("'".equals(firstToken) || "\"".equals(firstToken) || "„".equals(firstToken))) {
      matchTokenPos = 2;
      secondToken = tokens[matchTokenPos].getToken();
    }
    final String firstDutchToken = dutchSpecialCase(firstToken, secondToken, tokens);
    if (firstDutchToken != null) {
      thirdToken = firstDutchToken;
      matchTokenPos = 3;
    }

    String checkToken = firstToken;
    if (thirdToken != null) {
      checkToken = thirdToken;
    } else if (secondToken != null) {
      checkToken = secondToken;
    }

    String lastToken = tokens[tokens.length - 1].getToken();
    if (tokens.length >= 2 && WHITESPACE_OR_QUOTE.matcher(lastToken).matches()) {
      // ignore trailing whitespace or quote
      lastToken = tokens[tokens.length - 2].getToken();
    }
    
    boolean preventError = false;
    if (lastParagraphString.equals(",") || lastParagraphString.equals(";")) {
      preventError = true;
    }
    if (!SENTENCE_END1.matcher(lastParagraphString).matches() && !SENTENCE_END2.matcher(lastToken).matches()) {
      preventError = true;
    }
    
    lastParagraphString = lastToken;
    
    //allows enumeration with lowercase letters: a), iv., etc.
    if (matchTokenPos+1 < tokens.length
        && NUMERALS_EN.matcher(tokens[matchTokenPos].getToken()).matches()
        && (tokens[matchTokenPos+1].getToken().equals(".")
         || tokens[matchTokenPos+1].getToken().equals(")"))) {
      preventError = true;
    }
    
    if (isUrl(checkToken)) {
      preventError = true;
    }

    if (checkToken.length() > 0) {
      final char firstChar = checkToken.charAt(0);
      if (!preventError && Character.isLowerCase(firstChar)) {
        final RuleMatch ruleMatch = new RuleMatch(this,
                tokens[matchTokenPos].getStartPos(),
                tokens[matchTokenPos].getStartPos() + tokens[matchTokenPos].getToken().length(),
                messages.getString("incorrect_case"));
        ruleMatch.setSuggestedReplacement(StringTools.uppercaseFirstChar(checkToken));
        ruleMatches.add(ruleMatch);
      }
    }
    return toRuleMatchArray(ruleMatches);
  }

  private String dutchSpecialCase(final String firstToken,
      final String secondToken, final AnalyzedTokenReadings[] tokens) {
    if (!language.getShortName().equals("nl")) {
      return null;
    }
    if (tokens.length >= 3 && firstToken.equals("'")
        && DUTCH_SPECIAL_CASE.matcher(secondToken).matches()) {
      return tokens[3].getToken();
    }
    return null;
  }

  @Override
  public void reset() {
  }
  
  protected boolean isUrl(String token) {
    for (String protocol : WordTokenizer.getProtocols()) {
      if (token.startsWith(protocol + "://")) {
        return true;
      }
    }
    return false;
  }

}
