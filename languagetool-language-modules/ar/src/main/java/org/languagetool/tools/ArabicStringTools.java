/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.tools;

import java.text.Normalizer;

/**
 * Tools for working with arabic strings.
 *
 * @author Taha Zerrouki
 */
public class ArabicStringTools {

  public static final String TASHKEEL_CHARS =
    "\u064B"    // Fathatan
    + "\u064C"  // Dammatan
    + "\u064D"  // Kasratan
    + "\u064E"  // Fatha
    + "\u064F"  // Damma
    + "\u0650"  // Kasra
    + "\u0651"  // Shadda
    + "\u0652"  // Sukun
    + "\u0653"  // Maddah Above
    + "\u0654"  // Hamza Above
    + "\u0655"  // Hamza Below
    + "\u0656"  // Subscript Alef
    + "\u0640"; // Tatweel

  /**
   * Return <code>str</code> without tashkeel characters
   * @param str input str
   */
  public static String removeTashkeel(String str) {
    String s = Normalizer.normalize(str, Normalizer.Form.NFD);
     String striped = str.replaceAll("["
      + TASHKEEL_CHARS
      + "]", "");
     return striped;
   }
}
