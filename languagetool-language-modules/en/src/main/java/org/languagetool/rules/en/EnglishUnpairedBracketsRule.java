/* LanguageTool, a natural language style checker
 * Copyright (C) 2010 Daniel Naber (http://www.languagetool.org)
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

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.SymbolLocator;
import org.languagetool.rules.UnsyncStack;

public class EnglishUnpairedBracketsRule extends GenericUnpairedBracketsRule {

  private static final List<String> EN_START_SYMBOLS = Arrays.asList("[", "(", "{", "“", "\"", "'");
  private static final List<String> EN_END_SYMBOLS   = Arrays.asList("]", ")", "}", "”", "\"", "'");

  private static final Pattern NUMBER = Pattern.compile("\\d+(?:-\\d+)?");
  private static final Pattern YEAR_NUMBER = Pattern.compile("\\d\\ds?");
  private static final Pattern ALPHA = Pattern.compile("\\p{L}+");

  public EnglishUnpairedBracketsRule(ResourceBundle messages, Language language) {
    super(messages, EN_START_SYMBOLS, EN_END_SYMBOLS);
    addExamplePair(Example.wrong("<marker>\"</marker>I'm over here, she said."),
                   Example.fixed("\"I'm over here,<marker>\"</marker> she said."));
  }

  @Override
  public String getId() {
    return "EN_UNPAIRED_BRACKETS";
  }

  @Override
  protected boolean isNoException(String tokenStr,
      AnalyzedTokenReadings[] tokens, int i, int j, boolean precSpace,
      boolean follSpace, UnsyncStack<SymbolLocator> symbolStack) {

    //TODO: add an', o', 'till, 'tain't, 'cept, 'fore in the disambiguator
    //and mark up as contractions somehow

    if (i <= 1) {
      return true;
    }

    if (i > 2) { // we need this for al-'Adad, as we tokenize on final '-'
      if ("'".equals(tokens[i].getToken())) {
        if ("-".equals(tokens[i - 1].getToken()) &&
            !tokens[i - 1].isWhitespaceBefore() &&
            ALPHA.matcher(tokens[i - 2].getToken()).matches()) {
          return false;
        }
      }
    }

    boolean superException = !super.isNoException(tokenStr, tokens, i, j, precSpace, follSpace, symbolStack);
    if (superException) {
      return false;
    }

    if (!precSpace && follSpace || tokens[i].isSentenceEnd()) {
      // exception for English inches, e.g., 20"
      AnalyzedTokenReadings prevToken = tokens[i - 1];
      if ("\"".equals(tokenStr)) {
        if (!symbolStack.empty() && "\"".equals(symbolStack.peek().getSymbol())) {
          return true;
        } else if (NUMBER.matcher(prevToken.getToken()).matches()) {
          return false;
        }
      }
      // Exception for English plural Saxon genitive
      if (("'".equals(tokenStr) || "’".equals(tokenStr)) && tokens[i].hasPosTag("POS")) {
        return false;
      }
      // puttin' on the Ritz
      if ("'".equals(tokenStr) && prevToken.hasPosTag("VBG")
          && prevToken.getToken().endsWith("in")) {
        return false;
      }
    }
    if (precSpace && !follSpace) {
      if ("'".equals(tokenStr) && i + 1 < tokens.length) {
        // hold 'em!
        if ("em".equals(tokens[i + 1].getToken())) {
          return false;
        }
        // '60 campaign
        else if (YEAR_NUMBER.matcher(tokens[i + 1].getToken()).matches()) {
          return false;
        }
      }
    }
    return true;
  }

}
