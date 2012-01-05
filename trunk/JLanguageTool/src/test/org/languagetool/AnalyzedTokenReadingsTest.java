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

package org.languagetool;

import junit.framework.TestCase;

public class AnalyzedTokenReadingsTest extends TestCase {

  public void testNewTags() {
    AnalyzedTokenReadings tokenReadings = new AnalyzedTokenReadings(new AnalyzedToken("word", "POS", "lemma"));
    assertEquals(false, tokenReadings.isLinebreak());
    assertEquals(false, tokenReadings.isSentEnd());
    assertEquals(false, tokenReadings.isParaEnd());
    assertEquals(false, tokenReadings.isSentStart());
    tokenReadings.setSentEnd();
    assertEquals(false, tokenReadings.isSentStart());
    assertEquals(true, tokenReadings.isSentEnd());
    //test SEND_END or PARA_END added without directly via addReading
    //which is possible e.g. in rule disambiguator 
    tokenReadings = new AnalyzedTokenReadings(new AnalyzedToken("word", null, "lemma"));    
    tokenReadings.addReading(new AnalyzedToken("word", "SENT_END", null));
    assertEquals(true, tokenReadings.isSentEnd());
    assertEquals(false, tokenReadings.isParaEnd());
    tokenReadings.addReading(new AnalyzedToken("word", "PARA_END", null));
    assertEquals(true, tokenReadings.isParaEnd());
    assertEquals(false, tokenReadings.isSentStart());
    //but you can't add SENT_START to a non-empty token
    //and get isSentStart == true
    tokenReadings.addReading(new AnalyzedToken("word", "SENT_START", null));
    assertEquals(false, tokenReadings.isSentStart());
    AnalyzedToken aTok = new AnalyzedToken("word", "POS", "lemma");
    aTok.setWhitespaceBefore(true);
    tokenReadings = new AnalyzedTokenReadings(aTok);       
    assertEquals(aTok, tokenReadings.getAnalyzedToken(0));
    AnalyzedToken aTok2 = new AnalyzedToken("word", "POS", "lemma");
    assertTrue(!aTok2.equals(tokenReadings.getAnalyzedToken(0)));
    AnalyzedToken aTok3 = new AnalyzedToken("word", "POS", "lemma");
    aTok3.setWhitespaceBefore(true);
    assertEquals(aTok3, tokenReadings.getAnalyzedToken(0));
  }
}
