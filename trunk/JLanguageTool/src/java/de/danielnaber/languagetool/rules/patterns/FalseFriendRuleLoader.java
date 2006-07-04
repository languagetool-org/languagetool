/* LanguageTool, a natural language style checker 
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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;

/**
 * Loads {@link PatternRule}s from a false friends XML file.
 * 
 * @author Daniel Naber
 */
public class FalseFriendRuleLoader extends DefaultHandler {

  private List rules;

  public FalseFriendRuleLoader() {
  }

  public List getRules(String filename, Language textLanguage, Language motherTongue) throws ParserConfigurationException, SAXException, IOException {
    FalseFriendRuleHandler handler = new FalseFriendRuleHandler(textLanguage, motherTongue);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(JLanguageTool.getAbsoluteFile(filename), handler);
    rules = handler.getRules();
    return rules;
  }
  
  /** Testing only. */
  public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
    FalseFriendRuleLoader prg = new FalseFriendRuleLoader();
    List l = prg.getRules("rules/false-friends.xml", Language.ENGLISH, Language.GERMAN);
    System.out.println("Hints for German native speakers:");
    for (Iterator iter = l.iterator(); iter.hasNext();) {
      PatternRule rule = (PatternRule) iter.next();
      System.out.println(rule);
    }
    System.out.println("=======================================");
    System.out.println("Hints for English native speakers:");
    l = prg.getRules("rules/false-friends.xml", Language.GERMAN, Language.ENGLISH);
    for (Iterator iter = l.iterator(); iter.hasNext();) {
      PatternRule rule = (PatternRule) iter.next();
      System.out.println(rule);
    }
  }
  
}

class FalseFriendRuleHandler extends XMLRuleHandler {

  private ResourceBundle messages;
  private MessageFormat formatter; 
  
  private Language textLanguage; 
  private Language motherTongue;
  
  private String id;
  private Language language;
  private Language translationLanguage;
  private String ruleGroupId;
  private List<StringBuffer> translations = new ArrayList<StringBuffer>();
  private StringBuffer translation = new StringBuffer();
  
  private boolean inTranslation = false;
  
  public FalseFriendRuleHandler(Language textLanguage, Language motherTongue) {
    messages = ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle",
        motherTongue.getLocale());
    formatter = new MessageFormat("");
    formatter.setLocale(motherTongue.getLocale());
    this.textLanguage = textLanguage;
    this.motherTongue = motherTongue;
  }
  
  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================

  @SuppressWarnings("unused")
  public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
    if (qName.equals("rule")) {
      translations = new ArrayList<StringBuffer>();
      id = attrs.getValue("id");
      if (inRuleGroup && id == null)
        id = ruleGroupId;
      correctExamples = new ArrayList<String>();
      incorrectExamples = new ArrayList<String>();
    } else if (qName.equals("pattern")) {
      pattern = new StringBuffer();
      inPattern = true;
      String languageStr = attrs.getValue("lang");
      language = Language.getLanguageforShortName(languageStr);
      if (language == null) {
        throw new SAXException("Unknown language '" + languageStr + "'");
      }
    } else if (qName.equals("translation")) {
      inTranslation = true;
      String languageStr = attrs.getValue("lang");
      translationLanguage = Language.getLanguageforShortName(languageStr);
      if (translationLanguage == null) {
        throw new SAXException("Unknown language '" + languageStr + "'");
      }
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
      inRuleGroup = true;
    }
  }

  @SuppressWarnings("unused")
  public void endElement(String namespaceURI, String sName, String qName) {
    if (qName.equals("rule")) {
      if (language == textLanguage && translationLanguage == motherTongue) {
        formatter.applyPattern(messages.getString("false_friend_hint"));
        Object[] messageArguments = {
          pattern,
          textLanguage.getShortName(),
          formatTranslations(translations),
          motherTongue.getShortName()
        };
        String description = formatter.format(messageArguments);
        String rulePattern = makeRulePattern(pattern.toString());
        PatternRule rule = new PatternRule(id, language, rulePattern, 
            messages.getString("false_friend_desc") + " " + pattern.toString(),
            description);
        rule.setCorrectExamples(correctExamples);
        rule.setIncorrectExamples(incorrectExamples);
        rules.add(rule);
      }
    } else if (qName.equals("pattern")) {
      inPattern = false;
    } else if (qName.equals("translation")) {
      translations.add(translation);
      translation = new StringBuffer();
      inTranslation = false;
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
    }
  }

  private String formatTranslations(List<StringBuffer> translations) {
    StringBuffer sb = new StringBuffer();
    for (Iterator<StringBuffer> iter = translations.iterator(); iter.hasNext();) {
      StringBuffer trans = iter.next();
      sb.append("\"");
      sb.append(trans.toString());
      sb.append("\"");
      if (iter.hasNext())
        sb.append(", ");
    }
    return sb.toString();
  }

  /**
   * Enclose all parts of a pattern with quotes.
   */
  private String makeRulePattern(String pattern) {
    String[] rulePatternParts = pattern.split("\\s+");
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < rulePatternParts.length; i++) {
      sb.append("\"");
      sb.append(rulePatternParts[i]);
      sb.append("\"");
      if (i < rulePatternParts.length-1)
        sb.append(" ");
    }
    return sb.toString();
  }

  public void characters(char buf[], int offset, int len) {
    String s = new String(buf, offset, len);
    if (inPattern) {
      pattern.append(s);
    } else if (inCorrectExample) {
      correctExample.append(s);
    } else if (inIncorrectExample) {
      incorrectExample.append(s);
    } else if (inTranslation) {
      translation.append(s);
    }
  }

}
