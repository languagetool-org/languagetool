/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà
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

package org.languagetool.tokenizers.ca;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class CatalanWordTokenizerTest {

  @Test
  public void testTokenize() {
    CatalanWordTokenizer wordTokenizer = new CatalanWordTokenizer();
    List<String> tokens;
    tokens = wordTokenizer.tokenize("name@example.com");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("name@example.com.");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("name@example.com:");
    assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("L'origen de name@example.com.");
    assertEquals(tokens.size(), 7);
    assertEquals("[L', origen,  , de,  , name@example.com, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("L'origen de name@example.com i de name2@example.com.");
    assertEquals(tokens.size(), 13);
    assertEquals("[L', origen,  , de,  , name@example.com,  , i,  , de,  , name2@example.com, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("L'\"ala bastarda\".");
    assertEquals(tokens.size(), 7);
    assertEquals("[L', \", ala,  , bastarda, \", .]", tokens.toString());
    tokens = wordTokenizer.tokenize("d'\"ala bastarda\".");
    assertEquals(tokens.size(), 7);
    assertEquals("[d', \", ala,  , bastarda, \", .]", tokens.toString());
    tokens = wordTokenizer.tokenize("Emporta-te'ls a l'observatori dels mars");
    assertEquals(tokens.size(), 13);
    assertEquals(
        "[Emporta, -te, 'ls,  , a,  , l', observatori,  , de, ls,  , mars]",
        tokens.toString());
    tokens = wordTokenizer.tokenize("Emporta-te’ls a l’observatori dels mars");
    assertEquals(tokens.size(), 13);
    assertEquals(
        "[Emporta, -te, ’ls,  , a,  , l’, observatori,  , de, ls,  , mars]",
        tokens.toString());
    tokens = wordTokenizer.tokenize("‘El tren Barcelona-València’");
    assertEquals(tokens.size(), 9);
    assertEquals("[‘, El,  , tren,  , Barcelona, -, València, ’]", tokens.toString());
    tokens = wordTokenizer.tokenize("El tren Barcelona-València");
    assertEquals(tokens.size(), 7);
    assertEquals("[El,  , tren,  , Barcelona, -, València]", tokens.toString());
    tokens = wordTokenizer.tokenize("No acabava d’entendre’l bé");
    assertEquals(tokens.size(), 9);
    assertEquals("[No,  , acabava,  , d’, entendre, ’l,  , bé]", tokens.toString());
    tokens = wordTokenizer.tokenize("N'hi ha vint-i-quatre");
    assertEquals(tokens.size(), 6);
    assertEquals("[N', hi,  , ha,  , vint-i-quatre]", tokens.toString());
    tokens = wordTokenizer.tokenize("Mont-ras");
    assertEquals(tokens.size(), 1);
    assertEquals("[Mont-ras]", tokens.toString());
    tokens = wordTokenizer.tokenize("És d'1 km.");
    assertEquals(tokens.size(), 7);
    assertEquals("[És,  , d', 1,  , km, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("És d'1,5 km.");
    assertEquals(tokens.size(), 7);
    assertEquals("[És,  , d', 1,5,  , km, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("És d'5 km.");
    assertEquals(tokens.size(), 7);
    assertEquals("[És,  , d', 5,  , km, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("la direcció E-SE");
    assertEquals(tokens.size(), 7);
    assertEquals("[la,  , direcció,  , E, -, SE]", tokens.toString());
    tokens = wordTokenizer.tokenize("la direcció NW-SE");
    assertEquals(tokens.size(), 7);
    assertEquals("[la,  , direcció,  , NW, -, SE]", tokens.toString());
    tokens = wordTokenizer.tokenize("Se'n dóna vergonya");
    assertEquals(tokens.size(), 6);
    assertEquals("[Se, 'n,  , dóna,  , vergonya]", tokens.toString());
    tokens = wordTokenizer.tokenize("Emília-Romanya");
    assertEquals(tokens.size(), 3);
    assertEquals("[Emília, -, Romanya]", tokens.toString());
    tokens = wordTokenizer.tokenize("L'Emília-Romanya");
    assertEquals(tokens.size(), 4);
    assertEquals("[L', Emília, -, Romanya]", tokens.toString());
    tokens = wordTokenizer.tokenize("col·laboració");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("col.laboració");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("col•laboració");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("col·Laboració");
    assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("Sud-Est");
    assertEquals(tokens.size(), 3);
    assertEquals("[Sud, -, Est]", tokens.toString());
    tokens = wordTokenizer.tokenize("Sud-est");
    assertEquals(tokens.size(), 1);
    
  }
}
