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

package org.languagetool.tokenizers.nl;

import java.util.List;

import junit.framework.TestCase;

public class DutchWordTokenizerTest extends TestCase {

  public void testTokenize() {
    DutchWordTokenizer wordTokenizer = new DutchWordTokenizer();
    List<String> testList = wordTokenizer.tokenize("This is\u00A0a test");
    assertEquals(testList.size(), 7);
    assertEquals("[This,  , is, \u00A0, a,  , test]", testList.toString());
    testList = wordTokenizer.tokenize("Bla bla oma's bla bla 'test");
    assertEquals(testList.size(), 12);
    assertEquals("[Bla,  , bla,  , oma's,  , bla,  , bla,  , ', test]",
        testList.toString());
    testList = wordTokenizer.tokenize("Ik zie het''");
    assertEquals(7, testList.size());
    assertEquals("[Ik,  , zie,  , het, ', ']",
        testList.toString());
    testList = wordTokenizer.tokenize("''Ik zie het");
    assertEquals(7, testList.size());
    assertEquals("[', ', Ik,  , zie,  , het]",
        testList.toString());
  }
}
