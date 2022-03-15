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

package org.languagetool.tokenizers.es;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SpanishWordTokenizerTest {

  @Test
  public void testTokenize() {
    final SpanishWordTokenizer wordTokenizer = new SpanishWordTokenizer();
    List <String> tokens = wordTokenizer.tokenize("*test+");
    Assertions.assertEquals(tokens.size(), 3);
    Assertions.assertEquals("[*, test, +]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("best-seller Covid-19;sars-cov-2");
    Assertions.assertEquals(tokens.size(), 5);
    Assertions.assertEquals("[best-seller,  , Covid-19, ;, sars-cov-2]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("e-mails");
    Assertions.assertEquals(tokens.size(), 1);
    Assertions.assertEquals("[e-mails]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("$100");
    Assertions.assertEquals(tokens.size(), 1);
    Assertions.assertEquals("[$100]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("$1.000");
    Assertions.assertEquals(tokens.size(), 1);
    Assertions.assertEquals("[$1.000]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("$1,400.50");
    Assertions.assertEquals(tokens.size(), 1);
    Assertions.assertEquals("[$1,400.50]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("1,400.50$");
    Assertions.assertEquals(tokens.size(), 1);
    Assertions.assertEquals("[1,400.50$]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("Ven ‒dijo."); // \u2012
    Assertions.assertEquals(tokens.size(), 5);
    Assertions.assertEquals("[Ven,  , ‒, dijo, .]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("1.º");
    Assertions.assertEquals(tokens.size(), 1);
    
    tokens = wordTokenizer.tokenize("Es la 21.ª y el 45.º");
    Assertions.assertEquals(tokens.size(), 11);
    
    tokens = wordTokenizer.tokenize("Es la 21.a y el 45.o");
    Assertions.assertEquals(tokens.size(), 11);
    
    tokens = wordTokenizer.tokenize("11.as Jornadas de Estudio");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[11.as,  , Jornadas,  , de,  , Estudio]", tokens.toString());
    
    tokens = wordTokenizer.tokenize("al-Ándalus");
    Assertions.assertEquals(tokens.size(), 1);
  }
}
