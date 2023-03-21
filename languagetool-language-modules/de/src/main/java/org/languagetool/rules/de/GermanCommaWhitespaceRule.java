/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.de;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.CommaWhitespaceRule;
import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.IncorrectExample;

import java.net.URL;
import java.util.ResourceBundle;

public class GermanCommaWhitespaceRule extends CommaWhitespaceRule {

  public GermanCommaWhitespaceRule(ResourceBundle messages, IncorrectExample incorrectExample, CorrectExample correctExample, URL url) {
    super(messages, incorrectExample, correctExample, url);
  }

  @Override
  protected boolean isException(AnalyzedTokenReadings[] tokens, int tokenIdx) {
    if (tokenIdx+2 < tokens.length &&
        tokens[tokenIdx].getToken().equals(".") &&
        tokens[tokenIdx+1].getToken().matches("[a-z]{2,10}-Domains?")) {
      return true;
    }
    return false;
  }

}
