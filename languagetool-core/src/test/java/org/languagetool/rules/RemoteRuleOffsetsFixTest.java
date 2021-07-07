package org.languagetool.rules;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.language.Demo;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

public class RemoteRuleOffsetsFixTest
{

  private List<Integer> printShifts(String text) {
    int[] shifts = RemoteRule.computeOffsetShifts(text);
    for(int i = 0; i < text.length(); i++)  {
      System.out.printf("%c -> %d | ", text.charAt(i), shifts[i]);
    }
    System.out.println();
    return Arrays.stream(shifts).boxed().collect(Collectors.toList());
  }

  @Test
  public void testShiftCalculation() {
    assertEquals(Arrays.asList(0, 1, 1, 1, 1), printShifts("😁foo"));
    assertEquals(Arrays.asList(0, 0, 0, 0, 0, 1, 1, 1, 1, 1), printShifts("foo 😁 bar"));
    assertEquals(Arrays.asList(0, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2), printShifts("😁 foo 😁 bar"));
  }

  @Test
  public void testMatches() throws IOException {
    JLanguageTool lt = new JLanguageTool(new Demo());
    AnalyzedSentence s = lt.getAnalyzedSentence("😁foo bar");
    Rule r = new FakeRule();
    List<RuleMatch> matches = Arrays.asList(
      new RuleMatch(r, s, 0, 1, "Emoji"),
      new RuleMatch(r, s, 1, 4, "foo")
      );
    matches.forEach(System.out::println);
    RemoteRule.fixMatchOffsets(s, matches);
    matches.forEach(System.out::println);
    assertEquals(matches.get(0).getFromPos(), 0);
    assertEquals(matches.get(0).getToPos(), 2);
    assertEquals(matches.get(1).getFromPos(), 2);
    assertEquals(matches.get(1).getToPos(), 5);
  }

}
