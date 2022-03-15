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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class RomanianWordTokenizerTest {

  @Test
  public void testTokenize() {
    // basic test - simple words, no diacritics
    RomanianWordTokenizer w = new RomanianWordTokenizer();
    List<String> testList = w.tokenize("Aceaste mese sunt bune");
    Assertions.assertEquals(7, testList.size());
    Assertions.assertEquals("[Aceaste,  , mese,  , sunt,  , bune]", testList.toString());
    
    // basic test - simle words, with diacritics
    testList = w.tokenize("Această carte este frumoasă");
    Assertions.assertEquals(7, testList.size());
    Assertions.assertEquals("[Această,  , carte,  , este,  , frumoasă]", testList.toString());
    
    // test for "-"
    testList = w.tokenize("nu-ți doresc");
    Assertions.assertEquals(5, testList.size());
    Assertions.assertEquals("[nu, -, ți,  , doresc]", testList.toString());

    // test for "„"
    testList = w.tokenize("zicea „merge");
    Assertions.assertEquals(4, testList.size());
    Assertions.assertEquals("[zicea,  , „, merge]", testList.toString());

    // test for "„" with white space
    testList = w.tokenize("zicea „ merge");
    Assertions.assertEquals(5, testList.size());
    Assertions.assertEquals("[zicea,  , „,  , merge]", testList.toString());

    // test for "„"
    testList = w.tokenize("zicea merge”");
    Assertions.assertEquals(4, testList.size());
    Assertions.assertEquals("[zicea,  , merge, ”]", testList.toString());
    
    // test for "„" and "„" 
    testList = w.tokenize("zicea „merge bine”");
    Assertions.assertEquals(7, testList.size());
    Assertions.assertEquals("[zicea,  , „, merge,  , bine, ”]", testList.toString());
    
    //ți-am
    testList = w.tokenize("ți-am");
    Assertions.assertEquals(3, testList.size());
    Assertions.assertEquals("[ți, -, am]", testList.toString());
    
    // test for "«" and "»" 
    testList = w.tokenize("zicea «merge bine»");
    Assertions.assertEquals(7, testList.size());
    Assertions.assertEquals("[zicea,  , «, merge,  , bine, »]", testList.toString());
    // test for "<" and ">" 
    testList = w.tokenize("zicea <<merge bine>>");
    Assertions.assertEquals(9, testList.size());
    Assertions.assertEquals("[zicea,  , <, <, merge,  , bine, >, >]", testList.toString());
    // test for "%"  
    testList = w.tokenize("avea 15% apă");
    Assertions.assertEquals(6, testList.size());
    Assertions.assertEquals("[avea,  , 15, %,  , apă]", testList.toString());
    // test for "°"  
    testList = w.tokenize("are 30°C");
    Assertions.assertEquals(5, testList.size());
    Assertions.assertEquals("[are,  , 30, °, C]", testList.toString());
    // test for "="  
    testList = w.tokenize("fructe=mere");
    Assertions.assertEquals(3, testList.size());
    Assertions.assertEquals("[fructe, =, mere]", testList.toString());
    // test for "|"  
    testList = w.tokenize("pere|mere");
    Assertions.assertEquals(testList.size(), 3);
    Assertions.assertEquals("[pere, |, mere]", testList.toString());
    // test for "\n"  
    testList = w.tokenize("pere\nmere");
    Assertions.assertEquals(3, testList.size());
    Assertions.assertEquals("[pere, \n, mere]", testList.toString());
    // test for "\r"  
    testList = w.tokenize("pere\rmere");
    Assertions.assertEquals(3, testList.size());
    Assertions.assertEquals("[pere, \r, mere]", testList.toString());
    // test for "\n\r"  
    testList = w.tokenize("pere\n\rmere");
    Assertions.assertEquals(4, testList.size());
    Assertions.assertEquals("[pere, \n, \r, mere]", testList.toString());
    // test for URLs  
    testList = w.tokenize("www.LanguageTool.org");
    Assertions.assertEquals(1, testList.size());
  }
}
