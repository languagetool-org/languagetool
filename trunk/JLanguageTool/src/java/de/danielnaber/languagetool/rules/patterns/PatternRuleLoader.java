/* JLanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.rules.patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

/**
 * Loads {@link PatternRule}s from an XML file.
 * 
 * @author Daniel Naber
 */
public class PatternRuleLoader extends DefaultHandler {

  private List rules;

  public PatternRuleLoader() {
  }

  public List getRules(String filename) throws ParserConfigurationException, SAXException, IOException {
    PatternRuleHandler handler = new PatternRuleHandler();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    factory.setValidating(true);
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(JLanguageTool.getAbsoluteFile(filename), handler);
    rules = handler.getRules();
    return rules;
  }
  
}

class PatternRuleHandler extends DefaultHandler {

  private List rules = new ArrayList();
  private String id;
  private boolean caseSensitive = false;
  private String languageStr;
  private StringBuffer pattern = null;
  private String description;
  private String ruleGroupId;
  private String ruleGroupDescription;
  private StringBuffer correctExample = new StringBuffer();
  private StringBuffer incorrectExample = new StringBuffer();
  private List correctExamples = new ArrayList();
  private List incorrectExamples = new ArrayList();
  private StringBuffer message = new StringBuffer();
  private int startPositionCorrection = 0;
  private int endPositionCorrection = 0;
  
  private boolean inRuleGroup = false;
  private boolean inPattern = false;
  private boolean inMessage = false;
  private boolean inCorrectExample = false;
  private boolean inIncorrectExample = false;
  
  List getRules() {
    return rules;
  }
  
  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================

  public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) {
    if (namespaceURI == null) namespaceURI = null;      // avoid compiler warning
    if (lName == null) lName = null;      // avoid compiler warning
    if (qName.equals("rule")) {
      id = attrs.getValue("id");
      if (inRuleGroup && id == null)
        id = ruleGroupId;
      description = attrs.getValue("name");
      if (inRuleGroup && description == null)
        description = ruleGroupDescription;
      correctExamples = new ArrayList();
      incorrectExamples = new ArrayList();
    } else if (qName.equals("pattern")) {
      pattern = new StringBuffer();
      inPattern = true;
      languageStr = attrs.getValue("lang");
      if (attrs.getValue("mark_from") != null)
        startPositionCorrection = Integer.parseInt(attrs.getValue("mark_from"));
      if (attrs.getValue("mark_to") != null)
        endPositionCorrection = Integer.parseInt(attrs.getValue("mark_to"));
      if (attrs.getValue("case_sensitive") != null && attrs.getValue("case_sensitive").equals("yes"))
        caseSensitive = true;
    } else if (qName.equals("example") && attrs.getValue("type").equals("correct")) {
      inCorrectExample = true;
      correctExample = new StringBuffer();
    } else if (qName.equals("example") && attrs.getValue("type").equals("incorrect")) {
      inIncorrectExample = true;
      incorrectExample = new StringBuffer();
    } else if (qName.equals("message")) {
      inMessage = true;
      message = new StringBuffer();
    } else if (qName.equals("rulegroup")) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupDescription = attrs.getValue("name");
      inRuleGroup = true;
    } else if (qName.equals("em") && inMessage) {
      message.append("<em>");
    }
  }

  public void endElement(String namespaceURI, String sName, String qName) throws SAXException {
    if (namespaceURI == null) namespaceURI = null;      // avoid compiler warning
    if (sName == null) sName = null;      // avoid compiler warning
    Language language = null;
    if (qName.equals("rule")) {
      language = Language.getLanguageforShortName(languageStr);
      if (language == null) {
        throw new SAXException("Unknown language '" + languageStr + "'");
      }
      PatternRule rule = new PatternRule(id, language, pattern.toString(), description,
          message.toString());
      rule.setStartPositionCorrection(startPositionCorrection);
      rule.setEndPositionCorrection(endPositionCorrection);
      startPositionCorrection = 0;
      endPositionCorrection = 0;
      rule.setCorrectExamples(correctExamples);
      rule.setIncorrectExamples(incorrectExamples);
      rule.setCaseSensitive(caseSensitive);
      caseSensitive = false;
      rules.add(rule);
    } else if (qName.equals("pattern")) {
      inPattern = false;
    } else if (qName.equals("example")) {
      if (inCorrectExample) {
        correctExamples.add(correctExample.toString());
      } else if (inIncorrectExample) {
        incorrectExamples.add(incorrectExample.toString());
      }
      inCorrectExample = false;
      inIncorrectExample = false;
      correctExample = new StringBuffer();
      incorrectExample = new StringBuffer();
    } else if (qName.equals("message")) {
      inMessage = false;
    } else if (qName.equals("rulegroup")) {
      inRuleGroup = false;
    } else if (qName.equals("em") && inMessage) {
      message.append("</em>");
    }
  }

  public void characters(char buf[], int offset, int len) {
    String s = new String(buf, offset, len);
    if (inPattern) {
      pattern.append(s);
    } else if (inCorrectExample) {
      correctExample.append(s);
    } else if (inIncorrectExample) {
      incorrectExample.append(s);
    } else if (inMessage) {
      message.append(s);
    }
  }

  public void warning (SAXParseException e) throws SAXException {
    throw e;
  }
  
  public void error (SAXParseException e) throws SAXException {
    throw e;
  }

}
