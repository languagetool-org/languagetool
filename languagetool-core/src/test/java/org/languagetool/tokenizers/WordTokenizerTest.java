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

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

import java.util.List;

public class WordTokenizerTest {

  private final WordTokenizer wordTokenizer = new WordTokenizer();

  @Test
  public void testTokenize() {
    WordTokenizer wordTokenizer = new WordTokenizer();
    List <String> tokens = wordTokenizer.tokenize("This is\u00A0a test");
    Assertions.assertEquals(tokens.size(), 7);
    Assertions.assertEquals("[This,  , is, \u00A0, a,  , test]", tokens.toString());
    tokens = wordTokenizer.tokenize("This\rbreaks");
    Assertions.assertEquals(3, tokens.size());
    Assertions.assertEquals("[This, \r, breaks]", tokens.toString());
    tokens = wordTokenizer.tokenize("dev.all@languagetool.org");
    Assertions.assertEquals(1, tokens.size());
    tokens = wordTokenizer.tokenize("dev.all@languagetool.org.");
    Assertions.assertEquals(2, tokens.size());
    tokens = wordTokenizer.tokenize("dev.all@languagetool.org:");
    Assertions.assertEquals(2, tokens.size());
    tokens = wordTokenizer.tokenize("Schreiben Sie Hr. Meier (meier@mail.com).");
    Assertions.assertEquals(tokens.size(), 13);
    tokens = wordTokenizer.tokenize("Get more at languagetool.org/foo, and via twitter");
    Assertions.assertEquals(14, tokens.size());
    Assertions.assertTrue(tokens.contains("languagetool.org/foo"));
    tokens = wordTokenizer.tokenize("Get more at sub.languagetool.org/foo, and via twitter");
    Assertions.assertEquals(14, tokens.size());
    Assertions.assertTrue(tokens.contains("sub.languagetool.org/foo"));
  }

  @Test
  public void testIsUrl() {
    Assertions.assertTrue(WordTokenizer.isUrl("www.languagetool.org"));
    Assertions.assertTrue(WordTokenizer.isUrl("languagetool.org/"));
    Assertions.assertTrue(WordTokenizer.isUrl("languagetool.org/foo"));
    Assertions.assertTrue(WordTokenizer.isUrl("subdomain.languagetool.org/"));
    Assertions.assertTrue(WordTokenizer.isUrl("http://www.languagetool.org"));
    Assertions.assertTrue(WordTokenizer.isUrl("https://www.languagetool.org"));
    Assertions.assertFalse(WordTokenizer.isUrl("languagetool.org"));  // not detected yet
    Assertions.assertFalse(WordTokenizer.isUrl("sub.languagetool.org"));  // not detected yet
    Assertions.assertFalse(WordTokenizer.isUrl("something-else"));
  }

  @Test
  public void testIsEMail() {
    Assertions.assertTrue(WordTokenizer.isEMail("martin.mustermann@test.de"));
    Assertions.assertTrue(WordTokenizer.isEMail("martin.mustermann@test.languagetool.de"));
    Assertions.assertTrue(WordTokenizer.isEMail("martin-mustermann@test.com"));
    Assertions.assertFalse(WordTokenizer.isEMail("@test.de"));
    Assertions.assertFalse(WordTokenizer.isEMail("f.test@test"));
    Assertions.assertFalse(WordTokenizer.isEMail("f@t.t"));
  }

  @Test
  public void testUrlTokenize() {
    Assertions.assertEquals("\"|This| |http://foo.org|.|\"", tokenize("\"This http://foo.org.\""));
    Assertions.assertEquals("«|This| |http://foo.org|.|»", tokenize("«This http://foo.org.»"));
    Assertions.assertEquals("This| |http://foo.org|.|.|.", tokenize("This http://foo.org..."));
    Assertions.assertEquals("This| |http://foo.org|.", tokenize("This http://foo.org."));
    Assertions.assertEquals("This| |http://foo.org| |blah", tokenize("This http://foo.org blah"));
    Assertions.assertEquals("This| |http://foo.org| |and| |ftp://bla.com| |blah", tokenize("This http://foo.org and ftp://bla.com blah"));
    Assertions.assertEquals("foo| |http://localhost:32000/?ch=1| |bar", tokenize("foo http://localhost:32000/?ch=1 bar"));
    Assertions.assertEquals("foo| |ftp://localhost:32000/| |bar", tokenize("foo ftp://localhost:32000/ bar"));
    Assertions.assertEquals("foo| |http://google.de/?aaa| |bar", tokenize("foo http://google.de/?aaa bar"));
    Assertions.assertEquals("foo| |http://www.flickr.com/123@N04/hallo#test| |bar", tokenize("foo http://www.flickr.com/123@N04/hallo#test bar"));
    Assertions.assertEquals("foo| |http://www.youtube.com/watch?v=wDN_EYUvUq0| |bar", tokenize("foo http://www.youtube.com/watch?v=wDN_EYUvUq0 bar"));
    Assertions.assertEquals("foo| |http://example.net/index.html?s=A54C6FE2%23info| |bar", tokenize("foo http://example.net/index.html?s=A54C6FE2%23info bar"));
    Assertions.assertEquals("foo| |https://writerduet.com/script/#V6922~***~branch=-MClu-LnPrTNz8oz_rJb| |bar", tokenize("foo https://writerduet.com/script/#V6922~***~branch=-MClu-LnPrTNz8oz_rJb bar"));
    Assertions.assertEquals("foo| |https://joe:passwd@example.net:8080/index.html?action=x&session=A54C6FE2#info| |bar", tokenize("foo https://joe:passwd@example.net:8080/index.html?action=x&session=A54C6FE2#info bar"));
  }

  @Test
  public void testUrlTokenizeWithQuote() {
    Assertions.assertEquals("This| |'|http://foo.org|'| |blah", tokenize("This 'http://foo.org' blah"));
    Assertions.assertEquals("This| |\"|http://foo.org|\"| |blah", tokenize("This \"http://foo.org\" blah"));
    Assertions.assertEquals("This| |(|\"|http://foo.org|\"|)| |blah", tokenize("This (\"http://foo.org\") blah"));   // issue #1226
  }

  @Test
  public void testUrlTokenizeWithAppendedCharacter() {
    Assertions.assertEquals("foo| |(|http://ex.net/p?a=x#i|)| |bar", tokenize("foo (http://ex.net/p?a=x#i) bar"));
    Assertions.assertEquals("foo| |http://ex.net/p?a=x#i|,| |bar", tokenize("foo http://ex.net/p?a=x#i, bar"));
    Assertions.assertEquals("foo| |http://ex.net/p?a=x#i|.| |bar", tokenize("foo http://ex.net/p?a=x#i. bar"));
    Assertions.assertEquals("foo| |http://ex.net/p?a=x#i|:| |bar", tokenize("foo http://ex.net/p?a=x#i: bar"));
    Assertions.assertEquals("foo| |http://ex.net/p?a=x#i|?| |bar", tokenize("foo http://ex.net/p?a=x#i? bar"));
    Assertions.assertEquals("foo| |http://ex.net/p?a=x#i|!| |bar", tokenize("foo http://ex.net/p?a=x#i! bar"));
  }

  @Test
  public void testIncompleteUrlTokenize() {
    Assertions.assertEquals("http|:|/", tokenize("http:/"));
    Assertions.assertEquals("http://", tokenize("http://"));
    Assertions.assertEquals("http://a", tokenize("http://a"));
    Assertions.assertEquals("foo| |http| |bar", tokenize("foo http bar"));
    Assertions.assertEquals("foo| |http|:| |bar", tokenize("foo http: bar"));
    Assertions.assertEquals("foo| |http|:|/| |bar", tokenize("foo http:/ bar"));
    Assertions.assertEquals("foo| |http://| |bar", tokenize("foo http:// bar"));
    Assertions.assertEquals("foo| |http://a| |bar", tokenize("foo http://a bar"));
    Assertions.assertEquals("foo| |http://|?| |bar", tokenize("foo http://? bar"));
  }

  private String tokenize(String text) {
    List<String> tokens = wordTokenizer.tokenize(text);
    return String.join("|", tokens);
  }
  
}
