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

package org.languagetool.rules;

/**
 * Helper class for GenericUnpairedBracketsRule to identify
 * symbols indexed with integers.
 * 
 * @author Marcin Miłkowski
 * @deprecated deprecated since 2.6 (will be kept, but made non-public in the future)
 */
public class SymbolLocator {

  String symbol;
  int index;

  public SymbolLocator(final String symbol, final int index) {
    this.symbol = symbol;
    this.index = index;
  }

  /**
   * @return The symbol in the locator
   * @since 2.5
   */
  public String getSymbol() {
    return symbol;
  }

}
