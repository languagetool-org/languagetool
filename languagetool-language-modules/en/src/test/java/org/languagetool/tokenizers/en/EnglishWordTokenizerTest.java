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
    assertEquals(7, tokens.size());
    assertEquals("[This,  , is, \u00A0, a,  , test]", tokens.toString());
  }

  @Test
  public void testToken2(){
    final List <String> tokens = wordTokenizer.tokenize("This\rbreaks");
    assertEquals(3, tokens.size());
    assertEquals("[This, \r, breaks]", tokens.toString());
  }

  @Test
  public void testToken3(){
    //hyphen with no whitespace
    final List <String> tokens = wordTokenizer.tokenize("Now this is-really!-a test.");
    assertEquals(11, tokens.size());
    assertEquals("[Now,  , this,  , is-really, !, -, a,  , test, .]", tokens.toString());
  }
  @Test
  public void testToken4(){
    //hyphen at the end of the word
    final List <String> tokens = wordTokenizer.tokenize("Now this is- really!- a test.");
    assertEquals(15, tokens.size());
    assertEquals("[Now,  , this,  , is, -,  , really, !, -,  , a,  , test, .]", tokens.toString());
  }
  @Test
  public void testToken5(){
    //mdash
    final List <String> tokens = wordTokenizer.tokenize("Now this is—really!—a test.");
    assertEquals(13, tokens.size());
    assertEquals("[Now,  , this,  , is, —, really, !, —, a,  , test, .]", tokens.toString());
  }
  @Test
  public void testToken6(){
    //exception
    final List <String> tokens = wordTokenizer.tokenize("fo'c'sle");
    assertEquals(1, tokens.size());
  }
  @Test
  public void testToken7(){
    //contraction
    final List <String> tokens = wordTokenizer.tokenize("I'm John.");
    assertEquals("[I, 'm,  , John, .]", tokens.toString());
    assertEquals(5, tokens.size());
  }
  @Test
  public void testToken8(){
    final List <String> tokens = wordTokenizer.tokenize("You hadn’t.");
    assertEquals("[You,  , had, n’t, .]", tokens.toString());
    assertEquals(5, tokens.size());
  }
  @Test
  public void testToken9(){
    final List <String> tokens = wordTokenizer.tokenize("'We're'");
    assertEquals("[', We, 're, ']", tokens.toString());
    assertEquals(4, tokens.size());
  }
  @Test
  public void testToken10(){
    final List <String> tokens = wordTokenizer.tokenize("'We’re the best.'");
    assertEquals("[', We, ’re,  , the,  , best, ., ']", tokens.toString());
    assertEquals(9, tokens.size());
  }
  @Test
  public void testToken11(){
    final List <String> tokens = wordTokenizer.tokenize("'Don't do it'");
    assertEquals("[', Do, n't,  , do,  , it, ']", tokens.toString());
    assertEquals(8, tokens.size());
  }
  @Test
  public void testToken12(){
    final List <String> tokens = wordTokenizer.tokenize("‘Don’t do it’");
    assertEquals("[‘, Do, n’t,  , do,  , it, ’]", tokens.toString());
    assertEquals(8, tokens.size());
  }
  @Test
  public void testToken13(){
    final List <String> tokens = wordTokenizer.tokenize("Don't do it");
    assertEquals("[Do, n't,  , do,  , it]", tokens.toString());
    assertEquals(6, tokens.size()); 
  }

  @Test
  public void testToken14(){
    String oneToken = "Bahá'í";
    List<String> oneTokenList = Arrays.asList("Bahá'í");
    final List <String> tokens = wordTokenizer.tokenize(oneToken);
    assertEquals(oneTokenList, tokens);
    assertEquals(1, tokens.size());
  }
  /** test case for issue #2096 */
  @Test
  public void testToken15(){
    String oneToken = "‘Swindon's Baha'i religious group’";
    List<String> oneTokenList = Arrays.asList(
      "‘", "Swindon", "'s", " ","Baha'i", " ", "religious", " ", "group", "’"
    );
    final List <String> tokens = wordTokenizer.tokenize(oneToken);
    assertEquals(oneTokenList, tokens);
    assertEquals(10, tokens.size());
  }
  /** test case for issue #2096 */
  @Test
  public void testToken16(){
    String oneToken = " Bahá'í  Baha'i  Bahá'í";
    List<String> oneTokenList = Arrays.asList(
      " ", "Bahá'í", " ", " ", "Baha'i", " ", " ","Bahá'í"
    );
    final List <String> tokens = wordTokenizer.tokenize(oneToken);
    assertEquals(oneTokenList, tokens);
    assertEquals(8, tokens.size());
  }
}