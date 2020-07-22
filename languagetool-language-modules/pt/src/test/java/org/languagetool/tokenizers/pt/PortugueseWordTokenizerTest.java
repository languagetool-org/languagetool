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

import static org.junit.Assert.assertEquals;

/**
 * @author Tiago F. Santos
 * @since 3.6
 */

public class PortugueseWordTokenizerTest {

  @Test
  public void testTokenize() {
    final PortugueseWordTokenizer wordTokenizer = new PortugueseWordTokenizer();
    final List <String> tokens = wordTokenizer.tokenize("Isto é\u00A0um teste");
    assertEquals(tokens.size(), 7);
    assertEquals("[Isto,  , é, \u00A0, um,  , teste]", tokens.toString());
    final List <String> tokens2 = wordTokenizer.tokenize("Isto\rquebra");
    assertEquals(3, tokens2.size());
    assertEquals("[Isto, \r, quebra]", tokens2.toString());
    //hyphen with no whitespace
    final List <String> tokens3 = wordTokenizer.tokenize("Agora isto sim é-mesmo!-um teste.");
    assertEquals(tokens3.size(), 15);
    assertEquals("[Agora,  , isto,  , sim,  , é, -, mesmo, !, -, um,  , teste, .]", tokens3.toString());
    //hyphen at the end of the word
    final List <String> tokens4 = wordTokenizer.tokenize("Agora isto é- realmente!- um teste.");
    assertEquals(tokens4.size(), 15);
    assertEquals("[Agora,  , isto,  , é, -,  , realmente, !, -,  , um,  , teste, .]", tokens4.toString());
    //mdash
    final List <String> tokens5 = wordTokenizer.tokenize("Agora isto é—realmente!—um teste.");
    assertEquals(tokens5.size(), 13);
    assertEquals("[Agora,  , isto,  , é, —, realmente, !, —, um,  , teste, .]", tokens5.toString());
    
    final List <String> tokens6 = wordTokenizer.tokenize("sex-appeal");
    assertEquals(tokens6.size(), 1);
    assertEquals("[sex-appeal]", tokens6.toString());
    final List<String> tokens7 = wordTokenizer.tokenize("Aix-en-Provence");
    assertEquals(tokens7.size(), 1);
    assertEquals("[Aix-en-Provence]", tokens7.toString());
    final List<String> tokens8 = wordTokenizer.tokenize("Montemor-o-Novo");
    assertEquals(tokens8.size(), 1);
    assertEquals("[Montemor-o-Novo]", tokens8.toString());
    
  }
}
