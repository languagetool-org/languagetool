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

import java.util.ArrayList;
import java.util.List;

class UnicodeBasedLangIdentifier {

  private static final int DEFAULT_MAX_CHECK_LENGTH = 50;
  private static final float THRESHOLD = 0.5f;

  private final int maxCheckLength;

  UnicodeBasedLangIdentifier() {
    this(DEFAULT_MAX_CHECK_LENGTH);
  }

  UnicodeBasedLangIdentifier(int maxCheckLength) {
    this.maxCheckLength = maxCheckLength;
  }

  List<String> getAdditionalLangCodes(String str) {
    int cyrillicChars = 0;
    int cjkChars = 0;
    int khmerChars = 0;
    int tamilChars = 0;
    int greekChars = 0;
    int devanagariChars = 0;
    int significantChars = 0;
    for (int i = 0; i < Math.min(str.length(), maxCheckLength); i++) {
      int val = str.charAt(i);
      if (!Character.isWhitespace(val) && !Character.isDigit(val)) {
        significantChars++;
      }
      if (val >= 0x0400 && val <= 0x04FF) {
        cyrillicChars++;
      }
      if (val >= 0x4E00 && val <= 0x9FFF ||
          val >= 0x3040 && val <= 0x309F ||
          val >= 0x30A0 && val <= 0x30FF) {  // https://de.wikipedia.org/wiki/Japanische_Schrift
        // there might be a better way to tell Chinese from Japanese, but we rely
        // on the actual language identifier in a later step, so finding candidates is enough here
        cjkChars++;
      }
      if (val >= 0x1780 && val <= 0x17FF) {
        khmerChars++;
      }
      if (val >= 0xB82 && val <= 0xBFA) {
        tamilChars++;
      }
      if (val >= 0x0370 && val <= 0x03FF || val >= 0x1F00 && val <= 0x1FFF) {
        greekChars++;
      }
      if (val >= 0x0900 && val <= 0x097F) {
        devanagariChars++;
      }
    }
    List<String> langCodes = new ArrayList<>();
    if ((float)cyrillicChars / significantChars >= THRESHOLD) {
      langCodes.add("ru");
      langCodes.add("uk");
      langCodes.add("be");
    }
    if ((float)cjkChars / significantChars >= THRESHOLD) {
      langCodes.add("zh");
      langCodes.add("ja");
      // Korean is not supported by LT, do we don't add it
    }
    if ((float)khmerChars / significantChars >= THRESHOLD) {
      langCodes.add("km");
    }
    if ((float)tamilChars / significantChars >= THRESHOLD) {
      langCodes.add("ta");
    }
    if ((float)greekChars / significantChars >= THRESHOLD) {
      langCodes.add("el");
    }
    if ((float)devanagariChars / significantChars >= THRESHOLD) {
      langCodes.add("hi");
      langCodes.add("mr");
    }
    return langCodes;
  }

}
