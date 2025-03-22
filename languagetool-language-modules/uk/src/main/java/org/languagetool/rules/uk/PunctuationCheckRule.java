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
package org.languagetool.rules.uk;

import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.languagetool.rules.AbstractPunctuationCheckRule;

/**
 * A rule that matches "..", "::", "-," but not "...", "!..", "?!!", ",-" etc
 * TODO: spaces seem to be special, extract from regexp?
 * 
 * @author Andriy Rysin
 */
public class PunctuationCheckRule extends AbstractPunctuationCheckRule {

  private static final Pattern PATTERN_1 = Pattern.compile("([,:] | *- |,- | ) *");
  private static final Pattern PATTERN_2 = Pattern.compile("([.!?]|!!!|\\?\\?\\?|\\?!!|!\\.\\.|\\?\\.\\.|\\.\\.\\.) *");
  private static final Pattern PATTERN_3 = Pattern.compile("^[.,!?: -]$");

  public PunctuationCheckRule(ResourceBundle messages) {
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
   * org.languagetool.rules.AbstractPunctuationCheckRule#isPunctsJoinOk
   * (java.lang.String)
   */
  @Override
  protected final boolean isPunctsJoinOk(String tokens) {
    return // we ignore duplicated spaces - too many errors
           PATTERN_1.matcher(tokens).matches() // internal punctuation
        || PATTERN_2.matcher(tokens).matches();
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
    return PATTERN_3.matcher(token).matches();
  }

}
