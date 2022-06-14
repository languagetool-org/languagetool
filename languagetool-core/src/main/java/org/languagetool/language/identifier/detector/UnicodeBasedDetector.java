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
package org.languagetool.language.identifier.detector;

import java.util.ArrayList;
import java.util.List;

public class UnicodeBasedDetector {

  private static final int DEFAULT_MAX_CHECK_LENGTH = 50;
  private static final float THRESHOLD = 0.5f;
  private final int maxCheckLength;

  public UnicodeBasedDetector() {
    this(DEFAULT_MAX_CHECK_LENGTH);
  }

  public UnicodeBasedDetector(int maxCheckLength) {
    this.maxCheckLength = maxCheckLength;
  }

  public List<String> getDominantLangCodes(String str) {
    // For a more complete list of script/language relations,
    // see https://unicode-org.github.io/cldr-staging/charts/37/supplemental/scripts_and_languages.html
    // Another more complete approach might be to use Character.UnicodeScript.of() for each character.
    int arabicChars = 0;
    int cyrillicChars = 0;
    int cjkChars = 0;
    int khmerChars = 0;
    int tamilChars = 0;
    int greekChars = 0;
    int devanagariChars = 0;
    int thaiChars = 0;
    int hebrewChars = 0;
    int hangulChars = 0;
    int significantChars = 0;
    for (int i = 0; i < Math.min(str.length(), maxCheckLength); i++) {
      int val = str.charAt(i);
      if (!Character.isWhitespace(val) && !Character.isDigit(val) && val != '.') {
        significantChars++;
      }
      if (val >= 0x0600 && val <= 0x06FF) {
        arabicChars++;
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
      if (val >= 0x0E00 && val <= 0x0E7F) {
        thaiChars++;
      }
      if (val >= 0x0590 && val <= 0x05FF || val >= 0xFB1D && val <= 0xFB40) {
        hebrewChars++;
      }
      if (val >= 0xAC00 && val <= 0xD7AF ||  // https://en.wikipedia.org/wiki/Hangul
              val >= 0x1100 && val <= 0x11FF ||
              val >= 0x3130 && val <= 0x318F ||
              val >= 0xA960 && val <= 0xA97F ||
              val >= 0xD7B0 && val <= 0xD7FF) {
        hangulChars++;
      }
    }
    List<String> langCodes = new ArrayList<>();
    if ((float) arabicChars / significantChars >= THRESHOLD) {
      langCodes.add("ar");
      langCodes.add("fa");
    }
    if ((float) cyrillicChars / significantChars >= THRESHOLD) {
      langCodes.add("ru");
      langCodes.add("uk");
      langCodes.add("be");
    }
    if ((float) cjkChars / significantChars >= THRESHOLD) {
      langCodes.add("zh");
      langCodes.add("ja");
      // Korean: see hangulChars
    }
    if ((float) khmerChars / significantChars >= THRESHOLD) {
      langCodes.add("km");
    }
    if ((float) tamilChars / significantChars >= THRESHOLD) {
      langCodes.add("ta");
    }
    if ((float) greekChars / significantChars >= THRESHOLD) {
      langCodes.add("el");
    }
    if ((float) devanagariChars / significantChars >= THRESHOLD) {
      langCodes.add("hi");
      langCodes.add("mr");
    }
    if ((float) thaiChars / significantChars >= THRESHOLD) {
      langCodes.add("th");
    }
    if ((float) hebrewChars / significantChars >= THRESHOLD) {
      langCodes.add("he");
    }
    if ((float) hangulChars / significantChars >= THRESHOLD) {
      langCodes.add("ko");
    }
    //System.out.println("CJK: " + cjkChars);
    //System.out.println("Hangul: " + hangulChars);
    //
    // NOTE: if you add languages here that LT doesn't support, also update LanguageIdentifier.detectLanguage()
    //       so it makes use of the fact that we have safely detected a language by its character set
    //       (we can then directly assume it's not supported)
    //
    return langCodes;
  }
}
