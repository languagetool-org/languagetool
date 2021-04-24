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
import java.util.Arrays;
import static org.junit.Assert.assertEquals;

public class EnglishWordTokenizerTest {
  private final EnglishWordTokenizer wordTokenizer = new EnglishWordTokenizer();

  @Test
  public void testToken1(){
    final List <String> tokens = wordTokenizer.tokenize("This is\u00A0a test");
    assertEquals(tokens.size(), 7);
    assertEquals("[This,  , is, \u00A0, a,  , test]", tokens.toString());
  }

  @Test
  public void testToken2(){
    final List <String> tokens2 = wordTokenizer.tokenize("This\rbreaks");
    assertEquals(3, tokens2.size());
    assertEquals("[This, \r, breaks]", tokens2.toString());
  }

  @Test
  public void testToken3(){
    //hyphen with no whitespace
    final List <String> tokens3 = wordTokenizer.tokenize("Now this is-really!-a test.");
    assertEquals(tokens3.size(), 11);
    assertEquals("[Now,  , this,  , is-really, !, -, a,  , test, .]", tokens3.toString());
  }
  @Test
  public void testToken4(){
    //hyphen at the end of the word
    final List <String> tokens4 = wordTokenizer.tokenize("Now this is- really!- a test.");
    assertEquals(tokens4.size(), 15);
    assertEquals("[Now,  , this,  , is, -,  , really, !, -,  , a,  , test, .]", tokens4.toString());
  }
  @Test
  public void testToken5(){
    //mdash
    final List <String> tokens5 = wordTokenizer.tokenize("Now this is—really!—a test.");
    assertEquals(tokens5.size(), 13);
    assertEquals("[Now,  , this,  , is, —, really, !, —, a,  , test, .]", tokens5.toString());
  }
  @Test
  public void testToken6(){
    //exception
    final List <String> tokens6 = wordTokenizer.tokenize("fo'c'sle");
    assertEquals(tokens6.size(), 1);
  }
  @Test
  public void testToken7(){
    //contractions
    final List <String> tokens7 = wordTokenizer.tokenize("I'm John.");
    assertEquals("[I, 'm,  , John, .]", tokens7.toString());
    assertEquals(tokens7.size(), 5);
  }
  @Test
  public void testToken8(){
    final List <String> tokens8 = wordTokenizer.tokenize("You hadn’t.");
    assertEquals("[You,  , had, n’t, .]", tokens8.toString());
    assertEquals(tokens8.size(), 5);
  }
  @Test
  public void testToken9(){
    final List <String> tokens10 = wordTokenizer.tokenize("'We're'");
    assertEquals("[', We, 're, ']", tokens10.toString());
    assertEquals(tokens10.size(), 4);
  }
  @Test
  public void testToken10(){
    final List <String> tokens11 = wordTokenizer.tokenize("'We’re the best.'");
    assertEquals("[', We, ’re,  , the,  , best, ., ']", tokens11.toString());
    assertEquals(tokens11.size(), 9);
  }
  @Test
  public void testToken11(){
    final List <String> tokens12 = wordTokenizer.tokenize("'Don't do it'");
    assertEquals("[', Do, n't,  , do,  , it, ']", tokens12.toString());
    assertEquals(tokens12.size(), 8);
  }
  @Test
  public void testToken12(){
    final List <String> tokens13 = wordTokenizer.tokenize("‘Don’t do it’");
    assertEquals("[‘, Do, n’t,  , do,  , it, ’]", tokens13.toString());
    assertEquals(tokens13.size(), 8);
  }
  @Test
  public void testToken13(){
    final List <String> tokens14 = wordTokenizer.tokenize("Don't do it");
    assertEquals("[Do, n't,  , do,  , it]", tokens14.toString());
    assertEquals(tokens14.size(), 6); 
  }
/*
  @Test
  public void testToken2{
    
  }
*/

  @Test
  public void testOnetoken(){
    String oneToken = "Bahá'í";
    final List <String> tokens = wordTokenizer.tokenize(oneToken);
    assertEquals("[Bahá'í]", tokens.toString());
    assertEquals(tokens.size(), 1);
  }

  @Test
  public void testOnetoken2(){
    String oneToken = "Baha'i";
    final List <String> tokens = wordTokenizer.tokenize(oneToken);
    assertEquals("[Baha'i]", tokens.toString());
    assertEquals(tokens.size(), 1);
  }

  @Test
  public void testOnetoken3(){
    String oneToken = " Baha'i ";
    List<String> oneTokenList = Arrays.asList(" ", "Baha'i", " ");
    final List <String> tokens = wordTokenizer.tokenize(oneToken);
    assertEquals(oneTokenList, tokens);
    assertEquals(tokens.size(), 3);
  }

  @Test
  public void testOnetoken4(){
    String oneToken = "‘Swindon's Baha'i religious group’";
    List<String> oneTokenList = Arrays.asList("‘", "Swindon", "'s", " ","Baha'i", " ", "religious", " ", "group", "’");
    final List <String> tokens = wordTokenizer.tokenize(oneToken);
    assertEquals(oneTokenList, tokens);
    assertEquals(tokens.size(), 10);
  }

}
