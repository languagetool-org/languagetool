/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Fabian Richter
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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


public class RemoteRuleOffsetsFixTest {

  private List<Integer> printShifts(String text) {
    int[] shifts = RemoteRule.computeOffsetShifts(text);
    for (int i = 0; i < text.length(); i++) {
      //System.out.printf("%c -> %d | ", text.charAt(i), shifts[i]);
    }
    //System.out.println();
    return Arrays.stream(shifts).boxed().collect(Collectors.toList());
  }

  @Test
  public void testShiftCalculation() {
    Assertions.assertEquals(Arrays.asList(0, 2, 3, 4, 5, 6), printShifts("😁foo"));
    Assertions.assertEquals(Arrays.asList(0, 1, 2, 3, 4, 6, 7, 8, 9, 10, 11), printShifts("foo 😁 bar"));
    Assertions.assertEquals(Arrays.asList(0, 2, 3, 4, 5, 6, 7, 9, 10, 11, 12, 13, 14, 15), printShifts("😁 foo 😁 bar"));

    Assertions.assertEquals(Arrays.asList(0, 2, 3), printShifts("👪"), "1 code point, length 2 / 1");
    Assertions.assertEquals(Arrays.asList(0, 2, 4, 5, 6), printShifts("👍🏼"), "1 code point for each part, length 4 / 2, displayed as 1");
    Assertions.assertEquals(Arrays.asList(0, 1), printShifts("a"), "normal text");
  }

  private void testMatch(String text, int from, int to, int fixedFrom, int fixedTo) throws IOException {
    JLanguageTool lt = new JLanguageTool(new Demo());
    AnalyzedSentence s = lt.getAnalyzedSentence(text);
    Rule r = new FakeRule();
    List<RuleMatch> matches = Collections.singletonList(new RuleMatch(r, s, from, to, "test match"));
    RemoteRule.fixMatchOffsets(s, matches);
    Assertions.assertEquals(fixedFrom, matches.get(0).getFromPos());
    Assertions.assertEquals(fixedTo, matches.get(0).getToPos());
  }

  @Test
  public void testMatches() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Demo());
    AnalyzedSentence s = lt.getAnalyzedSentence("😁foo bar");
    Rule r = new FakeRule();
    // multiple matches
    List<RuleMatch> matches = Arrays.asList(
      new RuleMatch(r, s, 0, 1, "Emoji"),
      new RuleMatch(r, s, 1, 4, "foo")
      );
    RemoteRule.fixMatchOffsets(s, matches);
    Assertions.assertEquals(matches.get(0).getFromPos(), 0);
    Assertions.assertEquals(matches.get(0).getToPos(), 2);
    Assertions.assertEquals(matches.get(1).getFromPos(), 2);
    Assertions.assertEquals(matches.get(1).getToPos(), 5);

    testMatch("😁foo bar", 0, 1, 0, 2);
    testMatch("😁foo bar", 1, 4, 2, 5);
    testMatch("👪", 0, 1, 0, 2);
    testMatch("👍🏼", 0, 2, 0, 4);
  }

  @Test
  public void testException() throws Exception {
    JLanguageTool lt = new JLanguageTool(new Demo());
    AnalyzedSentence s = lt.getAnalyzedSentence("abondoned");
    Rule r = new FakeRule();
    // multiple matches
    List<RuleMatch> matches = Collections.singletonList(
            new RuleMatch(r, s, 0, 9, "Match")
    );

    printShifts("abondoned");
    RemoteRule.fixMatchOffsets(s, matches);
  }

}
