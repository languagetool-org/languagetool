/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Jaume Ortolà
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

package org.languagetool.tokenizers.ca;

import junit.framework.TestCase;

import java.util.List;

public class CatalanWordTokenizerTest extends TestCase {

  public void testTokenize() {
    CatalanWordTokenizer wordTokenizer = new CatalanWordTokenizer();
    List<String> tokens = wordTokenizer.tokenize("Emporta-te'ls a l'observatori dels mars");
    assertEquals(tokens.size(), 13);
    assertEquals("[Emporta, -te, 'ls,  , a,  , l', observatori,  , de, ls,  , mars]",
            tokens.toString());
  }

}
