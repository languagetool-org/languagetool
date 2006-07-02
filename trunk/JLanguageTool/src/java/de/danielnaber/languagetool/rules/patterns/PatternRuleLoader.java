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
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
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
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(JLanguageTool.getAbsoluteFile(filename), handler);
    rules = handler.getRules();
    return rules;
  }
  
  /** Testing only. */
  public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
    PatternRuleLoader prg = new PatternRuleLoader();
    List l = prg.getRules("rules/de/grammar.xml");
    System.out.println(l);
  }
  
}

class PatternRuleHandler extends XMLRuleHandler {

  private String id;
  private boolean caseSensitive = false;
  private boolean regExpression = false;
  private boolean tokenNegated = false;
  private Language language;
  private String description;
  private String ruleGroupId;
  private String ruleGroupDescription;
  private String[] exceptions;
  private List elementList = null;
  private boolean regular=false; 
  private int startPositionCorrection = 0;
  private int endPositionCorrection = 0;
  
  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================

  public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
    if (namespaceURI == null) namespaceURI = null;      // avoid compiler warning
    if (lName == null) lName = null;      // avoid compiler warning
    if (qName.equals("rules")) {
      String languageStr = attrs.getValue("lang");
      language = Language.getLanguageforShortName(languageStr);
      if (language == null) {
        throw new SAXException("Unknown language '" + languageStr + "'");
      }
    } else if (qName.equals("rule")) {
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
      if (attrs.getValue("mark_from") != null)
        startPositionCorrection = Integer.parseInt(attrs.getValue("mark_from"));
      if (attrs.getValue("mark_to") != null)
        endPositionCorrection = Integer.parseInt(attrs.getValue("mark_to"));
      if (attrs.getValue("case_sensitive") != null && attrs.getValue("case_sensitive").equals("yes"))
        caseSensitive = true;
    }  else if (qName.equals("token")) {
    	inToken = true;
    	if (attrs.getValue("negate")!=null){
	 		tokenNegated=attrs.getValue("negate").equals("yes");
    	}
    	elements = new StringBuffer();
    	if (elementList == null) //lazy init
        {
      	  elementList = new ArrayList();
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
          	  elementList = new ArrayList();
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
    	
    }    else if (qName.equals("example") && attrs.getValue("type").equals("correct")) {
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

  public void endElement(String namespaceURI, String sName, String qName) {
    if (namespaceURI == null) namespaceURI = null;      // avoid compiler warning
    if (sName == null) sName = null;      // avoid compiler warning
    if (qName.equals("rule")) {
      PatternRule rule = new PatternRule(id, language, pattern.toString(), description,
          message.toString());
      //TODO: add lexemes, the class StringElement should be changed
      //or StringElement and POSElement should be in the same class,
      //after all -- we could test both
      if (elementList!=null) {
    	  rule.addPatternElements(elementList);
      }
      rule.setStartPositionCorrection(startPositionCorrection);
      rule.setEndPositionCorrection(endPositionCorrection);
      startPositionCorrection = 0;
      endPositionCorrection = 0;
      rule.setCorrectExamples(correctExamples);
      rule.setIncorrectExamples(incorrectExamples);
      rule.setCaseSensitive(caseSensitive);
      caseSensitive = false;
      rules.add(rule);
      if (elementList!=null)
      {
    	  elementList.clear();
      }
    } else if (qName.equals("token")) {
    	//TODO: enable testing for pos AND token string
    	//left for compatibility with earlier notation
      	if (inToken)
      	{
    	StringElement stringElement = new StringElement(elements.toString(), caseSensitive, regExpression);
    	stringElement.setNegation(tokenNegated);
    	elementList.add(stringElement);
    	tokenNegated=false;
      	}
        inToken = false;
        regExpression = false;
    }  else if (qName.equals("pattern")) {
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
    //if (inPattern) {
      //pattern.append(s);
    //} else 
    	if (inToken) {
    	elements.append(s);
    } else if (inCorrectExample) {
      correctExample.append(s);
    } else if (inIncorrectExample) {
      incorrectExample.append(s);
    } else if (inMessage) {
      message.append(s);
    }
    
  }

}
