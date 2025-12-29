package org.languagetool.rules.en;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class GithubUrlCapitalizationTest {

  @Test
  public void testUrlNotFlagged() throws Exception {
    JLanguageTool langTool = new JLanguageTool(AmericanEnglish.getInstance());
    String text = "Visit https://github.com/SomeUser/Repo for code!";
    List<RuleMatch> matches = langTool.check(text);
    // Expect no capitalization errors
    assertTrue(matches.stream().noneMatch(
        m -> m.getRule().getId().contains("UPPERCASE") ||
             m.getRule().getId().contains("CAPITALIZATION")));
  }
}
