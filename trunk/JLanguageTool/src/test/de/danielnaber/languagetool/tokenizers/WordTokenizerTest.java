package de.danielnaber.languagetool.tokenizers;

import junit.framework.TestCase;

import java.util.List;

public class WordTokenizerTest extends TestCase {

  public void testTokenize() {
    WordTokenizer w = new WordTokenizer();
    List <String> testList = w.tokenize("This is\u00A0a test");
    assertEquals(testList.size(), 7);
    assertEquals("[This,  , is, \u00A0, a,  , test]", testList.toString());
  }
}
