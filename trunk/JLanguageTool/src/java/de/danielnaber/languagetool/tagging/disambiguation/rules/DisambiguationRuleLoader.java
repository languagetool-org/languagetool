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
package de.danielnaber.languagetool.tagging.disambiguation.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.patterns.Element;

/**
 * Loads {@link DisambiguationPatternRule}s from a 
 * disambiguation rules XML file.
 * 
 * @author Marcin Miłkowski
 */
public class DisambiguationRuleLoader extends DefaultHandler {

  private List<DisambiguationPatternRule> rules;

  public DisambiguationRuleLoader() {
  }

  public final List<DisambiguationPatternRule> getRules(final InputStream file) throws ParserConfigurationException, SAXException, IOException {
    DisambiguationRuleHandler handler = new DisambiguationRuleHandler();
    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(file, handler);
    rules = handler.getRules();      
    return rules;
  }
  
}

class DisambiguationRuleHandler extends XMLRuleHandler {

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
  private boolean exceptionValidNext = true;
  private boolean exceptionSet = false;
  
  private List<Element> elementList = null;
  private boolean regular = false; 
  private int skipPos = 0;
  private Element stringElement = null;
  
  private String id;
  private String name;
  private Language language;  
  private String ruleGroupId;
  private String ruleGroupName;
  private StringBuilder disamb = new StringBuilder();
  private List<String> suggestions = new ArrayList<String>();
  
  private String disambiguatedPOS;
  
  private int positionCorrection = 0;
  
  public DisambiguationRuleHandler() {    
  }    


  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================

  @SuppressWarnings("unused")
  public void startElement(String namespaceURI, String lName, String qName, Attributes attrs) throws SAXException {
    if (qName.equals("rule")) {      
      id = attrs.getValue("id");
      name = attrs.getValue("name");
      if (inRuleGroup && id == null)
        id = ruleGroupId;
      if (inRuleGroup && name == null) {
        name = ruleGroupName;
      }
    } else if (qName.equals("rules")) {
      language = Language.getLanguageForShortName(attrs.getValue("lang"));
    } else if (qName.equals("pattern")) {
      inPattern = true;
      if (attrs.getValue("mark") != null)
        positionCorrection = Integer.parseInt(attrs.getValue("mark"));
    } else if (qName.equals("exception")) {
        inException = true;
        exceptionSet = true;
        exceptions = new StringBuffer();
        
        if (attrs.getValue("negate") != null) {
        exceptionStringNegation=attrs.getValue("negate").equals("yes");
        }
            if (attrs.getValue("scope") != null) {
              exceptionValidNext = attrs.getValue("scope").equals("next");
            }
        if (attrs.getValue("inflected") != null) {
        exceptionStringInflected=attrs.getValue("inflected").equals("yes");
        }
        if (attrs.getValue("postag") != null) {       
          exceptionPosToken = attrs.getValue("postag");
          if (attrs.getValue("postag_regexp") != null) {
            exceptionPosRegExp = attrs.getValue("postag_regexp").equals("yes");
          }
          if (attrs.getValue("negate_pos") != null) {
            exceptionPosNegation = attrs.getValue("negate_pos").equals("yes");
            }       
        }
        if (attrs.getValue("regexp") != null) {
          exceptionStringRegExp = attrs.getValue("regexp").equals("yes");
        }
        
      } else if (qName.equals("token")) {
      inToken = true;
      if (attrs.getValue("negate") != null) {
        tokenNegated = attrs.getValue("negate").equals("yes");
      }
      if (attrs.getValue("inflected") != null) {
        tokenInflected = attrs.getValue("inflected").equals("yes");
      }
      if (attrs.getValue("skip") != null) {
        skipPos = Integer.parseInt(attrs.getValue("skip"));
      }
      elements = new StringBuffer();
      if (elementList == null) {
        elementList = new ArrayList<Element>();
      }
      if (attrs.getValue("postag") != null) {
        posToken = attrs.getValue("postag");
        if (attrs.getValue("postag_regexp") != null) {
          regular = attrs.getValue("postag_regexp").equals("yes");
        }
        if (attrs.getValue("negate_pos") != null) {
          posNegation = (attrs.getValue("negate_pos").equals("yes"));
        }
        
        if (elementList == null) { //lazy init
          elementList = new ArrayList<Element>();
        }
      }
      if (attrs.getValue("regexp") != null) {
        regExpression = attrs.getValue("regexp").equals("yes");
      }
      
    }  else if (qName.equals("disambig")) {
      inDisamb = true;
      disambiguatedPOS = attrs.getValue("postag");
      message = new StringBuffer();
    } else if (qName.equals("rulegroup")) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupName = attrs.getValue("name");
      inRuleGroup = true;
    }
  }

  @SuppressWarnings("unused")
  public void endElement(String namespaceURI, String sName, String qName) {
    if (qName.equals("rule")) {        
        DisambiguationPatternRule rule = new DisambiguationPatternRule(id, name, 
            language, elementList, disambiguatedPOS);
        rule.setStartPositionCorrection(positionCorrection);
        rules.add(rule);      
      
      if (suggestions.size() > 0) {
        List<String> l = new ArrayList<String>(suggestions);
        suggestions.clear();
      }
      if (elementList != null) {
        elementList.clear();
      }
      
    } else if (qName.equals("exception")) {     
        inException = false;
             if (!exceptionSet) {
                  stringElement = new Element(elements.toString(), caseSensitive, regExpression,
                      tokenInflected);
                  exceptionSet = true;
                  }
                  stringElement.setNegation(tokenNegated);
                    if (!exceptions.toString().equals("")) {
                    stringElement.setStringException(exceptions.toString(), exceptionStringRegExp, 
                        exceptionStringInflected, exceptionStringNegation, exceptionValidNext);
                    }              
                  if (exceptionPosToken != null) {
                    stringElement.setPosException(exceptionPosToken, exceptionPosRegExp, exceptionPosNegation, exceptionValidNext);
                    exceptionPosToken = null;
                  }
    } else if (qName.equals("token")) {
      if (inToken) {
            if (!exceptionSet || stringElement == null) {
              stringElement = new Element(elements.toString(), caseSensitive, regExpression,
                  tokenInflected);
              stringElement.setNegation(tokenNegated);
              } else {
                stringElement.setStringElement(elements.toString());
              }
        if (skipPos != 0) {
          stringElement.setSkipNext(skipPos);
          skipPos = 0;
        }
        if (posToken != null) {
          stringElement.setPosElement(posToken, regular, posNegation);
          posToken = null;
        }
                        
        elementList.add(stringElement);
        tokenNegated = false;
        tokenInflected = false;
        posNegation = false;
          regular = false;
              exceptionValidNext = true;
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
    } else if (qName.equals("disambig")) {
      inDisamb = false;
    } else if (qName.equals("rulegroup")) {
      inRuleGroup = false;
    }
  }

  public void characters(final char[] buf, final int offset, final int len) {
    String s = new String(buf, offset, len);
    if (inException) {
        exceptions.append(s);
    } else if (inToken && inPattern) {
      elements.append(s);
    } else if (inCorrectExample) {
      correctExample.append(s);
    } else if (inIncorrectExample) {
      incorrectExample.append(s);
    } else if (inDisamb) {
      disamb.append(s);
    }
  }

}
