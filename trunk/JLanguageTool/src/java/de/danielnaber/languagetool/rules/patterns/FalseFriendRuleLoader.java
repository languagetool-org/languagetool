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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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

  private List<PatternRule> rules;

  public FalseFriendRuleLoader() {
  }

  public List<PatternRule> getRules(String filename, Language textLanguage, Language motherTongue) throws ParserConfigurationException, SAXException, IOException {
    FalseFriendRuleHandler handler = new FalseFriendRuleHandler(textLanguage, motherTongue);
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(JLanguageTool.getAbsoluteFile(filename), handler);
    rules = handler.getRules();
    // Add suggestions to each rule:
    ResourceBundle messages = ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle",
        motherTongue.getLocale());
    for (PatternRule rule : rules) {
      List<String> sugg = handler.getSuggestionMap().get(rule.getId());
      if (sugg != null) {
        MessageFormat msgFormat = new MessageFormat(messages.getString("false_friend_suggestion"));
        Object [] msg = new Object[] {formatSuggestions(sugg)};
        rule.setMessage(rule.getMessage() + " " + msgFormat.format(msg));
      }
    }
    return rules;
  }
  
  private String formatSuggestions(List<String> l) {
    StringBuilder sb = new StringBuilder();
    for (Iterator iter = l.iterator(); iter.hasNext();) {
      String s = (String) iter.next();
      sb.append("<em>");
      sb.append(s);
      sb.append("</em>");
      if (iter.hasNext())
        sb.append(", ");
    }
    return sb.toString();
  }
  
  /** Testing only. */
  public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException {
    FalseFriendRuleLoader prg = new FalseFriendRuleLoader();
    List<PatternRule> l = prg.getRules("rules/false-friends.xml", Language.ENGLISH, Language.GERMAN);
    System.out.println("Hints for German native speakers:");
    for (PatternRule rule : l) {
      System.out.println(rule);
    }
    System.out.println("=======================================");
    System.out.println("Hints for English native speakers:");
    l = prg.getRules("rules/false-friends.xml", Language.GERMAN, Language.ENGLISH);
    for (PatternRule rule : l) {
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
  private boolean posNegation = false;
  
  private String posToken;
  
  private String exceptionPosToken;
  private boolean exceptionStringRegExp = false;
  private boolean exceptionStringNegation = false;
  private boolean exceptionStringInflected = false;
  private boolean exceptionPosNegation = false;
  private boolean exceptionPosRegExp = false;
  private boolean exceptionSet = false;
  
  private List<Element> elementList = null;
  private boolean regular = false; 
  private int skipPos = 0;
  
  private String id;
  private Language language;
  private Language translationLanguage;
  private Language currentTranslationLanguage;
  private String ruleGroupId;
  private List<StringBuilder> translations = new ArrayList<StringBuilder>();
  private StringBuilder translation = new StringBuilder();
  private List<String> suggestions = new ArrayList<String>();
  // rule ID -> list of translations:
  private Map<String,List<String>> suggestionMap = new HashMap<String,List<String>>();
  
  private boolean inTranslation = false;
  
  public FalseFriendRuleHandler(Language textLanguage, Language motherTongue) {
    messages = ResourceBundle.getBundle("de.danielnaber.languagetool.MessagesBundle",
        motherTongue.getLocale());
    formatter = new MessageFormat("");
    formatter.setLocale(motherTongue.getLocale());
    this.textLanguage = textLanguage;
    this.motherTongue = motherTongue;
  }
  
  Map<String,List<String>> getSuggestionMap() {
    return suggestionMap;
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
	  } else if (qName.equals("exception")) {
	    	inException = true;
	    	exceptionSet = true;
	    	exceptions = new StringBuffer();
	    	
	    	if (attrs.getValue("negate")!=null) {
		 		exceptionStringNegation=attrs.getValue("negate").equals("yes");
	    	}
	    	if (attrs.getValue("inflected")!=null) {
		 		exceptionStringInflected=attrs.getValue("inflected").equals("yes");
	    	}
	    	if (attrs.getValue("postag")!=null) {    		
	    		exceptionPosToken = attrs.getValue("postag");
	    		if (attrs.getValue("postag_regexp")!=null) {
	    			exceptionPosRegExp = attrs.getValue("postag_regexp").equals("yes");
	    		}
	    		if (attrs.getValue("negate_pos")!=null) {
	    	 		exceptionPosNegation = attrs.getValue("negate_pos").equals("yes");
	        	}    	 	
	    	}
	    	if (attrs.getValue("regexp")!=null) {
	    		exceptionStringRegExp = attrs.getValue("regexp").equals("yes");
	    	}
	    	
	    } else if (qName.equals("token")) {
		  inToken = true;
		  if (attrs.getValue("negate") != null){
			  tokenNegated = attrs.getValue("negate").equals("yes");
		  }
		  if (attrs.getValue("inflected") != null){
			  tokenInflected = attrs.getValue("inflected").equals("yes");
		  }
		  if (attrs.getValue("skip") != null){
			  skipPos = Integer.parseInt(attrs.getValue("skip"));
		  }
		  elements = new StringBuffer();
		  if (elementList == null) //lazy init
		  {
			  elementList = new ArrayList<Element>();
		  }
		  if (attrs.getValue("postag") != null)
		  {
			  posToken = attrs.getValue("postag");
			  if (attrs.getValue("postag_regexp") != null){
				  regular = attrs.getValue("postag_regexp").equals("yes");
			  }
			  if (attrs.getValue("negate_pos") != null){
				  posNegation = (attrs.getValue("negate_pos").equals("yes"));
			  }
			  
			  if (elementList == null) { //lazy init
				  elementList = new ArrayList<Element>();
			  }
		  }
		  if (attrs.getValue("regexp") != null){
			  regExpression = attrs.getValue("regexp").equals("yes");
		  }
		  
	  } else if (qName.equals("translation")) {
		  inTranslation = true;
		  String languageStr = attrs.getValue("lang");
      Language tmpLang = Language.getLanguageforShortName(languageStr);
      currentTranslationLanguage = tmpLang;
      if (tmpLang == motherTongue) {
        translationLanguage = tmpLang;
        if (translationLanguage == null) {
          throw new SAXException("Unknown language '" + languageStr + "'");
        }
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
		  if (language == textLanguage && translationLanguage != null && translationLanguage == motherTongue) {
			  formatter.applyPattern(messages.getString("false_friend_hint"));
			  Object[] messageArguments = {
					  elements.toString().replace('|', '/'),
            messages.getString(textLanguage.getShortName()),
					  formatTranslations(translations),
            messages.getString(motherTongue.getShortName())
			  };
			  String description = formatter.format(messageArguments);
			  PatternRule rule = new PatternRule(id, language, elementList, 
					  messages.getString("false_friend_desc") + " " + pattern.toString(),
					  description);
			  rule.setCorrectExamples(correctExamples);
			  rule.setIncorrectExamples(incorrectExamples);
			  rules.add(rule);
		  }
		  
      if (suggestions.size() > 0) {
        List<String> l = new ArrayList<String>(suggestions);
        suggestionMap.put(id, l);
        suggestions.clear();
      }
		  if (elementList != null) {
			  elementList.clear();
		  }
		  
	  } else if (qName.equals("exception")) {    	
	    	inException = false;    	
	  } else if (qName.equals("token")) {
		  if (inToken)
		  {
			  Element stringElement = new Element(elements.toString(), caseSensitive, regExpression, tokenInflected);
			  stringElement.setNegation(tokenNegated);
			  if (skipPos!=0) {
				  stringElement.setSkipNext(skipPos);
				  skipPos = 0;
			  }
			  if (posToken!=null) {
			 		stringElement.setPosElement(posToken, regular, posNegation);
			 		posToken = null;
			 	}
			  
			  if (exceptionSet) {
		    		stringElement.setStringException(exceptions.toString(), caseSensitive, exceptionStringRegExp, exceptionStringInflected, exceptionStringNegation);
		    		exceptionSet = false;
		    	}
		    	if (exceptionPosToken!=null) {
		    		stringElement.setPosException(exceptionPosToken, exceptionPosRegExp, exceptionPosNegation);
		    		exceptionPosToken = null;
		    	}
		    
			  elementList.add(stringElement);
			  tokenNegated = false;
			  tokenInflected = false;
			  posNegation = false;
		      regular = false;
		  }
		  inToken = false;
		  regExpression = false;
		  
		  exceptionStringNegation = false;
		  exceptionStringInflected = false;
		  exceptionPosNegation = false;
		  exceptionPosRegExp = false;
		  exceptionStringRegExp = false;
		  
	  } else if (qName.equals("pattern")) {
		  inPattern = false;
	  } else if (qName.equals("translation")) {
      if (currentTranslationLanguage == motherTongue) {
        translations.add(translation);
      }
      if (currentTranslationLanguage == textLanguage) {
        suggestions.add(translation.toString());
      }
      translation = new StringBuilder();
		  inTranslation = false;
      currentTranslationLanguage = null;
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
    if (inException) {
        exceptions.append(s);
    } else if (inToken && inPattern) {
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
