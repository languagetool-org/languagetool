/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.diff;

import org.junit.Test;

import java.io.StringReader;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class LightRuleMatchParserTest {
  
  @Test
  public void testParse() {
    LightRuleMatchParser parser = new LightRuleMatchParser();
    String s =
      "1.) Line 1, column 9, Rule ID: EN_A_VS_AN\n" +
      "Message: Use 'a' instead of 'an'\n" +
      "Suggestion: a\n" +
      "This is an test. \n" +
      "        ^^       \n" +
      "Time: 10ms for 1 sentences (0.7 sentences/sec)\n";
    List<LightRuleMatch> matches = parser.parse(new StringReader(s));
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getLine(), is(1));
    assertThat(matches.get(0).getColumn(), is(9));
    assertThat(matches.get(0).getRuleId(), is("EN_A_VS_AN"));
    assertThat(matches.get(0).getSuggestions(), is("a"));
    assertThat(matches.get(0).getMessage(), is("Use 'a' instead of 'an'"));
    assertThat(matches.get(0).getCoveredText(), is("an"));
    assertNull(matches.get(0).getSource());
    assertThat(matches.get(0).getContext(), is("This is <span class='marker'> an</span> test. "));
  }

  @Test
  public void testParseTwoMatches() {
    LightRuleMatchParser parser = new LightRuleMatchParser();
    String s = 
      "1.) Line 1, column 9, Rule ID: EN_A_VS_AN\n" +
      "Message: Use 'a' instead of 'an'\n" +
      "Suggestion: a\n" +
      "This is an test. \n" +
      "        ^^       \n" +
      "\n" +
      "2.) Line 5, column 6, Rule ID: FOO2\n" +
      "Message: message2\n" +
      "Suggestion: something\n" +
      "This is somethink test. \n" +
      "        ^^^^^^^^^       \n" +
      "Time: 10ms for 1 sentences (0.7 sentences/sec)\n";
    List<LightRuleMatch> matches = parser.parse(new StringReader(s));
    assertThat(matches.size(), is(2));
    LightRuleMatch match1 = matches.get(0);
    assertThat(match1.getLine(), is(1));
    assertThat(match1.getColumn(), is(9));
    assertThat(match1.getRuleId(), is("EN_A_VS_AN"));
    assertThat(match1.getSuggestions(), is("a"));
    assertThat(match1.getMessage(), is("Use 'a' instead of 'an'"));
    assertThat(match1.getCoveredText(), is("an"));
    LightRuleMatch match2 = matches.get(1);
    assertThat(match2.getLine(), is(5));
    assertThat(match2.getColumn(), is(6));
    assertThat(match2.getRuleId(), is("FOO2"));
    assertThat(match2.getSuggestions(), is("something"));
    assertThat(match2.getMessage(), is("message2"));
    assertThat(match2.getCoveredText(), is("somethink"));
    assertNull(match2.getSource());
    assertThat(match2.getContext(), is("This is <span class='marker'> somethink</span> test. "));
  }

  @Test
  public void testParseNightlyFormat() {
    LightRuleMatchParser parser = new LightRuleMatchParser();
    String s =
      "Title: Anarchism\n" +
      "Line 1, column 35, Rule ID: EN_QUOTES[1]\n" +
      "Message: Use a smart opening quote here: '“'.\n" +
      "Suggestion: “\n" +
      "Rule source: /org/languagetool/rules/en/grammar.xml\n" +
      "Proponents of anarchism, known as \"anarchists\", advocate stateless societies based on...\n" +
      "                                  ^                                                  \n";
    List<LightRuleMatch> matches = parser.parse(new StringReader(s));
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getLine(), is(1));
    assertThat(matches.get(0).getColumn(), is(35));
    assertThat(matches.get(0).getRuleId(), is("EN_QUOTES[1]"));
    assertThat(matches.get(0).getSuggestions(), is("“"));
    assertThat(matches.get(0).getMessage(), is("Use a smart opening quote here: '“'."));
    assertThat(matches.get(0).getCoveredText(), is("\""));
    assertThat(matches.get(0).getSource(), is("/org/languagetool/rules/en/grammar.xml"));
    assertThat(matches.get(0).getContext(), is("Proponents of anarchism, known as <span class='marker'> \"</span>anarchists\", advocate stateless societies based on..."));
  }

  @Test
  public void testParseNightlyFormatnoSuggestion() {
    LightRuleMatchParser parser = new LightRuleMatchParser();
    String s =
      "Title: Anarchism\n" +
      "Line 1, column 35, Rule ID: EN_QUOTES[1]\n" +
      "Message: Use a smart opening quote here: '“'.\n" +
      "Rule source: /org/languagetool/rules/en/grammar-testme.xml\n" +
      "Proponents of anarchism, known as \"anarchists\", advocate stateless societies based on...\n" +
      "                                  ^                                                  \n";
    List<LightRuleMatch> matches = parser.parse(new StringReader(s));
    assertThat(matches.size(), is(1));
    assertThat(matches.get(0).getLine(), is(1));
    assertThat(matches.get(0).getColumn(), is(35));
    assertThat(matches.get(0).getRuleId(), is("EN_QUOTES[1]"));
    assertThat(matches.get(0).getSuggestions(), is(""));
    assertThat(matches.get(0).getMessage(), is("Use a smart opening quote here: '“'."));
    assertThat(matches.get(0).getCoveredText(), is("\""));
    assertThat(matches.get(0).getSource(), is("/org/languagetool/rules/en/grammar-testme.xml"));
    assertThat(matches.get(0).getContext(), is("Proponents of anarchism, known as <span class='marker'> \"</span>anarchists\", advocate stateless societies based on..."));
  }

}
