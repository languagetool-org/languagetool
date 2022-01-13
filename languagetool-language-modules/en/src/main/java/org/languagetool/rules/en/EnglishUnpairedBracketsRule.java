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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.rules.Example;
import org.languagetool.rules.GenericUnpairedBracketsRule;
import org.languagetool.rules.SymbolLocator;
import org.languagetool.rules.UnsyncStack;

public class EnglishUnpairedBracketsRule extends GenericUnpairedBracketsRule {

  //private static final List<String> EN_START_SYMBOLS = Arrays.asList("[", "(", "{");
  //private static final List<String> EN_END_SYMBOLS   = Arrays.asList("]", ")", "}");
  private static final Pattern INCH_PATTERN = Pattern.compile(".*\\d\".*", Pattern.DOTALL);
  // This is more strict, but also leads to confusing messages for users who mix up the many
  // characters that are be used as a quote character (https://github.com/languagetool-org/languagetool/issues/2356): 
  private static final List<String> EN_START_SYMBOLS = Arrays.asList("[", "(", "{", "“", "\"", "'", "‘");
  private static final List<String> EN_END_SYMBOLS   = Arrays.asList("]", ")", "}", "”", "\"", "'", "’");

  public EnglishUnpairedBracketsRule(ResourceBundle messages, Language language) {
    super(messages, EN_START_SYMBOLS, EN_END_SYMBOLS);
      addExamplePair(Example.wrong("\"I'm over here,<marker></marker> she said."),
                     Example.fixed("\"I'm over here,<marker>\"</marker> she said."));
  }

  @Override
  public String getId() {
    return "EN_UNPAIRED_BRACKETS";
  }

  @Override
  protected boolean preventMatch(AnalyzedSentence sentence) {
    String text = sentence.getText();
    Matcher inchMatcher = INCH_PATTERN.matcher(text);
    if (inchMatcher.matches()) {    // could be >>3"<< (3 inch) or a quote that ends with a number
      return true;
    }
    return false;
  }

  @Override
  protected boolean isNoException(String tokenStr,
      AnalyzedTokenReadings[] tokens, int i, int j, boolean precSpace,
      boolean follSpace, UnsyncStack<SymbolLocator> symbolStack) {

    if (tokens[i].hasPosTag("_apostrophe_contraction_") || tokens[i].hasPosTag("POS")) {
      return false;
    }

    if (i <= 1) {
      return true;
    }

    boolean superException = !super.isNoException(tokenStr, tokens, i, j, precSpace, follSpace, symbolStack);
    if (superException) {
      return false;
    }
    
    //TODO: What is done here? Examples?
    //The exception for 20" is in preventMatch()
    if (!precSpace && follSpace || tokens[i].isSentenceEnd()) {
      // exception for English inches, e.g., 20"
      if ("\"".equals(tokenStr)) {
        if (!symbolStack.empty() && "\"".equals(symbolStack.peek().getSymbol())) {
          return true;
        }
      }
    }
    return true;
  }
 

}
