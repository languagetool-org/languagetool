/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.language;

class CyrillicIdentifier {

  private static final int DEFAULT_MAX_CHECK_LENGTH = 50;
  private static final float THRESHOLD = 0.5f;

  private final int maxCheckLength;

  CyrillicIdentifier() {
    this(DEFAULT_MAX_CHECK_LENGTH);
  }

  CyrillicIdentifier(int maxCheckLength) {
    this.maxCheckLength = maxCheckLength;
  }

  boolean isCyrillic(String str) {
    int cyrillicChars = 0;
    int significantChars = 0;
    for (int i = 0; i < Math.min(str.length(), maxCheckLength); i++) {
      int numericValue = str.charAt(i);
      if (!Character.isWhitespace(numericValue) && !Character.isDigit(numericValue)) {
        significantChars++;
      }
      if (numericValue > 1024 && numericValue < 1279) {
        cyrillicChars++;
      }
    }
    float cyrillicCharsRate = (float)cyrillicChars / significantChars;
    //System.out.println("cyrillicCharsRate: " + cyrillicCharsRate + " (" + cyrillicChars + "/" + significantChars + ")");
    return cyrillicCharsRate >= THRESHOLD;
  }

}
