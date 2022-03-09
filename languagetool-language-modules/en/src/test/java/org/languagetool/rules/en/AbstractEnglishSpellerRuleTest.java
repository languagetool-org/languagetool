/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

public class AbstractEnglishSpellerRuleTest {

  private JLanguageTool lt;
  private Rule rule;

  public void testNonVariantSpecificSuggestions(Rule rule, Language language) throws IOException {
    this.lt = new JLanguageTool(language);
    this.rule = rule;
    assertFirstMatch("teh", "the");

    // from http://waxy.org/2003/04/typo_popularity/:
    //assertFirstMatch("didnt", "didn't"); - covered by ContractionSpellingRule
    //assertFirstMatch("doesnt", "doesn't"); - covered by ContractionSpellingRule
    assertFirstMatch("seperate", "separate");
    assertFirstMatch("definately", "definitely");
    assertFirstMatch("recieve", "receive");
    assertFirstMatch("offical", "official");
    assertFirstMatch("managment", "management");
    assertFirstMatch("goverment ", "government");
    //assertFirstMatch("commerical", "commercial"); // word was added to en_GB.dic
    assertFirstMatch("Febuary", "February");
    assertFirstMatch("enviroment", "environment");
    assertFirstMatch("occurence", "occurrence");
    assertFirstMatch("commision", "commission");
    assertFirstMatch("assocation", "association");
    assertFirstMatch("Cincinatti", "Cincinnati");
    assertFirstMatch("milennium", "millennium");
    assertFirstMatch("accomodation", "accommodation");
    assertFirstMatch("foriegn", "foreign");
    assertFirstMatch("chemcial", "chemical");
    assertFirstMatch("developement", "development");
    assertFirstMatch("maintainance", "maintenance");
    assertFirstMatch("restaraunt", "restaurant");
    assertFirstMatch("garentee", "guarantee");
    assertFirstMatch("greatful", "grateful");
    assertFirstMatch("hipocrit", "hypocrite");
    assertFirstMatch("mischevious", "mischievous");
    assertFirstMatch("hygeine", "hygiene");
    assertFirstMatch("ur", "your");
    assertFirstMatch("doublecheck", "double-check");
    assertAllMatches(lt, rule, "campaignt", "campaign", "campaigns");
    assertAllMatches(lt, rule, "campaignd", "campaign", "campaigns", "campaigned");
    assertAllMatches(lt, rule, "campaignll");
    assertAllMatches(lt, rule, "spreaded", "spread", "spreader");
    //assertFirstMatch("vehical", "medical", "vehicle"); // suggests vesical now, medical term
    //assertFirstMatch("calender", "calendar");  // handled by grammar.xml
    
    assertEquals(0, rule.match(lt.getAnalyzedSentence("You couldn't; he didn't; it doesn't; they aren't; I hadn't; etc.")).length);

    // currently solved as a special case, also see https://github.com/morfologik/morfologik-stemming/issues/32:
    assertFirstMatch("alot", "a lot");
    // currently solved as a special case (AbstractEnglishSpellerRule.getAdditionalTopSuggestions()):
    assertFirstMatch("speach", "speech");
    assertFirstMatch("qualifys", "qualifies");
    assertFirstMatch("nicefys", "nicely");

    // TODO: these are not very good, maybe caused by https://github.com/morfologik/morfologik-stemming/issues/30?
    //assertFirstMatch("rythem", "them", "rather", "rhythm"); // suggests Ryther now, has been added to en_GB
    assertFirstMatch("vacume", "value", "volume", "acute", "vacuum");
    
    // TODO:
    // http://grammar.yourdictionary.com/spelling-and-word-lists/misspelled.html
    // https://en.wikipedia.org/wiki/Commonly_misspelled_English_words#cite_note-YD-4
  }

  private void assertFirstMatch(String text, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(text));
    assertTrue("Expected 1 match for '" + text + "', got " + matches.length, matches.length == 1);
    List<String> suggestions = matches[0].getSuggestedReplacements();
    assertTrue("Expected at least one suggestion for '" + text + "'", suggestions.size() > 0);
    int i = 0;
    for (String expectedSuggestion : expectedSuggestions) {
      assertThat("Expected suggestion '" + expectedSuggestion + "' not found at position " + i + " in suggestions: "
              + suggestions, suggestions.get(i), is(expectedSuggestion));
      i++;
    }
  }

  protected void assertAllMatches(JLanguageTool lt, Rule rule, String text, String... expectedSuggestions) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(text));
    assertEquals("Expected 1 match for '" + text + "', got " + matches.length, 1, matches.length);
    List<String> suggestions = matches[0].getSuggestedReplacements();
    assertEquals("Expected " + expectedSuggestions.length + " suggestions for '" + text + "', got: " + suggestions,
      suggestions.size(), expectedSuggestions.length);
    int i = 0;
    for (String expectedSuggestion : expectedSuggestions) {
      assertThat("Expected suggestion '" + expectedSuggestion + "' not found at position " + i + " in suggestions: "
              + suggestions, suggestions.get(i), is(expectedSuggestion));
      i++;
    }
  }
}
