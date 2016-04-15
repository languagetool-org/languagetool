/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tokenizers.ml;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.languagetool.tokenizers.Tokenizer;

/**
 * Tokenizes a sentence into words. Punctuation and whitespace gets its own token.
 * 
 * @author Daniel Naber
 */
public class MalayalamWordTokenizer implements Tokenizer {

  public MalayalamWordTokenizer() {
  }
  
  @Override
  public List<String> tokenize(String text) {
    List<String> tokens = new ArrayList<>();
    StringTokenizer st = new StringTokenizer(text, 
        "\u0020\u00A0\u115f\u1160\u1680" 
        + ",.;()[]{}!?:\"'’‘„“”…\\/\t\n", true);
    while (st.hasMoreElements()) {
      tokens.add(st.nextToken());
    }
    return tokens;
  }
  
}
