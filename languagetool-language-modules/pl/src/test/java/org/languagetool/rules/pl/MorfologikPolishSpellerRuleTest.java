/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Marcin Miłkowski
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
package org.languagetool.rules.pl;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MorfologikPolishSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    final MorfologikPolishSpellerRule rule =
        new MorfologikPolishSpellerRule (TestTools.getMessages("pl"), new Polish(), null, Collections.emptyList());

    final JLanguageTool lt = new JLanguageTool(new Polish());

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("To jest test bez jakiegokolwiek błędu.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Żółw na starość wydziela dziwną woń.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Żółw na starość wydziela dziwną woń numer 1234.")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("W MI-6 pracuje 15-letni agent.")).length);
    //test for "LanguageTool":
    assertEquals(0, rule.match(lt.getAnalyzedSentence("LanguageTool jest świetny!")).length);

    //test for the ignored uppercase word "Gdym":
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Gdym to zobaczył, zdębiałem.")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);

    //test for accentuated (foreign) words, such as "purée"
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Roztrzepać widelcem jajka razem z purée z dyni.")).length);

    //compound word with ignored part "techniczno"
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Bogactwo nie rośnie proporcjonalnie do jej rozwoju techniczno-terytorialnego.")).length);

    //compound word with one of the compound prefixes:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Antypostmodernistyczna batalia hiperfilozofów")).length);
   //compound words: "trzynastobitowy", "zgniłożółty"
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Trzynastobitowe przystawki w kolorze zgniłożółtym")).length);

    //compound word that failed test when CamelCase was on:

    assertEquals(0, rule.match(lt.getAnalyzedSentence("Nie spotkałem tutaj Saint-Exupérych")).length);


    //incorrect sentences:

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Zolw"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(4, matches[0].getToPos());
    assertEquals("Żółw", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("😂 Zolw"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(3, matches[0].getFromPos());
    assertEquals(7, matches[0].getToPos());
    assertEquals("Żółw", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("😂😂 Zolw"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(5, matches[0].getFromPos());
    assertEquals(9, matches[0].getToPos());
    assertEquals("Żółw", matches[0].getSuggestedReplacements().get(0));

    assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);

    //tokenizing on prefixes niby- and quasi-
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Niby-artysta spotkał się z quasi-opiekunem i niby-Francuzem.")).length);

    final RuleMatch[] prunedMatches = rule.match(lt.getAnalyzedSentence("Clarkem"));
    assertEquals(1, prunedMatches.length);
    assertEquals(6, prunedMatches[0].getSuggestedReplacements().size());
    assertEquals("Clarke", prunedMatches[0].getSuggestedReplacements().get(0));
    assertEquals("Czarkę", prunedMatches[0].getSuggestedReplacements().get(1));
    assertEquals("Clarkiem", prunedMatches[0].getSuggestedReplacements().get(2));
    assertEquals("Ciarkę", prunedMatches[0].getSuggestedReplacements().get(3));
    assertEquals("Clarkom", prunedMatches[0].getSuggestedReplacements().get(4));

    //with default ignore-camel-case == yes this was not catched at all
    assertEquals(1, rule.match(lt.getAnalyzedSentence("Saint-Exupérymu")).length);

    // There should be a match, this is not a prefix!

    assertEquals(1, rule.match(lt.getAnalyzedSentence("premoc")).length);

    // "0" instead "o"...
    assertEquals(1, rule.match(lt.getAnalyzedSentence("dziwneg0")).length);
  }

}
