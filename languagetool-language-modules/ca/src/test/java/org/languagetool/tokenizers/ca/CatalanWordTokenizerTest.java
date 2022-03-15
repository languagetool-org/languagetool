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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class CatalanWordTokenizerTest {

  @Test
  public void testTokenize() {
    CatalanWordTokenizer wordTokenizer = new CatalanWordTokenizer();
    List<String> tokens;
    
    tokens = wordTokenizer.tokenize("-contar-se'n-");
    Assertions.assertEquals("[-, contar, -se, 'n, -]", tokens.toString());
    tokens = wordTokenizer.tokenize("-M'agradaria.");
    Assertions.assertEquals("[-, M', agradaria, .]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("Visiteu 'http://www.softcatala.org'");
    Assertions.assertEquals("[Visiteu,  , ', http://www.softcatala.org, ']", tokens.toString());
    tokens = wordTokenizer.tokenize("Visiteu \"http://www.softcatala.org\"");
    Assertions.assertEquals("[Visiteu,  , \", http://www.softcatala.org, \"]", tokens.toString());
    tokens = wordTokenizer.tokenize("name@example.com");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("name@example.com.");
    Assertions.assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("name@example.com:");
    Assertions.assertEquals(tokens.size(), 2);
    tokens = wordTokenizer.tokenize("L'origen de name@example.com.");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[L', origen,  , de,  , name@example.com, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("L'origen de name@example.com i de name2@example.com.");
    Assertions.assertEquals(tokens.size(), 13);
    Assertions.assertEquals("[L', origen,  , de,  , name@example.com,  , i,  , de,  , name2@example.com, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("L'\"ala bastarda\".");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[L', \", ala,  , bastarda, \", .]", tokens.toString());
    tokens = wordTokenizer.tokenize("d'\"ala bastarda\".");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[d', \", ala,  , bastarda, \", .]", tokens.toString());
    tokens = wordTokenizer.tokenize("Emporta-te'ls a l'observatori dels mars");
    Assertions.assertEquals(tokens.size(), 13);
    Assertions.assertEquals("[Emporta, -te, 'ls,  , a,  , l', observatori,  , de, ls,  , mars]", tokens.toString());
    tokens = wordTokenizer.tokenize("Emporta-te’ls a l’observatori dels mars");
    Assertions.assertEquals(tokens.size(), 13);
    Assertions.assertEquals("[Emporta, -te, ’ls,  , a,  , l’, observatori,  , de, ls,  , mars]", tokens.toString());
    tokens = wordTokenizer.tokenize("‘El tren Barcelona-València’");
    Assertions.assertEquals(tokens.size(), 9);
    Assertions.assertEquals("[‘, El,  , tren,  , Barcelona, -, València, ’]", tokens.toString());
    tokens = wordTokenizer.tokenize("El tren Barcelona-València");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[El,  , tren,  , Barcelona, -, València]", tokens.toString());
    tokens = wordTokenizer.tokenize("No acabava d’entendre’l bé");
    Assertions.assertEquals(tokens.size(), 9);
    Assertions.assertEquals("[No,  , acabava,  , d’, entendre, ’l,  , bé]", tokens.toString());
    tokens = wordTokenizer.tokenize("N'hi ha vint-i-quatre");
    Assertions.assertEquals(tokens.size(), 6);
    Assertions.assertEquals("[N', hi,  , ha,  , vint-i-quatre]", tokens.toString());
    tokens = wordTokenizer.tokenize("Mont-ras");
    Assertions.assertEquals(tokens.size(), 1);
    Assertions.assertEquals("[Mont-ras]", tokens.toString());
    tokens = wordTokenizer.tokenize("És d'1 km.");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[És,  , d', 1,  , km, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("És d'1,5 km.");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[És,  , d', 1,5,  , km, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("És d'5 km.");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[És,  , d', 5,  , km, .]", tokens.toString());
    tokens = wordTokenizer.tokenize("la direcció E-SE");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[la,  , direcció,  , E, -, SE]", tokens.toString());
    tokens = wordTokenizer.tokenize("la direcció NW-SE");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[la,  , direcció,  , NW, -, SE]", tokens.toString());
    tokens = wordTokenizer.tokenize("Se'n dóna vergonya");
    Assertions.assertEquals(tokens.size(), 6);
    Assertions.assertEquals("[Se, 'n,  , dóna,  , vergonya]", tokens.toString());
    tokens = wordTokenizer.tokenize("Emília-Romanya");
    Assertions.assertEquals(tokens.size(), 1);
    Assertions.assertEquals("[Emília-Romanya]", tokens.toString());
    tokens = wordTokenizer.tokenize("L'Emília-Romanya");
    Assertions.assertEquals(tokens.size(), 2);
    Assertions.assertEquals("[L', Emília-Romanya]", tokens.toString());
    tokens = wordTokenizer.tokenize("col·laboració");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("col.laboració");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("col•laboració");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("col·Laboració");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("abans-d’ahir");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("abans-d'ahir");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("Sud-Est");
    Assertions.assertEquals(tokens.size(), 3);
    Assertions.assertEquals("[Sud, -, Est]", tokens.toString());
    tokens = wordTokenizer.tokenize("Sud-est");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("10 000");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("1 000 000");
    Assertions.assertEquals(tokens.size(), 1);
    tokens = wordTokenizer.tokenize("2005 57 114");
    Assertions.assertEquals(tokens.size(), 3);
    Assertions.assertEquals("[2005,  , 57 114]", tokens.toString());   
    tokens = wordTokenizer.tokenize("2005 454");
    Assertions.assertEquals(tokens.size(), 3);
    Assertions.assertEquals("[2005,  , 454]", tokens.toString());
    tokens = wordTokenizer.tokenize("$1");
    Assertions.assertEquals(tokens.size(), 1);
    Assertions.assertEquals("[$1]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("AVALUA'T");
    Assertions.assertEquals(tokens.size(), 2);
    Assertions.assertEquals("[AVALUA, 'T]", tokens.toString());
    tokens = wordTokenizer.tokenize("Tel-Aviv");
    Assertions.assertEquals(tokens.size(), 1);
    Assertions.assertEquals("[Tel-Aviv]", tokens.toString());
    tokens = wordTokenizer.tokenize("\"El cas 'Barcelona'\"");
    Assertions.assertEquals(tokens.size(), 9);
    Assertions.assertEquals("[\", El,  , cas,  , ', Barcelona, ', \"]", tokens.toString());
    tokens = wordTokenizer.tokenize("\"El cas 'd'aquell'\"");
    Assertions.assertEquals(tokens.size(), 10);
    Assertions.assertEquals("[\", El,  , cas,  , ', d', aquell, ', \"]", tokens.toString());
    tokens = wordTokenizer.tokenize("\"El cas ‘d’aquell’\"");
    Assertions.assertEquals(tokens.size(), 10);
    Assertions.assertEquals("[\", El,  , cas,  , ‘, d’, aquell, ’, \"]", tokens.toString());
    tokens = wordTokenizer.tokenize("Sàsser-l'Alguer");
    Assertions.assertEquals(tokens.size(), 4);
    Assertions.assertEquals("[Sàsser, -, l', Alguer]", tokens.toString());
    tokens = wordTokenizer.tokenize("Castella-la Manxa");
    Assertions.assertEquals(tokens.size(), 5);
    Assertions.assertEquals("[Castella, -, la,  , Manxa]", tokens.toString());
    tokens = wordTokenizer.tokenize("Qui-sap-lo temps");
    Assertions.assertEquals(tokens.size(), 3);
    Assertions.assertEquals("[Qui-sap-lo,  , temps]", tokens.toString());
  }
}
