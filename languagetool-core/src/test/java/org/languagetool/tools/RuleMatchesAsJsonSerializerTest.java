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
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class RuleMatchesAsJsonSerializerTest {

  private final RuleMatchesAsJsonSerializer serializer = new RuleMatchesAsJsonSerializer();
  private final List<RuleMatch> matches = Arrays.asList(
          new RuleMatch(new FakeRule(), 1, 3, "My Message, use <suggestion>foo</suggestion> instead", "short message")
  );

  @Test
  public void testJson() {
    String json = serializer.ruleMatchesToJson(matches, "This is an text.", 5, Languages.getLanguageForShortCode("xx-XX"));
    // Software:
    assertTrue(json.contains("\"LanguageTool\""));
    assertTrue(json.contains(JLanguageTool.VERSION));
    // Language:
    assertTrue(json.contains("\"Testlanguage\""));
    assertTrue(json.contains("\"xx-XX\""));
    // Matches:
    assertTrue(json.contains("\"My Message, use \\\"foo\\\" instead\""));
    assertTrue(json.contains("\"My rule description\""));
    assertTrue(json.contains("\"FAKE_ID\""));
    assertTrue(json.contains("\"This is ...\""));
    assertTrue(json.contains("\"http://foobar.org/blah\""));
    assertTrue(json.contains("\"addition\""));
    assertTrue(json.contains("\"short message\""));
  }
  
  @Test
  public void testJsonWithUnixLinebreak() {
    String json = serializer.ruleMatchesToJson(matches, "This\nis an text.", 5, Languages.getLanguageForShortCode("xx-XX"));
    assertTrue(json.contains("This is ..."));  // got filtered out by ContextTools
  }
  
  @Test
  public void testJsonWithWindowsLinebreak() {
    String json = serializer.ruleMatchesToJson(matches, "This\ris an text.", 5, Languages.getLanguageForShortCode("xx-XX"));
    assertTrue(json.contains("This\\ris ..."));
  }
  
  static class FakeRule extends Rule {
    FakeRule() {
      setLocQualityIssueType(ITSIssueType.Addition);
      try {
        setUrl(new URL("http://foobar.org/blah"));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
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
    @Override
    public void reset() {}
  }

}