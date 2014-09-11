/* LanguageTool, a natural language style checker
 * Copyright (C) 2009 Ionuț Păduraru
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
package org.languagetool.dev.wikipedia;

/**
 * Helper class for romanian diacritics correction. Many romanian texts
 * (including Romanian wikipedia) contain wrong diacritics: <b>ş</b> instead of
 * <b>ș</b> and <b>ţ</b> instead of <b>ț</b>.
 * 
 * @author Ionuț Păduraru
 * @deprecated deprecated since 2.7 (not used anymore)
 */
@Deprecated
public final class RomanianDiacriticsModifier {

  private static final int REPLACEMENT_BUFF_SIZE = 10 * 1024;
  
  private static char[] cCorrectDiacritics = null;
  private static char[] replacementBuff = null;

  private RomanianDiacriticsModifier() {
    // private constructor
  }

  /**
   * Initialize internal buffers
   */
  private static synchronized void initCharMap() {
    if (cCorrectDiacritics == null) {
      replacementBuff = new char[REPLACEMENT_BUFF_SIZE];
      cCorrectDiacritics = new char[Character.MAX_VALUE - Character.MIN_VALUE];
      char c = Character.MIN_VALUE;
      for (int i = 0; i < Character.MAX_VALUE - Character.MIN_VALUE; i++) {
        final char newC = diac(c);
        cCorrectDiacritics[i] = newC;
        c++;
      }
    }
  }

  /**
   * Single character correction. Used internally during buffers
   * initialization.
   */
  private static char diac(char c) {
    char result = c;
    switch (c) {
      case 'ş':
        result = 'ș';
        break;
      case 'ţ':
        result = 'ț';
        break;
      case 'Ţ':
        result = 'Ț';
        break;
      case 'Ş':
        result = 'Ș';
        break;
      default:
        break;
    }
    return result;
  }

  /**
   * Romanian diacritics correction: replace <b>ş</b> with <b>ș</b> and
   * <b>ţ</b> with <b>ț</b> (including upper-case variants).<br/>
   * Thread-safe method.
   */
  static synchronized String correctDiacritics(String s) {
    if (s == null) {
      return null;
    }
    initCharMap();
    final int length = s.length();
    // check buffer size
    if (length > replacementBuff.length) {
      replacementBuff = new char[length];
    }
    // get current chars
    s.getChars(0, length, replacementBuff, 0);
    // replace
    for (int i = 0; i < length; i++) {
      replacementBuff[i] = cCorrectDiacritics[replacementBuff[i]];
    }
    // return the corrected string
    return String.valueOf(replacementBuff, 0, length);
  }

}