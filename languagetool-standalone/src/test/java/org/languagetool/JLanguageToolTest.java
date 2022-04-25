/* LanguageTool, a natural language style checker 
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import org.hamcrest.CoreMatchers;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.language.Demo;
import org.languagetool.language.English;
import org.languagetool.language.GermanyGerman;
import org.languagetool.markup.AnnotatedText;
import org.languagetool.markup.AnnotatedTextBuilder;
import org.languagetool.rules.*;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class JLanguageToolTest {

  private static final English english = new English();

  @Test
  public void testGetAllActiveRules() {
    JLanguageTool lt = new JLanguageTool(new Demo());
    List<String> ruleIds = getActiveRuleIds(lt);
    assertTrue(ruleIds.contains("DEMO_RULE"));
    assertFalse(ruleIds.contains("DEMO_RULE_OFF"));
    for (Rule rule : lt.getAllRules()) {
      if (rule.getId().equals("DEMO_RULE_OFF")) {
        rule.setDefaultOn();
      }
    }
    List<String> ruleIds2 = getActiveRuleIds(lt);
    assertTrue(ruleIds2.contains("DEMO_RULE_OFF"));
  }

  @Test
  public void testIsPremium() {
    assertFalse(Premium.isPremiumVersion());
  }

  @Test
  public void testEnableRulesCategories() {
    JLanguageTool lt = new JLanguageTool(new Demo());
    List<String> ruleIds = getActiveRuleIds(lt);
    assertTrue(ruleIds.contains("DEMO_RULE"));
    assertFalse(ruleIds.contains("IN_OFF_CATEGORY"));
    
    lt.disableCategory(new CategoryId("MISC"));
    List<String> ruleIds2 = getActiveRuleIds(lt);
    assertFalse(ruleIds2.contains("DEMO_RULE"));
    assertFalse(ruleIds2.contains("IN_OFF_CATEGORY"));
    
    lt.enableRuleCategory(new CategoryId("MISC"));
    List<String> ruleIds3 = getActiveRuleIds(lt);
    assertTrue(ruleIds3.contains("DEMO_RULE"));
    assertFalse(ruleIds3.contains("IN_OFF_CATEGORY"));
    
    lt.enableRuleCategory(new CategoryId("DEFAULT_OFF"));
    List<String> ruleIds4 = getActiveRuleIds(lt);
    assertTrue(ruleIds4.contains("DEMO_RULE"));
    assertTrue(ruleIds4.contains("IN_OFF_CATEGORY"));
    assertFalse(ruleIds4.contains("IN_OFF_CATEGORY_OFF_ITSELF"));
    
    lt.enableRule("IN_OFF_CATEGORY_OFF_ITSELF");
    List<String> ruleIds5 = getActiveRuleIds(lt);
    assertTrue(ruleIds5.contains("IN_OFF_CATEGORY_OFF_ITSELF"));
  }

  private List<String> getActiveRuleIds(JLanguageTool lt) {
    List<String> ruleIds = new ArrayList<>();
    for (Rule rule : lt.getAllActiveRules()) {
      ruleIds.add(rule.getId());
    }
    return ruleIds;
  }

  @Test
  public void testGetMessageBundle() {
    ResourceBundle bundle1 = JLanguageTool.getMessageBundle(new GermanyGerman());
    assertThat(bundle1.getString("de"), is("Deutsch"));

    ResourceBundle bundle2 = JLanguageTool.getMessageBundle(english);
    assertThat(bundle2.getString("de"), is("German"));

    ResourceBundle bundle3 = JLanguageTool.getMessageBundle(new AmericanEnglish());
    assertThat(bundle3.getString("de"), is("German"));
  }

  @Test
  public void testCountLines() {
    assertEquals(0, JLanguageTool.countLineBreaks(""));
    assertEquals(1, JLanguageTool.countLineBreaks("Hallo,\nnächste Zeile"));
    assertEquals(2, JLanguageTool.countLineBreaks("\nZweite\nDritte"));
    assertEquals(4, JLanguageTool.countLineBreaks("\nZweite\nDritte\n\n"));
  }

  @Test
  public void testSentenceTokenize() {
    JLanguageTool lt = new JLanguageTool(english);
    List<String> sentences = lt.sentenceTokenize("This is a sentence! This is another one.");
    assertEquals(2, sentences.size());
    assertEquals("This is a sentence! ", sentences.get(0));
    assertEquals("This is another one.", sentences.get(1));
  }

  @Test
  public void testAnnotateTextCheck() throws IOException {
    JLanguageTool lt = new JLanguageTool(english);
    AnnotatedText annotatedText = new AnnotatedTextBuilder()
            .addMarkup("<b>")
            .addText("here")
            .addMarkup("</b>")
            .addText(" is an error")
            .build();
    List<RuleMatch> matches = lt.check(annotatedText);
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getFromPos(), is(3));
    assertThat(matches.get(0).getToPos(), is(7));
  }

  @Test
  public void testAnnotateTextCheckMultipleSentences() throws IOException {
    JLanguageTool lt = new JLanguageTool(english);
    AnnotatedText annotatedText = new AnnotatedTextBuilder()
            .addMarkup("<b>")
            .addText("here")
            .addMarkup("</b>")
            .addText(" is an error. And ")
            .addMarkup("<i attr='foo'>")
            .addText("here is also")
            .addMarkup("</i>")
            .addText(" a error.")
            .build();
    List<RuleMatch> matches = lt.check(annotatedText);
    assertThat(matches.size(), is(2));
    assertThat(matches.get(0).getFromPos(), is(3));
    assertThat(matches.get(0).getToPos(), is(7));
    assertThat(matches.get(1).getFromPos(), is(60));
    assertThat(matches.get(1).getToPos(), is(61));
  }

  @Test
  public void testAnnotateTextCheckMultipleSentences2() throws IOException {
    JLanguageTool lt = new JLanguageTool(english);

    String textToCheck = "here is an error. And <i attr='foo'/>here is also <i>a</i> error.";
    AnnotatedText annotatedText = new AnnotatedTextBuilder()
            .addText("here")
            .addText(" is an error. And ")
            .addMarkup("<i attr='foo'/>")
            .addText("here is also ")
            .addMarkup("<i>")
            .addText("a")
            .addMarkup("</i>")
            .addText(" error.")
            .build();

    assertThat(annotatedText.getTextWithMarkup(), is(textToCheck));
    List<RuleMatch> matches = lt.check(annotatedText);
    assertThat(matches.size(), is(2));
    assertThat(matches.get(0).getFromPos(), is(0));
    assertThat(matches.get(0).getToPos(), is(4));
    assertThat(matches.get(1).getFromPos(), is(53));
    assertThat(matches.get(1).getToPos(), is(54));
  }

  @Test
  public void testAnnotateTextCheckPlainText() throws IOException {
    JLanguageTool lt = new JLanguageTool(english);
    AnnotatedText annotatedText = new AnnotatedTextBuilder()
            .addText("A good sentence. But here's a error.").build();
    List<RuleMatch> matches = lt.check(annotatedText);
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getFromPos(), is(28));
    assertThat(matches.get(0).getToPos(), is(29));
  }

  @Test
  public void testStrangeInput() throws IOException {
    JLanguageTool lt = new JLanguageTool(english);
    List<RuleMatch> matches = lt.check("­");  // used to be a bug (it's not a normal dash)
    assertThat(matches.size(), is(0));
  }

  @Test
  public void testCache() throws IOException {
    ResultCache cache = new ResultCache(1000);
    JLanguageTool ltEnglish = new JLanguageTool(english, null, cache);
    assertThat(ltEnglish.check("This is an test").size(), is(1));
    assertThat(cache.hitCount(), is(0L));
    assertThat(ltEnglish.check("This is an test").size(), is(1));
    assertThat(cache.hitCount(), is(2L));

    JLanguageTool ltGerman = new JLanguageTool(new GermanyGerman(), null, cache);
    assertTrue(ltGerman.check("This is an test").size() >= 3);
    assertThat(cache.hitCount(), is(2L));

    assertThat(ltEnglish.check("This is an test").size(), is(1));
    assertThat(cache.hitCount(), is(4L));
  }

  @Test
  public void testMatchPositionsWithCache() throws IOException {
    ResultCache cache = new ResultCache(1000);
    JLanguageTool lt = new JLanguageTool(english, null, cache);
    List<RuleMatch> matches1 = lt.check("A test. This is an test.");
    assertThat(matches1.size(), is(1));
    assertThat(matches1.get(0).getFromPos(), is(16));
    assertThat(matches1.get(0).getToPos(), is(18));
    List<RuleMatch> matches2 = lt.check("Another test. This is an test.");
    assertThat(matches2.size(), is(1));
    assertThat(matches2.get(0).getFromPos(), is(16+6));  // position up-to-date despite result from cache
    assertThat(matches2.get(0).getToPos(), is(18+6));
    lt.disableRule("EN_A_VS_AN");
    assertThat(lt.check("Another test. This is an test.").size(), is(0));
    lt.enableRule("EN_A_VS_AN");
    assertThat(lt.check("Another test. This is an test.").size(), is(1));  // still correct even though cache is activated
  }

  @Test
  public void testCacheWithTextLevelRules() throws IOException {
    ResultCache cache = new ResultCache(1000);
    JLanguageTool ltNoCache = new JLanguageTool(new GermanyGerman(), null);
    assertThat(ltNoCache.check("Ein Delfin. Noch ein Delfin.").size(), is(0));
    assertThat(ltNoCache.check("Ein Delfin. Noch ein Delphin.").size(), is(1));

    JLanguageTool ltWithCache = new JLanguageTool(new GermanyGerman(), null, cache);
    assertThat(ltWithCache.check("Ein Delfin. Noch ein Delfin.").size(), is(0));
    assertThat(cache.hitCount(), is(0L));
    assertThat(ltWithCache.check("Ein Delfin. Noch ein Delphin.").size(), is(1));
    assertThat(cache.hitCount(), is(2L));
    assertThat(ltWithCache.check("Ein Delphin. Noch ein Delfin.").size(), is(1));
    assertThat(cache.hitCount(), is(4L));
    assertThat(ltWithCache.check("Ein Delfin. Noch ein Delfin.").size(), is(0));   // try again - no state is kept
    assertThat(cache.hitCount(), is(8L));
    assertThat(ltWithCache.check("Ein Delphin. Noch ein Delphin.").size(), is(0));   // try again - no state is kept
    assertThat(cache.hitCount(), is(12L));
  }

  class InternalRule extends Rule{
    @Override
    public String getId() {
      return "INTERNAL_RULE";
    }
    @Override
    public String getDescription() {
      return "Internal rule";
    }
    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      throw new UnsupportedOperationException();
    }
  }

  @Test
  public void testDisableInternalRule() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Demo(), null);
    lt.addRule(new DemoRule() {
      @Override
      public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
        return toRuleMatchArray(Arrays.stream(super.match(sentence)).map(match ->
          new RuleMatch(new InternalRule(), sentence, match.getFromPos(), match.getToPos(), match.getMessage())
        ).collect(Collectors.toList()));
      }
    });
    List<RuleMatch> matches;
    List<Rule> rules;

    String text = "demo";
    matches = lt.check(text);
    rules = matches.stream().map(RuleMatch::getRule).collect(Collectors.toList());
    assertThat(rules, is(not(hasItem(CoreMatchers.isA(DemoRule.class)))));
    assertThat(rules, is(hasItem(CoreMatchers.isA(InternalRule.class))));

    // disabling implementing rule works
    lt.disableRule(new DemoRule().getId());
    matches = lt.check(text);
    assertThat(matches.size(), is(0));

    // reset
    lt.enableRule(new DemoRule().getId());
    matches = lt.check(text);
    assertThat(matches.size(), is(not(0)));

    // disabling match rule works
    lt.disableRule(new InternalRule().getId());
    matches = lt.check(text);
    assertThat(matches.size(), is(0));

  }

  class TestRule extends Rule{
    private final int subId;

    public TestRule(int subId) {
      this.subId = subId;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      return toRuleMatchArray(Collections.emptyList());
    }

    @Override
    public String getFullId() {
      return String.format("TEST_RULE[%d]", subId);
    }

    @Override
    public String getDescription() {
      return "Test rule";
    }

    @Override
    public String getId() {
      return "TEST_RULE";
    }
  }

  @Test
  public void testDisableFullId() {
    List<Rule> activeRules;
    JLanguageTool lt = new JLanguageTool(new Demo(), null);
    Rule testRule1 = new TestRule(1), testRule2 = new TestRule(2);
    // preconditions / sanity checks
    assertEquals("ruleID equal", testRule1.getId(), testRule2.getId());
    assertNotEquals("fullRuleID not equal", testRule1.getFullId(), testRule2.getFullId());
    assertNotEquals("rule objects not equal", testRule1, testRule2);

    lt.addRule(testRule1);
    lt.addRule(testRule2);

    activeRules = lt.getAllActiveRules();
    assertTrue("added rules are active", activeRules.contains(testRule1));
    assertTrue("added rules are active", activeRules.contains(testRule2));

    // disable rule TEST_RULE -> both TEST_RULE[1] and TEST_RULE[2] should be disabled
    lt.disableRule(testRule1.getId());
    activeRules = lt.getAllActiveRules();
    assertFalse("rules are disabled", activeRules.contains(testRule1));
    assertFalse("rules are disabled", activeRules.contains(testRule2));

    // enable TEST_RULE, disable TEST_RULE[1] -> only TEST_RULE[2] active
    lt.enableRule(testRule1.getId());
    lt.disableRule(testRule1.getFullId());
    activeRules = lt.getAllActiveRules();
    assertFalse("rule disabled by full ID", activeRules.contains(testRule1));
    assertTrue("rule enabled by partial ID ", activeRules.contains(testRule2));
  }

  private class IgnoreInterval {
    int left, right;

    IgnoreInterval(int left, int right) {
      this.left = left;
      this.right = right;
    }

    boolean contains(int position) {
      return left <= position & position <= right;
    }
  }

  private List<IgnoreInterval> calculateIgnoreIntervals(String message, boolean ignoreQuotes, boolean ignoreBrackets) {
    String ignorePattern = "(<.+>[^<]+</.+>)";
    if (ignoreQuotes) {
      ignorePattern += "|('[^']+')|(\"[^\"]\")";
    }
    if (ignoreBrackets) {
      ignorePattern += "|(\\([^)]+\\))";
    }
    Matcher ignoreMat = Pattern.compile(ignorePattern).matcher(message);
    List<IgnoreInterval> ignoreIntervals = new ArrayList<>();
    if (ignoreMat.find()) {
      for (int i = 0; i < ignoreMat.groupCount(); i++) {
        ignoreIntervals.add(new IgnoreInterval(ignoreMat.start(i), ignoreMat.end(i)));
      }
    }
    return ignoreIntervals;
  }

  private String getRuleMessage(Rule rule, JLanguageTool lt) throws Exception {
    Pattern p = Pattern.compile("<.+>([^<]+)</.+>");
    String example = rule.getIncorrectExamples().get(0).getExample();
    example = p.matcher(example).replaceAll("$1");
    List<AnalyzedSentence> sentences = lt.analyzeText(example);

    RuleMatch[] matches;
    if (rule instanceof TextLevelRule) {
      matches = ((TextLevelRule) rule).match(sentences);
    } else {
      matches = rule.match(sentences.get(0));
    }
    if (matches.length == 0) {
      return null;
    }
    return matches[0].getMessage().replace("<suggestion>", "").replace("</suggestion>", "");
  }

  @Test
  @Ignore
  public void testRuleMessagesForSpellingErrors() throws Exception {
    JLanguageTool lt = new JLanguageTool(english);
    //JLanguageTool lt = new JLanguageTool(new GermanyGerman());
    //JLanguageTool lt = new JLanguageTool(new Russian());
    String[] rulesDisabled = {
            // en:
            "EN_QUOTES", "UPPERCASE_SENTENCE_START", "WHITESPACE_RULE",
            "EN_UNPAIRED_BRACKETS", "DASH_RULE", "COMMA_PARENTHESIS_WHITESPACE",
            // de:
            "TYPOGRAFISCHE_ANFUEHRUNGSZEICHEN", "GROESSER_KLEINER_ANFUEHRUNG",
            "ABKUERZUNG_LEERZEICHEN"
    };
    lt.disableRules(Arrays.asList(rulesDisabled));
    int matchesCounter = 0;

    List<Rule> rules = lt.getAllRules();
    for (Rule rule : rules) {
      if (rule.getIncorrectExamples().isEmpty()) {
        continue;
      }
      String message = getRuleMessage(rule, lt);
      if (message == null) {
        continue;
      }
      List<RuleMatch> allMatches = lt.check(message);
      // Ignore errors inside <>..</>, '..', "..", (..)
      List<IgnoreInterval> ignoreIntervals = calculateIgnoreIntervals(message, true, true);
      matches:
      for (RuleMatch ruleMatch : allMatches) {
        if (ruleMatch.getRule().getId().equals(rule.getId())) {
          continue;
        }
        for (IgnoreInterval interval : ignoreIntervals) {
          if (interval.contains(ruleMatch.getFromPos()) || interval.contains(ruleMatch.getToPos())) {
            continue matches;
          }
        }
        System.out.println(String.format("Rule: %s\nMessage: %s\nMatch:\n%s: %s",
                rule.getId(), message, ruleMatch.getRule().getId(), ruleMatch.getMessage()));
        System.out.println(String.format("Error in [%d,%d]: \"%s\"", ruleMatch.getFromPos(),
                ruleMatch.getToPos(), message.substring(ruleMatch.getFromPos(), ruleMatch.getToPos())));
        System.out.println("-------");
        matchesCounter++;
      }
    }
    System.out.println("Total matches:" + matchesCounter);
    assertThat(matchesCounter, is(0));
  }

}
