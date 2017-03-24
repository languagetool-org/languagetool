/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice;

/**
 * Helps detecting the language of strings by the Unicode range used by the characters.
 * @since 2.7
 */
abstract class LanguageDetector {

  private static final int MAX_CHECK_LENGTH = 100;
  
  /** Lower bound of Unicode range that this language's characters use. */
  abstract int getLowerBound();

  /** Upper bound of Unicode range that this language's characters use. */
  abstract int getUpperBound();

  boolean isThisLanguage(String str) {
    int maxCheckLength = Math.min(str.length(), MAX_CHECK_LENGTH);
    for (int i = 0; i < maxCheckLength; i++) {
      int numericValue = str.charAt(i);
      if (numericValue >= getLowerBound() && numericValue <= getUpperBound()) {
        return true;
      }
    }
    return false;
  }

}
