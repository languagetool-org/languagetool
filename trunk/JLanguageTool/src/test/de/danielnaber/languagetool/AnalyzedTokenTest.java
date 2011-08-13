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

package de.danielnaber.languagetool;

import junit.framework.TestCase;

public class AnalyzedTokenTest extends TestCase {

  public void testToString() {
    AnalyzedToken testToken = new AnalyzedToken("word", "POS", "lemma");
    assertEquals("lemma/POS", testToken.toString());
    assertEquals("lemma", testToken.getLemma());
    testToken = new AnalyzedToken("word", "POS", null);
    assertEquals("word/POS", testToken.toString());
    assertEquals(null, testToken.getLemma());
    assertEquals("word", testToken.getToken());
  }
  
  public void testMatches() {
    AnalyzedToken testToken1 = new AnalyzedToken("word", "POS", "lemma");    
    AnalyzedToken testToken2 = new AnalyzedToken("", null, null);
    assertFalse(testToken1.matches(testToken2));
    testToken2 = new AnalyzedToken("word", null, null);
    assertTrue(testToken1.matches(testToken2));
    testToken2 = new AnalyzedToken("word", "POS", null);    		
    assertTrue(testToken1.matches(testToken2));
    testToken2 = new AnalyzedToken("word", "POS", "lemma");        
    assertTrue(testToken1.matches(testToken2));
    testToken2 = new AnalyzedToken("word", "POS1", "lemma");
    assertFalse(testToken1.matches(testToken2));
    testToken2 = new AnalyzedToken("word1", "POS", "lemma");
    assertFalse(testToken1.matches(testToken2));
    testToken2 = new AnalyzedToken("word", "POS", "lemma1");
    assertFalse(testToken1.matches(testToken2));
    testToken2 = new AnalyzedToken("", "POS", "lemma");
    assertTrue(testToken1.matches(testToken2));    
    testToken2 = new AnalyzedToken("", null, "lemma");
    assertTrue(testToken1.matches(testToken2));
  }
  
}
