/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Sohaib Afifi, Taha Zerrouki
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
package org.languagetool.tokenizers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @since 4.9
 */
public class ArabicWordTokenizer extends WordTokenizer {

  @Override
  public String getTokenizingCharacters() {
    return super.getTokenizingCharacters() + "،؟؛";
  }

  /**
   * Tokenizes text.
   * The Arabic tokenizer differs from the standard one
   * in some respects:
   * <ol>
   * <li> strip Tashkeel and tatweel;</li>
   * </ol>
   *
   * @param text String of words to tokenize.
   */
  @Override
  public List<String> tokenize(String text) {
    List<String> l = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, getTokenizingCharacters(), true);
    while (st.hasMoreElements()) {
      String token = st.nextToken();
      // Strip Tashkeel and tatweel ([ًٌٍَُِْـ]",")
      String striped = token.replaceAll("[\u064B\u064C\u064D\u064E\u064F\u0650\u0651\u0652\u0653\u0654\u0655\u0656\u0640]", "");
      l.add(striped);
    }
    return joinEMailsAndUrls(l);
  }
}
