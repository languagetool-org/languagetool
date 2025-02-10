package org.languagetool;

import org.junit.Test;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpellCheckingWorkFlowTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));

  @Test
  public void testSpelling() throws IOException {
    List<RuleMatch> matches = lt.check("Aple");
    System.out.println(matches.get(0).getSentence());
    System.out.println(matches.get(0));
    System.out.println(matches.get(0).getSuggestedReplacements().toString());
    assertEquals(1, matches.size());    // One error, plural subject is mismatched with a singular verb
  }
}
