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
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.JLanguageTool;
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

  public PatternRuleLoader() {
    super();
  }

  public final List<PatternRule> getRules(final InputStream is,
      final String filename) throws IOException {
    try {
      final PatternRuleHandler handler = new PatternRuleHandler();
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      final SAXParser saxParser = factory.newSAXParser();
      saxParser.getXMLReader().setFeature(
          "http://apache.org/xml/features/nonvalidating/load-external-dtd",
          false);
      saxParser.parse(is, handler);
      return handler.getRules();
    } catch (final Exception e) {
      final IOException ioe = new IOException("Cannot load or parse '"
          + filename + "'");
      ioe.initCause(e);
      throw ioe;
    }
  }

  /** Testing only. */
  public final void main(final String[] args) throws IOException {
    final PatternRuleLoader prg = new PatternRuleLoader();
    final String name = "/de/grammar.xml";
    final List<PatternRule> l = prg.getRules(JLanguageTool.getDataBroker().getFromRulesDirAsStream(name), name);
    System.out.println(l);
  }

}

class PatternRuleHandler extends XMLRuleHandler {

  private String id;
  private int subId;

  /** Current phrase ID. **/
  private String phraseId;

  /** ID reference to the phrase. **/
  private String phraseIdRef;

  private boolean caseSensitive;
  private boolean stringRegExp;
  private boolean tokenNegated;
  private boolean tokenInflected;
  private boolean tokenSpaceBefore;
  private boolean tokenSpaceBeforeSet;
  private String posToken;
  private boolean posNegation;
  private boolean posRegExp;

  private boolean defaultOff;
  private boolean defaultOn;

  private Language language;
  private Category category;
  private String description;
  private String ruleGroupId;
  private String ruleGroupDescription;

  private String exceptionPosToken;
  private boolean exceptionStringRegExp;
  private boolean exceptionStringNegation;
  private boolean exceptionStringInflected;
  private boolean exceptionPosNegation;
  private boolean exceptionPosRegExp;
  private boolean exceptionValidNext;
  private boolean exceptionValidPrev;
  private boolean exceptionSet;
  private boolean exceptionSpaceBefore;
  private boolean exceptionSpaceBeforeSet;

  /** true when phraseref is the last element in the rule. **/
  private boolean lastPhrase;

  /** List of elements as specified by tokens. **/
  private final List<Element> elementList;

  /** Phrase store - elementLists keyed by phraseIds. **/
  private Map<String, List<List<Element>>> phraseMap;

  /**
   * Logically forking element list, used for including multiple phrases in the
   * current one.
   **/
  private List<ArrayList<Element>> phraseElementList;

  private List<Match> suggestionMatches;

  private int startPositionCorrection;
  private int endPositionCorrection;
  private int skipPos;

  private Element tokenElement;

  private int andGroupCounter;
  
  private int tokenCounter;

  private Match tokenReference;

  private StringBuilder shortMessage = new StringBuilder();
  private boolean inShortMessage;

  private boolean inUnification;
  private boolean inUnificationDef;
  private boolean uniNegation;

  private String uFeature;
  private String uType = "";
  
  private List<String> uTypeList;
  
  private Map<String, List<String>> equivalenceFeatures;

  private Locator pLocator;

  public PatternRuleHandler() {
    elementList = new ArrayList<Element>();
    equivalenceFeatures = new HashMap<String, List<String>>();
    uTypeList = new ArrayList<String>();
  }

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  public void setDocumentLocator(final Locator locator) {
    pLocator = locator;
    super.setDocumentLocator(locator);
  }

  @Override
  public void startElement(final String namespaceURI, final String lName,
      final String qName, final Attributes attrs) throws SAXException {
    if (qName.equals("category")) {
      final String catName = attrs.getValue("name");
      final String priorityStr = attrs.getValue("priority");
      // int prio = 0;
      if (priorityStr != null) {
        category = new Category(catName, Integer.parseInt(priorityStr));
      } else {
        category = new Category(catName);
      }

      if ("off".equals(attrs.getValue(DEFAULT))) {
        category.setDefaultOff();
      }

    } else if (qName.equals("rules")) {
      final String languageStr = attrs.getValue("lang");
      language = Language.getLanguageForShortName(languageStr);
      if (language == null) {
        throw new SAXException("Unknown language '" + languageStr + "'");
      }
    } else if (qName.equals("rule")) {
      id = attrs.getValue("id");
      if (inRuleGroup)
        subId++;
      if (!(inRuleGroup && defaultOff)) {
        defaultOff = "off".equals(attrs.getValue(DEFAULT));
      }

      if (!(inRuleGroup && defaultOn)) {
        defaultOn = "on".equals(attrs.getValue(DEFAULT));
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
        if (endPositionCorrection > 0) {
          throw new SAXException("End position correction (mark_to="+ endPositionCorrection
              + ") cannot be larger than 0: " + "\n Line: "
              + pLocator.getLineNumber() + ", column: "
              + pLocator.getColumnNumber() + ".");
        }
      }
      caseSensitive = YES.equals(attrs.getValue("case_sensitive"));     
    } else if (qName.equals("and")) {
      inAndGroup = true;
    } else if (qName.equals("unify")) {
        inUnification = true;           
        uniNegation = YES.equals(attrs.getValue("negate"));
    } else if (qName.equals("feature")) {
        uFeature = attrs.getValue("id");        
    } else if (qName.equals(TYPE)) {      
        uType = attrs.getValue("id");
        uTypeList.add(uType);
    } else if (qName.equals("token")) {
      inToken = true;

      if (lastPhrase) {
        elementList.clear();
      }

      lastPhrase = false;
      tokenNegated = YES.equals(attrs.getValue(NEGATE));
      tokenInflected = YES.equals(attrs.getValue(INFLECTED));      
      if (attrs.getValue("skip") != null) {
        skipPos = Integer.parseInt(attrs.getValue("skip"));
      }
      elements = new StringBuilder();
      // POSElement creation
      if (attrs.getValue(POSTAG) != null) {
        posToken = attrs.getValue(POSTAG);
        posRegExp = YES.equals(attrs.getValue(POSTAG_REGEXP));
        posNegation = YES.equals(attrs.getValue(NEGATE_POS));       
      }
      stringRegExp = YES.equals(attrs.getValue(REGEXP));
      
      if (attrs.getValue(SPACEBEFORE) != null) {
        tokenSpaceBefore = YES.equals(attrs.getValue(SPACEBEFORE));
        tokenSpaceBeforeSet = !"ignore".equals(attrs.getValue(SPACEBEFORE));
      }

     if (!inAndGroup) {
       tokenCounter++;
     }
    } else if (qName.equals("exception")) {
      inException = true;
      exceptions = new StringBuilder();
      resetException();

      exceptionStringNegation = YES.equals(attrs.getValue(NEGATE));      
      exceptionValidNext = "next".equals(attrs.getValue(SCOPE));
      exceptionValidPrev = "previous".equals(attrs.getValue(SCOPE));      
      exceptionStringInflected = YES.equals(attrs.getValue(INFLECTED));
      
      if (attrs.getValue(POSTAG) != null) {
        exceptionPosToken = attrs.getValue(POSTAG);
        exceptionPosRegExp = YES.equals(attrs.getValue(POSTAG_REGEXP));
        exceptionPosNegation = YES.equals(attrs.getValue(NEGATE_POS));        
      }
      exceptionStringRegExp = YES.equals(attrs.getValue(REGEXP));
      if (attrs.getValue(SPACEBEFORE) != null) {
        exceptionSpaceBefore = YES.equals(attrs.getValue(SPACEBEFORE));
        exceptionSpaceBeforeSet = !"ignore".equals(attrs.getValue(SPACEBEFORE));
      }
    } else if (qName.equals(EXAMPLE)
        && attrs.getValue(TYPE).equals("correct")) {
      inCorrectExample = true;
      correctExample = new StringBuilder();
    } else if (qName.equals(EXAMPLE)
        && attrs.getValue(TYPE).equals("incorrect")) {
      inIncorrectExample = true;
      incorrectExample = new StringBuilder();
      exampleCorrection = new StringBuilder();
      if (attrs.getValue("correction") != null) {
        exampleCorrection.append(attrs.getValue("correction"));
      }
    } else if (qName.equals("message")) {
      inMessage = true;
      inSuggestion = false;
      message = new StringBuilder();
    } else if (qName.equals("short")) {
      inShortMessage = true;
      shortMessage = new StringBuilder();
    } else if (qName.equals("rulegroup")) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupDescription = attrs.getValue("name");
      defaultOff = "off".equals(attrs.getValue(DEFAULT));
      defaultOn = "on".equals(attrs.getValue(DEFAULT));
      inRuleGroup = true;
      subId = 0;
    } else if (qName.equals("suggestion") && inMessage) {
      message.append("<suggestion>");
      inSuggestion = true;
    } else if (qName.equals("match")) {
      inMatch = true;
      match = new StringBuilder();
      Match.CaseConversion caseConversion = Match.CaseConversion.NONE;
      if (attrs.getValue("case_conversion") != null) {
        caseConversion = Match.CaseConversion.toCase(attrs
            .getValue("case_conversion").toUpperCase());
      }
      Match.IncludeRange includeRange = Match.IncludeRange.NONE;
      if (attrs.getValue("include_skipped") != null) {
        includeRange = Match.IncludeRange.toRange(attrs
            .getValue("include_skipped").toUpperCase());
      }
      final Match mWorker = new Match(attrs.getValue(POSTAG), attrs
          .getValue("postag_replace"), YES
          .equals(attrs.getValue(POSTAG_REGEXP)), attrs
          .getValue("regexp_match"), attrs.getValue("regexp_replace"),
          caseConversion, YES.equals(attrs.getValue("setpos")),
          includeRange);
      mWorker.setInMessageOnly(!inSuggestion);
      if (inMessage) {
        if (suggestionMatches == null) {
          suggestionMatches = new ArrayList<Match>();
        }
        suggestionMatches.add(mWorker);
        //add incorrect XML character for simplicity
        message.append("\u0001\\");
        message.append(attrs.getValue("no"));
        if (StringTools.isEmpty(attrs.getValue("no"))) {
          throw new SAXException("References cannot be empty: " + "\n Line: "
              + pLocator.getLineNumber() + ", column: "
              + pLocator.getColumnNumber() + ".");
        } else if (Integer.parseInt(attrs.getValue("no")) < 1) {
          throw new SAXException("References must be larger than 0: "
              + attrs.getValue("no") + "\n Line: " + pLocator.getLineNumber()
              + ", column: " + pLocator.getColumnNumber() + ".");
        }
      } else if (inToken && attrs.getValue("no") != null) {
        final int refNumber = Integer.parseInt(attrs.getValue("no"));
        if (refNumber > elementList.size()) {
          throw new SAXException(
              "Only backward references in match elements are possible, tried to specify token "
                  + refNumber
                  + "\n Line: "
                  + pLocator.getLineNumber()
                  + ", column: " + pLocator.getColumnNumber() + ".");
        }
        mWorker.setTokenRef(refNumber);
        tokenReference = mWorker;
        elements.append("\\");
        elements.append(refNumber);
      }
    } else if (qName.equals(MARKER) && inCorrectExample) {
      correctExample.append("<marker>");
    } else if (qName.equals(MARKER) && inIncorrectExample) {
      incorrectExample.append("<marker>");
    } else if (qName.equals("unification")) {
      uFeature = attrs.getValue("feature");
      inUnificationDef = true;
    } else if (qName.equals("equivalence")) {
      uType = attrs.getValue(TYPE);
    } else if (qName.equals("phrases")) {
      inPhrases = true;
    } else if (qName.equals("includephrases")) {
      phraseElementInit();
    } else if (qName.equals("phrase") && inPhrases) {
      phraseId = attrs.getValue("id");
    } else if (qName.equals("phraseref") && (attrs.getValue("idref") != null)) {
      phraseIdRef = attrs.getValue("idref");
      if (phraseMap.containsKey(phraseIdRef)) {
        for (final List<Element> curPhrEl : phraseMap.get(phraseIdRef)) {
          for (final Element e : curPhrEl) {
            e.setPhraseName(phraseIdRef);
          }
          if (elementList.isEmpty()) {
            phraseElementList.add(new ArrayList<Element>(curPhrEl));
          } else {
            final ArrayList<Element> prevList = new ArrayList<Element>(
                elementList);
            prevList.addAll(curPhrEl);
            phraseElementList.add(new ArrayList<Element>(prevList));
            prevList.clear();
          }
        }
        lastPhrase = true;
      }
    }    
  }

  @Override
  public void endElement(final String namespaceURI, final String sName,
      final String qName) throws SAXException {

    if (qName.equals("rule")) {
      phraseElementInit();
      if (phraseElementList.isEmpty()) {
        final PatternRule rule = new PatternRule(id, language, elementList,
            description, message.toString(), shortMessage.toString());
        prepareRule(rule);
        rules.add(rule);
      } else {
        if (!elementList.isEmpty()) {
          for (final ArrayList<Element> ph : phraseElementList) {
            ph.addAll(new ArrayList<Element>(elementList));
          }
        }

        for (final ArrayList<Element> phraseElement : phraseElementList) {
          processElement(phraseElement);
          final PatternRule rule = new PatternRule(id, language, phraseElement,
              description, message.toString(), shortMessage.toString(),
              phraseElementList.size() > 1);
          prepareRule(rule);
          rules.add(rule);
        }
      }
      elementList.clear();
      if (phraseElementList != null) {
        phraseElementList.clear();
      }

    } else if (qName.equals("exception")) {
      inException = false;
      if (!exceptionSet) {
        tokenElement = new Element(StringTools.trimWhitespace(elements
            .toString()), caseSensitive, stringRegExp, tokenInflected);
        exceptionSet = true;
      }
      tokenElement.setNegation(tokenNegated);
      if (!StringTools.isEmpty(exceptions.toString())) {
        tokenElement.setStringException(StringTools.trimWhitespace(exceptions
            .toString()), exceptionStringRegExp, exceptionStringInflected,
            exceptionStringNegation, exceptionValidNext, exceptionValidPrev);
      }
      if (exceptionPosToken != null) {
        tokenElement.setPosException(exceptionPosToken, exceptionPosRegExp,
            exceptionPosNegation, exceptionValidNext, exceptionValidPrev);
        exceptionPosToken = null;
      }
      if (exceptionSpaceBeforeSet) {
        tokenElement.setExceptionSpaceBefore(exceptionSpaceBefore);
      }
      resetException();
    } else if (qName.equals("and")) {
      inAndGroup = false;
      andGroupCounter = 0;
      tokenCounter++;
    } else if (qName.equals("token")) {
      if (!exceptionSet || tokenElement == null) {
        tokenElement = new Element(StringTools.trimWhitespace(elements
            .toString()), caseSensitive, stringRegExp, tokenInflected);
        tokenElement.setNegation(tokenNegated);
      } else {
        tokenElement.setStringElement(StringTools.trimWhitespace(elements
            .toString()));
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
        elementList.get(elementList.size() - 1)
            .setAndGroupElement(tokenElement);
      } else {
        elementList.add(tokenElement);
      }
      if (inAndGroup) {
        andGroupCounter++;
      }

      if (inUnification) {
        tokenElement.setUnification(equivalenceFeatures);
        if (uniNegation) {
          tokenElement.setUniNegation();
        }
      }

      if (inUnificationDef) {
        language.getUnifier().setEquivalence(uFeature, uType, tokenElement);
        elementList.clear();
      }
      if (tokenSpaceBeforeSet) {
        tokenElement.setWhitespaceBefore(tokenSpaceBefore);
      }
      resetToken();
    } else if (qName.equals("pattern")) {
      if (phraseElementList == null || phraseElementList.size() == 0) {
          final int endMarker = elementList.size() + endPositionCorrection;
          if (endMarker <= startPositionCorrection) {
              throw new RuntimeException("Invalid combination of mark_from (" + startPositionCorrection
                      + ") and mark_to (" + endPositionCorrection + ") for rule " + id
                      + " with " + elementList.size() 
                      + " tokens: the error position created by mark_from and mark_to is less than one token");
          }
      }
      inPattern = false;
      if (lastPhrase) {
        elementList.clear();
      }
      if (phraseElementList == null || phraseElementList.isEmpty()) {
        checkPositions(0);
      } else {
        for (List<Element> elements : phraseElementList) {
          checkPositions(elements.size());
        }
      }
      tokenCounter = 0;
    } else if (qName.equals(EXAMPLE)) {
      if (inCorrectExample) {
        correctExamples.add(correctExample.toString());
      } else if (inIncorrectExample) {
        IncorrectExample example = null;
        final String[] corrections = exampleCorrection.toString().split("\\|");
        if (corrections.length > 0 && corrections[0].length() > 0) {
          example = new IncorrectExample(incorrectExample.toString(),
              corrections);
        } else {
          example = new IncorrectExample(incorrectExample.toString());
        }
        incorrectExamples.add(example);
      }
      inCorrectExample = false;
      inIncorrectExample = false;
      correctExample = new StringBuilder();
      incorrectExample = new StringBuilder();
      exampleCorrection = new StringBuilder();
    } else if (qName.equals("message")) {
      suggestionMatches = addLegacyMatches();
      inMessage = false;
    } else if (qName.equals("short")) {
      inShortMessage = false;
    } else if (qName.equals("match")) {
      if (inMessage) {
        suggestionMatches.get(suggestionMatches.size() - 1).setLemmaString(
            match.toString());
      } else if (inToken) {
        tokenReference.setLemmaString(match.toString());
      }
      inMatch = false;
    } else if (qName.equals("rulegroup")) {
      inRuleGroup = false;
    } else if (qName.equals("suggestion") && inMessage) {
      message.append("</suggestion>");
      inSuggestion = false;
    } else if (qName.equals(MARKER) && inCorrectExample) {
      correctExample.append("</marker>");
    } else if (qName.equals(MARKER) && inIncorrectExample) {
      incorrectExample.append("</marker>");
    } else if (qName.equals("phrase") && inPhrases) {
      // lazy init
      if (phraseMap == null) {
        phraseMap = new HashMap<String, List<List<Element>>>();
      }
      phraseElementInit();

      if (phraseElementList.isEmpty()) {
        phraseElementList.add(new ArrayList<Element>(elementList));
      } else {
        for (final ArrayList<Element> ph : phraseElementList) {
          ph.addAll(new ArrayList<Element>(elementList));
        }
      }

      phraseMap.put(phraseId, new ArrayList<List<Element>>(phraseElementList));
      elementList.clear();

      phraseElementList.clear();
    } else if (qName.equals("includephrases")) {
        elementList.clear();
    } else if (qName.equals("phrases") && inPhrases) {
        inPhrases = false;
    } else if (qName.equals("unification")) {
        inUnificationDef = false;
    } else if (qName.equals("feature")) {        
        equivalenceFeatures.put(uFeature, uTypeList);
        uTypeList = new ArrayList<String>();
    } else if (qName.equals("unify")) {      
      inUnification = false;
      //clear the features...
      equivalenceFeatures = new HashMap<String, List<String>>();
    }
  }
  
  private void checkPositions(final int add) throws SAXException {
    if (startPositionCorrection >= tokenCounter + add) {
      throw new SAXException(
          "Attempt to mark a token no. ("+ startPositionCorrection +") that is outside the pattern ("
          + tokenCounter + "). Pattern elements are numbered starting from 0!" + "\n Line: "
              + pLocator.getLineNumber() + ", column: "
              + pLocator.getColumnNumber() + ".");
    }
    if (tokenCounter +add - endPositionCorrection < 0) {
      throw new SAXException(
          "Attempt to mark a token no. ("+ endPositionCorrection +") that is outside the pattern ("
          + tokenCounter + " elements). End positions should be negative but not larger than the token count!"
          + "\n Line: "
              + pLocator.getLineNumber() + ", column: "
              + pLocator.getColumnNumber() + ".");
    } 
  }

  private void resetToken() {
    posNegation = false;
    posRegExp = false;
    inToken = false;
    tokenSpaceBefore = false;
    tokenSpaceBeforeSet = false;

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
    exceptionSpaceBefore = false;
    exceptionSpaceBeforeSet = false;
  }

  /**
   * Calculates the offset of the match reference (if any) in case the match
   * element has been used in the group.
   * 
   * @param elList
   *          Element list where the match element was used. It is directly changed.
   */
  private void processElement(final List<Element> elList) {
    int counter = 0;
    for (final Element elTest : elList) {
      if (elTest.getPhraseName() != null && counter > 0) {
        if (elTest.isReferenceElement()) {
          final int tokRef = elTest.getMatch().getTokenRef();
          elTest.getMatch().setTokenRef(tokRef + counter - 1);
          final String offsetToken = elTest.getString().replace("\\" + tokRef,
              "\\" + (tokRef + counter - 1));
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
    if (inRuleGroup)
      rule.setSubId(Integer.toString(subId));
    else
      rule.setSubId("1");
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

    if (category.isDefaultOff() && !defaultOn) {
      rule.setDefaultOff();
    }

  }

  private void phraseElementInit() {
    // lazy init
    if (phraseElementList == null) {
      phraseElementList = new ArrayList<ArrayList<Element>>();
    }
  }

  /**
   * Adds Match objects for all references to tokens
   * (including '\1' and the like). 
   */
  private List<Match> addLegacyMatches() {
    if (suggestionMatches == null || suggestionMatches.isEmpty()) {
      return null;
    }
    final List<Match> sugMatch = new ArrayList<Match>();
    final String messageStr = message.toString();
    int pos = 0;
    int ind = 0;
    int matchCounter = 0;
    while (pos != -1) {
      pos = messageStr.indexOf('\\', ind + 1);
      if (pos != -1 && messageStr.length() > pos) {
        if (Character.isDigit(messageStr.charAt(pos + 1))) {
          if (pos == 1 || messageStr.charAt(pos - 1) != '\u0001') {
            final Match mWorker = new Match(null, null, false, null, 
                null, Match.CaseConversion.NONE, false, Match.IncludeRange.NONE);
            mWorker.setInMessageOnly(true);
            sugMatch.add(mWorker);
          } else if (messageStr.charAt(pos - 1) == '\u0001') { // real suggestion marker
            sugMatch.add(suggestionMatches.get(matchCounter));
            message.deleteCharAt(pos - 1 - matchCounter);
            matchCounter++;
          }
        }
      }
      ind = pos;
    }
    if (sugMatch.isEmpty()) {
      return suggestionMatches;
    }
    return sugMatch;
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
    } else if (inShortMessage) {
      shortMessage.append(s);
    }
  }

}
