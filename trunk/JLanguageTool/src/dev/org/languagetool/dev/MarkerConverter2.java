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
package org.languagetool.dev;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.tools.StringTools;

import javax.xml.stream.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convert to the new marker format. Note: is buggy at least with "and" elements.
 * Does not expand entities, unlike MarkerConverter.
 *
 * @deprecated for internal one-time conversion only
 */
public class MarkerConverter2 {

  private final Map<String, Integer> startPos = new HashMap<String, Integer>();
  private final Map<String, Integer> endPos = new HashMap<String, Integer>();
  private boolean needsMarker;
  private boolean inRuleGroup;
  private int currentTokenPos = 0;
  private String currentId = "";
  private int currentSubId = 0;
  private int mark_from = Integer.MAX_VALUE;
  private int mark_to = Integer.MAX_VALUE;

  public void convert(String filename) throws IOException, XMLStreamException {

    initStartAndEndPositions();

    final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
    inputFactory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
    final InputStream in = new FileInputStream(filename);
    final XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);

    while (streamReader.hasNext()) {

      final int eventType = streamReader.next();
      if (eventType == XMLStreamConstants.COMMENT) {
        if (streamReader.hasText()) {
          System.out.print("<!--" + streamReader.getText() + "-->");
        }
      } else if (eventType == XMLStreamConstants.CHARACTERS) {
        if (streamReader.hasText()) {
          System.out.print(StringTools.escapeXML(streamReader.getText()));
        }
      } else {
        if (eventType == XMLStreamConstants.START_ELEMENT) {
          final String localName = streamReader.getLocalName();

          initMarkFromAndMarkTo(streamReader);
          if (localName.equals("rulegroup")) {
            currentSubId = 0;
            inRuleGroup = true;
            currentTokenPos = 0;
          } else if (localName.equals("rule")) {
            currentTokenPos = 0;
            if (inRuleGroup) {
              currentSubId++;
            } else {
              currentSubId = 1;
            }
          } else if (localName.equals("pattern")) {
            final boolean defaultStart = mark_from == Integer.MAX_VALUE || mark_from == 0;
            final boolean defaultEnd = mark_to == Integer.MAX_VALUE || mark_to == 0;
            needsMarker = !defaultStart || !defaultEnd;
          } else if (localName.equals("token")) {
            final String key = currentId + " " + currentSubId;
            //System.out.println("###needsMarker: " + needsMarker + ", " + startPos.get(key) + "==" + (currentTokenPos));
            if (needsMarker && startPos.get(key) == currentTokenPos) {
              System.out.print("<marker>\n");
            }
            currentTokenPos++;
          }

          printStartTag(streamReader, localName);

          // ----------------------------------------------------

        } else if (eventType == XMLStreamConstants.END_ELEMENT) {
          final String localName = streamReader.getLocalName();
          System.out.print("</" + localName + ">");
          if (localName.equals("rulegroup")) {
            inRuleGroup = false;
          } else if (localName.equals("token")) {
            final String key = currentId + " " + currentSubId;
            if (needsMarker && endPos.get(key) == currentTokenPos) {
              System.out.print("\n</marker>");
            }
          }
        } else if (eventType == XMLStreamConstants.ENTITY_REFERENCE) {
          final String localName = streamReader.getLocalName();
          System.out.print("&" + localName + ";");
        }
      }
    }
  }

  private void initStartAndEndPositions() throws IOException {
    final JLanguageTool languageTool = new JLanguageTool(Language.GERMAN);
    languageTool.activateDefaultPatternRules();
    final List<Rule> rules = languageTool.getAllRules();
    for (Rule rule : rules) {
      if (rule instanceof PatternRule) {
        final PatternRule pRule = (PatternRule) rule;
        final String key = pRule.getId() + " " + pRule.getSubId();
        startPos.put(key, pRule.getStartPositionCorrection());
        endPos.put(key, pRule.getElements().size() + pRule.getEndPositionCorrection());
      }
    }
  }

  private void printStartTag(XMLStreamReader eventReader, String localName) {
    System.out.print("<" + localName);
    mark_from = Integer.MAX_VALUE;
    mark_to = Integer.MAX_VALUE;
    for (int i = 0; i < eventReader.getAttributeCount(); i++) {
      final String attributeValue = eventReader.getAttributeValue(i);

      final String attributeLocalName = eventReader.getAttributeLocalName(i);
      if (!attributeLocalName.equals("mark_from") && !attributeLocalName.equals("mark_to")) {
        System.out.print(" " + attributeLocalName + "=\"" + StringTools.escapeXML(attributeValue) + "\"");
      }

      if (attributeLocalName.equals("mark_from")) {
        mark_from = Integer.parseInt(attributeValue);
      } else if (attributeLocalName.equals("mark_to")) {
        mark_to = Integer.parseInt(attributeValue);
      }
      if ((localName.equals("rulegroup") || localName.equals("rule")) && attributeLocalName.equals("id")) {
        if (attributeValue != null) {
          currentId = attributeValue;
        }
      }
    }
    System.out.print(">");
  }

  private void initMarkFromAndMarkTo(XMLStreamReader eventReader) {
    mark_from = Integer.MAX_VALUE;
    mark_to = Integer.MAX_VALUE;
    for (int i = 0; i < eventReader.getAttributeCount(); i++) {
      final String attributeValue = eventReader.getAttributeValue(i);
      if (eventReader.getAttributeLocalName(i).equals("mark_from")) {
        mark_from = Integer.parseInt(attributeValue);
      } else if (eventReader.getAttributeLocalName(i).equals("mark_to")) {
        mark_to = Integer.parseInt(attributeValue);
      }
    }
  }

  public static void main(String args[]) throws XMLStreamException, IOException {
    final MarkerConverter2 converter = new MarkerConverter2();
    converter.convert("/home/dnaber/languagetool/src/rules/de/grammar.xml");
  }

}
