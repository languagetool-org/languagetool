package de.danielnaber.languagetool.dev.index;

import java.io.StringReader;

import junit.framework.TestCase;

import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class AnyCharTokenizerTest extends TestCase {

  public void testAnyCharTokenizer() throws Exception {
    StringReader sr = new StringReader("It's a good day, I liked it!");

    AnyCharTokenizer tokenizer = new AnyCharTokenizer(Version.LUCENE_31, sr);
    CharTermAttribute termAtt = tokenizer.addAttribute(CharTermAttribute.class);

    assertTrue(tokenizer.incrementToken());
    // AnyCharTokenizer emits the entire input (i.e. a sentence) as a single token
    assertEquals("It's a good day, I liked it!", termAtt.toString());

    // it emits only one token.
    assertFalse(tokenizer.incrementToken());

  }
}
