/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.rules.patterns;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.languagetool.language.Demo;

import java.io.IOException;

public class PatternRuleXmlCreatorTest {

  @Test
  public void testToXML() throws IOException {
    PatternRuleId ruleId = new PatternRuleId("DEMO_RULE");
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    String xml = creator.toXML(ruleId, new Demo());
    Assertions.assertEquals("<rule id=\"DEMO_RULE\" name=\"Find 'foo bar'\"><!-- a trivial demo rule that matches \"foo\" followed by \"bar\" -->\n" +
    "  <pattern case_sensitive=\"no\">\n" +
    "    <token>foo</token>\n" +
    "    <token>bar</token>\n" +
    "  </pattern>\n" +
    "  <message>Did you mean <suggestion><match no=\"1\"/> fuu bah</suggestion>?</message>\n" +
    "  <url>http://fake-server.org/foo-bar-error-explained</url>\n" +
    "  <example>This is <marker>fuu bah</marker>.</example>\n" +
    "  <example correction=\"foo fuu bah\">This is <marker>foo bar</marker>.</example>\n" +
    "</rule>", xml);
  }

  @Test
  public void testToXMLWithRuleGroup() {
    PatternRuleId ruleId = new PatternRuleId("test_spacebefore");
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    String xml = creator.toXML(ruleId, new Demo());
    Assertions.assertTrue(xml.contains("<rulegroup id=\"test_spacebefore\""));
    Assertions.assertTrue(xml.contains("</rulegroup>"));
    Assertions.assertTrue(xml.contains("<rule>"));
    Assertions.assertTrue(xml.contains("<rule type=\"duplication\">"));
    Assertions.assertTrue(xml.contains("<token>blah</token>"));
  }

  @Test
  public void testToXMLWithRuleGroupAndSubId1() {
    PatternRuleId ruleId = new PatternRuleId("test_spacebefore", "1");
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    String xml = creator.toXML(ruleId, new Demo());
    Assertions.assertFalse(xml.contains("<rulegroup"));
    Assertions.assertFalse(xml.contains("</rulegroup>"));
    Assertions.assertTrue(xml.contains("<message>This is a dummy message 1.</message>"));
  }

  @Test
  public void testToXMLWithRuleGroupAndSubId2() {
    PatternRuleId ruleId = new PatternRuleId("test_spacebefore", "2");
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    String xml = creator.toXML(ruleId, new Demo());
    Assertions.assertFalse(xml.contains("<rulegroup id=\"test_spacebefore\""));
    Assertions.assertFalse(xml.contains("</rulegroup>"));
    Assertions.assertTrue(xml.contains("<message>This is a dummy message 2.</message>"));
  }

  @Test
  public void testToXMLWithAntiPattern() {
    PatternRuleId ruleId = new PatternRuleId("DEMO_RULE_ANTIPATTERN");
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    String xml = creator.toXML(ruleId, new Demo());
    Assertions.assertTrue(xml.contains(
            "  <antipattern>\n" +
            "    <token>bar</token>\n" +
            "    <token>,</token>\n" +
            "  </antipattern>\n"));
  }

  @Test
  public void testToXMLInvalidRuleId() {
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    PatternRuleId fakeRuleId = new PatternRuleId("FAKE_ID");
    try {
      creator.toXML(fakeRuleId, new Demo());
      Assertions.fail();
    } catch(RuntimeException ignored) {}
  }

}
