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

import org.junit.BeforeClass;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.UserConfig;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.CanadianEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MorfologikAmericanSpellerRuleTest extends AbstractEnglishSpellerRuleTest {

  private static final AmericanEnglish language = new AmericanEnglish();
  
  private static MorfologikAmericanSpellerRule rule;
  private static JLanguageTool langTool;
  private static MorfologikCanadianSpellerRule caRule;
  private static JLanguageTool caLangTool;

  @BeforeClass
  public static void setup() throws IOException {
    rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language);
    langTool = new JLanguageTool(language);
    CanadianEnglish canadianEnglish = new CanadianEnglish();
    caRule = new MorfologikCanadianSpellerRule(TestTools.getMessages("en"), canadianEnglish, null, Collections.emptyList());
    caLangTool = new JLanguageTool(canadianEnglish);
  }

  @Test
  public void testSuggestions() throws IOException {
    Language language = new AmericanEnglish();
    Rule rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language);
    super.testNonVariantSpecificSuggestions(rule, language);
  }

  @Test
  public void testVariantMessages() throws IOException {
    Language language = new AmericanEnglish();
    Rule rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language);
    RuleMatch[] matches = rule.match(langTool.getAnalyzedSentence("This is a nice colour."));
    assertEquals(1, matches.length);
    assertTrue(matches[0].getMessage().contains("is British English"));
  }

  @Test
  public void testUserDict() throws IOException {
    Language language = new AmericanEnglish();
    UserConfig userConfig = new UserConfig(Arrays.asList("mytestword", "mytesttwo"));
    Rule rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language, userConfig, Collections.emptyList());
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("mytestword")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("mytesttwo")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("mytestthree")).length);
  }

  @Test
  public void testMorfologikSpeller() throws IOException {

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("mansplaining")).length); // test merge of spelling.txt
    // test suggesting words with diacritics
    assertTrue(rule.match(langTool.getAnalyzedSentence("fianc"))[0].getSuggestedReplacements().contains("fiancé"));


    // correct sentences:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("This is an example: we get behavior as a dictionary word.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("Why don't we speak today.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("An URL like http://sdaasdwe.com is no error.")).length);
    //with doesn't
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("He doesn't know what to do.")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence(",")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("123454")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("I like my emoji 😾")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("μ")).length);

    // test words in language-specific spelling_en-US.txt
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("USTestWordToBeIgnored")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("NZTestWordToBeIgnored")).length);

    //incorrect sentences:

    RuleMatch[] matches1 = rule.match(langTool.getAnalyzedSentence("behaviour"));
    // check match positions:
    assertEquals(1, matches1.length);
    assertEquals(0, matches1[0].getFromPos());
    assertEquals(9, matches1[0].getToPos());
    assertEquals("behavior", matches1[0].getSuggestedReplacements().get(0));

    assertEquals(1, rule.match(langTool.getAnalyzedSentence("aõh")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("a")).length);
    
    //based on replacement pairs:

    RuleMatch[] matches2 = rule.match(langTool.getAnalyzedSentence("He teached us."));
    // check match positions:
    assertEquals(1, matches2.length);
    assertEquals(3, matches2[0].getFromPos());
    assertEquals(10, matches2[0].getToPos());
    assertEquals("taught", matches2[0].getSuggestedReplacements().get(0));
    
    // hyphens - accept words if all their parts are okay:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("A web-based software.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("A wxeb-based software.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("A web-baxsed software.")).length);
    // yes, we also accept fantasy words:
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("A web-feature-driven-car software.")).length);
    assertEquals(1, rule.match(langTool.getAnalyzedSentence("A web-feature-drivenx-car software.")).length);
  }

  @Test
  public void testIgnoredChars() throws IOException {
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("software")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("soft\u00ADware")).length);

    assertEquals(0, rule.match(langTool.getAnalyzedSentence("A software")).length);
    assertEquals(0, rule.match(langTool.getAnalyzedSentence("A soft\u00ADware")).length);
  }  
  
  @Test
  public void testSuggestionForIrregularWords() throws IOException {
    // verbs:
    assertSuggestion("He teached us.", "taught");
    assertSuggestion("He buyed the wrong brand", "bought");
    assertSuggestion("I thinked so.", "thought");
    assertSuggestion("She awaked", "awoke");
    assertSuggestion("She becomed", "became");
    assertSuggestion("It begined", "began");
    assertSuggestion("It bited", "bit");
    assertSuggestion("She dealed", "dealt");
    assertSuggestion("She drived", "drove");
    assertSuggestion("He drawed", "drew");
    assertSuggestion("She finded", "found");
    assertSuggestion("It hurted", "hurt");
    assertSuggestion("It was keeped", "kept");
    assertSuggestion("He maked", "made");
    assertSuggestion("She runed", "ran");
    assertSuggestion("She selled", "sold");
    assertSuggestion("He speaked", "spoke");  // needs dict update to not include 'spake'

    // double consonants not yet supported:
    //assertSuggestion("He cutted", "cut");
    //assertSuggestion("She runned", "ran");

    // nouns:
    assertSuggestion("auditory stimuluses", "stimuli");
    assertSuggestion("analysises", "analyses");
    assertSuggestion("parenthesises", "parentheses");
    assertSuggestion("childs", "children");
    assertSuggestion("womans", "women");
    assertSuggestion("criterions", "criteria");
    //accepted by spell checker, e.g. as third-person verb:
    // foots, mouses, man
    
    // adjectives (comparative):
    assertSuggestion("gooder", "better");
    assertSuggestion("bader", "worse");
    assertSuggestion("farer", "further", "farther");
    //accepted by spell checker:
    //badder

    // adjectives (superlative):
    assertSuggestion("goodest", "best");
    assertSuggestion("badest", "worst");
    assertSuggestion("farest", "furthest", "farthest");
    //double consonants not yet supported:
    //assertSuggestion("baddest", "worst");
    // suggestions from language specific spelling_en-XX.txt
    assertSuggestion("USTestWordToBeIgnore", "USTestWordToBeIgnored");
    assertSuggestion("CATestWordToBeIgnore", "USTestWordToBeIgnored");
    assertSuggestion("CATestWordToBeIgnore", caRule, caLangTool, "CATestWordToBeIgnored");
    assertSuggestion("CATestWordToBeIgnore", "USTestWordToBeIgnored");  // test again because of caching
  }

  private void assertSuggestion(String input, String... expectedSuggestions) throws IOException {
    assertSuggestion(input, rule, langTool, expectedSuggestions);
  }

  private void assertSuggestion(String input, Rule rule, JLanguageTool lt, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat(matches.length, is(1));
    assertTrue("Expected >= " + expectedSuggestions.length + ", got: " + matches[0].getSuggestedReplacements(),
            matches[0].getSuggestedReplacements().size() >= expectedSuggestions.length);
    for (String expectedSuggestion : expectedSuggestions) {
      assertTrue(matches[0].getSuggestedReplacements().contains(expectedSuggestion));
    }
  }
}
