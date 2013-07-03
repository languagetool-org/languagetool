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

import org.languagetool.Language;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

/**
 * Makes XML definition of rules accessible as strings.
 *
 * @since 1.8, public since 2.3
 */
public class PatternRuleXmlCreator {

  /**
   * Return the given pattern rule as an XML string.
   * @since 2.3
   */
  public final String toXML(PatternRuleId ruleId, Language language) throws IOException {
    final List<String> filenames = language.getRuleFileNames();
    final XPath xpath = XPathFactory.newInstance().newXPath();
    for (String filename : filenames) {
      final InputStream is = this.getClass().getResourceAsStream(filename);
      try {
        final Document doc = getDocument(is);
        final Node ruleNode = (Node) xpath.evaluate("/rules/category/rule[@id='" + ruleId.getId() + "']", doc, XPathConstants.NODE);
        if (ruleNode != null) {
          return nodeToString(ruleNode);
        }
        if (ruleId.getSubId() != null) {
          final NodeList ruleGroupNodes = (NodeList) xpath.evaluate("/rules/category/rulegroup[@id='" + ruleId.getId() + "']/rule", doc, XPathConstants.NODESET);
          if (ruleGroupNodes != null) {
            for (int i = 1; i <= ruleGroupNodes.getLength(); i++) {
              if (Integer.toString(i).equals(ruleId.getSubId())) {
                return nodeToString(ruleGroupNodes.item(i - 1));
              }
            }
          }
        } else {
          final Node ruleGroupNode = (Node) xpath.evaluate("/rules/category/rulegroup[@id='" + ruleId.getId() + "']", doc, XPathConstants.NODE);
          if (ruleGroupNode != null) {
            return nodeToString(ruleGroupNode);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException("Could not turn rule " + ruleId + " for language " + language + " into a string", e);
      } finally {
        is.close();
      }
    }
    throw new RuntimeException("Could not find rule " + ruleId + " for language " + language);
  }

  private Document getDocument(InputStream is) throws ParserConfigurationException, SAXException, IOException {
    final InputSource inputSource = new InputSource(is);
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    factory.setNamespaceAware(false);  // we just ignore namespaces
    final DocumentBuilder builder = factory.newDocumentBuilder();
    return builder.parse(inputSource);
  }

  private String nodeToString(Node node) {
    final StringWriter sw = new StringWriter();
    try {
      final Transformer t = TransformerFactory.newInstance().newTransformer();
      t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      t.setOutputProperty(OutputKeys.INDENT, "yes");
      t.transform(new DOMSource(node), new StreamResult(sw));
    } catch (TransformerException e) {
      throw new RuntimeException(e);
    }
    return cleanIndent(sw.toString());
  }

  // not sure why this is necessary, but the "indent" looks broken otherwise
  private String cleanIndent(String xml) {
    String cleanXml = xml;
    cleanXml = cleanXml.replace("\n</", "</");
    cleanXml = cleanXml.replace("\n<", "<");
    return cleanXml;
  }

}
