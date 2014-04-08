/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging;

import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

public class TokenPoSTest {
  
  @Test
  public void testSimpleIsSubsetOf() {
    assertTrue(pos().isSubsetOf(pos()));
    assertTrue(pos().isSubsetOf(pos("pos:noun")));
    assertTrue(pos("pos:noun").isSubsetOf(pos("pos:noun")));
    assertTrue(pos("pos:noun").isSubsetOf(pos("pos:noun", "person:1")));
    assertTrue(pos("pos:noun", "person:1").isSubsetOf(pos("pos:noun", "person:1")));
    
    assertFalse(pos("pos:other").isSubsetOf(pos("pos:noun")));
    assertFalse(pos("other:noun").isSubsetOf(pos("pos:noun")));
    assertFalse(pos("other:other").isSubsetOf(pos("pos:noun")));
    assertFalse(pos("pos:noun", "person:2").isSubsetOf(pos("pos:noun", "person:1")));
  }
  
  @Test
  public void testIsSubsetOfWithAmbiguousText() {
    assertTrue(pos().isSubsetOf(pos("pos:noun|verb")));
    assertTrue(pos("pos:noun").isSubsetOf(pos("pos:noun|verb")));
    assertTrue(pos("pos:verb").isSubsetOf(pos("pos:noun|verb")));
    
    assertFalse(pos("pos:other").isSubsetOf(pos("pos:noun|verb")));
  }
  
  @Test
  public void testIsSubsetOfWithAmbiguousPattern() {
    assertTrue(pos("pos:noun|verb").isSubsetOf(pos("pos:noun")));
    assertTrue(pos("pos:noun|verb").isSubsetOf(pos("pos:verb")));
    assertTrue(pos("pos:noun|verb").isSubsetOf(pos("pos:noun|verb")));
    assertTrue(pos("pos:noun|verb", "person:1").isSubsetOf(pos("pos:noun|verb", "person:1")));
    assertTrue(pos("pos:noun|verb", "person:1").isSubsetOf(pos("pos:noun|verb", "person:1|2")));
    assertTrue(pos("pos:noun|verb", "person:2").isSubsetOf(pos("pos:noun|verb", "person:1|2")));

    assertFalse(pos("pos:noun|verb").isSubsetOf(pos("pos:other")));
    assertFalse(pos("pos:noun|verb", "person:1").isSubsetOf(pos("pos:noun|verb", "person:2|3")));
  }
  
  @Test
  public void testEquals() {
    assertTrue(pos().equals(pos()));
    assertTrue(pos("pos:noun").equals(pos("pos:noun")));
    assertTrue(pos("pos:noun|verb").equals(pos("pos:noun|verb")));
    assertTrue(pos("pos:noun", "foo:bar").equals(pos("pos:noun", "foo:bar")));
    assertTrue(pos("pos:noun", "foo:bar").equals(pos("pos:noun", "foo:bar")));
    assertTrue(pos("pos:a|b").equals(pos("pos:b|a")));

    assertFalse(pos("pos:noun").equals(pos("pos:verb")));
    assertFalse(pos("pos:noun").equals(pos("pos:noun", "more:stuff")));
  }
  
  private TokenPoS pos(String... keyValues) {
    TokenPoSBuilder builder = new TokenPoSBuilder();
    for (String keyValue : keyValues) {
      String[] pair = keyValue.split(":");
      String valueStr = pair[1];
      if (valueStr.contains("|")) {
        String[] values = valueStr.split("\\|");
        for (String value : values) {
          builder.add(pair[0], value);
        }
      } else {
        builder.add(pair[0], pair[1]);
      }
    }
    return builder.create();
  }
  
}
