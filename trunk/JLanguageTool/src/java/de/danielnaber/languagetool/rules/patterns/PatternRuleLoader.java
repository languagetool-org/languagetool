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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Category;

/**
 * Loads {@link PatternRule}s from an XML file.
 * 
 * @author Daniel Naber
 */
public class PatternRuleLoader extends DefaultHandler {

  private List<PatternRule> rules;

  public PatternRuleLoader() {
  }

  public final List<PatternRule> getRules(final InputStream is, final String filename) throws IOException {
    try {
      PatternRuleHandler handler = new PatternRuleHandler();
      SAXParserFactory factory = SAXParserFactory.newInstance();
      SAXParser saxParser = factory.newSAXParser();
      saxParser.parse(is, handler);
      rules = handler.getRules();
      return rules;
    } catch (Exception e) {
      IOException ioe = new IOException("Cannot load or parse '"+filename+"'");
      ioe.initCause(e);
      throw ioe;
    }
  }

  /** Testing only. */
  public void main(String[] args) throws IOException {
    PatternRuleLoader prg = new PatternRuleLoader();
    String name = "/rules/de/grammar.xml";
    List<PatternRule> l = prg.getRules(this.getClass().getResourceAsStream(name), name);
    System.out.println(l);
  }

}

class PatternRuleHandler extends XMLRuleHandler {

  private String id;

  /** Current phrase ID. **/
  private String phraseId;
  
  /** ID reference to the phrase. **/
  private String phraseIdRef;
  
  private boolean caseSensitive = false;
  private boolean stringRegExp = false;
  private boolean tokenNegated = false;
  private boolean tokenInflected = false;
  private String posToken;
  private boolean posNegation = false;
  private boolean posRegExp = false;
  
  private Language language;
  private Category category;
  private String description;
  private String ruleGroupId;
  private String ruleGroupDescription;
  
  private String exceptionPosToken;
  private boolean exceptionStringRegExp = false;
  private boolean exceptionStringNegation = false;
  private boolean exceptionStringInflected = false;
  private boolean exceptionPosNegation = false;
  private boolean exceptionPosRegExp = false;
  private boolean exceptionValidNext = false;
  private boolean exceptionSet = false;
  
  /** true when phraseref is the last element in the rule. **/ 
  private boolean lastPhrase = false;
  
  /** List of elements as specified by tokens. **/ 
  private ArrayList < Element > elementList = null;  
  
  /** Phrase store - elementLists keyed by phraseIds. **/
  private HashMap < String, ArrayList < ArrayList < Element > > > phraseMap = null;
  
  /** Logically forking element list, used for including
   * multiple phrases in the current one. **/
  private ArrayList < ArrayList < Element > > phraseElementList = null;  
  
  private ArrayList<Match> suggestionMatches = null;
  
  private int startPositionCorrection = 0;
  private int endPositionCorrection = 0;
  private int skipPos = 0;
  
  private Element tokenElement = null;
  
  private int andGroupCounter = 0;
  
  private Match tokenReference = null;

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @SuppressWarnings("unused")
  public void startElement(String namespaceURI, String lName, String qName, Attributes attrs)
      throws SAXException {
    if (qName.equals("category")) {
      String catName = attrs.getValue("name");
      String prioStr = attrs.getValue("priority");
      int prio = 0;
      if (prioStr != null)
        category = new Category(catName, Integer.parseInt(prioStr));
      else
        category = new Category(catName);
    } else if (qName.equals("rules")) {
      String languageStr = attrs.getValue("lang");
      language = Language.getLanguageForShortName(languageStr);
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
      correctExamples = new ArrayList<String>();
      incorrectExamples = new ArrayList<String>();
    } else if (qName.equals("pattern")) {
      inPattern = true;
      if (attrs.getValue("mark_from") != null)
        startPositionCorrection = Integer.parseInt(attrs.getValue("mark_from"));
      if (attrs.getValue("mark_to") != null)
        endPositionCorrection = Integer.parseInt(attrs.getValue("mark_to"));
      if (attrs.getValue("case_sensitive") != null
          && attrs.getValue("case_sensitive").equals("yes"))
        caseSensitive = true;
    } else if (qName.equals("and")) {
      inAndGroup = true;
    } else if (qName.equals("token")) {
      inToken = true;
      
      if (lastPhrase && elementList != null) {
        elementList.clear();
      }
      
      lastPhrase = false;
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
      // POSElement creation
      if (attrs.getValue("postag") != null) {
        posToken = attrs.getValue("postag");
        if (attrs.getValue("postag_regexp") != null) {
          posRegExp = attrs.getValue("postag_regexp").equals("yes");
        }
        if (attrs.getValue("negate_pos") != null) {
          posNegation = attrs.getValue("negate_pos").equals("yes");
        }
      }
      if (attrs.getValue("regexp") != null) {
        stringRegExp = attrs.getValue("regexp").equals("yes");
      }

    } else if (qName.equals("exception")) {
      inException = true;      
      exceptions = new StringBuffer();

      if (attrs.getValue("negate") != null) {
        exceptionStringNegation = attrs.getValue("negate").equals("yes");
      }
      if (attrs.getValue("scope") != null) {
        exceptionValidNext = attrs.getValue("scope").equals("next");
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

    } else if (qName.equals("example") 
        && attrs.getValue("type").equals("correct")) {
      inCorrectExample = true;
      correctExample = new StringBuffer();
    } else if (qName.equals("example") 
        && attrs.getValue("type").equals("incorrect")) {
      inIncorrectExample = true;
      incorrectExample = new StringBuffer();
      //kludgy but simple
      if (attrs.getValue("correction") != null) {
        incorrectExample.append("<correction>" 
            + attrs.getValue("correction")
            + "</correction>");
      }
    } else if (qName.equals("message")) {
      inMessage = true;
      message = new StringBuffer();
    } else if (qName.equals("rulegroup")) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupDescription = attrs.getValue("name");
      inRuleGroup = true;
    } else if (qName.equals("suggestion") && inMessage) {
      message.append("<suggestion>");
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
          attrs.getValue("regexp_replace"), caseConv);
      if (inMessage) {
        if (suggestionMatches == null) {
          suggestionMatches = new ArrayList<Match>();        
        }        
      suggestionMatches.add(mWorker);
      message.append("\\" + attrs.getValue("no"));
      } else if (inToken) {
        if (attrs.getValue("no") != null) {
        int refNumber = Integer.parseInt(attrs.getValue("no"));
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
    } else if (qName.equals("marker") && inCorrectExample) {
      correctExample.append("<marker>");
    } else if (qName.equals("marker") && inIncorrectExample) {
      incorrectExample.append("<marker>");
    } else if (qName.equals("phrases")) {
      inPhrases = true;
    } else if (qName.equals("includephrases")) {
      phraseElementInit();      
      if (elementList == null) {
        elementList = new ArrayList < Element >();
      }
          
    } else if (qName.equals("phrase") && inPhrases) {
      phraseId = attrs.getValue("id");      
    } else if (qName.equals("phraseref")) {
      if (attrs.getValue("idref") != null) {
        phraseIdRef = attrs.getValue("idref");
      if (phraseMap.containsKey(phraseIdRef)) {      
      ArrayList < ArrayList < Element > > curPhraseElementList = phraseMap.get(phraseIdRef);              
      for (ArrayList < Element > curPhrEl : curPhraseElementList) {              
        if (!elementList.isEmpty()) {
          ArrayList < Element > prevList = new ArrayList < Element > (elementList);
          prevList.addAll(curPhrEl);
          phraseElementList.add(new ArrayList <Element>(prevList));
          prevList.clear();
        } else {        
        phraseElementList.add(new ArrayList <Element>(curPhrEl));
        }       
      }
      lastPhrase = true;
      }                     
     }
    }
   }

  @SuppressWarnings("unused")
  public void endElement(String namespaceURI, String sName, String qName) {

    if (qName.equals("rule")) {
      phraseElementInit();
      if (!phraseElementList.isEmpty()) {                                       

        if (!elementList.isEmpty()) { 
          for (ArrayList < Element > ph : phraseElementList) {
            ph.addAll(new ArrayList <Element> (elementList));
          }
        }

        for (ArrayList < Element > phraseElement : phraseElementList) {
          PatternRule rule = new PatternRule(id, language, phraseElement, description, message.toString(), phraseElementList.size()>1);      
          prepareRule(rule);
          rules.add(rule);              
        }
      } else {
        PatternRule rule = new PatternRule(id, language, elementList, description, message.toString());
        prepareRule(rule);
        rules.add(rule);
      }

      if (elementList != null) {
        elementList.clear();
      }      
      if (phraseElementList != null) {
        phraseElementList.clear();
      }

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
            exceptionStringInflected, exceptionStringNegation, exceptionValidNext);
      }              
      if (exceptionPosToken != null) {
        tokenElement.setPosException(exceptionPosToken, exceptionPosRegExp, exceptionPosNegation, exceptionValidNext);
        exceptionPosToken = null;
      }

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
      if (lastPhrase) {
        elementList.clear();
      }
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
    } else if (qName.equals("match")) {
      if (inMessage) {
      suggestionMatches.get(suggestionMatches.size() - 1)
        .setLemmaString(match.toString());
      } else if (inToken) {
        tokenReference.setLemmaString(match.toString());
      }
      inMatch = false;
    } else if (qName.equals("rulegroup")) {
      inRuleGroup = false;
    } else if (qName.equals("suggestion") && inMessage) {
      message.append("</suggestion>");
    } else if (qName.equals("marker") && inCorrectExample) {
      correctExample.append("</marker>");
    } else if (qName.equals("marker") && inIncorrectExample) {
      incorrectExample.append("</marker>");
    } else if (qName.equals("phrase") && inPhrases) {      
      // lazy init
      if (phraseMap == null) {
        phraseMap = new HashMap < String, ArrayList < ArrayList < Element > > > ();
      }      
      phraseElementInit();

      if (elementList != null) {
        if (!phraseElementList.isEmpty()) {
          for (ArrayList < Element > ph : phraseElementList) {
            ph.addAll(new ArrayList <Element> (elementList));
          }
        } else {
          phraseElementList.add(new ArrayList < Element > (elementList));
        }
      }     
      phraseMap.put(phraseId, new ArrayList < ArrayList < Element > >(phraseElementList));
      if (elementList != null) {
        elementList.clear();
      }
      phraseElementList.clear();
    } else if (qName.equals("includephrases")) {
      elementList.clear();      
    } else if (qName.equals("phrases") && inPhrases) {
      inPhrases = false;
    } 
  }

  private void resetToken() {
    tokenNegated = false;
    tokenInflected = false;
    posNegation = false;
    posRegExp = false;
    inToken = false;
    stringRegExp = false;

    exceptionStringNegation = false;
    exceptionStringInflected = false;
    exceptionPosNegation = false;
    exceptionPosRegExp = false;
    exceptionStringRegExp = false;
    exceptionValidNext = false;
    exceptionSet = false; 
    tokenReference = null;
  }
  
  private void prepareRule(PatternRule rule) {
    rule.setStartPositionCorrection(startPositionCorrection);
    rule.setEndPositionCorrection(endPositionCorrection);
    startPositionCorrection = 0;
    endPositionCorrection = 0;
    rule.setCorrectExamples(correctExamples);
    rule.setIncorrectExamples(incorrectExamples);      
    rule.setCategory(category);
    caseSensitive = false;
    if (suggestionMatches != null) {
      for (Match m : suggestionMatches) {
        rule.addSuggestionMatch(m);
      }
      suggestionMatches.clear();
    } 
  }
  
  private void phraseElementInit(){
    // lazy init
    if (phraseElementList == null) {
      phraseElementList = new ArrayList < ArrayList < Element > > ();
    }
  }
  
  public void characters(final char[] buf, final int offset, final int len) {
    String s = new String(buf, offset, len);
    if (inException) {
      exceptions.append(s);
    } else if (inToken) {
      elements.append(s);
    } else if (inCorrectExample) {
      correctExample.append(s);
    } else if (inIncorrectExample) {
      incorrectExample.append(s);
    } else if (inMatch) {
      match.append(s);
    } else if (inMessage) {
      message.append(s);
    }
  }

}
