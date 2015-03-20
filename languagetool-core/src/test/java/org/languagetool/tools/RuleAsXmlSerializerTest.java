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
package org.languagetool.tools;

import org.junit.Test;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.TestTools;
import org.languagetool.rules.Category;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.FakeLanguage;
import org.languagetool.rules.patterns.PatternToken;
import org.languagetool.rules.patterns.PatternRule;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.TestCase.assertTrue;

@SuppressWarnings("MagicNumber")
public class RuleAsXmlSerializerTest {
  
  private static final RuleAsXmlSerializer SERIALIZER = new RuleAsXmlSerializer();
  private static final Language language = TestTools.getDemoLanguage();

  @Test
  public void testLanguageAttributes() throws IOException {
    final String xml1 = SERIALIZER.ruleMatchesToXml(Collections.<RuleMatch>emptyList(), "Fake", 5, language);
    assertTrue(xml1.contains("shortname=\"xx-XX\""));
    assertTrue(xml1.contains("name=\"Testlanguage\""));
    final String xml2 = SERIALIZER.ruleMatchesToXml(Collections.<RuleMatch>emptyList(), "Fake", 5, language, new FakeLanguage());
    assertTrue(xml2.contains("shortname=\"xx-XX\""));
    assertTrue(xml2.contains("name=\"Testlanguage\""));
    assertTrue(xml2.contains("shortname=\"yy\""));
    assertTrue(xml2.contains("name=\"FakeLanguage\""));
  }

  @Test
  public void testRuleMatchesToXML() throws IOException {
    final List<RuleMatch> matches = new ArrayList<>();
    final String text = "This is an test sentence. Here's another sentence with more text.";
    final FakeRule rule = new FakeRule();
    final RuleMatch match = new RuleMatch(rule, 8, 10, "myMessage");
    match.setColumn(99);
    match.setEndColumn(100);
    match.setLine(44);
    match.setEndLine(45);
    matches.add(match);
    final String xml = SERIALIZER.ruleMatchesToXml(matches, text, 5, language);
    assertTrue(xml.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"));
    final Pattern matchesPattern =
            Pattern.compile(".*<matches software=\"LanguageTool\" version=\"" + JLanguageTool.VERSION + "\" buildDate=\".*?\">.*", Pattern.DOTALL);
    final Matcher matcher = matchesPattern.matcher(xml);
    assertTrue("Did not find expected '<matches>' element, got: " + xml, matcher.matches());
    assertTrue(xml.contains(">\n" +
            "<error fromy=\"44\" fromx=\"98\" toy=\"45\" tox=\"99\" ruleId=\"FAKE_ID\" msg=\"myMessage\" " +
            "replacements=\"\" context=\"...s is an test...\" contextoffset=\"8\" offset=\"8\" errorlength=\"2\" " +
            "locqualityissuetype=\"misspelling\"/>\n" +
            "</matches>\n"));
  }

  @Test
  public void testRuleMatchesToXMLWithCategory() throws IOException {
    final List<RuleMatch> matches = new ArrayList<>();
    final String text = "This is a test sentence.";
    final List<PatternToken> patternTokens = Collections.emptyList();
    final Rule patternRule = new PatternRule("MY_ID", language, patternTokens, "my description", "my message", "short message");
    patternRule.setCategory(new Category("MyCategory"));
    final RuleMatch match = new RuleMatch(patternRule, 8, 10, "myMessage");
    match.setColumn(99);
    match.setEndColumn(100);
    match.setLine(44);
    match.setEndLine(45);
    matches.add(match);
    final String xml = SERIALIZER.ruleMatchesToXml(matches, text, 5, language, language);
    assertTrue(xml.contains(">\n" +
            "<error fromy=\"44\" fromx=\"98\" toy=\"45\" tox=\"99\" ruleId=\"MY_ID\" msg=\"myMessage\" " +
            "replacements=\"\" context=\"...s is a test ...\" contextoffset=\"8\" offset=\"8\" errorlength=\"2\" category=\"MyCategory\" " +
            "locqualityissuetype=\"uncategorized\"/>\n" +
            "</matches>\n"));
  }

  @Test
  public void testRuleMatchesWithUrlToXML() throws IOException {
    final List<RuleMatch> matches = new ArrayList<>();
    final String text = "This is an test sentence. Here's another sentence with more text.";
    final RuleMatch match = new RuleMatch(new FakeRule() {
      @Override
      public URL getUrl() {
        try {
          return new URL("http://server.org?id=1&foo=bar");
        } catch (MalformedURLException e) {
          throw new RuntimeException(e);
        }
      }
    }, 8, 10, "myMessage");
    match.setColumn(99);
    match.setEndColumn(100);
    match.setLine(44);
    match.setEndLine(45);
    matches.add(match);
    final String xml = SERIALIZER.ruleMatchesToXml(matches, text, 5, language);
    assertTrue(xml.contains(">\n" +
            "<error fromy=\"44\" fromx=\"98\" toy=\"45\" tox=\"99\" ruleId=\"FAKE_ID\" msg=\"myMessage\" " +
            "replacements=\"\" context=\"...s is an test...\" contextoffset=\"8\" offset=\"8\" errorlength=\"2\" url=\"http://server.org?id=1&amp;foo=bar\" " +
            "locqualityissuetype=\"misspelling\"/>\n" +
            "</matches>\n"));
  }

  @Test
  public void testRuleMatchesToXMLEscapeBug() throws IOException {
    final List<RuleMatch> matches = new ArrayList<>();
    final String text = "This is \"an test sentence. Here's another sentence with more text.";
    final RuleMatch match = new RuleMatch(new FakeRule(), 9, 11, "myMessage");
    match.setColumn(99);
    match.setEndColumn(100);
    match.setLine(44);
    match.setEndLine(45);
    matches.add(match);
    final String xml = SERIALIZER.ruleMatchesToXml(matches, text, 5, language);
    assertTrue(xml.contains(">\n" +
            "<error fromy=\"44\" fromx=\"98\" toy=\"45\" tox=\"99\" ruleId=\"FAKE_ID\" msg=\"myMessage\" " +
            "replacements=\"\" context=\"... is &quot;an test...\" contextoffset=\"8\" offset=\"9\" errorlength=\"2\" " +
            "locqualityissuetype=\"misspelling\"/>\n" +
            "</matches>\n"));
  }

  private class FakeRule extends PatternRule {
    public FakeRule() {
      super("FAKE_ID", TestTools.getDemoLanguage(), Collections.singletonList(new PatternToken("foo", true, false, false)),
              "My fake description", "Fake message", "Fake short message");
    }
    @Override
    public ITSIssueType getLocQualityIssueType() {
      return ITSIssueType.Misspelling;
    }
  }

}
