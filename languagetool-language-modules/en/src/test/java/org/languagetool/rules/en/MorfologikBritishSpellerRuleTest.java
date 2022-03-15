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
import org.languagetool.Languages;
import org.languagetool.TestTools;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MorfologikBritishSpellerRuleTest extends AbstractEnglishSpellerRuleTest {

  @Test
  public void testSuggestions() throws IOException {
    Language language = Languages.getLanguageForShortCode("en-GB");
    Rule rule = new MorfologikBritishSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());
    super.testNonVariantSpecificSuggestions(rule, language);

    JLanguageTool lt = new JLanguageTool(language);
    // suggestions from language specific spelling_en-XX.txt
    assertSuggestion(rule, lt, "GBTestWordToBeIgnore", "GBTestWordToBeIgnored");
  }

  @Test
  public void testVariantMessages() throws IOException {
    Language language = Languages.getLanguageForShortCode("en-GB");
    JLanguageTool lt = new JLanguageTool(language);
    Rule rule = new MorfologikBritishSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("This is a nice color."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertTrue(matches[0].getMessage().contains("is American English"));
    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("Color is the American English word."));
    Assertions.assertEquals(1, matches2.length);
    Assertions.assertTrue(matches2[0].getMessage().contains("is American English"));
  }

  @Test
  public void testMorfologikSpeller() throws IOException {
    Language language = Languages.getLanguageForShortCode("en-GB");
    MorfologikBritishSpellerRule rule =
            new MorfologikBritishSpellerRule(TestTools.getMessages("en"), language, null, Collections.emptyList());

    JLanguageTool lt = new JLanguageTool(language);

    // correct sentences:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("This is an example: we get behaviour as a dictionary word.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Why don't we speak today.")).length);
    //with doesn't
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("He doesn't know what to do.")).length);
    //with diacritics 
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("The entrée at the café.")).length);
    //with an abbreviation:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("This is my Ph.D. thesis.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);
    // Greek letters
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("μ")).length);
    // With multiwords
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Ménage à trois")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("ménage à trois")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("The quid pro quo")).length);
    // apostrophes
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Ma'am, O'Connell, O’Connell, O'Connor, O’Neill")).length);

    //incorrect sentences:

    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("Behavior"));
    // check match positions:
    Assertions.assertEquals(1, matches1.length);
    Assertions.assertEquals(0, matches1[0].getFromPos());
    Assertions.assertEquals(8, matches1[0].getToPos());
    Assertions.assertEquals("Behaviour", matches1[0].getSuggestedReplacements().get(0));

    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("a")).length);
    
    //based on replacement pairs:

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("He teached us."));
    // check match positions:
    Assertions.assertEquals(1, matches2.length);
    Assertions.assertEquals(3, matches2[0].getFromPos());
    Assertions.assertEquals(10, matches2[0].getToPos());
    Assertions.assertEquals("taught", matches2[0].getSuggestedReplacements().get(0));
    
    RuleMatch[] matches3 = rule.match(lt.getAnalyzedSentence("I'm g oing"));
    assertThat(matches3.length, is(1));
    assertThat(matches3[0].getSuggestedReplacements().get(0), is("go ing"));
    assertThat(matches3[0].getSuggestedReplacements().get(1), is("going"));
    assertThat(matches3[0].getFromPos(), is(4));
    assertThat(matches3[0].getToPos(), is(10));
    
    // custom URLs
    RuleMatch[] matches4 = rule.match(lt.getAnalyzedSentence("archeological"));
    Assertions.assertEquals("https://languagetool.org/insights/post/our-or/#likeable-vs-likable-judgement-vs-judgment-oestrogen-vs-estrogen", matches4[0].getUrl().toString());
    
    
  }

  private void assertSuggestion(Rule rule, JLanguageTool lt, String input, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches.length, is(1));
    Assertions.assertTrue(matches[0].getSuggestedReplacements().size() >= expectedSuggestions.length, "Expected >= " + expectedSuggestions.length + ", got: " + matches[0].getSuggestedReplacements());
    for (String expectedSuggestion : expectedSuggestions) {
      Assertions.assertTrue(matches[0].getSuggestedReplacements().contains(expectedSuggestion));
    }
  }
}
