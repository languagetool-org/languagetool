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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class AbstractEnglishSpellerRuleTest {

  private JLanguageTool lt;
  private Rule rule;

  public void testNonVariantSpecificSuggestions(Rule rule, Language language) throws IOException {
    this.lt = new JLanguageTool(language);
    this.rule = rule;
    assertFirstMatch("teh", "the");
    
    // from http://waxy.org/2003/04/typo_popularity/:
    assertFirstMatch("transexual", "transsexual");
    //assertFirstMatch("didnt", "didn't"); - covered by ContractionSpellingRule
    //assertFirstMatch("doesnt", "doesn't"); - covered by ContractionSpellingRule
    assertFirstMatch("seperate", "separate");
    //assertFirstMatch("calender", "calendar");  // TODO?
    assertFirstMatch("definately", "definitely");
    assertFirstMatch("recieve", "receive");
    assertFirstMatch("offical", "official");
    assertFirstMatch("managment", "management");
    assertFirstMatch("goverment ", "government");
    assertFirstMatch("commerical", "commercial");
    assertFirstMatch("Febuary", "February");
    assertFirstMatch("enviroment", "environment");
    assertFirstMatch("occurence", "occurrence");
    assertFirstMatch("commision", "commission");
    assertFirstMatch("assocation", "association");
    //assertFirstMatch("Cincinatti", "Cincinnati");  // TODO
    assertFirstMatch("milennium", "millennium");
    assertFirstMatch("accomodation", "accommodation");
    assertFirstMatch("foriegn", "foreign");
    assertFirstMatch("chemcial", "chemical");
    assertFirstMatch("developement", "development");
    
    // TODO:
    // http://grammar.yourdictionary.com/spelling-and-word-lists/misspelled.html
    // https://en.wikipedia.org/wiki/Commonly_misspelled_English_words#cite_note-YD-4
  }

  private void assertFirstMatch(String text, String expectedFirstSuggestion) throws IOException {
    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence(text));
    assertTrue("Expected 1 match for '" + text + "', got " + matches.length, matches.length == 1);
    assertTrue("Expected at least one suggestion for '" + text + "'", matches[0].getSuggestedReplacements().size() > 0);
    assertThat(matches[0].getSuggestedReplacements().get(0), is(expectedFirstSuggestion));
  }
}
