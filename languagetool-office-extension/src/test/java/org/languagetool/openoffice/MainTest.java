/* LanguageTool, a natural language style checker 
 * Copyright (C) 2009 Marcin Miłkowski (http://www.languagetool.org)
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
package org.languagetool.openoffice;

import junit.framework.TestCase;

import com.sun.star.beans.PropertyValue;
import com.sun.star.lang.Locale;
import com.sun.star.linguistic2.ProofreadingResult;

public class MainTest extends TestCase {
  
  public void testDoProofreading() {
    final Main prog = new Main(null);
    final String testString = "To jest trudne zdanie. A to następne.  A to przedostatnie jest.\u0002 Test ostatniego.";
    final Locale plLoc = new Locale("pl", "PL", "");
    final PropertyValue[] prop = new PropertyValue[0];
    for (int i = 0; i<=testString.length(); i++) {
      final ProofreadingResult paRes = prog.doProofreading("1", testString, plLoc, i, testString.length(), prop);
      assertEquals("1", paRes.aDocumentIdentifier);
      assertTrue(paRes.nStartOfNextSentencePosition >= i);
      if (i < "To jest trudne zdanie. ".length()) {
        assertEquals("To jest trudne zdanie. ".length(), paRes.nStartOfNextSentencePosition);
        assertEquals(0, paRes.nStartOfSentencePosition);
      }
    }
    final ProofreadingResult paRes1 = prog.doProofreading("1", testString, plLoc, 0, testString.length(), prop);
    assertEquals("1", paRes1.aDocumentIdentifier);
    assertEquals(23, paRes1.nStartOfNextSentencePosition);
    assertEquals(0, paRes1.nStartOfSentencePosition);
    //that was causing NPE but not anymore:
    final String testString2 = "To jest „nowy problem”. A to inny jeszcze( „problem. Co jest „?"; 
    final ProofreadingResult paRes2 = prog.doProofreading("1", testString2, plLoc, 0, testString2.length(), prop);
    assertEquals("1", paRes2.aDocumentIdentifier);
    assertEquals(24, paRes2.nStartOfNextSentencePosition);
    assertEquals(0, paRes2.nStartOfSentencePosition);
  }
  
  public void testShortenComment() {
    final Main prog = new Main(null);
    final String testString = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt.";
    final String testStringShortened = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt.";
    final String testLongString = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt ut labore (et dolore magna aliqua).";
    final String testLongStringShortened = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod tempor incidunt ut labore.";
    final String testVeryLongString = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod (tempor incidunt [ut labore et dolore magna aliqua]).";
    final String testVeryLongStringShortened = "Lorem ipsum dolor sit amet, consectetur (adipisici elit), sed eiusmod (tempor incidunt).";
    final String shortenedString = prog.shortenComment(testString);
    assertEquals(testStringShortened, shortenedString);
    final String shortenedLongString = prog.shortenComment(testLongString);
    assertEquals(testLongStringShortened, shortenedLongString);
    final String shortenedVeryLongString = prog.shortenComment(testVeryLongString);
    assertEquals(testVeryLongStringShortened, shortenedVeryLongString);
  }

}
