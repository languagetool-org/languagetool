/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Andriy Rysin
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
package org.languagetool.rules.uk;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class TypographyRuleTest {

  @Test
  public void testRule() throws IOException {
    TypographyRule rule = new TypographyRule(TestTools.getMessages("uk"));
    JLanguageTool lt = new JLanguageTool(new Ukrainian());

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("як-небудь")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("А\u2013Т")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("ХХ\u2013ХХІ")).length);
    
    //incorrect sentences:
    // TODO: does not work when word is the last in the sentence: "яскраво\u2013рожевий"
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("яскраво\u2013рожевий."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("яскраво-рожевий", "яскраво \u2014 рожевий"), matches[0].getSuggestedReplacements());

    // test unknown word
    matches = rule.match(lt.getAnalyzedSentence("яскраво\u2013шуруровий."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("яскраво-шуруровий", "яскраво \u2014 шуруровий"), matches[0].getSuggestedReplacements());

    assertEquals(0, rule.match(lt.getAnalyzedSentence("ХХ\u2014ХХІ")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("Вовка,— волкова")).length);

    matches = rule.match(lt.getAnalyzedSentence("Вовка\u2014Волкова."));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("Вовка-Волкова", "Вовка \u2014 Волкова"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("цукерок —знову низька"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("цукерок-знову", "цукерок \u2014 знову"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("—знову низька"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("\u2014 знову"), matches[0].getSuggestedReplacements());

    matches = rule.match(lt.getAnalyzedSentence("знову—"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("знову \u2014"), matches[0].getSuggestedReplacements());

    assertEquals(0, rule.match(lt.getAnalyzedSentence("\u2014 Київ, 1994")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("\u2013 Київ, 1994")).length);

    matches = rule.match(lt.getAnalyzedSentence("важливіше \u2013потенційні"));
    assertEquals(1, matches.length);
    assertEquals(Arrays.asList("важливіше-потенційні", "важливіше \u2014 потенційні"), matches[0].getSuggestedReplacements());
    
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Рахунки 1 класу –")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence("\u2013")).length);

    assertEquals(0, rule.match(lt.getAnalyzedSentence(" \u2013")).length);

    matches = rule.match(lt.getAnalyzedSentence("любили ,—люби"));
    assertEquals(1, matches.length);
    assertEquals(1, matches[0].getSuggestedReplacements().size());
  }

}
