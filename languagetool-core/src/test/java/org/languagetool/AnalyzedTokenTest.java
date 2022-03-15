/* LanguageTool, a natural language style checker 
 * Copyright (C) 2014 Daniel Naber, Marcin Miłkowski (http://www.languagetool.org)
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

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

public class AnalyzedTokenTest {

  @Test
  public void testToString() {
    AnalyzedToken testToken = new AnalyzedToken("word", "POS", "lemma");
    Assertions.assertEquals("lemma/POS", testToken.toString());
    Assertions.assertEquals("lemma", testToken.getLemma());
    AnalyzedToken testToken2 = new AnalyzedToken("word", "POS", null);
    Assertions.assertEquals("word/POS", testToken2.toString());
    Assertions.assertNull(testToken2.getLemma());
    Assertions.assertEquals("word", testToken2.getToken());
  }

  @Test
  public void testMatches() {
    AnalyzedToken testToken1 = new AnalyzedToken("word", "POS", "lemma");
    Assertions.assertFalse(testToken1.matches(new AnalyzedToken("", null, null)));
    Assertions.assertTrue(testToken1.matches(new AnalyzedToken("word", null, null)));
    Assertions.assertTrue(testToken1.matches(new AnalyzedToken("word", "POS", null)));
    Assertions.assertTrue(testToken1.matches(new AnalyzedToken("word", "POS", "lemma")));
    Assertions.assertFalse(testToken1.matches(new AnalyzedToken("word", "POS1", "lemma")));
    Assertions.assertFalse(testToken1.matches(new AnalyzedToken("word1", "POS", "lemma")));
    Assertions.assertFalse(testToken1.matches(new AnalyzedToken("word", "POS", "lemma1")));
    Assertions.assertTrue(testToken1.matches(new AnalyzedToken("", "POS", "lemma")));
    Assertions.assertTrue(testToken1.matches(new AnalyzedToken("", null, "lemma")));
  }
  
}
