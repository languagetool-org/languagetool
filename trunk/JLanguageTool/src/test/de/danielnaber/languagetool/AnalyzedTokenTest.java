package de.danielnaber.languagetool;

import junit.framework.TestCase;

public class AnalyzedTokenTest extends TestCase {

  public void testToString() {
  AnalyzedToken testToken = new AnalyzedToken("word", "POS", "lemma");
  assertEquals("lemma/POS", testToken.toString());
  assertEquals("lemma", testToken.getLemma());
  testToken = new AnalyzedToken("word", "POS", 0);
  assertEquals("word/POS", testToken.toString());
  assertEquals(null, testToken.getLemma());
  assertEquals("word", testToken.getToken());
  }
  
}
