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
  
  private boolean caseSensitive = false;
  private boolean regExpression = false;
  private boolean tokenNegated = false;
  private boolean tokenInflected = false;
  
  private List<Element> elementList = null;
  private boolean regular=false; 
  private String[] exceptions;
  private int skipPos = 0;
  
  private String id;
  private Language language;
  private Language translationLanguage;
  private String ruleGroupId;
  private List<StringBuilder> translations = new ArrayList<StringBuilder>();
  private StringBuilder translation = new StringBuilder();
  
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
		  translations = new ArrayList<StringBuilder>();
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
	  } else if (qName.equals("token")) {
		  inToken = true;
		  if (attrs.getValue("negate")!=null){
			  tokenNegated=attrs.getValue("negate").equals("yes");
		  }
		  if (attrs.getValue("inflected")!=null){
			  tokenInflected=attrs.getValue("inflected").equals("yes");
		  }
		  if (attrs.getValue("skip")!=null){
			  skipPos=Integer.parseInt(attrs.getValue("skip"));
		  }
		  elements = new StringBuffer();
		  if (elementList == null) //lazy init
		  {
			  elementList = new ArrayList<Element>();
		  }
		  // POSElement creation
		  if (attrs.getValue("postag")!=null)
		  {
			  //String exceptions[] = null;
			  if (attrs.getValue("postag_exceptions")!=null) {
				  exceptions=attrs.getValue("postag_exceptions").split("\\|");
			  }
			  else {
				  exceptions=null;
			  }
			  String[] pos = new String [1];
			  pos[0] = attrs.getValue("postag");
			  if (attrs.getValue("postag_regexp")!=null){
				  regular = attrs.getValue("postag_regexp").equals("yes");
			  }
			  POSElement posElement = new POSElement(pos, caseSensitive, regular, exceptions);
			  if (attrs.getValue("negate_pos")!=null){
				  posElement.setNegation(attrs.getValue("negate_pos").equals("yes"));
			  }
			  
			  if (elementList == null) //lazy init
			  {
				  elementList = new ArrayList<Element>();
			  }
			  elementList.add(posElement);
			  //TODO: add StringElement and POSElement to a single container element
			  //a list of Elements?
			  //elements would then be not elements but lists of elements...
			  inToken=false;
		  }
		  if (attrs.getValue("regexp")!=null){
			  regExpression = attrs.getValue("regexp").equals("yes");
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
					  elements,
					  textLanguage.getShortName(),
					  formatTranslations(translations),
					  motherTongue.getShortName()
			  };
			  String description = formatter.format(messageArguments);
			  //String rulePattern = makeRulePattern(pattern.toString());
			  PatternRule rule = new PatternRule(id, language, pattern.toString(), 
					  messages.getString("false_friend_desc") + " " + pattern.toString(),
					  description);
			  if (elementList!=null) {
				  rule.addPatternElements(elementList);
			  }
			  
			  rule.setCorrectExamples(correctExamples);
			  rule.setIncorrectExamples(incorrectExamples);
			  rules.add(rule);
		  }
		  
		  if (elementList!=null)
		  {
			  elementList.clear();
		  }
		  
	  } else if (qName.equals("token")) {
		  //TODO: enable testing for pos AND token string
		  //left for compatibility with earlier notation
		  if (inToken)
		  {
			  StringElement stringElement = new StringElement(elements.toString(), caseSensitive, regExpression, tokenInflected);
			  stringElement.setNegation(tokenNegated);
			  if (skipPos!=0) {
				  stringElement.setSkipNext(skipPos);
				  skipPos = 0;
			  }
			  elementList.add(stringElement);
			  tokenNegated=false;
			  tokenInflected=false;
		  }
		  inToken = false;
		  regExpression = false;
	  } else if (qName.equals("pattern")) {
		  inPattern = false;
	  } else if (qName.equals("translation")) {
		  translations.add(translation);
		  translation = new StringBuilder();
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

  private String formatTranslations(List<StringBuilder> translations) {
    StringBuilder sb = new StringBuilder();
    for (Iterator<StringBuilder> iter = translations.iterator(); iter.hasNext();) {
      StringBuilder trans = iter.next();
      sb.append("\"");
      sb.append(trans.toString());
      sb.append("\"");
      if (iter.hasNext())
        sb.append(", ");
    }
    return sb.toString();
  }

  public void characters(char buf[], int offset, int len) {
    String s = new String(buf, offset, len);
    if (inToken && inPattern) {
    	elements.append(s);
    } else if (inCorrectExample) {
      correctExample.append(s);
    } else if (inIncorrectExample) {
      incorrectExample.append(s);
    } else if (inTranslation) {
      translation.append(s);
    }
  }

}
