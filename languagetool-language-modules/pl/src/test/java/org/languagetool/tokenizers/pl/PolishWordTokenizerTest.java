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

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.language.Polish;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PolishWordTokenizerTest {

  @Test
  public void testTokenize() {
    final PolishWordTokenizer wordTokenizer = new PolishWordTokenizer();
    final List<String> tokens = wordTokenizer.tokenize("To jest\u00A0 test");
    assertEquals(tokens.size(), 6);
    assertEquals("[To,  , jest, \u00A0,  , test]", tokens.toString());
    final List<String> tokens2 = wordTokenizer.tokenize("To\rłamie");
    assertEquals(3, tokens2.size());
    assertEquals("[To, \r, łamie]", tokens2.toString());
    //hyphen with no whitespace
    final List<String> tokens3 = wordTokenizer.tokenize("A to jest-naprawdę-test!");
    assertEquals(tokens3.size(), 6);
    assertEquals("[A,  , to,  , jest-naprawdę-test, !]", tokens3.toString());
    //hyphen at the end of the word
    final List<String> tokens4 = wordTokenizer.tokenize("Niemiecko- i angielsko-polski");
    assertEquals(tokens4.size(), 6);
    assertEquals("[Niemiecko, -,  , i,  , angielsko-polski]", tokens4.toString());

    //hyphen probably instead of mdash
    final List<String> tokens5 = wordTokenizer.tokenize("Widzę krowę -i to dobrze!");
    assertEquals(11, tokens5.size());
    assertEquals("[Widzę,  , krowę,  , -, i,  , to,  , dobrze, !]", tokens5.toString());

    //mdash
    final List<String> tokens6 = wordTokenizer.tokenize("A to jest zdanie—rzeczywiście—z wtrąceniem.");
    assertEquals(tokens6.size(), 14);
    assertEquals("[A,  , to,  , jest,  , zdanie, —, rzeczywiście, —, z,  , wtrąceniem, .]", tokens6.toString());

    //compound words with hyphens
    final String compoundSentence = "To jest kobieta-wojownik w polsko-czeskim ubraniu, która wysłała dwa SMS-y.";
    List<String> compoundTokens = wordTokenizer.tokenize(compoundSentence);
    assertEquals(21, compoundTokens.size());
    assertEquals("[To,  , jest,  , kobieta-wojownik,  , w,  , polsko-czeskim,  , ubraniu, ,,  , która,  , wysłała,  , dwa,  , SMS-y, .]", compoundTokens.toString());
    //now setup the tagger...
    Language pl = new Polish();
    wordTokenizer.setTagger(pl.getTagger());
    compoundTokens = wordTokenizer.tokenize(compoundSentence);
    //we should get 4 more tokens: two hyphen tokens and two for the split words
    assertEquals(25, compoundTokens.size());
    assertEquals("[To,  , jest,  , kobieta, -, wojownik,  , " +
        "w,  , polsko, -, czeskim,  , ubraniu, ,,  " +
        ", która,  , wysłała,  , dwa,  , SMS-y, .]", compoundTokens.toString());
    compoundTokens = wordTokenizer.tokenize("Miała osiemnaście-dwadzieścia lat.");
    assertEquals(8, compoundTokens.size());
    assertEquals("[Miała,  , osiemnaście, -, dwadzieścia,  , lat, .]", compoundTokens.toString());
    // now three-part adja-adja-adj...:
    compoundTokens = wordTokenizer.tokenize("Słownik polsko-niemiecko-indonezyjski");
    assertEquals(7, compoundTokens.size());
    assertEquals("[Słownik,  , polsko, -, niemiecko, -, indonezyjski]", compoundTokens.toString());
    // number ranges:
    compoundTokens = wordTokenizer.tokenize("Impreza odbędzie się w dniach 1-23 maja.");
    assertEquals(16, compoundTokens.size());
    assertEquals("[Impreza,  , odbędzie,  , się,  , w,  , dniach,  , 1, -, 23,  , maja, .]", compoundTokens.toString());
    // number ranges:
    compoundTokens = wordTokenizer.tokenize("Impreza odbędzie się w dniach 1--23 maja.");
    assertEquals(18, compoundTokens.size());
    assertEquals("[Impreza,  , odbędzie,  , się,  , w,  , dniach,  , 1, -, , -, 23,  , maja, .]", compoundTokens.toString());
  }

}
