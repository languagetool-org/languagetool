/**
 * SWE 261P Software Testing Project
 * By Kenny Chen, Haitong Yan, Jiacheng Zhuo
 */

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
    // This word does not exist, and it should generate a list of suggestion for the correct words.
    List<RuleMatch> matches = lt.check("Aple");

    // The language should return a rule matched for spelling mistake
    String rm= matches.get(0).toString();
    assertEquals(1, matches.size());
    assertEquals("MORFOLOGIK_RULE_EN_US:0-4:Possible spelling mistake found.", rm);

    // It should generate suggestions
    String suggestions = matches.get(0).getSuggestedReplacements().toString();
    assertEquals("[Able, Apple, Ample, Axle, Maple, Ale, Ape, Apse, AELE, ALE, APE, APL, ARLE, EPLE, PLE]", suggestions);
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
    List<RuleMatch> matches = lt.check("I am going to tak a wlk");

    assertEquals(2, matches.size());

    // "tak" should be "take"
    // "wlk" should be "walk"
    assertEquals("take", matches.get(0).getSuggestedReplacements().get(0));
    assertEquals("walk", matches.get(1).getSuggestedReplacements().get(0));
  }
}
