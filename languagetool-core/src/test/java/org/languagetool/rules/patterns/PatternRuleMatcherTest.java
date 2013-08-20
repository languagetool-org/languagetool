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

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PatternRuleMatcherTest {

  private static JLanguageTool langTool;

  @BeforeClass
  public static void setup() throws IOException {
    langTool = new JLanguageTool(new Demo());
  }
  
  @Test
  public void testMatch() throws Exception {
    final PatternRuleMatcher matcher = new PatternRuleMatcher(getPatternRule("my test"), false);
    assertPartialMatch("This is my test.", matcher);
    assertNoMatch("This is no test.", matcher);
  }

  @Test
  public void testZeroMinOccurrences() throws Exception {
    final Element elementB = makeElement("b");
    elementB.setMinOccurrence(0);
    final PatternRuleMatcher matcher = getMatcher(makeElement("a"), elementB, makeElement("c"));  // regex syntax: a b? c
    assertNoMatch("b a", matcher);
    assertNoMatch("c a b", matcher);
    assertPartialMatch("b a c", matcher);
    assertPartialMatch("a c b", matcher);
    assertNoMatch("a b b c", matcher);
    assertCompleteMatch("a c", matcher);
    assertCompleteMatch("a b c", matcher);
    assertNoMatch("a X c", matcher);
    final RuleMatch[] matches = matcher.match(langTool.getAnalyzedSentence("a b c FOO a b c FOO a c a b c"));
    //......................................................................^^^^^.....^^^^^.....^^^.^^^^^
    assertThat(matches.length, is(4));
    assertPosition(matches[0], 0, 5);
    assertPosition(matches[1], 10, 15);
    assertPosition(matches[2], 20, 23);
    assertPosition(matches[3], 24, 29);
  }

  @Test
  public void testZeroMinOccurrencesWithEmptyElement() throws Exception {
    final Element elementB = makeElement(null);
    elementB.setMinOccurrence(0);
    final PatternRuleMatcher matcher = getMatcher(makeElement("a"), elementB, makeElement("c"));  // regex syntax: a .? c
    assertNoMatch("b a", matcher);
    assertNoMatch("c a b", matcher);
    assertPartialMatch("b a c", matcher);
    assertPartialMatch("a c b", matcher);
    assertNoMatch("a b b c", matcher);
    assertCompleteMatch("a c", matcher);
    assertCompleteMatch("a b c", matcher);
    assertCompleteMatch("a X c", matcher);
    final RuleMatch[] matches = matcher.match(langTool.getAnalyzedSentence("a b c FOO a X c"));
    //......................................................................^^^^^.....^^^^^
    assertThat(matches.length, is(2));
    assertPosition(matches[0], 0, 5);
    assertPosition(matches[1], 10, 15);
  }

  @Test
  @Ignore("min can only be 0 or 1 so far")
  public void testTwoMinOccurrences() throws Exception {
    final Element elementB = makeElement("b");
    elementB.setMinOccurrence(2);
    elementB.setMaxOccurrence(3);
    final PatternRuleMatcher matcher = getMatcher(makeElement("a"), elementB, makeElement("c"));  // a b b+ c
    assertCompleteMatch("a b b c", matcher);
    assertCompleteMatch("a b b b c", matcher);
    assertNoMatch("a c", matcher);
    assertNoMatch("a b c", matcher);
  }

  @Test
  public void testZeroMinTwoMaxOccurrences() throws Exception {
    final Element elementB = makeElement("b");
    elementB.setMinOccurrence(0);
    elementB.setMaxOccurrence(2);
    final PatternRuleMatcher matcher = getMatcher(makeElement("a"), elementB, makeElement("c"));
    assertCompleteMatch("a c", matcher);
    assertCompleteMatch("a  b c", matcher);
    assertCompleteMatch("a  b b c", matcher);
    assertNoMatch("a b b b c", matcher);
  }

  @Test
  public void testTwoMaxOccurrences() throws Exception {
    final Element elementB = makeElement("b");
    elementB.setMaxOccurrence(2);
    final PatternRuleMatcher matcher = getMatcher(makeElement("a"), elementB);
    assertNoMatch("a a", matcher);
    assertCompleteMatch("a b", matcher);
    assertCompleteMatch("a b b", matcher);
    assertPartialMatch("a b c", matcher);
    assertPartialMatch("a b b c", matcher);
    assertPartialMatch("x a b b", matcher);
    
    final RuleMatch[] matches1 = matcher.match(langTool.getAnalyzedSentence("a b b b"));
    assertThat(matches1.length, is(1));
    assertPosition(matches1[0], 0, 5);

    final RuleMatch[] matches2 = matcher.match(langTool.getAnalyzedSentence("a b b b foo a b b"));
    assertThat(matches2.length, is(2));
    assertPosition(matches2[0], 0, 5);
    assertPosition(matches2[1], 12, 17);
  }

  @Test
  public void testThreeMaxOccurrences() throws Exception {
    final Element elementB = makeElement("b");
    elementB.setMaxOccurrence(3);
    final PatternRuleMatcher matcher = getMatcher(makeElement("a"), elementB);
    assertNoMatch("a a", matcher);
    assertCompleteMatch("a b", matcher);
    assertCompleteMatch("a b b", matcher);
    assertCompleteMatch("a b b b", matcher);
    assertPartialMatch("a b b b b", matcher);
    
    final RuleMatch[] matches1 = matcher.match(langTool.getAnalyzedSentence("a b b b b"));
    assertThat(matches1.length , is(1));
    assertPosition(matches1[0], 0, 7);
  }

  @Test
  public void testUnlimitedMaxOccurrences() throws Exception {
    final Element elementB = makeElement("b");
    elementB.setMaxOccurrence(-1);
    final PatternRuleMatcher matcher = getMatcher(makeElement("a"), elementB, makeElement("c"));
    assertNoMatch("a c", matcher);
    assertNoMatch("a b", matcher);
    assertNoMatch("b c", matcher);
    assertCompleteMatch("a b c", matcher);
    assertCompleteMatch("a b b c", matcher);
    assertCompleteMatch("a b b b b b b b b b b b b b b b b b b b b b b b b b c", matcher);
  }

  @Test
  public void testMaxTwoAndThreeOccurrences() throws Exception {
    final Element elementA = makeElement("a");
    elementA.setMaxOccurrence(2);
    final Element elementB = makeElement("b");
    elementB.setMaxOccurrence(3);
    final PatternRuleMatcher matcher = getMatcher(elementA, elementB);
    assertCompleteMatch("a b", matcher);
    assertCompleteMatch("a b b", matcher);
    assertCompleteMatch("a b b b", matcher);
    assertNoMatch("a a", matcher);
    assertCompleteMatch("a a b", matcher);
    assertCompleteMatch("a a b b", matcher);
    assertCompleteMatch("a a b b b", matcher);
    assertNoMatch("a x b b b", matcher);
  }

  private void assertPosition(RuleMatch match, int expectedFromPos, int expectedToPos) {
    assertThat(match.getFromPos(), is(expectedFromPos));
    assertThat(match.getToPos(), is(expectedToPos));
  }

  private PatternRuleMatcher getMatcher(Element... patternElements) {
    return new PatternRuleMatcher(getPatternRule(Arrays.asList(patternElements)), false);
  }

  private void assertNoMatch(String input, PatternRuleMatcher matcher) throws IOException {
    final RuleMatch[] matches = matcher.match(langTool.getAnalyzedSentence(input));
    assertThat(matches.length , is(0));
  }

  private void assertPartialMatch(String input, PatternRuleMatcher matcher) throws IOException {
    final RuleMatch[] matches = matcher.match(langTool.getAnalyzedSentence(input));
    assertThat(matches.length , is(1));
    assertTrue(matches[0].getFromPos() > 0 || matches[0].getToPos() < input.length());
  }

  private void assertCompleteMatch(String input, PatternRuleMatcher matcher) throws IOException {
    final RuleMatch[] matches = matcher.match(langTool.getAnalyzedSentence(input));
    assertThat(matches.length , is(1));
    assertThat(matches[0].getFromPos(), is(0));
    assertThat(matches[0].getToPos(), is(input.length()));
  }

  private Element makeElement(String token) {
    return new Element(token, false, false, false);
  }

  private PatternRule getPatternRule(String pattern) {
    final String[] parts = pattern.split(" ");
    List<Element> elements = new ArrayList<>();
    for (String part : parts) {
      elements.add(new Element(part, false, false, false));
    }
    return getPatternRule(elements);
  }

  private PatternRule getPatternRule(List<Element> elements) {
    return new PatternRule("", new Demo(), elements, "my description", "my message", "short message");
  }
}
