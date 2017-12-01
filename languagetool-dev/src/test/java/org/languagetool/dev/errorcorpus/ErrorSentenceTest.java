/* LanguageTool, a natural language style checker 
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.errorcorpus;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class ErrorSentenceTest {

  private static final FakeRule rule = new FakeRule();

  @Test
  public void testHasErrorCoveredByMatch() {
    ErrorSentence s = new ErrorSentence("this is an test", null, Arrays.asList(new Error(8, 10, null)));

    assertTrue(s.hasErrorCoveredByMatch(new RuleMatch(rule, null, 8, 10, "msg")));  // exact match
    assertTrue(s.hasErrorCoveredByMatch(new RuleMatch(rule, null, 8, 12, "msg")));
    assertTrue(s.hasErrorCoveredByMatch(new RuleMatch(rule, null, 7, 10, "msg")));
    assertTrue(s.hasErrorCoveredByMatch(new RuleMatch(rule, null, 7, 11, "msg")));

    assertFalse(s.hasErrorCoveredByMatch(new RuleMatch(rule, null, 9, 10, "msg"))); // no complete overlap
    assertFalse(s.hasErrorCoveredByMatch(new RuleMatch(rule, null, 8, 9, "msg"))); // no complete overlap
  }

  @Test
  public void testHasErrorOverlappingWithMatch() {
    ErrorSentence s = new ErrorSentence("this is an test", null, Arrays.asList(new Error(8, 10, null)));
    
    assertTrue(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 8, 10, "msg")));  // exact match
    assertTrue(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 8, 12, "msg")));
    assertTrue(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 7, 10, "msg")));
    assertTrue(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 7, 11, "msg")));
    assertTrue(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 9, 10, "msg"))); // no complete overlap
    assertTrue(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 8, 9, "msg"))); // no complete overlap
    assertTrue(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 6, 8, "msg")));
    assertTrue(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 10, 12, "msg")));
    
    assertFalse(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 6, 7, "msg"))); // no overlap
    assertFalse(s.hasErrorOverlappingWithMatch(new RuleMatch(rule, null, 11, 13, "msg"))); // no overlap
  }

  private static class FakeRule extends Rule {
    @Override public String getId() { return "FAKE-RULE-FOR-FILTER"; }
    @Override public String getDescription() { return "<none>"; }
    @Override public RuleMatch[] match(AnalyzedSentence sentence) throws IOException { return new RuleMatch[0]; }
  }

}