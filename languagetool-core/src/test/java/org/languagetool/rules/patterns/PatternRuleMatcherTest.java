/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.language.Demo;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.Match.CaseConversion;
import org.languagetool.rules.patterns.Match.IncludeRange;

@SuppressWarnings("MagicNumber")
public class PatternRuleMatcherTest {

  private static JLanguageTool lt;

  @BeforeClass
  public static void setup() {
    lt = new JLanguageTool(new Demo());
  }

  @Test
  public void testMatch() throws Exception {
    PatternRuleMatcher matcher = new PatternRuleMatcher(getPatternRule("my test"), false);
    assertPartialMatch("This is my test.", matcher);
    assertNoMatch("This is no test.", matcher);
  }

  @Test
  public void testZeroMinOccurrences() throws Exception {
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMinOccurrence(0);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), patternTokenB, makeElement("c"));  // regex syntax: a b? c
    assertNoMatch("b a", matcher);
    assertNoMatch("c a b", matcher);
    assertPartialMatch("b a c", matcher);
    assertPartialMatch("a c b", matcher);
    assertNoMatch("a b b c", matcher);
    assertCompleteMatch("a c", matcher);
    assertCompleteMatch("a b c", matcher);
    assertNoMatch("a X c", matcher);
    RuleMatch[] matches = getMatches("a b c FOO a b c FOO a c a b c", matcher);
    //......................................^^^^^.....^^^^^.....^^^.^^^^^
    assertThat(matches.length, is(4));
    assertPosition(matches[0], 0, 5);
    assertPosition(matches[1], 10, 15);
    assertPosition(matches[2], 20, 23);
    assertPosition(matches[3], 24, 29);
  }

  @Test
  public void testTwoZeroMinOccurrences() throws Exception {
    PatternToken patternTokenB1 = makeElement("ba");
    patternTokenB1.setMinOccurrence(0);
    PatternToken patternTokenB2 = makeElement("bb");
    patternTokenB2.setMinOccurrence(0);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), patternTokenB1, patternTokenB2, makeElement("c"));  // regex syntax: a (ba)? (bb)? c
    
    assertNoMatch("ba a", matcher);
    assertNoMatch("c a bb", matcher);
    assertPartialMatch("z a c", matcher);
    assertPartialMatch("a c z", matcher);
    assertNoMatch("a ba ba c", matcher);
    assertCompleteMatch("a ba bb c", matcher);
    assertCompleteMatch("a ba c", matcher);
    assertCompleteMatch("a bb c", matcher);
    assertCompleteMatch("a c", matcher);
    assertNoMatch("a X c", matcher);
    
    RuleMatch[] matches = getMatches("a ba c FOO a bb c FOO a c a ba bb c", matcher);
    //......................................^^^^^.....^^^^^.....^^^.^^^^^
    assertThat(matches.length, is(4));
    assertPosition(matches[0], 0, 6);
    assertPosition(matches[1], 11, 17);
    assertPosition(matches[2], 22, 25);
    assertPosition(matches[3], 26, 35);
  }

  @Test
  public void testZeroMinOccurrences2() throws Exception {
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMinOccurrence(0);
    // regex syntax: a b? c d e
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), patternTokenB, makeElement("c"), makeElement("d"), makeElement("e"));
    assertCompleteMatch("a b c d e", matcher);
    assertCompleteMatch("a c d e", matcher);
    assertNoMatch("a d", matcher);
    assertNoMatch("a c b d", matcher);
    assertNoMatch("a c b d e", matcher);
  }

  @Test
  public void testZeroMinOccurrences3() throws Exception {
    PatternToken patternTokenC = makeElement("c");
    patternTokenC.setMinOccurrence(0);
    // regex syntax: a b c? d e
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), makeElement("b"), patternTokenC, makeElement("d"), makeElement("e"));
    assertCompleteMatch("a b c d e", matcher);
    assertCompleteMatch("a b d e", matcher);
    assertPartialMatch("a b c d e x", matcher);
    assertPartialMatch("x a b c d e", matcher);
    assertNoMatch("a b c e d", matcher);
    assertNoMatch("a c b d e", matcher);
  }

  @Test
  public void testZeroMinOccurrences4() throws Exception {
    PatternToken patternTokenA = makeElement("a");
    patternTokenA.setMinOccurrence(0);
    PatternToken patternTokenC = makeElement("c");
    patternTokenC.setMinOccurrence(0);
    // regex syntax: a? b c? d e
    PatternRuleMatcher matcher = getMatcher(patternTokenA, makeElement("b"), patternTokenC, makeElement("d"), makeElement("e"));
    RuleMatch[] matches = getMatches("a b c d e", matcher);
    assertThat(matches.length, is(1));  // just the longest match...
    assertPosition(matches[0], 0, 9);
  }

  @Test
  public void testZeroMinOccurrencesWithEmptyElement() throws Exception {
    PatternToken patternTokenB = makeElement(null);
    patternTokenB.setMinOccurrence(0);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), patternTokenB, makeElement("c"));  // regex syntax: a .? c
    assertNoMatch("b a", matcher);
    assertNoMatch("c a b", matcher);
    assertPartialMatch("b a c", matcher);
    assertPartialMatch("a c b", matcher);
    assertNoMatch("a b b c", matcher);
    assertCompleteMatch("a c", matcher);
    assertCompleteMatch("a b c", matcher);
    assertCompleteMatch("a X c", matcher);
    RuleMatch[] matches = getMatches("a b c FOO a X c", matcher);
    //......................................^^^^^.....^^^^^
    assertThat(matches.length, is(2));
    assertPosition(matches[0], 0, 5);
    assertPosition(matches[1], 10, 15);
  }

  @Test
  public void testZeroMinOccurrencesWithSuggestion() throws Exception {
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMinOccurrence(0);
    
    List<PatternToken> patternTokens = Arrays.asList(makeElement("a"), patternTokenB, makeElement("c"));   // regex: a b? c
    PatternRule rule = new PatternRule("", new Demo(), patternTokens, "my description", "<suggestion>\\1 \\2 \\3</suggestion>", "short message");
    PatternRuleMatcher matcher = new PatternRuleMatcher(rule, false);
    
    // we need to add this line to trigger proper replacement but I am not sure why :(
    rule.addSuggestionMatch(new Match(null, null, false, null, null, CaseConversion.NONE, false, false, IncludeRange.NONE));
    
    RuleMatch[] matches = getMatches("a b c", matcher);
    assertEquals(Arrays.asList("a b c"), matches[0].getSuggestedReplacements());

    RuleMatch[] matches2 = getMatches("a c", matcher);
    assertEquals(Arrays.asList("a c"), matches2[0].getSuggestedReplacements());
  }

  @Test
  @Ignore("min can only be 0 or 1 so far")
  public void testTwoMinOccurrences() throws Exception {
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMinOccurrence(2);
    patternTokenB.setMaxOccurrence(3);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), patternTokenB, makeElement("c"));  // regex: a b{2,3} c
    assertCompleteMatch("a b b c", matcher);
    assertCompleteMatch("a b b b c", matcher);
    assertNoMatch("a c", matcher);
    assertNoMatch("a b c", matcher);
  }

  @Test
  public void testZeroMinTwoMaxOccurrences() throws Exception {
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMinOccurrence(0);
    patternTokenB.setMaxOccurrence(2);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), patternTokenB, makeElement("c"));
    assertCompleteMatch("a c", matcher);
    assertCompleteMatch("a  b c", matcher);
    assertCompleteMatch("a  b b c", matcher);
    assertNoMatch("a b b b c", matcher);
  }

  @Test
  public void testTwoMaxOccurrencesWithAnyToken() throws Exception {
    PatternToken anyPatternToken = makeElement(null);
    anyPatternToken.setMaxOccurrence(2);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), anyPatternToken, makeElement("c"));
    assertCompleteMatch("a b c", matcher);
    assertCompleteMatch("a b b c", matcher);
    assertNoMatch("a b b b c", matcher);
  }

  @Test
  public void testThreeMaxOccurrencesWithAnyToken() throws Exception {
    PatternToken anyPatternToken = makeElement(null);
    anyPatternToken.setMaxOccurrence(3);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), anyPatternToken, makeElement("c"));
    assertCompleteMatch("a b c", matcher);
    assertCompleteMatch("a b b c", matcher);
    assertCompleteMatch("a b b b c", matcher);
    assertNoMatch("a b b b b c", matcher);
  }

  @Test
  public void testZeroMinTwoMaxOccurrencesWithAnyToken() throws Exception {
    PatternToken anyPatternToken = makeElement(null);
    anyPatternToken.setMinOccurrence(0);
    anyPatternToken.setMaxOccurrence(2);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), anyPatternToken, makeElement("c"));
    assertNoMatch("a b", matcher);
    assertNoMatch("b c", matcher);
    assertNoMatch("c", matcher);
    assertNoMatch("a", matcher);
    assertCompleteMatch("a c", matcher);
    assertCompleteMatch("a x c", matcher);
    assertCompleteMatch("a x x c", matcher);
    assertNoMatch("a x x x c", matcher);
  }

  @Test
  public void testTwoMaxOccurrences() throws Exception {
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMaxOccurrence(2);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), patternTokenB);
    assertNoMatch("a a", matcher);
    assertCompleteMatch("a b", matcher);
    assertCompleteMatch("a b b", matcher);
    assertPartialMatch("a b c", matcher);
    assertPartialMatch("a b b c", matcher);
    assertPartialMatch("x a b b", matcher);

    RuleMatch[] matches1 = getMatches("a b b b", matcher);
    assertThat(matches1.length, is(1));
    assertPosition(matches1[0], 0, 5);

    RuleMatch[] matches2 = getMatches("a b b b foo a b b", matcher);
    assertThat(matches2.length, is(2));
    assertPosition(matches2[0], 0, 5);
    assertPosition(matches2[1], 12, 17);
  }

  @Test
  public void testThreeMaxOccurrences() throws Exception {
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMaxOccurrence(3);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), patternTokenB);  // regex: a b{1,3}
    assertNoMatch("a a", matcher);
    assertCompleteMatch("a b", matcher);
    assertCompleteMatch("a b b", matcher);
    assertCompleteMatch("a b b b", matcher);
    assertPartialMatch("a b b b b", matcher);

    RuleMatch[] matches1 = getMatches("a b b b b", matcher);
    assertThat(matches1.length, is(1));
    assertPosition(matches1[0], 0, 7);
  }

  @Test
  public void testOptionalWithoutExplicitMarker() throws Exception {
    PatternToken patternTokenA = makeElement("a");
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMinOccurrence(0);
    PatternToken patternTokenC = makeElement("c");
    PatternRuleMatcher matcher = getMatcher(patternTokenA, patternTokenB, patternTokenC);  // regex syntax: a .? c

    RuleMatch[] matches1 = getMatches("A B C ZZZ", matcher);
    assertThat(matches1.length, is(1));
    assertPosition(matches1[0], 0, 5);

    RuleMatch[] matches2 = getMatches("A C ZZZ", matcher);
    assertThat(matches2.length , is(1));
    assertPosition(matches2[0], 0, 3);
  }

  @Test
  public void testOptionalWithExplicitMarker() throws Exception {
    PatternToken patternTokenA = makeElement("a");
    patternTokenA.setInsideMarker(true);
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMinOccurrence(0);
    patternTokenB.setInsideMarker(true);
    PatternToken patternTokenC = makeElement("c");
    patternTokenC.setInsideMarker(false);
    PatternRuleMatcher matcher = getMatcher(patternTokenA, patternTokenB, patternTokenC);  // regex syntax: (a .?) c

    RuleMatch[] matches1 = getMatches("A B C ZZZ", matcher);
    //.......................................^^^--
    assertThat(matches1.length , is(1));
    assertPosition(matches1[0], 0, 3);

    RuleMatch[] matches2 = getMatches("A C ZZZ", matcher);
    //.......................................^--
    assertThat(matches2.length , is(1));
    assertPosition(matches2[0], 0, 1);
  }

  @Test
  public void testOptionalAnyTokenWithExplicitMarker() throws Exception {
    PatternToken patternTokenA = makeElement("a");
    patternTokenA.setInsideMarker(true);
    PatternToken patternTokenB = makeElement(null);
    patternTokenB.setMinOccurrence(0);
    patternTokenB.setInsideMarker(true);
    PatternToken patternTokenC = makeElement("c");
    patternTokenC.setInsideMarker(false);
    PatternRuleMatcher matcher = getMatcher(patternTokenA, patternTokenB, patternTokenC);  // regex syntax: (a .?) c

    RuleMatch[] matches1 = getMatches("A x C ZZZ", matcher);
    //.......................................^^^--
    assertThat(matches1.length , is(1));
    assertPosition(matches1[0], 0, 3);

    RuleMatch[] matches2 = getMatches("A C ZZZ", matcher);
    //.......................................^--
    assertThat(matches2.length , is(1));
    assertPosition(matches2[0], 0, 1);
  }

  @Test
  public void testOptionalAnyTokenWithExplicitMarker2() throws Exception {
    PatternToken patternTokenA = makeElement("the");
    patternTokenA.setInsideMarker(true);
    PatternToken patternTokenB = makeElement(null);
    patternTokenB.setMinOccurrence(0);
    patternTokenB.setInsideMarker(true);
    PatternToken patternTokenC = makeElement("bike");
    patternTokenC.setInsideMarker(false);
    PatternRuleMatcher matcher = getMatcher(patternTokenA, patternTokenB, patternTokenC);  // regex syntax: (a .?) c

    RuleMatch[] matches1 = getMatches("the nice bike ZZZ", matcher);
    //.......................................^^^^^^^^-----
    assertThat(matches1.length , is(1));
    assertPosition(matches1[0], 0, 8);

    RuleMatch[] matches2 = getMatches("the bike ZZZ", matcher);
    //.......................................^^^-----
    assertThat(matches2.length, is(1));
    assertPosition(matches2[0], 0, 3);
  }

  @Test
  public void testUnlimitedMaxOccurrences() throws Exception {
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMaxOccurrence(-1);
    PatternRuleMatcher matcher = getMatcher(makeElement("a"), patternTokenB, makeElement("c"));
    assertNoMatch("a c", matcher);
    assertNoMatch("a b", matcher);
    assertNoMatch("b c", matcher);
    assertCompleteMatch("a b c", matcher);
    assertCompleteMatch("a b b c", matcher);
    assertCompleteMatch("a b b b b b b b b b b b b b b b b b b b b b b b b b c", matcher);
  }

  @Test
  public void testMaxTwoAndThreeOccurrences() throws Exception {
    PatternToken patternTokenA = makeElement("a");
    patternTokenA.setMaxOccurrence(2);
    PatternToken patternTokenB = makeElement("b");
    patternTokenB.setMaxOccurrence(3);
    PatternRuleMatcher matcher = getMatcher(patternTokenA, patternTokenB);  // regex: a{1,2} b{1,3}
    assertCompleteMatch("a b", matcher);
    assertCompleteMatch("a b b", matcher);
    assertCompleteMatch("a b b b", matcher);
    assertNoMatch("a a", matcher);
    assertNoMatch("a x b b b", matcher);
    RuleMatch[] matches2 = getMatches("a a b", matcher);
    assertThat(matches2.length , is(1)); // just the longest match
    assertPosition(matches2[0], 0, 5);

    RuleMatch[] matches3 = getMatches("a a b b", matcher);
    assertThat(matches3.length , is(1));
    assertPosition(matches3[0], 0, 7); // again, only the longest match

    RuleMatch[] matches4 = getMatches("a a b b b", matcher);
    assertThat(matches4.length , is(1));
    assertPosition(matches4[0], 0, 9);
  }

  @Test
  public void testInfiniteSkip() throws Exception {
    PatternToken patternTokenA = makeElement("a");
    patternTokenA.setSkipNext(-1);
    PatternRuleMatcher matcher = getMatcher(patternTokenA, makeElement("b"));
    assertCompleteMatch("a b", matcher);
    assertCompleteMatch("a x b", matcher);
    assertCompleteMatch("a x x b", matcher);
    assertCompleteMatch("a x x x b", matcher);
  }

  @Test
  public void testInfiniteSkipWithMatchReference() throws Exception {
    PatternToken patternTokenAB = new PatternToken("a|b", false, true, false);
    patternTokenAB.setSkipNext(-1);
    PatternToken patternTokenC = makeElement("\\0");
    Match match = new Match(null, null, false, null, null, Match.CaseConversion.NONE, false, false, Match.IncludeRange.NONE);
    match.setTokenRef(0);
    match.setInMessageOnly(true);
    patternTokenC.setMatch(match);
    PatternRuleMatcher matcher = getMatcher(patternTokenAB, patternTokenC);
    assertCompleteMatch("a a", matcher);
    assertCompleteMatch("b b", matcher);
    assertCompleteMatch("a x a", matcher);
    assertCompleteMatch("b x b", matcher);
    assertCompleteMatch("a x x a", matcher);
    assertCompleteMatch("b x x b", matcher);

    assertNoMatch("a b", matcher);
    assertNoMatch("b a", matcher);
    assertNoMatch("b x a", matcher);
    assertNoMatch("b x a", matcher);
    assertNoMatch("a x x b", matcher);
    assertNoMatch("b x x a", matcher);

    RuleMatch[] matches = getMatches("a foo a and b foo b", matcher);
    assertThat(matches.length , is(2));
    assertPosition(matches[0], 0, 7);
    assertPosition(matches[1], 12, 19);

    RuleMatch[] matches2 = getMatches("xx a b x x x b a", matcher);
    assertThat(matches2.length , is(1));
    assertPosition(matches2[0], 3, 16);
  }

  @Test
  public void testNoMatchReferenceRecursion() throws IOException {
    // \n in rule messages refers to matches, but if match text contains \n this should not be resolved
    PatternRule rule = new PatternRule("MATCH_REFERENCERE_CURSION_DEMO", new Demo(),
      Arrays.asList(new PatternToken("\\p{Punct}", false, true, false), new PatternToken("\\d+", false, true, false)),
      "", "Here come the match references: \\1\\2. This is the end", "");
    PatternRuleMatcher matcher = new PatternRuleMatcher(rule, false);
    RuleMatch[] matches = getMatches(":42", matcher);
    assertThat(matches.length, is(1));
    assertThat(matches[0].getMessage(), equalTo("Here come the match references: :42. This is the end"));
    RuleMatch[] matches2 = getMatches("\\42", matcher);
    assertThat(matches2.length, is(1));
    assertThat(matches2[0].getMessage(), equalTo("Here come the match references: \\42. This is the end"));
  }

  @Test
  public void testEquals() throws Exception {
    PatternRule patternRule1 = new PatternRule("id1", Languages.getLanguageForShortCode("xx"),
            Collections.emptyList(), "desc1", "msg1", "short1");
    RuleMatch ruleMatch1 = new RuleMatch(patternRule1, null, 0, 1, "message");
    RuleMatch ruleMatch2 = new RuleMatch(patternRule1, null, 0, 1, "message");
    assertTrue(ruleMatch1.equals(ruleMatch2));
    RuleMatch ruleMatch3 = new RuleMatch(patternRule1, null, 0, 9, "message");
    assertFalse(ruleMatch1.equals(ruleMatch3));
    assertFalse(ruleMatch2.equals(ruleMatch3));
  }

  private RuleMatch[] getMatches(String input, PatternRuleMatcher matcher) throws IOException {
    return matcher.match(lt.getAnalyzedSentence(input));
  }

  private PatternRuleMatcher getMatcher(PatternToken... patternPatternTokens) {
    return new PatternRuleMatcher(getPatternRule(Arrays.asList(patternPatternTokens)), false);
  }

  private void assertPosition(RuleMatch match, int expectedFromPos, int expectedToPos) {
    assertThat("Wrong start position", match.getFromPos(), is(expectedFromPos));
    assertThat("Wrong end position", match.getToPos(), is(expectedToPos));
  }

  private void assertNoMatch(String input, PatternRuleMatcher matcher) throws IOException {
    RuleMatch[] matches = getMatches(input, matcher);
    assertThat(matches.length , is(0));
  }

  private void assertPartialMatch(String input, PatternRuleMatcher matcher) throws IOException {
    RuleMatch[] matches = getMatches(input, matcher);
    assertThat(matches.length , is(1));
    assertTrue("Expected partial match, got '" + matches[0] + "' for '" + input + "'",
        matches[0].getFromPos() > 0 || matches[0].getToPos() < input.length());
  }

  private void assertCompleteMatch(String input, PatternRuleMatcher matcher) throws IOException {
    RuleMatch[] matches = getMatches(input, matcher);
    assertThat("Got matches: " + Arrays.toString(matches), matches.length , is(1));
    assertThat("Wrong start position", matches[0].getFromPos(), is(0));
    assertThat("Wrong end position", matches[0].getToPos(), is(input.length()));
  }

  private PatternToken makeElement(String token) {
    return new PatternToken(token, false, false, false);
  }

  private PatternRule getPatternRule(String pattern) {
    String[] parts = pattern.split(" ");
    List<PatternToken> patternTokens = new ArrayList<>();
    for (String part : parts) {
      patternTokens.add(new PatternToken(part, false, false, false));
    }
    return getPatternRule(patternTokens);
  }

  private PatternRule getPatternRule(List<PatternToken> patternTokens) {
    return new PatternRule("", new Demo(), patternTokens, "my description", "my message", "short message");
  }
}
