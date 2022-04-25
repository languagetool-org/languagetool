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
package org.languagetool;

import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool.ParagraphHandling;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.BritishEnglish;
import org.languagetool.language.English;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.*;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.spelling.SpellingCheckRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class JLanguageToolTest {

  @Ignore("not a test, but used on https://dev.languagetool.org/java-api")
  @Test
  public void demoCodeForHomepage() throws IOException {
    JLanguageTool lt = new JLanguageTool(new BritishEnglish());
    // comment in to use statistical ngram data:
    //lt.activateLanguageModelRules(new File("/data/google-ngram-data"));
    List<RuleMatch> matches = lt.check("A sentence with a error in the Hitchhiker's Guide tot he Galaxy");
    for (RuleMatch match : matches) {
      System.out.println("Potential error at characters " +
          match.getFromPos() + "-" + match.getToPos() + ": " +
          match.getMessage());
      System.out.println("Suggested correction(s): " +
          match.getSuggestedReplacements());
    }
  }

  @Ignore("not a test, but used on https://dev.languagetool.org/java-spell-checker")
  @Test
  public void spellCheckerDemoCodeForHomepage() throws IOException {
    JLanguageTool lt = new JLanguageTool(new BritishEnglish());
    for (Rule rule : lt.getAllRules()) {
      if (!rule.isDictionaryBasedSpellingRule()) {
        lt.disableRule(rule.getId());
      }
    }
    List<RuleMatch> matches = lt.check("A speling error");
    for (RuleMatch match : matches) {
      System.out.println("Potential typo at characters " +
          match.getFromPos() + "-" + match.getToPos() + ": " +
          match.getMessage());
      System.out.println("Suggested correction(s): " +
          match.getSuggestedReplacements());
    }
  }

  @Ignore("not a test, but used on https://dev.languagetool.org/java-spell-checker")
  @Test
  public void spellCheckerDemoCodeForHomepageWithAddedWords() throws IOException {
    JLanguageTool lt = new JLanguageTool(new BritishEnglish());
    for (Rule rule : lt.getAllRules()) {
      if (rule instanceof SpellingCheckRule) {
        ((SpellingCheckRule) rule).addIgnoreTokens(Arrays.asList("myspecialword", "anotherspecialword"));
      }
    }
    List<RuleMatch> matches = lt.check("These are myspecialword and anotherspecialword");
    System.out.println(matches.size() + " matches");   // => "0 matches"
  }

  @Test
  public void testEnglish() throws IOException {
    //more error-free sentences to deal with possible regressions
    if (System.getProperty("disableHardcodedTests") == null) {
      JLanguageTool lt = new JLanguageTool(new English());
      assertNoError("A test that should not give errors.", lt);
      assertNoError("As long as you have hope, a chance remains.", lt);
      assertNoError("A rolling stone gathers no moss.", lt);
      assertNoError("Hard work causes fitness.", lt);
      assertNoError("Gershwin overlays the slow blues theme from section B in the final “Grandioso.”", lt);
      assertNoError("Making ingroup membership more noticeable increases cooperativeness.", lt);
      assertNoError("Dog mushing is more of a sport than a true means of transportation.", lt);
      assertNoError("No one trusts him any more.", lt);
      assertNoError("A member of the United Nations since 1992, Azerbaijan was elected to membership in the newly established Human Rights Council by the United Nations General Assembly on May 9, 2006 (the term of office began on June 19, 2006).", lt);
      assertNoError("Anatomy and geometry are fused in one, and each does something to the other.", lt);
      assertNoError("Certain frogs that lay eggs underground have unpigmented eggs.", lt);
      assertNoError("It's a kind of agreement in which each party gives something to the other, Jack said.", lt);
      assertNoError("Later, you shall know it better.", lt);
      assertNoError("And the few must win what the many lose, for the opposite arrangement would not support markets as we know them at all, and is, in fact, unimaginable.", lt);
      assertNoError("He explained his errand, but without bothering much to make it plausible, for he felt something well up in him which was the reason he had fled the army.", lt);
      assertNoError("I think it's better, and it's not a big deal.", lt);

      assertOneError("A test test that should give errors.", lt);
      assertOneError("I can give you more a detailed description.", lt);
      assertTrue(lt.getAllRules().size() > 1000);
      assertNoError("The sea ice is highly variable — frozen solid during cold, calm weather and broke...", lt);
      assertTrue(lt.getAllRules().size() > 3);
      assertOneError("I can give you more a detailed description.", lt);
      lt.disableRule("MORE_A_JJ");
      assertNoError("I can give you more a detailed description.", lt);
      assertOneError("I've go to go.", lt);
      lt.disableCategory(Categories.TYPOS.getId());
      if (Premium.isPremiumVersion()) {
        assertOneError("I've go to go.", lt);
      } else {
        assertNoError("I've go to go.", lt);
      }
    }
  }

  private void assertNoError(String input, JLanguageTool lt) throws IOException {
    List<RuleMatch> matches = lt.check(input);
    assertEquals("Did not expect an error in test sentence: '" + input + "', but got: " + matches, 0, matches.size());
  }

  private void assertOneError(String input, JLanguageTool lt) throws IOException {
    List<RuleMatch> matches = lt.check(input);
    assertEquals("Did expect 1 error in test sentence: '" + input + "', but got: " + matches, 1, matches.size());
  }

  @Test
  public void testPositionsWithEnglish() throws IOException {
    JLanguageTool tool = new JLanguageTool(new AmericanEnglish());
    List<RuleMatch> matches = tool.check("A sentence with no period\n" +
        "A sentence. A typoh.");
    assertEquals(1, matches.size());
    RuleMatch match = matches.get(0);
    assertEquals(1, match.getLine());
    assertEquals(15, match.getColumn());
  }

  @Test
  public void testPositionsWithEnglishTwoLineBreaks() throws IOException {
    JLanguageTool tool = new JLanguageTool(new AmericanEnglish());
    List<RuleMatch> matches = tool.check("This sentence.\n\n" +
        "A sentence. A typoh.");
    assertEquals(1, matches.size());
    RuleMatch match = matches.get(0);
    assertEquals(2, match.getLine());
    // It was 14. It should actually be 15, as in testPositionsWithEnglish(). 
    // Fixed thanks to a change in the sentence split.
    if (Premium.isPremiumVersion()) {
      // TODO: there should be no difference here
      assertEquals(14, match.getColumn());
    } else {
      assertEquals(15, match.getColumn());
    }
  }

  @Test
  public void testAnalyzedSentence() throws IOException {
    JLanguageTool tool = new JLanguageTool(new English());
    //test soft-hyphen ignoring:
    assertEquals("<S> This[this/DT,B-NP-singular|E-NP-singular] " +
        "is[be/VBZ,B-VP] a[a/DT,B-NP-singular] " +
        "test­ed[tested/JJ,I-NP-singular] " +
        "sentence[sentence/NN,E-NP-singular].[./.,</S>./PCT,O]",
        tool.getAnalyzedSentence("This is a test\u00aded sentence.").toString());
    //test paragraph ends adding
    assertEquals("<S> </S><P/> ", tool.getAnalyzedSentence("\n").toString());
  }

  @Test
  public void testParagraphRules() throws IOException {
    JLanguageTool tool = new JLanguageTool(new English());

    //run normally
    List<RuleMatch> matches1 = tool.check("(This is an quote.\n It ends in the second sentence.");
    assertEquals(2, matches1.size());

    //run in a sentence-only mode
    List<RuleMatch> matches2 = tool.check("(This is an quote.\n It ends in the second sentence.", false, ParagraphHandling.ONLYNONPARA);
    assertEquals(1, matches2.size());
    assertEquals("EN_A_VS_AN", matches2.get(0).getRule().getId());

    //run in a paragraph mode - single sentence
    List<RuleMatch> matches3 = tool.check("(This is an quote.\n It ends in the second sentence.", false, ParagraphHandling.ONLYPARA);
    assertEquals(1, matches3.size());
    assertEquals("EN_UNPAIRED_BRACKETS", matches3.get(0).getRule().getId());

    //run in a paragraph mode - many sentences
    List<RuleMatch> matches4 = tool.check("(This is an quote.\n It ends in the second sentence.", true, ParagraphHandling.ONLYPARA);
    assertEquals(1, matches4.size());
    assertEquals("EN_UNPAIRED_BRACKETS", matches4.get(0).getRule().getId());
  }

  @Test
  public void testWhitespace() throws IOException {
    JLanguageTool tool = new JLanguageTool(new English());
    AnalyzedSentence raw = tool.getRawAnalyzedSentence("Let's do a \"test\", do you understand?");
    AnalyzedSentence cooked = tool.getAnalyzedSentence("Let's do a \"test\", do you understand?");
    //test if there was a change
    assertFalse(raw.equals(cooked));
    //see if nothing has been deleted
    assertEquals(raw.getTokens().length, cooked.getTokens().length);
    int i = 0;
    for (AnalyzedTokenReadings atr : raw.getTokens()) {
      assertEquals(atr.isWhitespaceBefore(),
          cooked.getTokens()[i].isWhitespaceBefore());
      i++;
    }
  }

  @Test
  public void testOverlapFilter() throws IOException {
    Category category = new Category(new CategoryId("TEST_ID"), "test category");
    List<PatternToken> elements1 = Arrays.asList(new PatternToken("one", true, false, false));
    PatternRule rule1 = new PatternRule("id1", new English(), elements1, "desc1", "msg1", "shortMsg1");
    rule1.setSubId("1");
    rule1.setCategory(category);

    List<PatternToken> elements2 = Arrays.asList(new PatternToken("one", true, false, false), new PatternToken("two", true, false, false));
    PatternRule rule2 = new PatternRule("id1", new English(), elements2, "desc2", "msg2", "shortMsg2");
    rule2.setSubId("2");
    rule2.setCategory(category);

    JLanguageTool tool = new JLanguageTool(new English());
    tool.addRule(rule1);
    tool.addRule(rule2);

    List<RuleMatch> ruleMatches1 = tool.check("And one two three.");
    assertEquals("one overlapping rule must be filtered out", 1, ruleMatches1.size());
    assertEquals("msg1", ruleMatches1.get(0).getMessage());

    String sentence = "And one two three.";
    AnalyzedSentence analyzedSentence = tool.getAnalyzedSentence(sentence);
    List<Rule> bothRules = new ArrayList<>(Arrays.asList(rule1, rule2));
    List<RuleMatch> ruleMatches2 = tool.checkAnalyzedSentence(ParagraphHandling.NORMAL, bothRules, analyzedSentence, true);
    assertEquals("one overlapping rule must be filtered out", 1, ruleMatches2.size());
    assertEquals("msg1", ruleMatches2.get(0).getMessage());
  }
  
  @Test
  public void testTextLevelRuleWithGlobalData() throws IOException {
    JLanguageTool tool = new JLanguageTool(new English());
    tool.addRule(new MyTextLevelRule());
    AnnotatedText text1 = new AnnotatedTextBuilder().addGlobalMetaData(AnnotatedText.MetaDataKey.EmailToAddress, "Foo Bar <foo@foo.de>").build();
    assertThat(tool.check(text1).size(), is(1));
    AnnotatedText text2 = new AnnotatedTextBuilder().addGlobalMetaData(AnnotatedText.MetaDataKey.EmailToAddress, "blah blah <foo@foo.de>").build();
    assertThat(tool.check(text2).size(), is(0));
  }
  
  class MyTextLevelRule extends TextLevelRule {
    @Override
    public RuleMatch[] match(List<AnalyzedSentence> sentences, AnnotatedText text) throws IOException {
      if (text.getGlobalMetaData(AnnotatedText.MetaDataKey.EmailToAddress, "").contains("Foo Bar")) {
        return new RuleMatch[]{new RuleMatch(this, null, 0, 1, "test message")};
      }
      return new RuleMatch[0];
    }
    @Override
    public RuleMatch[] match(List<AnalyzedSentence> sentences) throws IOException {
      throw new RuntimeException("not implemented");
    }
    @Override
    public String getId() {
      return "MyTextLevelRule";
    }
    @Override
    public String getDescription() {
      return "Test rule";
    }
    @Override
    public int minToCheckParagraph() {
      return -1;
    }
  }
  
  @Test
  public void testAdvancedTypography() {
    Language lang = new AmericanEnglish();
    assertEquals(lang.toAdvancedTypography("The genitive ('s) may be missing."), "The genitive (’s) may be missing.");
    assertEquals(lang.toAdvancedTypography("The word 'Language‘s' is not standard English"), "The word ‘Language‘s’ is not standard English");
    assertEquals(lang.toAdvancedTypography("Did you mean <suggestion>Language's</suggestion> (straight apostrophe) or <suggestion>Language’s</suggestion> (curly apostrophe)?"), "Did you mean “Language's” (straight apostrophe) or “Language’s” (curly apostrophe)?");
    assertEquals(lang.toAdvancedTypography("Did you mean <suggestion>Language’s</suggestion> (curly apostrophe) or <suggestion>Language's</suggestion> (straight apostrophe)?"), "Did you mean “Language’s” (curly apostrophe) or “Language's” (straight apostrophe)?");
    assertEquals(lang.toAdvancedTypography("Did you mean <suggestion>|?</suggestion>"), "Did you mean “|?”");
  }
}
