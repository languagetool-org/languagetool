/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber
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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;
import org.languagetool.Tag;
import org.languagetool.language.Demo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

public class CleanOverlappingFilterTest {

  @Test
  public void testFilter() {
    CleanOverlappingFilter filter = new MyCleanOverlappingFilter(new MyDemoLang(), true);
    AnalyzedSentence sentence = new AnalyzedSentence(new AnalyzedTokenReadings[]{});

    List<RuleMatch> matches1 = new ArrayList<>();
    matches1 = filter.filter(matches1);
    assertThat(matches1.size(), is(0));

    List<RuleMatch> matches2 = Arrays.asList(
      new RuleMatch(new FakeRule(), sentence, 0, 10, "msg"),
      new RuleMatch(new FakeRule(), sentence, 11, 20, "msg"));
    matches2 = filter.filter(matches2);
    assertThat(matches2.size(), is(2));  // no overlap -> not filtering

    List<RuleMatch> matches3 = Arrays.asList(
      new RuleMatch(new FakeRule(), sentence, 0, 10, "msg"),
      new RuleMatch(new FakeRule(), sentence, 10, 20, "msg"));
    matches3 = filter.filter(matches3);
    assertThat(matches3.size(), is(2));  // no overlap -> not filtering

    List<RuleMatch> matches4 = Arrays.asList(
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 0, 10, "msg1"),
      new RuleMatch(new FakeRule("P1_RULE_B"), sentence, 9, 20, "msg2"));
    matches4 = filter.filter(matches4);
    assertThat(matches4.size(), is(1));  // overlap -> filtering
    assertThat(matches4.get(0).getRule().getId(), is("P1_RULE_B"));

    List<RuleMatch> matches5 = Arrays.asList(
      new RuleMatch(new FakeRule("P1_RULE_B"), sentence, 0, 10, "msg1"),
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 9, 20, "msg2"));
    matches5 = filter.filter(matches5);
    assertThat(matches5.size(), is(1));
    assertThat(matches5.get(0).getRule().getId(), is("P1_RULE"));  // latest wins with equal priority

    List<RuleMatch> matches6 = Arrays.asList(
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 0, 10, "msg1"),
      new RuleMatch(new FakeRule("P2_RULE"), sentence, 9, 20, "msg2"));
    matches6 = filter.filter(matches6);
    assertThat(matches6.size(), is(1));
    assertThat(matches6.get(0).getRule().getId(), is("P2_RULE"));  // P2 has higher priority

    List<RuleMatch> matches7 = Arrays.asList(
      new RuleMatch(new FakeRule("P2_RULE"), sentence, 0, 10, "msg1"),
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 9, 20, "msg2"));
    matches7 = filter.filter(matches7);
    assertThat(matches7.size(), is(1));
    assertThat(matches7.get(0).getRule().getId(), is("P2_RULE"));  // P2 has higher priority

    List<RuleMatch> matches8 = Arrays.asList(
      new RuleMatch(new FakeRule("P2_PREMIUM_RULE"), sentence, 0, 10, "msg1"),
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 9, 20, "msg2"));
    matches8 = filter.filter(matches8);
    assertThat(matches8.size(), is(1));
    assertThat(matches8.get(0).getRule().getId(), is("P1_RULE"));  // P2 has higher priority but premium rules are hidden

    List<RuleMatch> matches8b = Arrays.asList(
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 0, 10, "msg2"),
      new RuleMatch(new FakeRule("P2_PREMIUM_RULE"), sentence, 9, 20, "msg1"));
    matches8b = filter.filter(matches8b);
    assertThat(matches8b.size(), is(1));
    assertThat(matches8b.get(0).getRule().getId(), is("P1_RULE"));  // P2 has higher priority but premium rules are hidden

    List<RuleMatch> matches9 = Arrays.asList(
      new RuleMatch(new FakeRule("P2_PREMIUM_RULE", Tag.picky), sentence, 0, 10, "msg1"),
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 9, 20, "msg2"));
    matches9 = filter.filter(matches9);
    assertThat(matches9.size(), is(1));
    assertThat(matches9.get(0).getRule().getId(), is("P1_RULE"));  // P2 has higher priority but premium rules are hidden

    List<RuleMatch> matches10 = Arrays.asList(
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 0, 10, "msg2"),
      new RuleMatch(new FakeRule("P2_PREMIUM_RULE", Tag.picky), sentence, 9, 20, "msg1"));
    matches10 = filter.filter(matches10);
    assertThat(matches10.size(), is(1));
    assertThat(matches10.get(0).getRule().getId(), is("P1_RULE"));  // P2 has higher priority but premium rules are hidden

    List<RuleMatch> matches11 = Arrays.asList(
      new RuleMatch(new FakeRule("P2_RULE", Tag.picky), sentence, 0, 10, "msg1"),
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 9, 20, "msg2"));
    matches11 = filter.filter(matches11);
    assertThat(matches11.size(), is(1));
    assertThat(matches11.get(0).getRule().getId(), is("P1_RULE"));  // P2 has higher priority but it's picky

    List<RuleMatch> matches12 = Arrays.asList(
      new RuleMatch(new FakeRule("P1_RULE"), sentence, 0, 10, "msg2"),
      new RuleMatch(new FakeRule("P2_RULE", Tag.picky), sentence, 9, 20, "msg1"));
    matches12 = filter.filter(matches12);
    assertThat(matches12.size(), is(1));
    assertThat(matches12.get(0).getRule().getId(), is("P1_RULE"));  // P2 has higher priority but it's picky

    /* TODO
    List<RuleMatch> matches13 = Arrays.asList(
      new RuleMatch(new FakeRule("P2_RULE", Tag.picky), sentence, 0, 10, "msg picky"),
      new RuleMatch(new FakeRule("P1_PREMIUM_RULE"), sentence, 9, 20, "msg"));
    matches13 = filter.filter(matches13);
    assertThat(matches13.size(), is(1));
    assertThat(matches13.get(0).getRule().getId(), is("P1_RULE"));  // hidden match should be kept
    */

    try {
      List<RuleMatch> unordered = Arrays.asList(
        new RuleMatch(new FakeRule("P1_RULE"), sentence, 11, 12, "msg2"),
        new RuleMatch(new FakeRule("P2_RULE", Tag.picky), sentence, 9, 10, "msg1"));
      filter.filter(unordered);
      fail();
    } catch (IllegalArgumentException expected) {}

  }

  static class MyCleanOverlappingFilter extends CleanOverlappingFilter {
    MyCleanOverlappingFilter(Language lang, boolean hidePremiumMatches) {
      super(lang, hidePremiumMatches);
    }
    @Override
    protected boolean isPremiumRule(RuleMatch ruleMatch) {
      return ruleMatch.getRule().getId().contains("PREMIUM");
    }
  }

  static class MyDemoLang extends Demo {
    @Override
    protected int getPriorityForId(String id) {
      switch (id) {
        case "P3_RULE": return 3;
        case "P2_RULE":
        case "P2_PREMIUM_RULE":
                        return 2;
        case "P1_RULE":
        case "P1_RULE_B":
        case "P1_PREMIUM_RULE":
                        return 1;
        case "MISC":    return 0;
        default: throw new RuntimeException("No priority defined for " + id);
      }
    }
  }

}