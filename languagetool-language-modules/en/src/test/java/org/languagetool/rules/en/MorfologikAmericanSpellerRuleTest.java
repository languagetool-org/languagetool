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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.languagetool.*;
import org.languagetool.language.CanadianEnglish;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class MorfologikAmericanSpellerRuleTest extends AbstractEnglishSpellerRuleTest {

  private static final Language language = Languages.getLanguageForShortCode("en-US");
  
  private static MorfologikAmericanSpellerRule rule;
  private static JLanguageTool lt;
  private static MorfologikCanadianSpellerRule caRule;
  private static JLanguageTool caLangTool;

  @BeforeAll
  public static void setup() throws IOException {
    rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language);
    lt = new JLanguageTool(language);
    CanadianEnglish canadianEnglish = new CanadianEnglish();
    caRule = new MorfologikCanadianSpellerRule(TestTools.getMessages("en"), canadianEnglish, null, emptyList());
    caLangTool = new JLanguageTool(canadianEnglish);
  }

  @Test
  public void testNamedEntityIgnore() throws IOException {
    Language language = Languages.getLanguageForShortCode("en-US");
    Map<String, Integer> map = new HashMap<>();
    map.put("Peter", 100);
    map.put("Petr", 10);
    LanguageModel lm = new FakeLanguageModel(map);
    Rule rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language, null, null, null, lm, null);
    //String s = "He was the son of Mehmed II (1432–81) and Valide Sultan Gülbahar Hatun, who died in 1492.";
    //String s = "This is a test with Elmar Reimann.";
    String s = "This is a test with Petr Smith.";
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    //System.out.println(Arrays.toString(matches));
  }

  @Test
  public void testSuggestions() throws IOException {
    Language language = Languages.getLanguageForShortCode("en-US");
    Rule rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language);
    super.testNonVariantSpecificSuggestions(rule, language);
  }

  @Test
  public void testVariantMessages() throws IOException {
    Language language = Languages.getLanguageForShortCode("en-US");
    Rule rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language);
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("This is a nice colour."));
    Assertions.assertEquals(1, matches.length);
    Assertions.assertTrue(matches[0].getMessage().contains("is British English"));
    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("Colour is the British words."));
    Assertions.assertEquals(1, matches2.length);
    Assertions.assertTrue(matches2[0].getMessage().contains("is British English"));
  }

  @Test
  public void testUserDict() throws IOException {
    Language language = Languages.getLanguageForShortCode("en-US");
    UserConfig userConfig = new UserConfig(Arrays.asList("mytestword", "mytesttwo"));
    Rule rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), language, userConfig, emptyList());
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("mytestword")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("mytesttwo")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("mytestthree")).length);
  }

  @Test
  public void testMorfologikSpeller() throws IOException {

    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("mansplaining")).length); // test merge of spelling.txt
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Qur'an")).length);
    // test suggesting words with diacritics
    Assertions.assertTrue(rule.match(lt.getAnalyzedSentence("fianc"))[0].getSuggestedReplacements().contains("fiancé"));


    // correct sentences:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("This is an example: we get behavior as a dictionary word.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Why don't we speak today.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("An URL like http://sdaasdwe.com is no error.")).length);
    //with doesn't
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("He doesn't know what to do.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence(",")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("123454")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("I like my emoji 😾")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("μ")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("I like my emoji ❤️")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("This is English text 🗺.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Yes ma'am.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Yes ma’am.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("'twas but a dream of thee")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("fo'c'sle")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("O'Connell, O’Connell, O'Connor, O’Neill")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("viva voce, a fortiori, in vitro")).length);
    // non-ASCII characters
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("5¼\" floppy disk drive")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("a visual magnitude of -2½")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Water freezes at 0º C. 175ºC")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("33°5′40″N and 32°59′0″E.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("It's up to 5·10-³ meters.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("141°00′7.128″W")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("1031－1095")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("It is thus written 1″.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("a 30½-inch scale length.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("symbolically stated as A ∈ ℝ3.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Thus ℵ0 is a regular cardinal.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("the classical space B(ℓ2)")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("🏽")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("🧡🚴🏽♂️ , 🎉💛✈️")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("компьютерная")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("中文維基百科 中文维基百科")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("The statements¹ of⁷ the⁵⁰ government⁹‽")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("At 3 o'clock.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("At 3 o’clock.")).length);
    
    // test words in language-specific spelling_en-US.txt
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("USTestWordToBeIgnored")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("NZTestWordToBeIgnored")).length);

    //incorrect sentences:

    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("behaviour"));
    // check match positions:
    Assertions.assertEquals(1, matches1.length);
    Assertions.assertEquals(0, matches1[0].getFromPos());
    Assertions.assertEquals(9, matches1[0].getToPos());
    Assertions.assertEquals("behavior", matches1[0].getSuggestedReplacements().get(0));

    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("aõh")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("a")).length);
    
    //based on replacement pairs:

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("He teached us."));
    // check match positions:
    Assertions.assertEquals(1, matches2.length);
    Assertions.assertEquals(3, matches2[0].getFromPos());
    Assertions.assertEquals(10, matches2[0].getToPos());
    Assertions.assertEquals("taught", matches2[0].getSuggestedReplacements().get(0));
    
    // hyphens - accept words if all their parts are okay:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("A web-based software.")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("A wxeb-based software.")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("A web-baxsed software.")).length);
    // yes, we also accept fantasy words:
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("A web-feature-driven-car software.")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("A web-feature-drivenx-car software.")).length);

    assertAllMatches(lt, rule, "robinson", "Robinson", "robin son", "robins on", "Robson", "Robeson", "robins", "Roberson");
    
    // contractions with apostrophe
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("You're only foolin' round.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("You’re only foolin’ round.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("This is freakin' hilarious.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("It's the meal that keeps on givin'.")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("Don't Stop Believin'.")).length);
    
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("wrongwordin'")).length);
    Assertions.assertEquals(1, rule.match(lt.getAnalyzedSentence("wrongwordin’")).length);
  }

  @Test
  public void testIgnoredChars() throws IOException {
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("software")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("soft\u00ADware")).length);

    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("A software")).length);
    Assertions.assertEquals(0, rule.match(lt.getAnalyzedSentence("A soft\u00ADware")).length);

    List<RuleMatch> ruleMatchesWithoutMerge = lt.check("sux\u00AD tainability");
    Assertions.assertEquals(2, ruleMatchesWithoutMerge.size());
    // make sure we offset correctly for ignored characters
    Assertions.assertEquals(Arrays.asList(0, 4), Arrays.asList(ruleMatchesWithoutMerge.get(0).getFromPos(), ruleMatchesWithoutMerge.get(0).getToPos()));
    Assertions.assertEquals(Arrays.asList(5, 16), Arrays.asList(ruleMatchesWithoutMerge.get(1).getFromPos(), ruleMatchesWithoutMerge.get(1).getToPos()));

    // see issue #1769
    List<RuleMatch> ruleMatches = lt.check("The sus\u00AD tainability");
    Assertions.assertEquals(1, ruleMatches.size());
    // make sure we offset correctly for ignored characters
    Assertions.assertEquals(Arrays.asList(4, 20), Arrays.asList(ruleMatches.get(0).getFromPos(), ruleMatches.get(0).getToPos()));
  }

  @Test
  public void testRuleWithWrongSplit() throws Exception {
    Language lang = Languages.getLanguageForShortCode("en-US");
    MorfologikAmericanSpellerRule rule = new MorfologikAmericanSpellerRule(TestTools.getMessages("en"), lang);
    JLanguageTool lt = new JLanguageTool(lang);
    
    RuleMatch[] matches1 = rule.match(lt.getAnalyzedSentence("But than kyou for the feedback"));
    assertThat(matches1.length, is(1));
    assertThat(matches1[0].getSuggestedReplacements().get(0), is("thank you"));
    assertThat(matches1[0].getFromPos(), is(4));
    assertThat(matches1[0].getToPos(), is(13));
    //assertThat(matches1[1].getSuggestedReplacements().get(0), is("you"));

    RuleMatch[] matches2 = rule.match(lt.getAnalyzedSentence("But thanky ou for the feedback"));
    assertThat(matches2.length, is(1));
    assertThat(matches2[0].getSuggestedReplacements().get(0), is("thank you"));
    assertThat(matches2[0].getFromPos(), is(4));
    assertThat(matches2[0].getToPos(), is(13));
    //assertThat(matches2[1].getSuggestedReplacements().get(0), is("of"));

    RuleMatch[] matches3 = rule.match(lt.getAnalyzedSentence("But thank you for th efeedback"));
    assertThat(matches3.length, is(1));
    assertThat(matches3[0].getSuggestedReplacements().get(0), is("the feedback"));
    assertThat(matches3[0].getFromPos(), is(18));
    assertThat(matches3[0].getToPos(), is(30));
    //assertThat(matches3[1].getSuggestedReplacements().get(0), is("feedback"));

    RuleMatch[] matches4 = rule.match(lt.getAnalyzedSentence("But thank you for thef eedback"));
    assertThat(matches4.length, is(1));
    assertThat(matches4[0].getSuggestedReplacements().get(0), is("the feedback"));
    assertThat(matches4[0].getFromPos(), is(18));
    assertThat(matches4[0].getToPos(), is(30));
    //assertThat(matches4[1].getSuggestedReplacements().get(0), is("feedback"));

    RuleMatch[] matches5 = rule.match(lt.getAnalyzedSentence("But thnk you fo rthe feedback"));
    assertThat(matches5.length, is(2));
    assertThat(matches5[0].getSuggestedReplacements().get(0), is("tank"));  // not really a good first suggestion...
    assertThat(matches5[1].getSuggestedReplacements().size(), is(1));
    assertThat(matches5[1].getSuggestedReplacements().get(0), is("for the"));
    assertThat(matches5[1].getFromPos(), is(13));
    assertThat(matches5[1].getToPos(), is(20));
    //assertThat(matches5[2].getSuggestedReplacements().get(0), is("the"));

    RuleMatch[] matches6 = rule.match(lt.getAnalyzedSentence("LanguageTol offer sspell checking"));
    assertThat(matches6.length, is(2));
    assertThat(matches6[0].getSuggestedReplacements().get(0), is("LanguageTool"));
    //assertThat(matches6[1].getSuggestedReplacements().size(), is(1));
    assertThat(matches6[1].getSuggestedReplacements().get(0), is("offers spell"));
    assertThat(matches6[1].getFromPos(), is(12));
    assertThat(matches6[1].getToPos(), is(24));
    //assertThat(matches6[2].getSuggestedReplacements().get(0), is("spell"));
    
    RuleMatch[] matches8 = rule.match(lt.getAnalyzedSentence("I'm g oing"));
    assertThat(matches8.length, is(1));
    assertThat(matches8[0].getSuggestedReplacements().get(0), is("going"));
    assertThat(matches8[0].getFromPos(), is(4));
    assertThat(matches8[0].getToPos(), is(10));
    
    RuleMatch[] matches9 = rule.match(lt.getAnalyzedSentence("I'm go ing"));
    assertThat(matches9.length, is(1));
    assertThat(matches9[0].getSuggestedReplacements().get(0), is("going"));
    assertThat(matches9[0].getFromPos(), is(4));
    assertThat(matches9[0].getToPos(), is(10));
    
    RuleMatch[] matches10 = rule.match(lt.getAnalyzedSentence("to thow"));
    assertThat(matches10.length, is(1));
    assertThat(matches10[0].getFromPos(), is(3));
    assertThat(matches10[0].getToPos(), is(7));
    assertThat(matches10[0].getSuggestedReplacements().get(0), is("show")); // not really a good first suggestion... "throw" is 5th
    
  }

  @Test
  public void testSuggestionForIrregularWords() throws IOException {
    // verbs:
    assertSuggestion("He teached us.", "taught");
    assertSuggestion("He buyed the wrong brand", "bought");
    assertSuggestion("I thinked so.", "thought");
    //assertSuggestion("She awaked", "awoke");   // to be added to spelling.txt
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
    assertSuggestion("He speaked", "spoke"); //needs dict update to not include 'spake'

    // double consonants not yet supported:
    //assertSuggestion("He cutted", "cut");
    //assertSuggestion("She runned", "ran");

    // nouns:
    assertSuggestion("auditory stimuluses", "stimuli");
    assertSuggestion("analysises", "analyses");
    assertSuggestion("parenthesises", "parentheses");
    assertSuggestion("childs", "children");
    assertSuggestion("womans", "women");
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

  @Test
  public void testIsMisspelled() throws IOException {
    Assertions.assertTrue(rule.isMisspelled("sdadsadas"));
    Assertions.assertTrue(rule.isMisspelled("bicylce"));
    Assertions.assertTrue(rule.isMisspelled("tabble"));
    Assertions.assertTrue(rule.isMisspelled("tabbles"));

    Assertions.assertFalse(rule.isMisspelled("bicycle"));
    Assertions.assertFalse(rule.isMisspelled("table"));
    Assertions.assertFalse(rule.isMisspelled("tables"));
  }
  
  @Test
  // case: signature is (mostly) English, user starts typing in German -> first, EN is detected for whole text
  // Also see GermanSpellerRuleTest
  public void testMultilingualSignatureCase() throws IOException {
    String sig = "-- " +
                 "Department of Electrical and Electronic Engineering\n" +
                 "Office XY, Sackville Street Building, The University of Manchester, Manchester\n";
    assertZZ("Hallo Herr Müller, wie geht\n\n" + sig);  // "Herr" and "Müller" are accepted by EN speller
    assertZZ("Hallo Frau Müller, wie\n\n" + sig);  // "Frau" and "Müller" are accepted by EN speller
    assertZZ("Hallo Frau Sauer, wie\n\n" + sig);
    //assertZZ("Hallo Frau Müller,\n\n" + sig);  // only "Hallo" not accepted by EN speller
  }

  private void assertZZ(String input) throws IOException {
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText(input);
    assertThat(analyzedSentences.size(), is(2));
    assertThat(rule.match(analyzedSentences.get(0))[0].getErrorLimitLang(), is("zz"));
    Assertions.assertNull(rule.match(analyzedSentences.get(1))[0].getErrorLimitLang());
  }

  @Test
  @Disabled
  public void testInteractiveMultilingualSignatureCase() throws IOException {
    String sig = "-- " +
            "Department of Electrical and Electronic Engineering\n" +
            "Office XY, Sackville Street Building, The University of Manchester, Manchester\n";
    List<AnalyzedSentence> analyzedSentences = lt.analyzeText("Hallo Herr Müller, wie geht\n\n" + sig);
    for (AnalyzedSentence analyzedSentence : analyzedSentences) {
      RuleMatch[] matches = rule.match(analyzedSentence);
      System.out.println("===================");
      System.out.println("S:" + analyzedSentence.getText());
      for (RuleMatch match : matches) {
        System.out.println("  getErrorLimitLang: " + match.getErrorLimitLang());
      }
    }
  }

  @Test
  public void testGetOnlySuggestions() throws IOException {
    assertThat(rule.getOnlySuggestions("cemetary").size(), is(1));
    assertThat(rule.getOnlySuggestions("cemetary").get(0).getReplacement(), is("cemetery"));
    assertThat(rule.getOnlySuggestions("Cemetary").size(), is(1));
    assertThat(rule.getOnlySuggestions("Cemetary").get(0).getReplacement(), is("Cemetery"));
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("cemetary"));
    assertThat(matches.length, is(1));
    assertThat(matches[0].getSuggestedReplacements().size(), is(1));
    assertThat(matches[0].getSuggestedReplacements().get(0), is("cemetery"));
  }

  private void assertSuggestion(String input, String... expectedSuggestions) throws IOException {
    assertSuggestion(input, rule, lt, expectedSuggestions);
  }

  private void assertSuggestion(String input, Rule rule, JLanguageTool lt, String... expectedSuggestions) throws IOException {
    assertSuggestion(input, singletonList(Arrays.asList(expectedSuggestions)), lt, rule);
  }

  private void assertSuggestion(String input, List<List<String>> expectedSuggestionLists, JLanguageTool lt, Rule rule) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(input));
    assertThat("Expected " + expectedSuggestionLists.size() + " match, got: " + Arrays.toString(matches), matches.length, is(expectedSuggestionLists.size()));
    int i = 0;
    for (List<String> expectedSuggestions : expectedSuggestionLists) {
      Assertions.assertTrue(matches[i].getSuggestedReplacements().size() >= expectedSuggestions.size(), "Expected >= " + expectedSuggestions.size() + ", got: " + matches[0].getSuggestedReplacements());
      for (String expectedSuggestion : expectedSuggestions) {
        Assertions.assertTrue(matches[i].getSuggestedReplacements().contains(expectedSuggestion), "Replacements '" + matches[i].getSuggestedReplacements() + "' don't contain expected replacement '" + expectedSuggestion + "'");
      }
      i++;
    }
  }
}
