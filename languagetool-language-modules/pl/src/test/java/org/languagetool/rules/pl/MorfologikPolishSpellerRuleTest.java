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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Polish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

public class MorfologikPolishSpellerRuleTest {

  @Test
  public void testMorfologikSpeller() throws IOException {
    final MorfologikPolishSpellerRule rule =
        new MorfologikPolishSpellerRule (TestTools.getMessages("pl"), new Polish(), null, Collections.emptyList());

    final JLanguageTool lt = new JLanguageTool(new Polish());

    // correct sentences:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("To jest test bez jakiegokolwiek błędu.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Żółw na starość wydziela dziwną woń.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Żółw na starość wydziela dziwną woń numer 1234.")).length);

    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("W MI-6 pracuje 15-letni agent.")).length);
    //test for "LanguageTool":
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("LanguageTool jest świetny!")).length);

    //test for the ignored uppercase word "Gdym":
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Gdym to zobaczył, zdębiałem.")).length);

    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);

    //compound word with ignored part "techniczno"
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Bogactwo nie rośnie proporcjonalnie do jej rozwoju techniczno-terytorialnego.")).length);

    //compound word with one of the compound prefixes:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Antypostmodernistyczna batalia hiperfilozofów")).length);
   //compound words: "trzynastobitowy", "zgniłożółty"
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Trzynastobitowe przystawki w kolorze zgniłożółtym")).length);

    //incorrect sentences:

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Zolw"));
    // check match positions:
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(0, matches[0].getFromPos());
    Assertions.assertEquals(4, matches[0].getToPos());
    Assertions.assertEquals("Żółw", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("😂 Zolw"));
    // check match positions:
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(3, matches[0].getFromPos());
    Assertions.assertEquals(7, matches[0].getToPos());
    Assertions.assertEquals("Żółw", matches[0].getSuggestedReplacements().get(0));

    matches = rule.match(lt.getAnalyzedSentence("😂😂 Zolw"));
    // check match positions:
    Assertions.assertEquals(1, matches.length);
    Assertions.assertEquals(5, matches[0].getFromPos());
    Assertions.assertEquals(9, matches[0].getToPos());
    Assertions.assertEquals("Żółw", matches[0].getSuggestedReplacements().get(0));

    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);

    //tokenizing on prefixes niby- and quasi-
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Niby-artysta spotkał się z quasi-opiekunem i niby-Francuzem.")).length);

    final RuleMatch[] prunedMatches = rule.match(lt.getAnalyzedSentence("Clarkem"));
    Assertions.assertEquals(1, prunedMatches.length);
    Assertions.assertEquals(5, prunedMatches[0].getSuggestedReplacements().size());
    Assertions.assertEquals("Clarke", prunedMatches[0].getSuggestedReplacements().get(0));
    Assertions.assertEquals("Clarkiem", prunedMatches[0].getSuggestedReplacements().get(1));
    Assertions.assertEquals("Ciarkę", prunedMatches[0].getSuggestedReplacements().get(2));
    Assertions.assertEquals("Clarkom", prunedMatches[0].getSuggestedReplacements().get(3));
    Assertions.assertEquals("Czarkę", prunedMatches[0].getSuggestedReplacements().get(4));

    // There should be a match, this is not a prefix!

    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("premoc")).length);

    // "0" instead "o"...
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("dziwneg0")).length);
  }

}
