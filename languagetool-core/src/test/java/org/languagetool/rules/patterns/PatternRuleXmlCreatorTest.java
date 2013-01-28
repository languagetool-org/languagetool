/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.languagetool.Language;

public class PatternRuleXmlCreatorTest extends TestCase {

  public void testToXML() throws Exception {
    final PatternRuleXmlCreator xmlCreator = new PatternRuleXmlCreator();
    final List<Element> elements = new ArrayList<Element>();
    elements.add(new Element("der", false, false, false));
    elements.add(new Element("Haus", true, true, true));
    final PatternRule rule = new PatternRule("myId", Language.DEMO, elements, "My Description", "My Message", "My short Message");
    final String xml = xmlCreator.toXML(rule);
    // NOTE: this tests the current state, which is not complete:
    assertEquals("<rule id=\"myId\" name=\"My Description\">\n" +
            "<pattern mark_from=\"0\" mark_to=\"0\">\n" +
            "<token>der</token>\n" +
            "<token regexp=\"yes\" inflected=\"yes\">Haus</token>\n" +
            "</pattern>\n" +
            "<message>My Message</message>\n" +
            "</rule>", xml);
  }
  
}
