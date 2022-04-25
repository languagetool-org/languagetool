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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.language.CanadianEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

public class MorfologikCanadianSpellerRuleTest extends AbstractEnglishSpellerRuleTest{

  @Test
  public void testSuggestions() throws IOException {
    Language language = new CanadianEnglish();
    Rule rule = new MorfologikCanadianSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());
    super.testNonVariantSpecificSuggestions(rule, language);

    JLanguageTool lt = new JLanguageTool(language);
    // suggestions from language specific spelling_en-XX.txt
    assertSuggestion(rule, lt, "CATestWordToBeIgnore", "CATestWordToBeIgnored");
  }

  @Test
  public void testMorfologikSpeller() throws IOException {
    CanadianEnglish language = new CanadianEnglish();
    MorfologikBritishSpellerRule rule =
            new MorfologikBritishSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());

    JLanguageTool lt = new JLanguageTool(language);

    // correct sentences:
    assertEquals(0, rule.match(lt.getAnalyzedSentence("This is an example: we get behaviour as a dictionary word.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("Why don't we speak today.")).length);
    //with doesn't
    assertEquals(0, rule.match(lt.getAnalyzedSentence("He doesn't know what to do.")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("I like my emoji (😥)...")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("μ")).length);

    //incorrect sentences:

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("arbor"));
    // check match positions:
    assertEquals(1, matches.length);
    assertEquals(0, matches[0].getFromPos());
    assertEquals(5, matches[0].getToPos());
    assertTrue(matches[0].getSuggestedReplacements().contains("arbour"));

    assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);
    assertEquals(0, rule.match(lt.getAnalyzedSentence("a")).length);
  }

  private void assertSuggestion(Rule rule, JLanguageTool lt, String input, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches.length, is(1));
    assertTrue("Expected >= " + expectedSuggestions.length + ", got: " + matches[0].getSuggestedReplacements(),
            matches[0].getSuggestedReplacements().size() >= expectedSuggestions.length);
    for (String expectedSuggestion : expectedSuggestions) {
      assertTrue(matches[0].getSuggestedReplacements().contains(expectedSuggestion));
    }
  }
}
