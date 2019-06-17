/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tools;

import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.DetectedLanguage;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RuleMatchesAsJsonSerializerTest {

  private final RuleMatchesAsJsonSerializer serializer = new RuleMatchesAsJsonSerializer();
  
  private final List<RuleMatch> matches = Arrays.asList(
          new RuleMatch(new FakeRule(),
          new JLanguageTool(Languages.getLanguageForShortCode("xx")).getAnalyzedSentence("This is an test sentence."),
          1, 3, "My Message, use <suggestion>foo</suggestion> instead", "short message")
  );

  public RuleMatchesAsJsonSerializerTest() throws IOException {
  }

  @Test
  public void testJson() {
    DetectedLanguage lang = new DetectedLanguage(Languages.getLanguageForShortCode("xx-XX"), Languages.getLanguageForShortCode("xx-XX")) ;
    String json = serializer.ruleMatchesToJson(matches, "This is an text.", 5, lang);
    // Software:
    assertContains("\"LanguageTool\"", json);
    assertContains(JLanguageTool.VERSION, json);
    // Language:
    assertContains("\"Testlanguage\"", json);
    assertContains("\"xx-XX\"", json);
    // Matches:
    assertContains("\"My Message, use \\\"foo\\\" instead\"", json);
    assertContains("\"My rule description\"", json);
    assertContains("\"FAKE_ID\"", json);
    assertContains("\"This is ...\"", json);
    assertContains("\"http://foobar.org/blah\"", json);
    assertContains("\"addition\"", json);
    assertContains("\"short message\"", json);
    assertContains("\"sentence\":\"This is an test sentence.\"", json);
  }

  private void assertContains(String expectedSubstring, String json) {
    assertTrue("Did not find expected string '" + expectedSubstring + "' in JSON:\n" + json, json.contains(expectedSubstring));
  }

  @Test
  public void testJsonWithUnixLinebreak() {
    DetectedLanguage lang = new DetectedLanguage(Languages.getLanguageForShortCode("xx-XX"), Languages.getLanguageForShortCode("xx-XX")) ;
    String json = serializer.ruleMatchesToJson(matches, "This\nis an text.", 5, lang);
    assertTrue(json.contains("This is ..."));  // got filtered out by ContextTools
  }
  
  @Test
  public void testJsonWithWindowsLinebreak() {
    DetectedLanguage lang = new DetectedLanguage(Languages.getLanguageForShortCode("xx-XX"), Languages.getLanguageForShortCode("xx-XX")) ;
    String json = serializer.ruleMatchesToJson(matches, "This\ris an text.", 5, lang);
    assertTrue(json.contains("This\\ris ..."));
  }
  
  static class FakeRule extends Rule {
    FakeRule() {
      setLocQualityIssueType(ITSIssueType.Addition);
      setUrl(Tools.getUrl("http://foobar.org/blah"));
    }
    @Override
    public String getId() {
      return "FAKE_ID";
    }
    @Override
    public String getDescription() {
      return "My rule description";
    }
    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
      throw new RuntimeException("not implemented");
    }
  }

}
