/* LanguageTool, a natural language style checker
 * Copyright (C) 2017 Markus Brenneis
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
package org.languagetool.rules.neuralnetwork;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.*;

public class DictionaryTest {

  @Test
  public void mapFromStringTest() {
    String dictionary = "{'a': 3, 'asa': 1, 'UNK': 42}";
    Dictionary dict = new Dictionary(dictionary);
    assertEquals(new Integer(1), dict.get("asa"));
    assertEquals(new Integer(3), dict.safeGet("a"));
    assertEquals(new Integer(42), dict.safeGet("foo"));
  }

  /**
   * This test verifies the validity of the new advanced Dictionary constructor.
   * This is a standard test case, so no Exceptions should be thrown.
   * @throws  Exception Any unhandled exceptions unrelated to constructor
   */
  // Issue this test is addressing here: https://github.com/languagetool-org/languagetool/issues/5609
  @Test
  public void extraDictionaryFieldsNoExceptionsTest() throws Exception {
    String dictionary = "{'a': 3, 'asa': 1, 'UNK': 42}";
    List<String> flags = new ArrayList<>();
    // arbitrary flags
    flags.add("L");
    flags.add("L, H");
    flags.add("W--");
    Dictionary dict = new Dictionary(dictionary, flags);
    HashMap<Integer, String> tempMap = new HashMap<>();
    tempMap.put(3, "L");

    assertEquals(dict.getAdvancedDict().size(), 3);
    assertEquals(dict.getAdvancedDictInfo("a"), tempMap);
    assertNull(dict.getAdvancedDictInfo("skdjhk"));
  }

  /**
   * This test verifies the validity of the new advanced Dictionary constructor
   * This case tests specifically that an Exception should be thrown if flags list < # of unique words
   */
  // Issue this test is addressing here: https://github.com/languagetool-org/languagetool/issues/5609
  @Test
  public void extraDictionaryFieldsExceptionsTest() {
    String dictionary = "{'a': 3, 'asa': 1, 'UNK': 42}";
    List<String> flags = new ArrayList<>();
    // arbitrary flags
    flags.add("L");
    flags.add("L, H");
    assertThrows(Exception.class, () -> {
      Dictionary dict = new Dictionary(dictionary, flags);
    });
  }

}
