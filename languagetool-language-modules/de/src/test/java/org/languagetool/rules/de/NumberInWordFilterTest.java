package org.languagetool.rules.de;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.FakeRule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;

public class NumberInWordFilterTest {

  @Test
  public void testFilter() throws IOException {
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("de"));
    runFilter("Schöne Grüß0e aus Potsdam.", "Grüß0e", "Grüße", 2, 0, 5, lt);
    runFilter("Schöne Grü0e aus Potsdam.", "Grü0e", "Grüße", 2, 0, 5, lt);
  }

  private void runFilter(String input, String arg, String newRepl, int patternTokenPos, int fromPos, int toPos, JLanguageTool lt) throws IOException {
    NumberInWordFilter filter = new NumberInWordFilter();
    AnalyzedSentence sentence = lt.getAnalyzedSentence(input);
    RuleMatch match = new RuleMatch(new FakeRule(), sentence, fromPos, toPos, "fake msg");
    HashMap<String, String> args = new HashMap<>();
    args.put("word", arg);
    RuleMatch matchTmp = filter.acceptRuleMatch(match, args, patternTokenPos, sentence.getTokensWithoutWhitespace());

    assertNotNull(matchTmp);
    assertTrue(matchTmp.getSuggestedReplacements().contains(newRepl));
  }
}
