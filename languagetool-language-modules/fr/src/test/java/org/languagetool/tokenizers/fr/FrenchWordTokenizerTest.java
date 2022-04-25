/* LanguageTool, a natural language style checker 
 * Copyright (C) 2020 Jaume Ortolà
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

package org.languagetool.tokenizers.fr;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class FrenchWordTokenizerTest {

  @Test
  public void testTokenize() {
    FrenchWordTokenizer wordTokenizer = new FrenchWordTokenizer();
    List<String> tokens;
    tokens = wordTokenizer.tokenize("name@example.com");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("name@example.com.");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("name@example.com:");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("L'origen de name@example.com.");
    assertEquals(tokens.size(), 7);
    tokens = wordTokenizer.tokenize("jusqu'au bout");
    assertEquals(tokens.size(), 4);
    tokens = wordTokenizer.tokenize("d’aujourd’hui");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("d'aujourd’hui");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("d'aujourd'hui");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("entr'ouvrions");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("entr’ouvrions");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("Penses-tu");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("Strauss-Kahn");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("Semble-t-elle");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("N’est-il");
    assertEquals(tokens.size(), 3);    
    tokens = wordTokenizer.tokenize("Faites-le-moi");
    assertEquals(tokens.size(), 3);
    tokens = wordTokenizer.tokenize("donne-t-on");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("qu'est-ce");
    assertEquals(tokens.size(), 3);
    tokens = wordTokenizer.tokenize("t'es-tu");
    assertEquals(tokens.size(), 3);
    tokens = wordTokenizer.tokenize("rendez-vous");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("Petit-déjeunes-tu");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("Y-a-t-il");
    assertEquals(tokens.size(), 4);
    tokens = wordTokenizer.tokenize("va-t-en"); // wrong, correct: va-t'en
    assertEquals(tokens.size(), 4);
    tokens = wordTokenizer.tokenize("va-t'en");
    assertEquals(tokens.size(), 3);
    tokens = wordTokenizer.tokenize("va-t’en");
    assertEquals(tokens.size(), 3);
    tokens = wordTokenizer.tokenize("d'1");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("Rendez-Vous");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("sous-trai\u00ADtants");
    assertEquals(tokens.size(), 1);
    
    tokens = wordTokenizer.tokenize("-L'homme.");
    assertEquals(tokens.toString(), "[-, L', homme, .]");
    tokens = wordTokenizer.tokenize("-Oui -l'homme.");
    assertEquals(tokens.toString(), "[-, Oui,  , -, l', homme, .]");
    
    tokens = wordTokenizer.tokenize("Qu’est-ce que ç’a à voir ?");
    assertEquals(tokens.toString(), "[Qu’, est, -ce,  , que,  , ç’, a,  , à,  , voir,  , ?]");
    tokens = wordTokenizer.tokenize("Qu’est-ce que ç'a à voir ?");
    assertEquals(tokens.toString(), "[Qu’, est, -ce,  , que,  , ç', a,  , à,  , voir,  , ?]");
    tokens = wordTokenizer.tokenize("Ç’allait être le rêve du XVIIIe siècle.");
    assertEquals(tokens.toString(), "[Ç’, allait,  , être,  , le,  , rêve,  , du,  , XVIIIe,  , siècle, .]");
    
    tokens = wordTokenizer.tokenize("10 000");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("1 000 000");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("2005 57 114");
    assertEquals(tokens.size(), 3);
    assertEquals("[2005,  , 57 114]", tokens.toString());   
    tokens = wordTokenizer.tokenize("2005 454");
    assertEquals(tokens.size(), 3);
    assertEquals("[2005,  , 454]", tokens.toString());
    tokens = wordTokenizer.tokenize("$1");
    assertEquals(tokens.size(), 1);
    assertEquals("[$1]", tokens.toString());
  }
}
