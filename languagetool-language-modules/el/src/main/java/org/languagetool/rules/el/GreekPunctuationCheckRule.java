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
package org.languagetool.rules.el;
import java.util.ResourceBundle;
import org.languagetool.rules.AbstractPunctuationCheckRule;

/**
 * A rule that matches regular expressions regarding 
 * punctuation in the greek language
 * 
 */
public class GreekPunctuationCheckRule extends AbstractPunctuationCheckRule {

  public GreekPunctuationCheckRule(ResourceBundle messages) {
    super(messages);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.languagetool.rules.AbstractPunctuationCheckRule#isPunctsJoinOk
   * (java.lang.String)
   */
  @Override
  protected final boolean isPunctsJoinOk(String tokens) {
	  String punctCases = "\\, |\\. |: |· | - [α-ωΑ-Ω0-9 ]* - |\\.{3} |! |; "; 
	  punctCases += "|\\,\n|\\.\n|:\n|·\n|\\.{3}\n|!\n|;\n"; 
    return tokens.matches(punctCases);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.languagetool.rules.AbstractPunctuationCheckRule#isPunctuation
   * (java.lang.String)
   */
  @Override
  protected final boolean isPunctuation(String token) {
    return token.matches("^[.,!;: -]$");
  }

}