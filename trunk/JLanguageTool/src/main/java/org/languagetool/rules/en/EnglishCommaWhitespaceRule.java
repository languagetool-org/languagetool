/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.CommaWhitespaceRule;

import java.util.ResourceBundle;

/**
 * Exception for English, according to The Chicago Manual of Style as quoted by
 * http://en.wikipedia.org/wiki/Ellipsis.
 */
public class EnglishCommaWhitespaceRule extends CommaWhitespaceRule {

  public EnglishCommaWhitespaceRule(final ResourceBundle messages) {
    super(messages);
  }

  @Override
  public final String getId() {
    return "ENGLISH_COMMA_PARENTHESIS_WHITESPACE";
  }

  @Override
  protected int getExceptionSkip(AnalyzedTokenReadings[] tokens, int pos) {
    // allow spaced end ellipsis, i.e. " . . . .":
    if (pos + 8 < tokens.length
            && isDotAt(tokens, pos + 2)
            && isDotAt(tokens, pos + 4)
            && isDotAt(tokens, pos + 6)
            && isDotAt(tokens, pos + 8)) {
      return 7;
    }
    // allow spaced ellipsis, i.e. " . . . ":
    if (pos + 6 < tokens.length
            && isDotAt(tokens, pos + 2)
            && isDotAt(tokens, pos + 4)
            && isDotAt(tokens, pos + 6)) {
      return 5;
    }
    return 0;
  }

  private boolean isDotAt(AnalyzedTokenReadings[] tokens, int pos) {
    final String str = tokens[pos].getToken();
    return str.length() > 0 && str.charAt(0) == '.';
  }

}
