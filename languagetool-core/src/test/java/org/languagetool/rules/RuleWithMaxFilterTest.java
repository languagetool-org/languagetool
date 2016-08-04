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
package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@SuppressWarnings("MagicNumber")
public class RuleWithMaxFilterTest {

  private static final Language language = TestTools.getDemoLanguage();

  @Test
  public void testFilter() {
    List<PatternToken> fakePatternTokens = new ArrayList<>();
    PatternRule rule1 = new PatternRule("id1", language, fakePatternTokens, "desc1", "msg1", "shortMsg1");
    PatternRule rule2 = new PatternRule("id1", language, fakePatternTokens, "desc2", "msg2", "shortMsg2");
    RuleMatch match1 = new RuleMatch(rule1, 10, 20, "Match1");
    RuleMatch match2 = new RuleMatch(rule2, 15, 25, "Match2");
    RuleWithMaxFilter filter = new RuleWithMaxFilter();
    List<RuleMatch> filteredMatches1 = filter.filter(Arrays.asList(match1, match2));
    assertEquals(2, filteredMatches1.size());
    RuleMatch match3 = new RuleMatch(rule2, 11, 19, "Match3");
    List<RuleMatch> filteredMatches2 = filter.filter(Arrays.asList(match1, match3));
    assertEquals(1, filteredMatches2.size());
  }

  @Test
  public void testNoFilteringIfNotOverlapping() {
    List<PatternToken> fakePatternTokens = new ArrayList<>();
    PatternRule rule1 = new PatternRule("id1", language, fakePatternTokens, "desc1", "msg1", "shortMsg1");
    PatternRule rule2 = new PatternRule("id1", language, fakePatternTokens, "desc2", "msg2", "shortMsg2");
    RuleMatch match1 = new RuleMatch(rule1, 10, 20, "Match1");
    RuleMatch match2 = new RuleMatch(rule2, 21, 25, "Match2");
    RuleWithMaxFilter filter = new RuleWithMaxFilter();
    List<RuleMatch> filteredMatches = filter.filter(Arrays.asList(match1, match2));
    assertEquals(2, filteredMatches.size());
  }

  @Test
  public void testNoFilteringIfDifferentRulegroups() {
    List<PatternToken> fakePatternTokens = new ArrayList<>();
    Rule rule1 = new PatternRule("id1", language, fakePatternTokens, "desc1", "msg1", "shortMsg1");
    Rule rule2 = new PatternRule("id2", language, fakePatternTokens, "desc2", "msg2", "shortMsg2");
    RuleMatch match1 = new RuleMatch(rule1, 10, 20, "Match1");
    RuleMatch match2 = new RuleMatch(rule2, 15, 25, "Match2");
    RuleWithMaxFilter filter = new RuleWithMaxFilter();
    List<RuleMatch> filteredMatches1 = filter.filter(Arrays.asList(match1, match2));
    assertEquals(2, filteredMatches1.size());
    RuleMatch match3 = new RuleMatch(rule2, 11, 19, "Match3");
    List<RuleMatch> filteredMatches2 = filter.filter(Arrays.asList(match1, match3));
    assertEquals(2, filteredMatches2.size());
  }

  @Test
  public void testOverlaps() {
    RuleWithMaxFilter filter = new RuleWithMaxFilter();

    assertTrue(filter.includes(makeRuleMatch(10, 20), makeRuleMatch(10, 20)));
    assertFalse(filter.includes(makeRuleMatch(10, 20), makeRuleMatch(5, 11)));
    assertFalse(filter.includes(makeRuleMatch(10, 20), makeRuleMatch(11, 21)));
    assertTrue(filter.includes(makeRuleMatch(10, 20), makeRuleMatch(11, 19)));
    assertFalse(filter.includes(makeRuleMatch(10, 20), makeRuleMatch(1, 10)));
    assertTrue(filter.includes(makeRuleMatch(10, 20), makeRuleMatch(19, 20)));

    assertFalse(filter.includes(makeRuleMatch(10, 20), makeRuleMatch(21, 30)));
    assertFalse(filter.includes(makeRuleMatch(10, 20), makeRuleMatch(1, 9)));
  }

  private RuleMatch makeRuleMatch(int fromPos, int toPos) {
    return new RuleMatch(new FakeRule(), fromPos, toPos, "FakeMatch1");
  }

}
