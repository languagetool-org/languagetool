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

import junit.framework.TestCase;
import org.languagetool.language.Demo;

import java.io.IOException;

/**
 * Test for a core class, but needs a language to be properly tested, so the test is here.
 */
public class PatternRuleXmlCreatorTest extends TestCase {

  public void testToXML() throws IOException {
    PatternRuleId ruleId = new PatternRuleId("DEMO_RULE");
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    String xml = creator.toXML(ruleId, new Demo());
    assertTrue(xml.contains("<token>foo</token>"));
    assertTrue(xml.contains("<token>bar</token>"));
    assertTrue(xml.contains("<example type=\"correct\">"));
    assertTrue(xml.contains("type=\"incorrect\">"));
  }

  public void testToXMLWithRuleGroup() throws IOException {
    PatternRuleId ruleId = new PatternRuleId("test_spacebefore");
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    String xml = creator.toXML(ruleId, new Demo());
    assertTrue(xml.contains("<rulegroup id=\"test_spacebefore\""));
    assertTrue(xml.contains("</rulegroup>"));
    assertTrue(xml.contains("<rule>"));
    assertTrue(xml.contains("<rule type=\"duplication\">"));
    assertTrue(xml.contains("<token>blah</token>"));
  }

  public void testToXMLWithRuleGroupAndSubId1() throws IOException {
    PatternRuleId ruleId = new PatternRuleId("test_spacebefore", "1");
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    String xml = creator.toXML(ruleId, new Demo());
    assertFalse(xml.contains("<rulegroup"));
    assertFalse(xml.contains("</rulegroup>"));
    assertTrue(xml.contains("<message>This is a dummy message 1.</message>"));
  }

  public void testToXMLWithRuleGroupAndSubId2() throws IOException {
    PatternRuleId ruleId = new PatternRuleId("test_spacebefore", "2");
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    String xml = creator.toXML(ruleId, new Demo());
    assertFalse(xml.contains("<rulegroup id=\"test_spacebefore\""));
    assertFalse(xml.contains("</rulegroup>"));
    assertTrue(xml.contains("<message>This is a dummy message 2.</message>"));
  }

  public void testToXMLInvalidRuleId() throws IOException {
    PatternRuleXmlCreator creator = new PatternRuleXmlCreator();
    PatternRuleId fakeRuleId = new PatternRuleId("FAKE_ID");
    try {
      creator.toXML(fakeRuleId, new Demo());
      fail();
    } catch(RuntimeException expected) {}
  }

}
