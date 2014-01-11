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

package org.languagetool.tokenizers.pl;

import java.util.List;

import junit.framework.TestCase;

public class PolishWordTokenizerTest extends TestCase {

  public void testTokenize() {
    final PolishWordTokenizer wordTokenizer = new PolishWordTokenizer();
    final List <String> tokens = wordTokenizer.tokenize("To jest\u00A0 test");
    assertEquals(tokens.size(), 6);
    assertEquals("[To,  , jest, \u00A0,  , test]", tokens.toString());
    final List <String> tokens2 = wordTokenizer.tokenize("To\rłamie");
    assertEquals(3, tokens2.size());
    assertEquals("[To, \r, łamie]", tokens2.toString());
    //hyphen with no whitespace
    final List <String> tokens3 = wordTokenizer.tokenize("A to jest-naprawdę-test!");
    assertEquals(tokens3.size(), 6);
    assertEquals("[A,  , to,  , jest-naprawdę-test, !]", tokens3.toString());
    //hyphen at the end of the word
    final List <String> tokens4 = wordTokenizer.tokenize("Niemiecko- i angielsko-polski");
    assertEquals(tokens4.size(), 6);
    assertEquals("[Niemiecko, -,  , i,  , angielsko-polski]", tokens4.toString());
    //mdash
    final List <String> tokens5 = wordTokenizer.tokenize("A to jest zdanie—rzeczywiście—z wtrąceniem.");
    assertEquals(tokens5.size(), 14);
    assertEquals("[A,  , to,  , jest,  , zdanie, —, rzeczywiście, —, z,  , wtrąceniem, .]", tokens5.toString());
  }

}
