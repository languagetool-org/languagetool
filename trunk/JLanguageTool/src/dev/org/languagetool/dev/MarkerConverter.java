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
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Convert to the new marker format. Note: is buggy at least with "and" elements.
 *
 * @deprecated for internal one-time conversion only
 */
public class MarkerConverter {

  private static final Language LANGUAGE = Language.CATALAN;

  public static void main(String[] args) throws SAXException, ParserConfigurationException, IOException {
    final ConverterHandler handler = new ConverterHandler();
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser saxParser = factory.newSAXParser();
    saxParser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
    saxParser.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", new MyLexicalHandler());
    saxParser.parse(new FileInputStream("/home/dnaber/prg/languagetool-svn/trunk/JLanguageTool/src/rules/" + LANGUAGE.getShortName() + "/grammar.xml"), handler);
  }

  static class ConverterHandler extends DefaultHandler {

    private final Map<String, Integer> startPos = new HashMap<String, Integer>();
    private final Map<String, Integer> endPos = new HashMap<String, Integer>();

    ConverterHandler() throws IOException {
      final JLanguageTool languageTool = new JLanguageTool(LANGUAGE);
      languageTool.activateDefaultPatternRules();
      final List<Rule> rules = languageTool.getAllRules();
      for (Rule rule : rules) {
        if (rule instanceof PatternRule) {
          final PatternRule pRule = (PatternRule) rule;
          //System.out.println("***" + pRule.getId() + " " + pRule.getSubId() + " --> " + pRule.getStartPositionCorrection()
          //        + ", " + (pRule.getElements().size() + pRule.getEndPositionCorrection()) + ", size: " + pRule.getElements());
          final String key = pRule.getId() + " " + pRule.getSubId();
          startPos.put(key, pRule.getStartPositionCorrection());
          endPos.put(key, pRule.getElements().size() + pRule.getEndPositionCorrection());
        }
      }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {}
    @Override
    public void notationDecl(String name, String publicId, String systemId) throws SAXException {}
    @Override
    public void unparsedEntityDecl(String name, String publicId, String systemId, String notationName) throws SAXException {}
    @Override
    public void skippedEntity(String name) throws SAXException {}

    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws IOException, SAXException {
      return super.resolveEntity(publicId, systemId);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
      System.out.println("<?" + target + " " + data + "?>");
    }

    private int currentTokenPos = 0;
    private String currentId = "";
    private int currentSubId = 0;
    private boolean inCategory = false;
    private boolean inRuleGroup = false;
    private boolean needsMarker = false;

    @Override
    public void startElement(final String namespaceURI, final String lName,
        final String qName, final Attributes attrs) throws SAXException {

      if (qName.equals("token") && inCategory) {
        currentTokenPos++;
        final String key = currentId + " " + currentSubId;
        if (needsMarker && startPos.get(key) == currentTokenPos - 1) {
          System.out.print("<marker>\n");
        }
      }

      if (attrs.getLength() > 0) {
        System.out.print("<" + qName);
        for (int i = 0; i < attrs.getLength(); i++) {
          final String qName1 = attrs.getQName(i);
          if (qName1.equals("mark_from") || qName1.equals("mark_to")) {
            continue;
          }
          System.out.print(" " + qName1 + "=\"" + attrs.getValue(qName1) + "\"");
        }
        System.out.print(">");
      } else {
        System.out.print("<" + qName + ">");
      }
      if (qName.equals("category")) {
        inCategory = true;
      } else if (qName.equals("pattern")) {
        final boolean defaultStart = attrs.getValue("mark_from") == null || attrs.getValue("mark_from").equals("0");
        final boolean defaultEnd = attrs.getValue("mark_to") == null || attrs.getValue("mark_to").equals("0");
        needsMarker = !defaultStart || !defaultEnd;
      } else if (qName.equals("rulegroup")) {
        currentTokenPos = 0;
        currentId = attrs.getValue("id");
        currentSubId = 0;
        inRuleGroup = true;
      } else if (qName.equals("rule")) {
        currentTokenPos = 0;
        if (attrs.getValue("id") != null) {
          currentId = attrs.getValue("id");
        }
        if (inRuleGroup) {
          currentSubId++;
        } else {
          currentSubId = 1;
        }
      }

    }

    @Override
    public void endElement(final String namespaceURI, final String sName,
        final String qName) throws SAXException {
      System.out.print("</" + qName + ">");
      if (qName.equals("rulegroup")) {
        inRuleGroup = false;
      } else if (qName.equals("category")) {
        inCategory = false;
      } else if (qName.equals("token") && inCategory) {
        final String key = currentId + " " + currentSubId;
        if (needsMarker && endPos.get(key) == currentTokenPos) {
          System.out.print("\n</marker>");
        }
      }
    }

    @Override
    public void characters(final char[] buf, final int offset, final int len) {
      final String s = new String(buf, offset, len);
      System.out.print(StringTools.escapeXML(s));
    }

  }

  static class MyLexicalHandler implements LexicalHandler {

    @Override
    public void startDTD(String name, String publicId, String systemId) throws SAXException {}
    @Override
    public void endDTD() throws SAXException {}
    @Override
    public void startEntity(String name) throws SAXException {}
    @Override
    public void endEntity(String name) throws SAXException {}
    @Override
    public void startCDATA() throws SAXException {}
    @Override
    public void endCDATA() throws SAXException {}

    @Override
    public void comment(char[] buf, int offset, int len) throws SAXException {
      final String s = new String(buf, offset, len);
      System.out.print("<!--" + s + "-->");
    }
  }

}
