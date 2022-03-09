/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.en;

import org.junit.Test;
import org.languagetool.*;
import org.languagetool.languagemodel.LanguageModel;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.ngrams.FakeLanguageModel;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class UpperCaseNgramRuleTest {

  private final static Map<String, Integer> map = new HashMap<>();
  static {
    map.put("really like", 100);
    map.put("like spaghetti", 100);
    map.put("This was", 100);
    map.put("This was a", 10);
    map.put("this was", 100);
    map.put("this was a", 10);
    map.put("indeed was", 100);
    map.put("indeed was a", 10);
  }
  private final LanguageModel lm = new FakeLanguageModel(map);
  private final Language lang = Languages.getLanguageForShortCode("en");
  private final UpperCaseNgramRule rule = new UpperCaseNgramRule(TestTools.getEnglishMessages(), lm, lang, null);
  private final JLanguageTool lt = new JLanguageTool(lang);

  @Test
  public void testRule() throws IOException {
    assertGood("The New York Times reviews their gallery all the time.");  // from spelling_global.txt
    assertGood("This Was a Good Idea");  // no dot = no real sentence
    assertGood("Professor Sprout acclimated the plant to a new environment.");  // "Professor ..." = antipattern
    assertGood("Beauty products, Clean & Clear facial wash.");
    assertGood("Please click Account > Withdraw > Update.");
    assertGood("The goal is to Develop, Discuss and Learn.");
    assertGood("(b) Summarize the strategy.");
    assertGood("Figure/Ground:");
    assertGood("What Happened?");
    assertGood("1- Have you personally made any improvements?");
    assertGood("Lesson #1 - Create a webinar.");
    assertGood("Please refund Order #5698656.");
    assertGood("Let's play games at Games.co.uk.");
    assertGood("Ben (Been).");
    assertGood("C stands for Curse.");
    assertGood("The United States also used the short-lived slogan, \"Tastes So Good, You'll Roar\", in the early 1980s.");
    assertGood("09/06 - Spoken to the business manager.");
    assertGood("12.3 Game.");
    assertGood("Let's talk to the Onboarding team.");
    assertGood("My name is Gentle.");
    assertGood("They called it Greet.");
    assertGood("What is Foreshadowing?");
    assertGood("His name is Carp.");
    assertGood("Victor or Rabbit as everyone calls him.");
    assertGood("Think I'm Tripping?");
    assertGood("Music and Concepts.");
    assertGood("It is called Ranked mode.");
    assertGood("I was into Chronicle of a Death Foretold.");
    assertGood("I talked with Engineering.");
    assertGood("They used Draft.js to solve it.");
    assertGood("And mine is Wed.");
    assertGood("I would support Knicks rather than Hawks.");
    assertGood("You Can't Judge a Book by the Cover");
    // TODO:
    //assertGood("Best Regards.");
    //assertGood("USB Port.");
    assertGood("ii) Expanded the notes.");

    assertMatch("I really Like spaghetti.");
    assertMatch("This Was a good idea.");
    assertMatch("But this Was a good idea.");
    assertMatch("This indeed Was a good idea.");
  }

  @Test
  public void testFirstLongWordToLeftIsUppercase() throws IOException, URISyntaxException {
    // FIXME commented out version doesn't work when running tests through maven
    //URL ngramUrl = JLanguageTool.getDataBroker().getFromResourceDirAsUrl("/yy/ngram-index");
    //try (LuceneLanguageModel lm = new LuceneLanguageModel(new File(ngramUrl.toURI()))) {
    UpperCaseNgramRule rule = new UpperCaseNgramRule(TestTools.getEnglishMessages(), lm, lang, null);

    AnalyzedTokenReadings[] tokens1 = lt.getAnalyzedSentence("As with Lifeboat and Rope, the principal characters were ...").getTokens();
    // left:
    assertFalse(rule.firstLongWordToLeftIsUppercase(tokens1, 1));   // 1 = "As"
    assertTrue(rule.firstLongWordToLeftIsUppercase(tokens1, 9));    // 9 = "Rope"
    // right:
    assertTrue(rule.firstLongWordToRightIsUppercase(tokens1, 3));    // 3 = "with"
    assertTrue(rule.firstLongWordToRightIsUppercase(tokens1, 5));    // 5 = "Lifeboat"
    assertFalse(rule.firstLongWordToRightIsUppercase(tokens1, 10));  // 10 = ","

    AnalyzedTokenReadings[] tokens2 = lt.getAnalyzedSentence("From Theory to Practice, followed by some other words").getTokens();
    // left:
    assertFalse(rule.firstLongWordToLeftIsUppercase(tokens2, 3));  // 3 = "Theory"
    assertTrue(rule.firstLongWordToLeftIsUppercase(tokens2, 4));   // 4 = "to"
    assertTrue(rule.firstLongWordToLeftIsUppercase(tokens2, 7));   // 7 = "Practice"
    assertFalse(rule.firstLongWordToLeftIsUppercase(tokens2, 1));  // 1 = "From" (sentence start)
    assertFalse(rule.firstLongWordToLeftIsUppercase(tokens2, 12)); // 12 = "by"
    // right:
    assertFalse(rule.firstLongWordToRightIsUppercase(tokens2, 8));   // 8 = ","
    assertFalse(rule.firstLongWordToRightIsUppercase(tokens2, 10));  // 10 = "followed"
    //}
  }

  private void assertGood(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertTrue("Expected no matches, got: " + Arrays.toString(matches), matches.length == 0);
  }

  private void assertMatch(String s) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(s));
    assertTrue("Expected 1 match, got: " + Arrays.toString(matches), matches.length == 1);
  }

}
