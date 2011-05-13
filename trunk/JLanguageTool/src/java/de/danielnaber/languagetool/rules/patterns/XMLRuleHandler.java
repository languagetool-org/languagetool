/* LanguageTool, a natural language style checker 
 * Copyright (C) 2006 Daniel Naber (http://www.danielnaber.de)
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.IncorrectExample;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * XML rule handler that loads rules from XML and throws
 * exceptions on errors and warnings.
 * 
 * @author Daniel Naber
 */
public class XMLRuleHandler extends DefaultHandler {

  public XMLRuleHandler() {
    elementList = new ArrayList<Element>();
    equivalenceFeatures = new HashMap<String, List<String>>();
    uTypeList = new ArrayList<String>();
  }
  
  List<PatternRule> rules = new ArrayList<PatternRule>();

  protected Language language;
  
  protected StringBuilder correctExample = new StringBuilder();
  protected StringBuilder incorrectExample = new StringBuilder();
  protected StringBuilder exampleCorrection = new StringBuilder();
  protected StringBuilder message = new StringBuilder();
  protected StringBuilder match = new StringBuilder();
  protected StringBuilder elements;
  protected StringBuilder exceptions;

  List<String> correctExamples = new ArrayList<String>();
  List<IncorrectExample> incorrectExamples = new ArrayList<IncorrectExample>();

  protected boolean inPattern;
  protected boolean inCorrectExample;
  protected boolean inIncorrectExample;
  protected boolean inMessage;
  protected boolean inSuggestion;
  protected boolean inMatch;
  protected boolean inRuleGroup;
  protected boolean inToken;
  protected boolean inException;
  protected boolean inPhrases;
  protected boolean inAndGroup;

  protected boolean tokenSpaceBefore;
  protected boolean tokenSpaceBeforeSet;
  protected String posToken;
  protected boolean posNegation;
  protected boolean posRegExp;

  protected boolean caseSensitive;
  protected boolean regExpression;
  protected boolean tokenNegated;
  protected boolean tokenInflected;

  protected String exceptionPosToken;
  protected boolean exceptionStringRegExp;
  protected boolean exceptionStringNegation;
  protected boolean exceptionStringInflected;
  protected boolean exceptionPosNegation;
  protected boolean exceptionPosRegExp;
  protected boolean exceptionValidNext;
  protected boolean exceptionValidPrev;
  protected boolean exceptionSet;
  protected boolean exceptionSpaceBefore;
  protected boolean exceptionSpaceBeforeSet;

  /** List of elements as specified by tokens. **/
  protected List<Element> elementList;
  
  /** true when phraseref is the last element in the rule. **/
  protected boolean lastPhrase;

  /** ID reference to the phrase. **/
  protected String phraseIdRef;

  /** Current phrase ID. **/
  protected String phraseId;

  protected int skipPos;

  protected String ruleGroupId;

  protected String id;

  protected Element tokenElement;

  protected Match tokenReference;

  protected List<Match> suggestionMatches;

  protected Locator pLocator;

  protected int startPositionCorrection;
  protected int endPositionCorrection;
  protected int tokenCounter;

  /** Phrase store - elementLists keyed by phraseIds. **/
  protected Map<String, List<List<Element>>> phraseMap;

  /**
   * Logically forking element list, used for including multiple phrases in the
   * current one.
   **/
  protected List<ArrayList<Element>> phraseElementList;

  protected int andGroupCounter;

  protected StringBuilder shortMessage = new StringBuilder();
  protected boolean inShortMessage;

  protected boolean inUnification;
  protected boolean inUnificationDef;
  protected boolean uniNegation;

  protected String uFeature;
  protected String uType = "";

  protected List<String> uTypeList;

  protected Map<String, List<String>> equivalenceFeatures;

  
  /** Definitions of values in XML files. */
  protected static final String YES = "yes";
  protected static final String POSTAG = "postag";
  protected static final String POSTAG_REGEXP = "postag_regexp";
  protected static final String REGEXP = "regexp";
  protected static final String NEGATE = "negate";
  protected static final String INFLECTED = "inflected";
  protected static final String NEGATE_POS = "negate_pos";
  protected static final String MARKER = "marker";
  protected static final String DEFAULT = "default";
  protected static final String TYPE = "type";
  protected static final String SPACEBEFORE = "spacebefore";
  protected static final String EXAMPLE = "example";
  protected static final String SCOPE = "scope";
  protected static final String IGNORE = "ignore";
  protected static final String SKIP = "skip";
  protected static final String TOKEN = "token";
  protected static final String FEATURE = "feature";
  protected static final String UNIFY = "unify";
  protected static final String AND = "and";
  protected static final String EXCEPTION = "exception";
  protected static final String CASE_SENSITIVE = "case_sensitive";
  protected static final String PATTERN = "pattern";
  protected static final String MATCH = "match";
  protected static final String UNIFICATION = "unification";
  protected static final String RULEGROUP = "rulegroup";
  protected static final String NO = "no";
  protected static final String MARK_TO = "mark_to";
  protected static final String MARK_FROM = "mark_from";
  protected static final String PHRASES = "phrases";
  protected static final String MESSAGE = "message";


  public List<PatternRule> getRules() {
    return rules;
  }

  @Override
  public void warning (final SAXParseException e) throws SAXException {
    throw e;
  }

  @Override
  public void error (final SAXParseException e) throws SAXException {
    throw e;
  }

  @Override
  public void setDocumentLocator(final Locator locator) {
    pLocator = locator;
    super.setDocumentLocator(locator);
  }
  
  protected void resetToken() {
    posNegation = false;
    posRegExp = false;
    inToken = false;
    tokenSpaceBefore = false;
    tokenSpaceBeforeSet = false;

    resetException();
    exceptionSet = false;
    tokenReference = null;
  }

  protected void resetException() {
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

  protected void phraseElementInit() {
    // lazy init
    if (phraseElementList == null) {
      phraseElementList = new ArrayList<ArrayList<Element>>();
    }
  }
  protected void preparePhrase(final Attributes attrs) {
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

  protected void finalizePhrase() {
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
  }
  
  protected void startPattern(final Attributes attrs) throws SAXException {
    inPattern = true;
    if (attrs.getValue(MARK_FROM) != null) {
      startPositionCorrection = Integer.parseInt(attrs.getValue(MARK_FROM));
    }
    if (attrs.getValue(MARK_TO) != null) {
      endPositionCorrection = Integer.parseInt(attrs.getValue(MARK_TO));
      if (endPositionCorrection > 0) {
        throw new SAXException("End position correction (mark_to="+ endPositionCorrection
            + ") cannot be larger than 0: " + "\n Line: "
            + pLocator.getLineNumber() + ", column: "
            + pLocator.getColumnNumber() + ".");
      }
    }
    caseSensitive = YES.equals(attrs.getValue(CASE_SENSITIVE));
  }  
  
  
  /**
   * Calculates the offset of the match reference (if any) in case the match
   * element has been used in the group.
   * 
   * @param elList
   *          Element list where the match element was used. It is directly changed.
   */
  protected void processElement(final List<Element> elList) {
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

  protected void setMatchElement(final Attributes attrs) throws SAXException {
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
      elements.append('\\');
      elements.append(refNumber);
    }
  }

  protected void setExceptions(final Attributes attrs) {
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
  }

  protected void finalizeExceptions() {
    inException = false;
    if (!exceptionSet) {
      tokenElement = new Element(StringTools.trimWhitespace(elements
          .toString()), caseSensitive, regExpression, tokenInflected);
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
  }

  protected void setToken(final Attributes attrs) {
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
    regExpression = YES.equals(attrs.getValue(REGEXP));
    
    if (attrs.getValue(SPACEBEFORE) != null) {
      tokenSpaceBefore = YES.equals(attrs.getValue(SPACEBEFORE));
      tokenSpaceBeforeSet = !"ignore".equals(attrs.getValue(SPACEBEFORE));
    }

   if (!inAndGroup) {
     tokenCounter++;
   }
  }
  
  protected void checkPositions(final int add) throws SAXException {
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

  protected void checkMarkPositions() {
    if (phraseElementList == null || phraseElementList.size() == 0) {
      final int endMarker = elementList.size() + endPositionCorrection;
      if (endMarker <= startPositionCorrection) {
        throw new RuntimeException("Invalid combination of mark_from (" + startPositionCorrection
            + ") and mark_to (" + endPositionCorrection + ") for rule " + id
            + " with " + elementList.size() 
            + " tokens: the error position created by mark_from and mark_to is less than one token");
      }
    }
  }
  
  /**
   * Adds Match objects for all references to tokens
   * (including '\1' and the like). 
   */
  protected List<Match> addLegacyMatches() {
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

  protected void finalizeTokens() {
    if (!exceptionSet || tokenElement == null) {
      tokenElement = new Element(StringTools.trimWhitespace(elements
          .toString()), caseSensitive, regExpression, tokenInflected);
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
  }

}
