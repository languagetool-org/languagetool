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

package org.languagetool.rules.ar;

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

public class ArabicUnpairedBracketsRule extends GenericUnpairedBracketsRule {

  private static final List<String> EN_START_SYMBOLS = Arrays.asList("[", "(", "{", "“", "\"", "'");
  private static final List<String> EN_END_SYMBOLS   = Arrays.asList("]", ")", "}", "”", "\"", "'");

  private static final Pattern NUMBER = Pattern.compile("\\d+(?:-\\d+)?");
  private static final Pattern YEAR_NUMBER = Pattern.compile("\\d\\ds?");
  private static final Pattern ALPHA = Pattern.compile("\\p{L}+");

  public ArabicUnpairedBracketsRule(ResourceBundle messages, Language language) {
    super(messages, EN_START_SYMBOLS, EN_END_SYMBOLS);
    addExamplePair(Example.wrong("قالت <marker>\"</marker>لقد تعبت."),
                   Example.fixed("قالت \"لقد تعبت<marker>\"</marker>."));
  }

  @Override
  public String getId() {
    return "AR_UNPAIRED_BRACKETS";
  }

  @Override
  protected boolean isNoException(String tokenStr,
      AnalyzedTokenReadings[] tokens, int i, int j, boolean precSpace,
      boolean follSpace, UnsyncStack<SymbolLocator> symbolStack) {


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

    return true;
  }

}
