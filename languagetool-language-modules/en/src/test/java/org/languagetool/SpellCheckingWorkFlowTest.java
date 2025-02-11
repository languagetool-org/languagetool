package org.languagetool;

import org.junit.Test;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SpellCheckingWorkFlowTest {

  private final JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode("en-US"));

  @Test
  public void testSpelling1() throws IOException {
    List<RuleMatch> matches = lt.check("Aple");
    System.out.println(matches.get(0).getSentence());
    System.out.println(matches.get(0));
    System.out.println(matches.get(0).getSuggestedReplacements().toString());
    assertEquals(1, matches.size());    // One error, plural subject is mismatched with a singular verb
  }

  @Test
  public void testSpelling2() throws IOException {
    // This word does exist, the matches variable should be empty
    List<RuleMatch> matches = lt.check("great");
    assertEquals(0, matches.size());
  }

  @Test
  public void testSpelling3() throws IOException {
    // Facebook does not exist in a dictionary, but here is considered a noun
    List<RuleMatch> matches = lt.check("Facebook");
    assertEquals(0, matches.size());
  }

  @Test
  public void testSpelling4() throws IOException {
    // "is" should be replaced by "am"
    // "tak" should be "take"
    // "wlk" should be "walk"
    List<RuleMatch> matches = lt.check("I is going to tak a wlk");
    for (RuleMatch match : matches) {
      System.out.println(match);
    }
    assertEquals(3, matches.size());
  }
}
