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
package org.languagetool.tokenizers.be;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class BelarusianWordTokenizerTest {
    private final BelarusianWordTokenizer wordTokenizer = new BelarusianWordTokenizer();

    @Test
    public void testTokenize() {
        final List<String> tokens = wordTokenizer.tokenize("камп'ютар");
        assertEquals(tokens.size(), 1);
        assertEquals(Arrays.asList("камп'ютар"), tokens);
        final List <String> tokens2 = wordTokenizer.tokenize("Яно\rразбіваецца");
        assertEquals(tokens2.size(), 3);
        assertEquals("[Яно, \r, разбіваецца]", tokens2.toString());
        final List<String> tokens3 = wordTokenizer.tokenize("Мой адрас — address@email.com");
        assertEquals(tokens3.size(), 7);
        assertEquals("[Мой,  , адрас,  , —,  , address@email.com]", tokens3.toString());
    }
}
