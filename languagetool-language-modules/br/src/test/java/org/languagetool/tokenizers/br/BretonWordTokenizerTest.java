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

package org.languagetool.tokenizers.br;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BretonWordTokenizerTest {

  @Test
  public void testTokenize() {
    final BretonWordTokenizer wordTokenizer = new BretonWordTokenizer();
    List <String> tokens = wordTokenizer.tokenize("Test c'h");
    assertEquals(3, tokens.size());
    assertEquals("[Test,  , c’h]", tokens.toString());
    tokens = wordTokenizer.tokenize("Test c’h");
    assertEquals(3, tokens.size());
    assertEquals("[Test,  , c’h]", tokens.toString());
    tokens = wordTokenizer.tokenize("C'hwerc'h merc'h gwerc'h war c'hwerc'h marc'h kalloc'h");
    assertEquals(13, tokens.size());
    assertEquals("[C’hwerc’h,  , merc’h,  , gwerc’h,  , war,  , c’hwerc’h,  , marc’h,  , kalloc’h]", tokens.toString());
    final List <String> tokens2 = wordTokenizer.tokenize("Test n’eo");
    assertEquals(4, tokens2.size());
    assertEquals("[Test,  , n’, eo]", tokens2.toString());

  }

}
