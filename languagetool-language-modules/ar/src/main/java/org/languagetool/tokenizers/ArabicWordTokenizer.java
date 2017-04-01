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
package org.languagetool.tokenizers;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class ArabicWordTokenizer extends WordTokenizer {

  @Override
  public String getTokenizingCharacters() {
    return "\u0020\u00A0\u115f" 
    		  + "\u1160\u1680"
    	      + "\u2000\u2001\u2002\u2003\u2004\u2005\u2006\u2007"
    	      + "\u2008\u2009\u200A\u200B\u200c\u200d\u200e\u200f"
    	      + "\u2028\u2029\u202a\u202b\u202c\u202d\u202e\u202f"
    	      + "\u205F\u2060\u2061\u2062\u2063\u206A\u206b\u206c\u206d"
    	      + "\u206E\u206F\u3000\u3164\ufeff\uffa0\ufff9\ufffa\ufffb"
    	      + ",.;()[]{}=*#∗×·+÷<>!:/|\\\"'«»„”“`´‘’‛′…¿¡→‼"
    	      + "—"  // em dash
    	      + "\t\n\r"
    	      + "،؟؛";
  }
  /**
   * Tokenizes text.
   * The Arabic tokenizer differs from the standard one
   * in some respects:
   * <ol>
   * <li> strip Tashkeel and tatweel;</li>
   * </ol>
   * @param text String of words to tokenize.
   */
  @Override
  public List<String> tokenize(String text) {
    List<String> l = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, getTokenizingCharacters(), true);
    while (st.hasMoreElements()) {
    	 String token = st.nextToken();
  		// Strip Tashkeel and tatweel
 		//String striped = token.replaceAll("[ًٌٍَُِْـ]","");
    	String striped = token.replaceAll("[\u064B\u064C\u064D\u064E\u064F\u0650\u0651\u0652\u0653\u0654\u0655\u0656\u0640]","");
 		l.add(striped);
    }
    return joinEMailsAndUrls(l);
  }
}
