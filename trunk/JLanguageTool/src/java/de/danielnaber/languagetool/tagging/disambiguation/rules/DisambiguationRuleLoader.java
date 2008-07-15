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
import de.danielnaber.languagetool.rules.patterns.Match;

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
    final DisambiguationRuleHandler handler = new DisambiguationRuleHandler();
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(file, handler);
    rules = handler.getRules();      
    return rules;
  }
  
}

class DisambiguationRuleHandler extends XMLRuleHandler {

  private boolean caseSensitive = false;
  private boolean stringRegExp = false;
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
  private boolean exceptionValidNext = false;
  private boolean exceptionValidPrev = false;
  private boolean exceptionSet = false;
  
  private List<Element> elementList = null;
  private boolean posRegExp = false; 
  private int skipPos = 0;
  private Element tokenElement = null;
  
  private String id;
  private String name;
  private Language language;  
  private String ruleGroupId;
  private String ruleGroupName;
  private StringBuilder disamb = new StringBuilder();
  
  private String disambiguatedPOS;
  
  private int positionCorrection = 0;
  
  private int andGroupCounter = 0;
  
  private Match tokenReference = null;
  
  private Match posSelector = null;
  
  public DisambiguationRuleHandler() {    
  }    


  //===========================================================
  // SAX DocumentHandler methods
  //===========================================================

  @Override
  @SuppressWarnings("unused")
  public void startElement(final String namespaceURI, final String lName, final String qName, final Attributes attrs) throws SAXException {
    if (qName.equals("rule")) {      
      id = attrs.getValue("id");
      name = attrs.getValue("name");
      if (inRuleGroup && id == null) {
        id = ruleGroupId;
      }
      if (inRuleGroup && name == null) {
        name = ruleGroupName;
      }
    } else if (qName.equals("rules")) {
      language = Language.getLanguageForShortName(attrs.getValue("lang"));
    } else if (qName.equals("pattern")) {
      inPattern = true;
      if (attrs.getValue("mark") != null) {
        positionCorrection = Integer.parseInt(attrs.getValue("mark"));
      }
    } else if (qName.equals("exception")) {
      inException = true;      
      exceptions = new StringBuffer();

      if (attrs.getValue("negate") != null) {
        exceptionStringNegation = attrs.getValue("negate").equals("yes");
      }
      if (attrs.getValue("scope") != null) {
        exceptionValidNext = attrs.getValue("scope").equals("next");
        exceptionValidPrev = attrs.getValue("scope").equals("previous");
      }
      if (attrs.getValue("inflected") != null) {
        exceptionStringInflected = attrs.getValue("inflected").equals("yes");
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
        
      } else if (qName.equals("and")) {
        inAndGroup = true;
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
          posRegExp = attrs.getValue("postag_regexp").equals("yes");
        }
        if (attrs.getValue("negate_pos") != null) {
          posNegation = (attrs.getValue("negate_pos").equals("yes"));
        }
                
      }
      if (attrs.getValue("regexp") != null) {
        stringRegExp = attrs.getValue("regexp").equals("yes");
      }
      
    }  else if (qName.equals("disambig")) {
      inDisamb = true;
      disambiguatedPOS = attrs.getValue("postag");      
    } else if (qName.equals("match")) {
      inMatch = true;
      match = new StringBuffer();      
      Match.CaseConversion caseConv = Match.CaseConversion.NONE; 
      if (attrs.getValue("case_conversion") != null) {
        caseConv = Match.CaseConversion.toCase(
              attrs.getValue("case_conversion").toUpperCase());
      }      
      final Match mWorker = new Match(attrs.getValue("postag"),
          attrs.getValue("postag_replace"),
          "yes".equals(attrs.getValue("postag_regexp")),
          attrs.getValue("regexp_match"), 
          attrs.getValue("regexp_replace"), caseConv,
          "yes".equals(attrs.getValue("setpos")));
      if (inDisamb) {
        if (attrs.getValue("no") != null) {
          final int refNumber = Integer.parseInt(attrs.getValue("no"));
            if (refNumber > elementList.size()) {
              throw new SAXException(
                  "Only backward references in match elements are possible, tried to specify token " + refNumber);
            } else {
              mWorker.setTokenRef(refNumber);
              posSelector = mWorker;
            }
        }
      } else if (inToken) {
        if (attrs.getValue("no") != null) {
        final int refNumber = Integer.parseInt(attrs.getValue("no"));
          if (refNumber > elementList.size()) {
            throw new SAXException(
                "Only backward references in match elements are possible, tried to specify token " + refNumber);
          } else {
            mWorker.setTokenRef(refNumber);
            tokenReference = mWorker;
            elements.append("\\" + refNumber);
          }
        }
      }
    } else if (qName.equals("rulegroup")) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupName = attrs.getValue("name");
      inRuleGroup = true;
    }
  }

  @Override
  @SuppressWarnings("unused")
  public void endElement(final String namespaceURI, final String sName, final String qName) {
    if (qName.equals("rule")) {        
      final DisambiguationPatternRule rule = new DisambiguationPatternRule(id, name, 
          language, elementList, disambiguatedPOS, posSelector);
      rule.setStartPositionCorrection(positionCorrection);      
      rules.add(rule);      

      if (elementList != null) {
        elementList.clear();
      }
      posSelector = null;      
    } else if (qName.equals("exception")) {     
      inException = false;
      if (!exceptionSet) {
        tokenElement = new Element(elements.toString(), 
            caseSensitive, stringRegExp, tokenInflected);
        exceptionSet = true;
      }
      tokenElement.setNegation(tokenNegated);
      if (!exceptions.toString().equals("")) {
        tokenElement.setStringException(exceptions.toString(), exceptionStringRegExp, 
            exceptionStringInflected, exceptionStringNegation, 
            exceptionValidNext, exceptionValidPrev);
      }              
      if (exceptionPosToken != null) {
        tokenElement.setPosException(exceptionPosToken, exceptionPosRegExp, 
            exceptionPosNegation, exceptionValidNext, exceptionValidPrev);
        exceptionPosToken = null;
      }
      resetException();
    } else if (qName.equals("and")) {
      inAndGroup = false;
      andGroupCounter = 0;
    } else if (qName.equals("token")) {
          if (!exceptionSet || tokenElement == null) {
            tokenElement = new Element(elements.toString(), caseSensitive, 
                stringRegExp, tokenInflected);
            tokenElement.setNegation(tokenNegated);
          } else {
            tokenElement.setStringElement(elements.toString());
          }
          if (skipPos != 0) {
            tokenElement.setSkipNext(skipPos);
            skipPos = 0;
          }
          if (posToken != null) {
            tokenElement.setPosElement(posToken, posRegExp, posNegation);
            posToken = null;
          }
          
          if (tokenReference != null) {
            tokenElement.setMatch(tokenReference);
          }
          
          if (inAndGroup && andGroupCounter > 0) {
            elementList.get(elementList.size() - 1).setAndGroupElement(tokenElement);
          } else {
            elementList.add(tokenElement);
          }
          if (inAndGroup) {
            andGroupCounter++;
          }
          resetToken();
          
    } else if (qName.equals("pattern")) {
      inPattern = false;
    } else if (qName.equals("disambig")) {
      inDisamb = false;
    } else if (qName.equals("rulegroup")) {
      inRuleGroup = false;
    }
  }

  private void resetToken() {
    tokenNegated = false;
    tokenInflected = false;
    posNegation = false;
    posRegExp = false;
    inToken = false;
    stringRegExp = false;
    
    resetException();
    exceptionSet = false;
    tokenReference = null;
  }
  
  private void resetException() {
    exceptionStringNegation = false;
    exceptionStringInflected = false;
    exceptionPosNegation = false;
    exceptionPosRegExp = false;
    exceptionStringRegExp = false;
    exceptionValidNext = false;
    exceptionValidPrev = false;
  }
  
  @Override
  public final void characters(final char[] buf, final int offset, final int len) {
    final String s = new String(buf, offset, len);
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
