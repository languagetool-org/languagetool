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
import org.languagetool.language.*;
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

  @Test
  public void testIgnoringEnglishWordsInSpanish() throws IOException {
    Language lang = new Spanish();
    JLanguageTool lt = new JLanguageTool(lang);
    // No error for unclosed exclamation marks ¡!
    List<RuleMatch> matches = lt.check("This is fantastic!");
    assertEquals(0, matches.size());
    matches = lt.check("The exhibition will feature a combination of new work as well as previously exhibited pieces.");
    assertEquals(0, matches.size());

    matches = lt.check("El president nos informa de la situación.");
    assertEquals(1, matches.size());
    assertEquals("presidente", matches.get(0).getSuggestedReplacements().get(0));
  }

  @Test
  public void testIgnoringEnglishWordsInCatalan() throws IOException {
    Language lang = new Catalan();
    JLanguageTool lt = new JLanguageTool(lang);
    List<RuleMatch> matches = lt.check("To do this");
    assertEquals(0, matches.size());
    matches = lt.check("I'm good at this");
    assertEquals(0, matches.size());
    lt.check("The Live Anthology");
    assertEquals(0, matches.size());
    matches = lt.check("The Ecology of Post-Cultural Revolution Frontier Art és el títol d'un llibre");
    assertEquals(0, matches.size());
    matches = lt.check("This is a good thing és una frase en anglès sense faltez.");
    assertEquals(1, matches.size());
    matches = lt.check("The Great Dictator és una pel·lícula de Chaplin.");
    assertEquals(0, matches.size());
    matches = lt.check("Llegirem una part de The Handmaid's Tale.");
    assertEquals(0, matches.size());
    matches = lt.check("The dit una cosa");
    assertEquals(1, matches.size());
    assertEquals("T'he", matches.get(0).getSuggestedReplacements().get(0));
    matches = lt.check("I am the goalkeeper.");
    assertEquals(0, matches.size());
    matches = lt.check("I'm the goalkeeper.");
    assertEquals(0, matches.size());
    matches = lt.check("Me'n vaig a Malaga.");
    assertEquals(1, matches.size());
    matches = lt.check("Me'n vaig a Malaga of Spain.");
    assertEquals(0, matches.size());
    matches = lt.check("a l´area de");
    assertEquals(2, matches.size());
    matches = lt.check("el interest: 98,850.00 euros");
    assertEquals(3, matches.size());
    matches = lt.check("En aquest video I en aquestes");
    assertEquals(1, matches.size());
    matches = lt.check("D'India i de Pakistan.");
    assertEquals(1, matches.size());
    matches = lt.check("My son is tall.");
    assertEquals(0, matches.size());
    matches = lt.check("The event was successful.");
    assertEquals(0, matches.size());

    matches = lt.check("This is the community manager.");
    assertEquals(0, matches.size());

    matches = lt.check("Aquest és el community manager.");
    assertEquals(1, matches.size());
  }

  @Test
  public void testIgnoringEnglishWordsInDutch() throws IOException {
    Language lang = new Dutch();
    JLanguageTool lt = new JLanguageTool(lang);
    List<RuleMatch> matches = lt.check("This for that was een goede film.");
    assertEquals(0, matches.size());
    matches = lt.check("We got this!");
    assertEquals(0, matches.size());
    // add more tests
  }

  @Test
  public void testIgnoringEnglishWordsInFrench() throws IOException {
    Language lang = new French();
    JLanguageTool lt = new JLanguageTool(lang);
    
    List<RuleMatch> matches = lt.check("Elle a fait le montage des deux clips sur After effect");
    assertEquals(1, matches.size());
    assertEquals("[After Effects]", matches.get(0).getSuggestedReplacements().toString());

    matches = lt.check("House of Entrepreneurship");
    assertEquals(0, matches.size());
  }

  @Test
  public void testIgnoreEnglishWordsInPortuguese() throws IOException {
    JLanguageTool lt = new JLanguageTool(new BrazilianPortuguese());
    lt.disableRules(lt.getAllRules().stream().map(Rule::getId).collect(Collectors.toList()));
    lt.enableRule("MORFOLOGIK_RULE_PT_BR");
    String[] noErrorSentences = new String[]{
      "Ontem vi A New Hope pela primeira vez.",
      "Ela gosta de The Empire Strikes Back.",
      "Mas prefiro The Return of the Jedi.",
      "E quanto a The Phantom Menace, melhor nada dizer.",
      "Aqui adoramos Whose line is it Anyway.",
      "Por enquanto, só How I Met Your Mother.",  // already a multi-token spelling entry
      "Aí ele disse: I am become death, destroyer of worlds.", // intervening punctuation
      "O filme Don't Look Up.",  // contractions
      "Só escrevem que we've told them we won't do it.",  // contractions
      "Esta palavra existe: the precariousness.",  // "-ness" suffix
      "Os melhores livros about business practices.",
      "O filme se chama No Country For Old Men.",  // "no"
      "Acho que era Driving Miss Daisy.",
      "Ele disse Luke I am your father.",
      "Ou teria sido Luke I am looking for your father?",  // "for"
      "Algo mais estranho: I am providing for Mother, talvez?",  // "for"
      "Mas mandou mensagem que she is waiting for your brother.",  // "for"
      "E se for business?",  // "for"
      "Em português é Conduzindo a Miss Daisy, não é?",  // "a"
      "A organização Law Enforcement Agent Protection (Leap)."  // single-word parenthetical
    };
    for (String sentence : noErrorSentences) {
      List<RuleMatch> matches = lt.check(sentence);
      assert matches.isEmpty();
    }
    HashMap<String, String> errorSentences = new HashMap<>();
    errorSentences.put("Foi uma melhora substantial.", "substancial");  // single word
    // match the suffix, but 'whateverness' is not tagged in English, so it's a spelling error
    errorSentences.put("Esta palavra não existe: the whateverness.", "lhe");
    for (Map.Entry<String, String> entry : errorSentences.entrySet()) {
      List<RuleMatch> matches = lt.check(entry.getKey());
      assert !matches.isEmpty();
      assertEquals(entry.getValue(), matches.get(0).getSuggestedReplacements().get(0));
    }
  }

}
