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
