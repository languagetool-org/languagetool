package org.languagetool.rules.en;

import org.junit.Test;
import static org.junit.Assert.*;

import org.languagetool.*;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;

public class EnglishWordRepeatRuleExtraTest {

  private final Language english = Languages.getLanguageForShortCode("en");
  private final EnglishWordRepeatRule rule = new EnglishWordRepeatRule(TestTools.getEnglishMessages(), english);
  private final JLanguageTool lt = new JLanguageTool(english);

  @Test
  public void testRuleId() {
    assertEquals("ENGLISH_WORD_REPEAT_RULE", rule.getId());
  }

  @Test
  public void testBasicRepetition() throws IOException {
    // Basic case that should trigger the rule
    assertMatch("This is is just an example.");

    // Control case - no repetition
    assertNoMatch("This is just an example.");
  }

  @Test
  public void testSentenceStartPosition() throws IOException {
    // Test the position == 0 condition
    assertNoMatch("Start of a sentence.");
  }

  @Test
  public void testModalVerbs() throws IOException {
    // Test did/do/does repetition with n't
    assertNoMatch("I did did n't know about that.");
    assertNoMatch("I do do n't understand.");
    assertNoMatch("He does does n't agree.");

    // Control case - should match when not followed by n't
    assertMatch("I did did know about that.");
  }

  @Test
  public void testHerRepetition() throws IOException {
    // Test "her her" with proper POS context
    assertNoMatch("He gave her her phone back.");
  }

  @Test
  public void testHadRepetition() throws IOException {
    // Test "had had" with proper context
    assertNoMatch("If I had had time, I would have gone.");
    assertNoMatch("The man had had enough.");

    // Control case
    assertMatch("The had had is incorrect.");
  }

  @Test
  public void testThatRepetition() throws IOException {
    // Test "that that" with various following POS tags
    assertNoMatch("I don't think that that is a problem.");
    assertNoMatch("I saw that that could be done.");
    assertNoMatch("I believe that that might work.");
    assertNoMatch("I know that that my approach works.");
    assertNoMatch("I thought that that beautiful view was amazing.");
    assertNoMatch("They said that that worked well.");

    // Control case
    assertMatch("That that");
  }

  @Test
  public void testCanRepetition() throws IOException {
    // Test "can can" with noun before
    assertNoMatch("The can can hold the water.");

    // Control case
    assertMatch("I can can do it.");
  }

  @Test
  public void testInterjectionPairs() throws IOException {
    // Test various interjection/phrase pairs
    assertNoMatch("They shouted hip hip hooray!");
    assertNoMatch("In the old days, they called him Bam Bam Bigelow.");
    assertNoMatch("In the wild wild west, there were many outlaws.");
    assertNoMatch("It is far far away from here.");
    assertNoMatch("There is so so much to do.");
    assertNoMatch("There are so so many options.");

    // Control cases
    assertMatch("Let's go hip hip now.");
    assertMatch("He is wild wild about sports.");
  }

  @Test
  public void testSLetterAfterApostrophe() throws IOException {
    // Test 's S case
    assertNoMatch("It's S.T.E.A.M. education.");
  }

  @Test
  public void testLogInRepetition() throws IOException {
    // Test "in in" with log/sign
    assertNoMatch("You need to log in in the morning.");
    assertNoMatch("He logged in in seconds.");
    assertNoMatch("She signs in in the attendance book.");
    assertNoMatch("Please log them in in the system.");

    // Control case
    assertMatch("Go in in the house.");
  }

  @Test
  public void testAbbreviations() throws IOException {
    // Test a.k.a a
    assertNoMatch("He is a.k.a a superhero.");

    // Test E.ON on
    assertNoMatch("Contact E.ON on their website.");
  }

  @Test
  public void testTripleRepetition() throws IOException {
    // Test triple word repetition
    assertNoMatch("The very very very best.");
    assertNoMatch("He said no no no.");
  }

  @Test
  public void testSpelledOutWords() throws IOException {
    // Test single characters with spaces (spelling out)
    assertNoMatch("He spelled it out as b a s i c.");
  }

  @Test
  public void testCommonRepeatedPhrases() throws IOException {
    // Test various idiomatic repetitions
    String[] repeatedPhrases = {
      "aye", "blah", "mau", "uh", "paw", "cha", "yum", "wop", "woop",
      "fnarr", "fnar", "ha", "omg", "boo", "tick", "twinkle", "ta", "la",
      "x", "hi", "ho", "heh", "jay", "walla", "sri", "hey", "hah", "oh",
      "ouh", "chop", "ring", "beep", "bleep", "yeah", "gout", "quack",
      "meow", "squawk", "whoa", "si", "honk", "brum", "chi", "santorio", "lapu",
      "chow", "shh", "yummy", "boom", "bye", "ah", "aah", "bang", "woof", "wink",
      "yes", "tsk", "hush", "ding", "choo", "miu", "tuk", "yadda", "doo", "sapiens",
      "tse", "no"
    };

    for (String phrase : repeatedPhrases) {
      assertNoMatch("They said " + phrase + " " + phrase + " and left.");
    }

    // Test the wait with position == 2
    assertNoMatch("But wait, wait for me!");

    // Control case - position != 2
    assertMatch("I will wait wait here.");
  }

  @Test
  public void testMonthCapitalization() throws IOException {
    // Test may/May cases
    assertNoMatch("It may May be the right time.");
    assertNoMatch("In May may we have good weather.");
    assertNoMatch("May May is my friend.");
  }

  @Test
  public void testWillCapitalization() throws IOException {
    // Test will/Will cases
    assertNoMatch("He will Will the company to his son.");
    assertNoMatch("Will will be here soon.");
    assertNoMatch("Will Will is my neighbor.");
  }

  @Test
  public void testSuperIgnore() throws IOException {
    // Test the super.ignore call
    assertMatch("This test test should match.");
  }

  private void assertMatch(String text) throws IOException {
    AnalyzedSentence aSentence = lt.getAnalyzedSentence(text);
    RuleMatch[] matches = rule.match(aSentence);
    assertNotEquals("Expected rule to match: " + text, 0, matches.length);
  }

  private void assertNoMatch(String text) throws IOException {
    AnalyzedSentence aSentence = lt.getAnalyzedSentence(text);
    RuleMatch[] matches = rule.match(aSentence);
    assertEquals("Expected no matches but got: " + Arrays.toString(matches) + " for text: " + text, 0, matches.length);
  }
}