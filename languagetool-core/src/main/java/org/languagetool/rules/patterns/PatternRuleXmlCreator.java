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

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.languagetool.Language;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSParser;

/**
 * Makes XML definition of rules accessible as strings.
 *
 * @since 1.8, public since 2.3
 */
public class PatternRuleXmlCreator {

  /**
   * Return the given pattern rule as an indented XML string.
   * @since 2.3
   */
  public final String toXML(PatternRuleId ruleId, Language language) {
    List<String> filenames = language.getRuleFileNames();
    XPath xpath = XPathFactory.newInstance().newXPath();
    for (String filename : filenames) {
      try (InputStream is = this.getClass().getResourceAsStream(filename)) {
        Document doc = getDocument(is);
        Node ruleNode = (Node) xpath.evaluate("/rules/category/rule[@id='" + ruleId.getId() + "']", doc, XPathConstants.NODE);
        if (ruleNode != null) {
          return nodeToString(ruleNode);
        }
        Node ruleNodeInGroup = (Node) xpath.evaluate("/rules/category/rulegroup/rule[@id='" + ruleId.getId() + "']", doc, XPathConstants.NODE);
        if (ruleNodeInGroup != null) {
          return nodeToString(ruleNodeInGroup);
        }
        if (ruleId.getSubId() != null) {
          NodeList ruleGroupNodes = (NodeList) xpath.evaluate("/rules/category/rulegroup[@id='" + ruleId.getId() + "']/rule", doc, XPathConstants.NODESET);
          if (ruleGroupNodes != null) {
            for (int i = 0; i < ruleGroupNodes.getLength(); i++) {
              if (Integer.toString(i+1).equals(ruleId.getSubId())) {
                return nodeToString(ruleGroupNodes.item(i));
              }
            }
          }
        } else {
          Node ruleGroupNode = (Node) xpath.evaluate("/rules/category/rulegroup[@id='" + ruleId.getId() + "']", doc, XPathConstants.NODE);
          if (ruleGroupNode != null) {
            return nodeToString(ruleGroupNode);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Could not turn rule '" + ruleId + "' for language " + language + " into a string", e);
      }
    }
    throw new RuntimeException("Could not find rule '" + ruleId + "' for language " + language + " in files: " + filenames);
  }

  private Document getDocument(InputStream is) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
    DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance();
    DOMImplementationLS impl = (DOMImplementationLS)registry.getDOMImplementation("LS");
    LSParser parser = impl.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
    // we need to ignore whitespace here so the nodeToString() method will be able to indent it properly:
    parser.setFilter(new IgnoreWhitespaceFilter());
    LSInput domInput = impl.createLSInput();
    domInput.setByteStream(is);
    return parser.parse(domInput);
  }

  private String nodeToString(Node node) {
    StringWriter sw = new StringWriter();
    try {
      Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      t.transform(new DOMSource(node), new StreamResult(sw));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
    // We have to use our own simple indentation. as the Java transformer indentation
    // introduces whitespace e.g. in the <suggestion> elements, breaking rules:
    String xml = sw.toString()
      .replace("<token", "\n    <token")
      .replace("<and", "\n    <and")
      .replace("</and>", "\n    </and>")
      .replace("<phraseref", "\n    <phraseref")
      .replace("<antipattern", "\n  <antipattern")
      .replace("<pattern", "\n  <pattern")
      .replace("</pattern", "\n  </pattern")
      .replace("</antipattern", "\n  </antipattern")
      .replace("</rule>", "\n</rule>")
      .replace("<filter", "\n  <filter")
      .replace("<message", "\n  <message")
      .replace("<short", "\n  <short")
      .replace("<url", "\n  <url")
      .replace("<example", "\n  <example")
      .replace("</suggestion><suggestion>", "</suggestion>\n  <suggestion>")
      .replace("</message><suggestion>", "</message>\n  <suggestion>");
    return xml;
  }

}
