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
package org.languagetool.rules.en;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.NewZealandEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

public class MorfologikNewZealandSpellerRuleTest extends AbstractEnglishSpellerRuleTest {

  @Test
  public void testSuggestions() throws IOException {
    Language language = new NewZealandEnglish();
    Rule rule = new MorfologikNewZealandSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());
    super.testNonVariantSpecificSuggestions(rule, language);
  }

  @Test
  public void testMorfologikSpeller() throws IOException {
    NewZealandEnglish language = new NewZealandEnglish();
    MorfologikNewZealandSpellerRule rule =
            new MorfologikNewZealandSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());

    JLanguageTool lt = new JLanguageTool(language);

    // correct sentences:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("This is an example: we get behaviour as a dictionary word.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Why don't we speak today.")).length);
    //with doesn't
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("He doesn't know what to do.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("μ")).length);

    //special New Zealand content:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Abercrombie")).length);

    //incorrect sentences:

    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("behavior"));
    // check match positions:
    Assertions.assertEquals(1, matches1.length);
    Assertions.assertEquals(0, matches1[0].getFromPos());
    Assertions.assertEquals(8, matches1[0].getToPos());
    Assertions.assertEquals("behaviour", matches1[0].getSuggestedReplacements().get(0));

    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("a")).length);
           
    //based on replacement pairs:

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("He teached us."));
    // check match positions:
    Assertions.assertEquals(1, matches2.length);
    Assertions.assertEquals(3, matches2[0].getFromPos());
    Assertions.assertEquals(10, matches2[0].getToPos());
    Assertions.assertEquals("taught", matches2[0].getSuggestedReplacements().get(0));
  }

}
