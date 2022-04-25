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

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DutchWordTokenizerTest {

  private final DutchWordTokenizer wordTokenizer = new DutchWordTokenizer();

  @Test
  public void testTokenize() {
    assertTokenize("This is\u00A0a test",
                   "[This,  , is,  , a,  , test]");
    assertTokenize("Bla bla oma's bla bla 'test",
                   "[Bla,  , bla,  , oma's,  , bla,  , bla,  , ', test]");
    assertTokenize("Bla bla oma`s bla bla 'test",
                   "[Bla,  , bla,  , oma`s,  , bla,  , bla,  , ', test]");
    assertTokenize("Ik zie het''",
                   "[Ik,  , zie,  , het, ', ']");
    assertTokenize("Ik zie het``",
                   "[Ik,  , zie,  , het, `, `]");
    assertTokenize("''Ik zie het",
                   "[', ', Ik,  , zie,  , het]");
    
    assertTokenize("Ik 'zie' het", "[Ik,  , ', zie, ',  , het]");
    assertTokenize("Ik ‘zie’ het", "[Ik,  , ‘, zie, ’,  , het]");
    assertTokenize("Ik \"zie\" het", "[Ik,  , \", zie, \",  , het]");
    assertTokenize("Ik “zie” het", "[Ik,  , “, zie, ”,  , het]");
    assertTokenize("'zie'", "[', zie, ']");
    assertTokenize("‘zie’", "[‘, zie, ’]");
    assertTokenize("\"zie\"", "[\", zie, \"]");
    assertTokenize("“zie”", "[“, zie, ”]");
    
    assertTokenize("Ik `zie het",
                   "[Ik,  , `, zie,  , het]");
    assertTokenize("Ik ``zie het",
                   "[Ik,  , `, `, zie,  , het]");
    assertTokenize("'", "[']");
    assertTokenize("''", "[, ', ']");
    assertTokenize("'x'", "[', x, ']");
    assertTokenize("`x`", "[`, x, `]");
  }

  private void assertTokenize(String input, String expected) {
    List<String> result = wordTokenizer.tokenize(input);
    assertEquals(expected, result.toString());
  }
}
