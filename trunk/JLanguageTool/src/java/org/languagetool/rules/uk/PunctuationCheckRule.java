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
package de.danielnaber.languagetool.rules.uk;

import java.util.ResourceBundle;

import de.danielnaber.languagetool.rules.AbstractPunctuationCheckRule;

/**
 * A rule that matches "..", "::", "-," but not "...", "!..", "?!!", ",-" etc
 * TODO: spaces seem to be special, extract from regexp?
 * 
 * @author Andriy Rysin
 */
public class PunctuationCheckRule extends AbstractPunctuationCheckRule {

  public PunctuationCheckRule(final ResourceBundle messages) {
    super(messages);
    // super.setCategory(new Category(messages.getString("category_misc")));
  }

  // private boolean isTripleOk(String token) {
  // return token.matches("^[.!?]$");
  // }

  /*
   * (non-Javadoc)
   * 
   * @see
   * de.danielnaber.languagetool.rules.AbstractPunctuationCheckRule#isPunctsJoinOk
   * (java.lang.String)
   */
  @Override
  protected final boolean isPunctsJoinOk(final String tokens) {
    return // we ignore duplicated spaces - too many errors
    tokens.matches("([,:] | *- |,- | ) *") // internal puctuation
        || tokens
            .matches("([.!?]|!!!|\\?\\?\\?|\\?!!|!\\.\\.|\\?\\.\\.|\\.\\.\\.) *");
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * de.danielnaber.languagetool.rules.AbstractPunctuationCheckRule#isPunctuation
   * (java.lang.String)
   */
  @Override
  protected final boolean isPunctuation(final String token) {
    return token.matches("^[.,!?: -]$");
  }

  /*
   * (non-Javadoc)
   * 
   * @see de.danielnaber.languagetool.rules.AbstractPunctuationCheckRule#reset()
   */
  @Override
  public void reset() {
    // nothing
  }

}
