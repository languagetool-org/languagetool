package org.languagetool.openoffice;

import junit.framework.TestCase;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.ProofreadingResult;

public class MainTest extends TestCase {
  
  public void testDoProofreading() {
    Main prog = new Main(null);
    final String testString = "To jest trudne zdanie. A to następne.  A to przedostatnie jest.\u0002 Test ostatniego.";
    final Locale plLoc = new Locale("pl", "PL", "");
    final PropertyValue[] prop = new PropertyValue[0];
    for (int i = 0; i<=testString.length(); i++) {
      ProofreadingResult paRes = prog.doProofreading("1", testString, plLoc, i, testString.length(), prop);
      assertEquals("1", paRes.aDocumentIdentifier);
      assertTrue(paRes.nStartOfNextSentencePosition >= i);
      if (i < "To jest trudne zdanie. ".length()) {
        assertEquals("To jest trudne zdanie. ".length(), paRes.nStartOfNextSentencePosition);
        assertEquals(0, paRes.nStartOfSentencePosition);
      }
    }
    ProofreadingResult paRes = prog.doProofreading("1", testString, plLoc, 0, testString.length(), prop);
    assertEquals("1", paRes.aDocumentIdentifier);
    assertEquals(23, paRes.nStartOfNextSentencePosition);
    assertEquals(0, paRes.nStartOfSentencePosition);
    //that was causing NPE but not anymore:
    String testString2 = "To jest „nowy problem”. A to inny jeszcze( „problem. Co jest „?"; 
    paRes = prog.doProofreading("1", testString2, plLoc, 0, testString2.length(), prop);
    assertEquals("1", paRes.aDocumentIdentifier);
    assertEquals(24, paRes.nStartOfNextSentencePosition);
    assertEquals(0, paRes.nStartOfSentencePosition);
  }
  
  public void testShortenComment() {
    Main prog = new Main(null);
    final String testString = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt.";
    final String testStringShortened = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt.";
    final String testLongString = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt ut labore (et dolore magna aliqua).";
    final String testLongStringShortened = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt ut labore.";
    final String testVeryLongString = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod (tempor incidunt [ut labore et dolore magna aliqua]).";
    final String testVeryLongStringShortened = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod (tempor incidunt).";
    String shortenedString = prog.shortenComment(testString);
    assertEquals(testStringShortened, shortenedString);
    String shortenedLongString = prog.shortenComment(testLongString);
    assertEquals(testLongStringShortened, shortenedLongString);
    String shortenedVeryLongString = prog.shortenComment(testVeryLongString);
    assertEquals(testVeryLongStringShortened, shortenedVeryLongString);
  }

}
