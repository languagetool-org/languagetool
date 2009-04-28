package de.danielnaber.languagetool.openoffice;

import junit.framework.TestCase;

import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.ProofreadingResult;
import com.sun.star.beans.PropertyValue;

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

}
