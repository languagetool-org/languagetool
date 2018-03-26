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

package org.languagetool.tokenizers.en;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class EnglishWordTokenizerTest {

    private final EnglishWordTokenizer wordTokenizer = new EnglishWordTokenizer();

	
  @Test
  public void testTokenize() {
    final List <String> tokens = wordTokenizer.tokenize("This is\u00A0a test");
    assertEquals(tokens.size(), 7);
    assertEquals("[This,  , is, \u00A0, a,  , test]", tokens.toString());
    final List <String> tokens2 = wordTokenizer.tokenize("This\rbreaks");
    assertEquals(3, tokens2.size());
    assertEquals("[This, \r, breaks]", tokens2.toString());
    //hyphen with no whitespace
    final List <String> tokens3 = wordTokenizer.tokenize("Now this is-really!-a test.");
    assertEquals(tokens3.size(), 10);
    assertEquals("[Now,  , this,  , is-really, !, -a,  , test, .]", tokens3.toString());
    //hyphen at the end of the word
    final List <String> tokens4 = wordTokenizer.tokenize("Now this is- really!- a test.");
    assertEquals(tokens4.size(), 15);
    assertEquals("[Now,  , this,  , is, -,  , really, !, -,  , a,  , test, .]", tokens4.toString());
    //mdash
    final List <String> tokens5 = wordTokenizer.tokenize("Now this is—really!—a test.");
    assertEquals(tokens5.size(), 13);
    assertEquals("[Now,  , this,  , is, —, really, !, —, a,  , test, .]", tokens5.toString());
  }
  
  
  @Test
  public void testValidWordTokenize() {
    String input1  = "12.3.a";
    String input2  = " 12.3.a";
    String input3  = " 12.3.a ";
    String input4  = "12.3.a.";
    String input5  = " 12.3.a.";
    String input6  = " 12.3.a. ";
    String input7  = "12.3.a, 17.7.4";
    String input8  = "12.3.a, 17.7.4 ";
    String input9  = "12.3.a, 17.7.4.";
    String input10 = "12.3.a, 17.7.4. ";
    String input11 = "12.3.a, 17.7.4 .";
    String input12 = " 12.3.a, 17.7.4 ";
    String input13 = " 12.3.a, 17.7.4.";
    String input14 = " 12.3.a, 17.7.4. ";
    String input15 = "12.3.a, 17.7.4.T";
    String input16 = "12.3.a ,17.7.4";
    String input17 = "12.3.a ,17.7.4.";
    String input18 = "12.3.a , 17.7.4";
    String input19 = " 12.3.a, 17.7.4";
    String input20 = "12.3.a , 17.7.4";
    String input21 = "12.3.4,15.6.7, 24.5.6 .";
    String input22 = "12.3.4,15.6.7, 24.5.6.";
    String input23 = "12.3.4,15.6.7, 24.5.6";
    String input24 = "12.3.4, 15.6.7, 24.5.6.";
    assertEquals(1, wordTokenizer.tokenize(input1).size());
    assertEquals(2, wordTokenizer.tokenize(input2).size());
    assertEquals(3, wordTokenizer.tokenize(input3).size());
    assertEquals(2, wordTokenizer.tokenize(input4).size());
    assertEquals(3, wordTokenizer.tokenize(input5).size());
    assertEquals(4, wordTokenizer.tokenize(input6).size());
    assertEquals(4, wordTokenizer.tokenize(input7).size());
    assertEquals(5, wordTokenizer.tokenize(input8).size());
    assertEquals(5, wordTokenizer.tokenize(input9).size());
    assertEquals(6, wordTokenizer.tokenize(input10).size());
    assertEquals(6, wordTokenizer.tokenize(input11).size());
    assertEquals(6, wordTokenizer.tokenize(input12).size());
    assertEquals(6, wordTokenizer.tokenize(input13).size());
    assertEquals(7, wordTokenizer.tokenize(input14).size());
    assertEquals(6, wordTokenizer.tokenize(input15).size());
    assertEquals(4, wordTokenizer.tokenize(input16).size());
    assertEquals(5, wordTokenizer.tokenize(input17).size());
    assertEquals(5, wordTokenizer.tokenize(input18).size());
    assertEquals(5, wordTokenizer.tokenize(input19).size());
    assertEquals(5, wordTokenizer.tokenize(input20).size());
    assertEquals(8, wordTokenizer.tokenize(input21).size());
    assertEquals(7, wordTokenizer.tokenize(input22).size());
    assertEquals(6, wordTokenizer.tokenize(input23).size());
    assertEquals(8, wordTokenizer.tokenize(input24).size());
    assertEquals("[12.3.a]", wordTokenizer.tokenize(input1).toString());
    assertEquals("[ , 12.3.a]", wordTokenizer.tokenize(input2).toString());
    assertEquals("[ , 12.3.a,  ]", wordTokenizer.tokenize(input3).toString());
    assertEquals("[12.3.a, .]", wordTokenizer.tokenize(input4).toString());
    assertEquals("[ , 12.3.a, .]", wordTokenizer.tokenize(input5).toString());
    assertEquals("[ , 12.3.a, .,  ]", wordTokenizer.tokenize(input6).toString());
    assertEquals("[12.3.a, ,,  , 17.7.4]", wordTokenizer.tokenize(input7).toString());
    assertEquals("[12.3.a, ,,  , 17.7.4,  ]", wordTokenizer.tokenize(input8).toString());
    assertEquals("[12.3.a, ,,  , 17.7.4, .]", wordTokenizer.tokenize(input9).toString());
    assertEquals("[12.3.a, ,,  , 17.7.4, .,  ]", wordTokenizer.tokenize(input10).toString());
    assertEquals("[12.3.a, ,,  , 17.7.4,  , .]", wordTokenizer.tokenize(input11).toString());
    assertEquals("[ , 12.3.a, ,,  , 17.7.4,  ]", wordTokenizer.tokenize(input12).toString());
    assertEquals("[ , 12.3.a, ,,  , 17.7.4, .]", wordTokenizer.tokenize(input13).toString());
    assertEquals("[ , 12.3.a, ,,  , 17.7.4, .,  ]", wordTokenizer.tokenize(input14).toString());
    assertEquals("[12.3.a, ,,  , 17.7.4, ., T]", wordTokenizer.tokenize(input15).toString());
    assertEquals("[12.3.a,  , ,, 17.7.4]", wordTokenizer.tokenize(input16).toString());
    assertEquals("[12.3.a,  , ,, 17.7.4, .]", wordTokenizer.tokenize(input17).toString());
    assertEquals("[12.3.a,  , ,,  , 17.7.4]", wordTokenizer.tokenize(input18).toString());
    assertEquals("[ , 12.3.a, ,,  , 17.7.4]", wordTokenizer.tokenize(input19).toString());
    assertEquals("[12.3.a,  , ,,  , 17.7.4]", wordTokenizer.tokenize(input20).toString());
    assertEquals("[12.3.4, ,, 15.6.7, ,,  , 24.5.6,  , .]", wordTokenizer.tokenize(input21).toString());
    assertEquals("[12.3.4, ,, 15.6.7, ,,  , 24.5.6, .]", wordTokenizer.tokenize(input22).toString());
    assertEquals("[12.3.4, ,, 15.6.7, ,,  , 24.5.6]", wordTokenizer.tokenize(input23).toString());
    assertEquals("[12.3.4, ,,  , 15.6.7, ,,  , 24.5.6, .]", wordTokenizer.tokenize(input24).toString());
  }
  
}
