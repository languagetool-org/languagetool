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

package org.languagetool.tokenizers.pt;

import org.junit.Test;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Tiago F. Santos
 * @since 3.6
 */

public class PortugueseWordTokenizerTest {
  final PortugueseWordTokenizer wordTokenizer = new PortugueseWordTokenizer();

  private void testTokenisation(String sentence, String[] tokens) {
    assertArrayEquals(tokens, wordTokenizer.tokenize(sentence).toArray());
  }

  @Test
  public void testTokenize() {
    testTokenisation("Isto é\u00A0um teste", new String[]{"Isto", " ", "é", " ", "um", " ", "teste"});
    testTokenisation("Isto\rquebra", new String[]{"Isto", "\r", "quebra"});

    // Hyphen with no whitespace
    testTokenisation("Agora isto sim é-mesmo!-um teste.",
      new String[]{"Agora", " ", "isto", " ", "sim", " ", "é", "-", "mesmo", "!", "-", "um", " ", "teste", "."});

    // Hyphen at the end of the word
    testTokenisation("Agora isto é- realmente!- um teste.",
      new String[]{"Agora", " ", "isto", " ", "é", "-", " ", "realmente", "!", "-", " ", "um", " ", "teste", "."});

    // M dash
    testTokenisation("Agora isto é—realmente!—um teste.",
      new String[]{"Agora", " ", "isto", " ", "é", "—", "realmente", "!", "—", "um", " ", "teste", "."});

    // More fun with hyphens
    testTokenisation("sex-appeal", new String[]{"sex-appeal"});
    testTokenisation("Aix-en-Provence", new String[]{"Aix-en-Provence"});
    testTokenisation("Montemor-o-Novo", new String[]{"Montemor-o-Novo"});

    // Twitter and whatnot; same as English
    testTokenisation("#CantadasDoBem @user", new String[]{"#", "CantadasDoBem", " ", "@user"});
  }
}
