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

package org.languagetool.tokenizers.ro;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RomanianWordTokenizerTest {

  @Test
  public void testTokenize() {
    // basic test - simple words, no diacritics
    RomanianWordTokenizer w = new RomanianWordTokenizer();
    List<String> testList = w.tokenize("Aceaste mese sunt bune");
    assertEquals(testList.size(), 7);
    assertEquals("[Aceaste,  , mese,  , sunt,  , bune]", testList.toString());
    
    // basic test - simle words, with diacritics
    testList = w.tokenize("Această carte este frumoasă");
    assertEquals(testList.size(), 7);
    assertEquals("[Această,  , carte,  , este,  , frumoasă]", testList.toString());
    
    // test for "-"
    testList = w.tokenize("nu-ți doresc");
    assertEquals(testList.size(), 5);
    assertEquals("[nu, -, ți,  , doresc]",
        testList.toString());

    // test for "„"
    testList = w.tokenize("zicea „merge");
    assertEquals(testList.size(), 4);
    assertEquals("[zicea,  , „, merge]",
        testList.toString());

    // test for "„" with white space
    testList = w.tokenize("zicea „ merge");
    assertEquals(testList.size(), 5);
    assertEquals("[zicea,  , „,  , merge]",
        testList.toString());

    // test for "„"
    testList = w.tokenize("zicea merge”");
    assertEquals(testList.size(), 4);
    assertEquals("[zicea,  , merge, ”]",
        testList.toString());
    
    // test for "„" and "„" 
    testList = w.tokenize("zicea „merge bine”");
    assertEquals(testList.size(), 7);
    assertEquals("[zicea,  , „, merge,  , bine, ”]",
        testList.toString());
    
    //ți-am
    testList = w.tokenize("ți-am");
    assertEquals(testList.size(), 3);
    assertEquals("[ți, -, am]",
        testList.toString());
    
    // test for "«" and "»" 
    testList = w.tokenize("zicea «merge bine»");
    assertEquals(testList.size(), 7);
    assertEquals("[zicea,  , «, merge,  , bine, »]",
        testList.toString());
    // test for "<" and ">" 
    testList = w.tokenize("zicea <<merge bine>>");
    assertEquals(testList.size(), 9);
    assertEquals("[zicea,  , <, <, merge,  , bine, >, >]",
        testList.toString());
    // test for "%"  
    testList = w.tokenize("avea 15% apă");
    assertEquals(testList.size(), 6);
    assertEquals("[avea,  , 15, %,  , apă]",
        testList.toString());
    // test for "°"  
    testList = w.tokenize("are 30°C");
    assertEquals(testList.size(), 5);
    assertEquals("[are,  , 30, °, C]",
        testList.toString());
    // test for "="  
    testList = w.tokenize("fructe=mere");
    assertEquals(testList.size(), 3);
    assertEquals("[fructe, =, mere]",
        testList.toString());
    // test for "|"  
    testList = w.tokenize("pere|mere");
    assertEquals(testList.size(), 3);
    assertEquals("[pere, |, mere]",
        testList.toString());
    // test for "\n"  
    testList = w.tokenize("pere\nmere");
    assertEquals(testList.size(), 3);
    assertEquals("[pere, \n, mere]",
        testList.toString());
    // test for "\r"  
    testList = w.tokenize("pere\rmere");
    assertEquals(testList.size(), 3);
    assertEquals("[pere, \r, mere]",
        testList.toString());
    // test for "\n\r"  
    testList = w.tokenize("pere\n\rmere");
    assertEquals(testList.size(), 4);
    assertEquals("[pere, \n, \r, mere]",
        testList.toString());
  }
}
