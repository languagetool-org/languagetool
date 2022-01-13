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

  @Test
  public void testTokenize() {
    final EnglishWordTokenizer wordTokenizer = new EnglishWordTokenizer();
    final List <String> tokens = wordTokenizer.tokenize("This is\u00A0a test");
    assertEquals(tokens.size(), 7);
    assertEquals("[This,  , is, \u00A0, a,  , test]", tokens.toString());
    final List <String> tokens2 = wordTokenizer.tokenize("This\rbreaks");
    assertEquals(3, tokens2.size());
    assertEquals("[This, \r, breaks]", tokens2.toString());
    //hyphen with no whitespace
    final List <String> tokens3 = wordTokenizer.tokenize("Now this is-really!-a test.");
    assertEquals(tokens3.size(), 11);
    assertEquals("[Now,  , this,  , is-really, !, -, a,  , test, .]", tokens3.toString());
    //hyphen at the end of the word
    final List <String> tokens4 = wordTokenizer.tokenize("Now this is- really!- a test.");
    assertEquals(tokens4.size(), 15);
    assertEquals("[Now,  , this,  , is, -,  , really, !, -,  , a,  , test, .]", tokens4.toString());
    //mdash
    final List <String> tokens5 = wordTokenizer.tokenize("Now this is—really!—a test.");
    assertEquals(tokens5.size(), 13);
    assertEquals("[Now,  , this,  , is, —, really, !, —, a,  , test, .]", tokens5.toString());
    //exception
    final List <String> tokens6 = wordTokenizer.tokenize("fo'c'sle");
    assertEquals(tokens6.size(), 1);
    //contractions
    final List <String> tokens7 = wordTokenizer.tokenize("I'm John.");
    assertEquals("[I, 'm,  , John, .]", tokens7.toString());
    assertEquals(tokens7.size(), 5);
    final List <String> tokens8 = wordTokenizer.tokenize("You hadn’t.");
    assertEquals("[You,  , had, n’t, .]", tokens8.toString());
    assertEquals(tokens7.size(), 5);
    final List <String> tokens9 = wordTokenizer.tokenize("We'are");
    assertEquals("[We, ', are]", tokens9.toString());
    assertEquals(tokens9.size(), 3);
    final List <String> tokens10 = wordTokenizer.tokenize("'We're'");
    assertEquals("[', We, 're, ']", tokens10.toString());
    assertEquals(tokens10.size(), 4);
    final List <String> tokens11 = wordTokenizer.tokenize("'We’re the best.'");
    assertEquals("[', We, ’re,  , the,  , best, ., ']", tokens11.toString());
    assertEquals(tokens11.size(), 9);
    final List <String> tokens12 = wordTokenizer.tokenize("'Don't do it'");
    assertEquals("[', Do, n't,  , do,  , it, ']", tokens12.toString());
    assertEquals(tokens12.size(), 8);
    final List <String> tokens13 = wordTokenizer.tokenize("‘Don’t do it’");
    assertEquals("[‘, Do, n’t,  , do,  , it, ’]", tokens13.toString());
    assertEquals(tokens13.size(), 8);
    final List <String> tokens14 = wordTokenizer.tokenize("Don't do it");
    assertEquals("[Do, n't,  , do,  , it]", tokens14.toString());
    assertEquals(tokens14.size(), 6);
    final List<String> tokens15 = wordTokenizer.tokenize("My address is address@email.com");
    assertEquals("[My,  , address,  , is,  , address@email.com]", tokens15.toString());
    assertEquals(tokens15.size(), 7);
    final List<String> tokens16 = wordTokenizer.tokenize("@test@test.social you are aweesome!");
    assertEquals("[@test@test.social,  , you,  , are,  , aweesome, !]", tokens16.toString());
    assertEquals(tokens16.size(), 8);
    final List<String> tokens17 = wordTokenizer.tokenize("My address is address@email.com or other@email.com.");
    assertEquals("[My,  , address,  , is,  , address@email.com,  , or,  , other@email.com, .]", tokens17.toString());
    assertEquals(tokens17.size(), 12);
    final List <String> tokens18 = wordTokenizer.tokenize("doin' that");
    assertEquals("[doin',  , that]", tokens18.toString());
    assertEquals(tokens18.size(), 3);
    final List <String> tokens19 = wordTokenizer.tokenize("ne’er e'er o’er jack-o'-lantern");
    assertEquals("[ne’er,  , e'er,  , o’er,  , jack-o'-lantern]", tokens19.toString());
    assertEquals(tokens19.size(), 7);
    
  }
}
