/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Ebrahim Byagowi <ebrahim@gnu.org>
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
package org.languagetool.rules.fa;

import java.util.ResourceBundle;

import org.languagetool.rules.CommaWhitespaceRule;

/**
 * A rule that matches periods, commas and closing parenthesis preceded by whitespace and
 * opening parenthesis followed by whitespace.
 * 
 * @author Ebrahim Byagowi
 * @since 2.7
 */
public class PersianCommaWhitespaceRule extends CommaWhitespaceRule {

  public PersianCommaWhitespaceRule(ResourceBundle messages) {
    super(messages);
  }
  
  @Override
  public final String getId() {
    return "PERSIAN_COMMA_PARENTHESIS_WHITESPACE";
  }

  @Override
  public String getCommaCharacter() {
    return "،";
  }

}
