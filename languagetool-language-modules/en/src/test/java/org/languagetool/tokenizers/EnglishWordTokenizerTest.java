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

package org.languagetool.tokenizers;

import java.util.List;

import junit.framework.TestCase;

import org.languagetool.tokenizers.en.EnglishWordTokenizer;
import org.languagetool.tools.StringTools;

public class EnglishWordTokenizerTest extends TestCase {

  private final EnglishWordTokenizer wordTokenizer = new EnglishWordTokenizer();

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
    assertEquals(tokens3.size(), 10);
    assertEquals("[Now,  , this,  , is-really, !, -a,  , test, .]", tokens3.toString());
    //hyphen at the end of the word
    final List <String> tokens4 = wordTokenizer.tokenize("Now this is- really!- a test.");
    assertEquals(tokens4.size(), 15);
    assertEquals("[Now,  , this,  , is, -,  , really, !, -,  , a,  , test, .]", tokens4.toString());
  }

  public void testUrlTokenize() {
    assertEquals("This| |http://foo.org| |blah", tokenize("This http://foo.org blah"));
    assertEquals("This| |http://foo.org| |and| |ftp://bla.com| |blah", tokenize("This http://foo.org and ftp://bla.com blah"));
    assertEquals("foo| |http://localhost:32000/?ch=1| |bar", tokenize("foo http://localhost:32000/?ch=1 bar"));
    assertEquals("foo| |ftp://localhost:32000/| |bar", tokenize("foo ftp://localhost:32000/ bar"));
    assertEquals("foo| |http://google.de/?aaa| |bar", tokenize("foo http://google.de/?aaa bar"));
    assertEquals("foo| |http://www.flickr.com/123@N04/hallo#test| |bar", tokenize("foo http://www.flickr.com/123@N04/hallo#test bar"));
    assertEquals("foo| |http://www.youtube.com/watch?v=wDN_EYUvUq0| |bar", tokenize("foo http://www.youtube.com/watch?v=wDN_EYUvUq0 bar"));
    assertEquals("foo| |http://example.net/index.html?s=A54C6FE2%23info| |bar", tokenize("foo http://example.net/index.html?s=A54C6FE2%23info bar"));
    assertEquals("foo| |https://joe:passwd@example.net:8080/index.html?action=x&session=A54C6FE2#info| |bar",
        tokenize("foo https://joe:passwd@example.net:8080/index.html?action=x&session=A54C6FE2#info bar"));
  }

  public void testUrlTokenizeWithAppendedCharacter() {
    assertEquals("foo| |(|http://ex.net/p?a=x#i|)| |bar", tokenize("foo (http://ex.net/p?a=x#i) bar"));
    assertEquals("foo| |http://ex.net/p?a=x#i|,| |bar", tokenize("foo http://ex.net/p?a=x#i, bar"));
    assertEquals("foo| |http://ex.net/p?a=x#i|.| |bar", tokenize("foo http://ex.net/p?a=x#i. bar"));
    assertEquals("foo| |http://ex.net/p?a=x#i|:| |bar", tokenize("foo http://ex.net/p?a=x#i: bar"));
    assertEquals("foo| |http://ex.net/p?a=x#i|?| |bar", tokenize("foo http://ex.net/p?a=x#i? bar"));
    assertEquals("foo| |http://ex.net/p?a=x#i|!| |bar", tokenize("foo http://ex.net/p?a=x#i! bar"));
  }

  public void testIncompleteUrlTokenize() {
    assertEquals("http|:|/", tokenize("http:/"));
    assertEquals("http://", tokenize("http://"));
    assertEquals("http://a", tokenize("http://a"));
    assertEquals("foo| |http| |bar", tokenize("foo http bar"));
    assertEquals("foo| |http|:| |bar", tokenize("foo http: bar"));
    assertEquals("foo| |http|:|/| |bar", tokenize("foo http:/ bar"));
    assertEquals("foo| |http://| |bar", tokenize("foo http:// bar"));
    assertEquals("foo| |http://a| |bar", tokenize("foo http://a bar"));
    assertEquals("foo| |http://|?| |bar", tokenize("foo http://? bar"));
  }

  private String tokenize(String text) {
    final List<String> tokens = wordTokenizer.tokenize(text);
    return StringTools.listToString(tokens, "|");
  }

}
