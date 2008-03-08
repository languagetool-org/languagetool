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
import de.danielnaber.languagetool.rules.IncorrectExample;
import de.danielnaber.languagetool.tools.StringTools;

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
      final PatternRuleHandler handler = new PatternRuleHandler();
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      final SAXParser saxParser = factory.newSAXParser();      
      saxParser.getXMLReader().setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd",
          false);      
      saxParser.parse(is, handler);
      rules = handler.getRules();
      return rules;
    } catch (final Exception e) {
      final IOException ioe = new IOException("Cannot load or parse '"+filename+"'");
      ioe.initCause(e);
      throw ioe;
    }
  }

  /** Testing only. */
  public final void main(final String[] args) throws IOException {
    final PatternRuleLoader prg = new PatternRuleLoader();
    final String name = "/rules/de/grammar.xml";
    final List<PatternRule> l = prg.getRules(this.getClass().getResourceAsStream(name), name);
    System.out.println(l);
  }

}

class PatternRuleHandler extends XMLRuleHandler {

  /** Defines "yes" value in XML files. 
   * 
   * **/   
  private static final String YES = "yes";  
  private static final String POSTAG = "postag";
  private static final String POSTAG_REGEXP = "postag_regexp";
  private static final String REGEXP = "regexp";
  private static final String NEGATE = "negate";
  private static final String INFLECTED = "inflected";
  private static final String NEGATE_POS = "negate_pos";
  private static final String MARKER = "marker";
  
  private String id;
  private int subId = 0;

  /** Current phrase ID. **/
  String phraseId;
  
  /** ID reference to the phrase. **/
  String phraseIdRef;
  
  private boolean caseSensitive = false;
  private boolean stringRegExp = false;
  private boolean tokenNegated = false;
  private boolean tokenInflected = false;
  private String posToken;
  private boolean posNegation = false;
  private boolean posRegExp = false;
  
  private boolean defaultOff = false;
  
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
  private boolean exceptionValidPrev = false;
  private boolean exceptionSet = false;
  
  /** true when phraseref is the last element in the rule. **/ 
  private boolean lastPhrase = false;
  
  /** List of elements as specified by tokens. **/ 
  private List < Element > elementList = null;  
  
  /** Phrase store - elementLists keyed by phraseIds. **/
  private HashMap < String, ArrayList < ArrayList < Element > > > phraseMap = null;
  
  /** Logically forking element list, used for including
   * multiple phrases in the current one. **/
  private List < ArrayList < Element > > phraseElementList = null;
  
  private List<Match> suggestionMatches = null;
  
  private int startPositionCorrection = 0;
  private int endPositionCorrection = 0;
  private int skipPos = 0;
  
  private Element tokenElement = null;
  
  private int andGroupCounter = 0;
  
  private Match tokenReference = null;

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  @SuppressWarnings("unused")
  public void startElement(final String namespaceURI, final String lName, final String qName, final Attributes attrs)
      throws SAXException {
    if (qName.equals("category")) {
      final String catName = attrs.getValue("name");
      final String prioStr = attrs.getValue("priority");
      //int prio = 0;
      if (prioStr != null) {
        category = new Category(catName, Integer.parseInt(prioStr));
      } else {
        category = new Category(catName);
      }
    } else if (qName.equals("rules")) {
      final String languageStr = attrs.getValue("lang");
      language = Language.getLanguageForShortName(languageStr);
      if (language == null) {
        throw new SAXException("Unknown language '" + languageStr + "'");
      }
    } else if (qName.equals("rule")) {
      id = attrs.getValue("id");
      subId++;
      if (!(inRuleGroup && defaultOff)) {
        defaultOff = "off".equals(attrs.getValue("default"));
      }
      if (inRuleGroup && id == null) {
        id = ruleGroupId;
      }
      description = attrs.getValue("name");
      if (inRuleGroup && description == null) {
        description = ruleGroupDescription;
      }
      correctExamples = new ArrayList<String>();
      incorrectExamples = new ArrayList<IncorrectExample>();
      if (suggestionMatches != null) {
        suggestionMatches.clear();
      }
    } else if (qName.equals("pattern")) {
      inPattern = true;
      if (attrs.getValue("mark_from") != null) {
        startPositionCorrection = Integer.parseInt(attrs.getValue("mark_from"));
      }
      if (attrs.getValue("mark_to") != null) {
        endPositionCorrection = Integer.parseInt(attrs.getValue("mark_to"));
      }
      if (attrs.getValue("case_sensitive") != null
          && YES.equals(attrs.getValue("case_sensitive"))) {
        caseSensitive = true;
      }
    } else if (qName.equals("and")) {
      inAndGroup = true;
    } else if (qName.equals("token")) {
      inToken = true;
      
      if (lastPhrase && elementList != null) {
        elementList.clear();
      }
      
      lastPhrase = false;
      if (attrs.getValue(NEGATE) != null) {
        tokenNegated = YES.equals(attrs.getValue(NEGATE));
      }
      if (attrs.getValue(INFLECTED) != null) {
        tokenInflected = YES.equals(attrs.getValue(INFLECTED));
      }
      if (attrs.getValue("skip") != null) {
        skipPos = Integer.parseInt(attrs.getValue("skip"));
      }
      elements = new StringBuffer();
      if (elementList == null) {
        elementList = new ArrayList<Element>();
      }
      // POSElement creation
      if (attrs.getValue(POSTAG) != null) {
        posToken = attrs.getValue(POSTAG);
        if (attrs.getValue(POSTAG_REGEXP) != null) {
          posRegExp = YES.equals(attrs.getValue(POSTAG_REGEXP));
        }
        if (attrs.getValue(NEGATE_POS) != null) {
          posNegation = YES.equals(attrs.getValue(NEGATE_POS));
        }
      }
      if (attrs.getValue(REGEXP) != null) {
        stringRegExp = YES.equals(attrs.getValue(REGEXP));
      }            

    } else if (qName.equals("exception")) {
      inException = true;      
      exceptions = new StringBuffer();
      resetException();

      if (attrs.getValue(NEGATE) != null) {
        exceptionStringNegation = YES.equals(attrs.getValue(NEGATE));
      }
      if (attrs.getValue("scope") != null) {
        exceptionValidNext = attrs.getValue("scope").equals("next");
        exceptionValidPrev = attrs.getValue("scope").equals("previous");
      }
      if (attrs.getValue(INFLECTED) != null) {
        exceptionStringInflected = YES.equals(attrs.getValue(INFLECTED));
      }
      if (attrs.getValue(POSTAG) != null) {
        exceptionPosToken = attrs.getValue(POSTAG);
        if (attrs.getValue(POSTAG_REGEXP) != null) {
          exceptionPosRegExp = YES.equals(attrs.getValue(POSTAG_REGEXP));
        }
        if (attrs.getValue(NEGATE_POS) != null) {
          exceptionPosNegation = YES.equals(attrs.getValue(NEGATE_POS));
        }
      }
      if (attrs.getValue(REGEXP) != null) {
        exceptionStringRegExp = YES.equals(attrs.getValue(REGEXP));
      }
    } else if (qName.equals("example") 
        && attrs.getValue("type").equals("correct")) {
      inCorrectExample = true;
      correctExample = new StringBuffer();
    } else if (qName.equals("example") 
        && attrs.getValue("type").equals("incorrect")) {
      inIncorrectExample = true;
      incorrectExample = new StringBuffer();
      exampleCorrection = new StringBuffer();
      if (attrs.getValue("correction") != null) {
        exampleCorrection.append(attrs.getValue("correction"));
      }
    } else if (qName.equals("message")) {
      inMessage = true;
      message = new StringBuffer();
    } else if (qName.equals("rulegroup")) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupDescription = attrs.getValue("name");
      defaultOff = "off".equals(attrs.getValue("default"));
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
      final Match mWorker = new Match(attrs.getValue(POSTAG),
          attrs.getValue("postag_replace"),
          YES.equals(attrs.getValue(POSTAG_REGEXP)),
          attrs.getValue("regexp_match"), 
          attrs.getValue("regexp_replace"), caseConv,
          YES.equals(attrs.getValue("setpos")));
      if (inMessage) {
        if (suggestionMatches == null) {
          suggestionMatches = new ArrayList<Match>();        
        }        
      suggestionMatches.add(mWorker);
      message.append("\\" + attrs.getValue("no"));
      } else if (inToken && attrs.getValue("no") != null) {
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
    } else if (qName.equals(MARKER) && inCorrectExample) {
      correctExample.append("<marker>");
    } else if (qName.equals(MARKER) && inIncorrectExample) {
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
    } else if (qName.equals("phraseref") 
        && (attrs.getValue("idref") != null)) {
      phraseIdRef = attrs.getValue("idref");
      if (phraseMap.containsKey(phraseIdRef)) {                            
        for (final ArrayList < Element > curPhrEl : phraseMap.get(phraseIdRef)) {
          for (final Element e : curPhrEl) {
            e.setPhraseName(phraseIdRef);
          }
          if (elementList.isEmpty()) {
            phraseElementList.add(new ArrayList <Element>(curPhrEl));                        
          } else {
            final ArrayList < Element > prevList = new ArrayList < Element > (elementList);
            prevList.addAll(curPhrEl);            
            phraseElementList.add(new ArrayList <Element>(prevList));            
            prevList.clear();
          }       
        }
        lastPhrase = true;
      }                     
    }
  }
   

  @Override
  @SuppressWarnings("unused")
  public void endElement(final String namespaceURI, final String sName, final String qName) {

    if (qName.equals("rule")) {      
      phraseElementInit();
      if (phraseElementList.isEmpty()) {                                       
        final PatternRule rule = new PatternRule(id, language, elementList, description, message.toString());
        prepareRule(rule);        
        rules.add(rule);        
      } else {
        if (!elementList.isEmpty()) { 
          for (final ArrayList < Element > ph : phraseElementList) {
            ph.addAll(new ArrayList <Element> (elementList));
          }
        }

        for (final ArrayList < Element > phraseElement : phraseElementList) {
          processElement(phraseElement);
          final PatternRule rule = new PatternRule(id, language, phraseElement, description,
              message.toString(), phraseElementList.size()>1);      
          prepareRule(rule);
          rules.add(rule);              
        }
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
        tokenElement = new Element(StringTools.trimWhitespace(elements.toString()), 
            caseSensitive, stringRegExp, tokenInflected);
        exceptionSet = true;
      }
      tokenElement.setNegation(tokenNegated);
      if (!"".equals(exceptions.toString())) {
        tokenElement.setStringException(StringTools.trimWhitespace(exceptions.toString()), 
            exceptionStringRegExp, exceptionStringInflected, exceptionStringNegation, 
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
        tokenElement = new Element(StringTools.trimWhitespace(elements.toString()), caseSensitive, 
            stringRegExp, tokenInflected);
        tokenElement.setNegation(tokenNegated);
      } else {
        tokenElement.setStringElement(StringTools.trimWhitespace(elements.toString()));
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
        IncorrectExample example = null;
        String[] corrections = exampleCorrection.toString().split("\\|");
        if (corrections.length > 0 && corrections[0].length() > 0) {
          example = new IncorrectExample(incorrectExample.toString(), corrections);
        } else {
          example = new IncorrectExample(incorrectExample.toString());
        }
        incorrectExamples.add(example);
      }
      inCorrectExample = false;
      inIncorrectExample = false;
      correctExample = new StringBuffer();
      incorrectExample = new StringBuffer();
      exampleCorrection = new StringBuffer();
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
      subId = 0;
    } else if (qName.equals("suggestion") && inMessage) {
      message.append("</suggestion>");
    } else if (qName.equals(MARKER) && inCorrectExample) {
      correctExample.append("</marker>");
    } else if (qName.equals(MARKER) && inIncorrectExample) {
      incorrectExample.append("</marker>");
    } else if (qName.equals("phrase") && inPhrases) {      
      // lazy init
      if (phraseMap == null) {
        phraseMap = new HashMap < String, ArrayList < ArrayList < Element > > > ();
      }      
      phraseElementInit();

      if (elementList != null) {
        if (phraseElementList.isEmpty()) {
          phraseElementList.add(new ArrayList < Element > (elementList));                    
        } else {
          for (final ArrayList < Element > ph : phraseElementList) {
            ph.addAll(new ArrayList <Element> (elementList));
          }
        }
      }     
      phraseMap.put(phraseId, 
            new ArrayList < ArrayList < Element > >(phraseElementList));
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
  
  /**
   * Calculates the offset of the match reference (if any)
   * in case the match element has been used in the group. 
   * @param elList Element list where the match element was used. 
   * It is directly changed.
   */
  private void processElement(ArrayList < Element > elList) {
    int counter = 0;    
    for (final Element elTest : elList) {
      if (elTest.getPhraseName() != null && counter > 0) {
        if (elTest.referenceElement()) {
          final int tokRef = elTest.getMatch().getTokenRef();          
          elTest.getMatch().setTokenRef(
              tokRef + counter - 1);
          final String offsetToken = 
            elTest.getString().
            replace("\\" + tokRef, "\\" + (tokRef + counter - 1));
          elTest.setStringElement(offsetToken);
          }        
        }                           
      counter++;
    }
  }
      
  
  private void prepareRule(final PatternRule rule) {
    rule.setStartPositionCorrection(startPositionCorrection);
    rule.setEndPositionCorrection(endPositionCorrection);
    startPositionCorrection = 0;
    endPositionCorrection = 0;
    rule.setCorrectExamples(correctExamples);
    rule.setIncorrectExamples(incorrectExamples);      
    rule.setCategory(category);
    rule.setSubId(subId + "");
    caseSensitive = false;
    if (suggestionMatches != null) {
      for (final Match m : suggestionMatches) {
        rule.addSuggestionMatch(m);
      }
    if (phraseElementList.size() <= 1) {
      suggestionMatches.clear();
    }
    }
    if (defaultOff) {
      rule.setDefaultOff();
    }
  }
  
  private void phraseElementInit(){
    // lazy init
    if (phraseElementList == null) {
      phraseElementList = new ArrayList < ArrayList < Element > > ();
    }
  }
  
  @Override
  public void characters(final char[] buf, final int offset, final int len) {
    final String s = new String(buf, offset, len);
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
