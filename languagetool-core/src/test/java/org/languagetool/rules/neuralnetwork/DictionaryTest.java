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

import static org.junit.Assert.assertEquals;

public class DictionaryTest {

  @Test
  public void mapFromStringTest() {
    String dictionary = "{'a': 3, 'asa': 1, 'UNK': 42}";
    Dictionary dict = new Dictionary(dictionary);
    assertEquals(new Integer(1), dict.get("asa"));
    assertEquals(new Integer(3), dict.safeGet("a"));
    assertEquals(new Integer(42), dict.safeGet("foo"));
  }

}
