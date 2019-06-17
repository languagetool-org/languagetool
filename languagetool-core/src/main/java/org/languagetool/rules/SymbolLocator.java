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

import org.languagetool.AnalyzedSentence;

/**
 * Helper class for {@link GenericUnpairedBracketsRule} to identify
 * symbols indexed with integers.
 * 
 * @author Marcin Miłkowski
 */
public class SymbolLocator {

  private final String symbol;
  private final int index;
  private final int startPos;
  private final AnalyzedSentence sentence;

  SymbolLocator(String symbol, int index, int startPos, AnalyzedSentence sentence) {
    this.symbol = symbol;
    this.index = index;
    this.startPos = startPos;
    this.sentence = sentence;
  }

  /**
   * @return The symbol in the locator
   * @since 2.5
   */
  public String getSymbol() {
    return symbol;
  }

  /** @since 2.9 */
  int getIndex() {
    return index;
  }

  /** @since 2.9 */
  int getStartPos() {
    return startPos;
  }

  /** @since 4.0 */
  AnalyzedSentence getSentence() {
    return sentence;
  }

  @Override
  public String toString() {
    return symbol + "/" + index;
  }
}
