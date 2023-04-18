/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.es;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.SymbolLocator;
import org.languagetool.rules.UnsyncStack;

public class SpanishUnpairedBracketsRule extends GenericUnpairedBracketsRule {

  private static final List<String> ES_START_SYMBOLS = Arrays.asList("[", "(", "{", "“", "«", "\"", "'", "‘");
  private static final List<String> ES_END_SYMBOLS   = Arrays.asList("]", ")", "}", "”", "»", "\"", "'", "’");

  //private static final Pattern NUMBER = Pattern.compile("[\\d,.]*\\d");
  
  private static final Pattern VALID_BEFORE_CLOSING_PARENTHESIS = Pattern
      .compile("\\d+|[a-zA-Z]", Pattern.UNICODE_CASE);
  private static final Pattern NUMBER = Pattern.compile("\\d[\\d., ]+\\d|\\d{1,2}", Pattern.UNICODE_CASE);

  public SpanishUnpairedBracketsRule(ResourceBundle messages) {
    super(messages, ES_START_SYMBOLS, ES_END_SYMBOLS);
  }

  @Override
  public String getId() {
    return "ES_UNPAIRED_BRACKETS";
  }

  @Override
  protected boolean isNoException(final String tokenStr,
      final AnalyzedTokenReadings[] tokens, final int i, final int j,
      final boolean precSpace, final boolean follSpace, UnsyncStack<SymbolLocator> symbolStack) {

    if (i < 1) {
      return true;
    }

    final boolean superException = !super.isNoException(tokenStr, tokens, i, j, precSpace, follSpace, symbolStack);
    if (superException) {
      return false;
    }
    
    if ((tokenStr.equals("’") || tokenStr.equals("'"))
        && (tokens[i].hasPosTagStartingWith("N") || tokens[i].hasPosTagStartingWith("A"))) {
      return false;
    }
    
    // Exception for English plural Saxon genitive
    if (i + 1 < tokens.length && (isQuote(tokenStr)) && "s".equalsIgnoreCase(tokens[i + 1].getToken())) {
      return false;
    }

    //degrees, minutes, seconds...
    if (("\"".equals(tokenStr) || "'".equals(tokenStr))
        && NUMBER.matcher(tokens[i - 1].getToken()).matches()
        && !tokens[i].isWhitespaceBefore()
        && ((i > 2 && (tokens[i - 2].getToken().contains("º") || tokens[i - 2].getToken().contains("°")))
        || (i > 4 && (tokens[i - 4].getToken().contains("º") || tokens[i - 4].getToken().contains("°"))))) {
      return false;
    }

    if (i == 1 && tokenStr.equals("»"))
      return false;

    if (i > 1 && tokenStr.equals(")")) {
      boolean isThereOpeningParenthesis = false;
      int k=1;
      while (i-k>0) {
        if (tokens[i-k].getToken().equals(")"))
          break;
        if (tokens[i-k].getToken().equals("(")) {
          isThereOpeningParenthesis=true;
          break;
        }
        k++;
      }
      if (!isThereOpeningParenthesis) {
        final Matcher mValidBeforeClosingParenthesis = VALID_BEFORE_CLOSING_PARENTHESIS
            .matcher(tokens[i - 1].getToken());
        if (mValidBeforeClosingParenthesis.matches())
          return false;
      }
    }

    return true;
  }
  
  private boolean isQuote(String tokenStr) {
    return "'".equals(tokenStr) || "’".equals(tokenStr);
  }

}
