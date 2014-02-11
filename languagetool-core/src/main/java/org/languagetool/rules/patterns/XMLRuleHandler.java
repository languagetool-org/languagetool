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
package org.languagetool.rules.patterns;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.ObjectUtils;
import org.languagetool.Language;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.tools.StringTools;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XML rule handler that loads rules from XML and throws
 * exceptions on errors and warnings.
 * 
 * @author Daniel Naber
 */
public class XMLRuleHandler extends DefaultHandler {

  protected List<PatternRule> rules = new ArrayList<>();
  protected Language language;

  protected StringBuilder correctExample = new StringBuilder();
  protected StringBuilder incorrectExample = new StringBuilder();
  protected StringBuilder exampleCorrection = new StringBuilder();
  protected StringBuilder message = new StringBuilder();
  protected StringBuilder suggestionsOutMsg = new StringBuilder();
  protected StringBuilder match = new StringBuilder();
  protected StringBuilder elements;
  protected StringBuilder exceptions;

  protected List<String> correctExamples = new ArrayList<>();
  protected List<IncorrectExample> incorrectExamples = new ArrayList<>();

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
  protected boolean inOrGroup;

  protected boolean tokenSpaceBefore;
  protected boolean tokenSpaceBeforeSet;
  protected String posToken;
  protected ChunkTag chunkTag;
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
  protected int minOccurrence = 1;
  protected int maxOccurrence = 1;
  protected String ruleGroupId;
  protected String id;
  protected Element tokenElement;
  protected Match tokenReference;
  protected List<Match> suggestionMatches;
  protected List<Match> suggestionMatchesOutMsg;
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
  protected int orGroupCounter;

  protected StringBuilder shortMessage = new StringBuilder();
  protected StringBuilder url = new StringBuilder();
  protected boolean inShortMessage;
  protected boolean inUrl;

  protected boolean inUnification;
  protected boolean inMarker;
  protected boolean inUnificationDef;
  protected boolean uniNegation;

  protected String uFeature;
  protected String uType = "";

  protected List<String> uTypeList;

  protected Map<String, List<String>> equivalenceFeatures;

  /** Definitions of values in XML files. */
  protected static final String YES = "yes";
  protected static final String POSTAG = "postag";
  protected static final String CHUNKTAG = "chunk";
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
  protected static final String MIN = "min";
  protected static final String MAX = "max";
  protected static final String TOKEN = "token";
  protected static final String FEATURE = "feature";
  protected static final String UNIFY = "unify";
  protected static final String AND = "and";
  protected static final String OR = "or";
  protected static final String EXCEPTION = "exception";
  protected static final String CASE_SENSITIVE = "case_sensitive";
  protected static final String PATTERN = "pattern";
  protected static final String MATCH = "match";
  protected static final String UNIFICATION = "unification";
  protected static final String RULE = "rule";
  protected static final String RULEGROUP = "rulegroup";
  protected static final String NO = "no";
  protected static final String PHRASES = "phrases";
  protected static final String MESSAGE = "message";
  protected static final String SUGGESTION = "suggestion";

  public XMLRuleHandler() {
    elementList = new ArrayList<>();
    equivalenceFeatures = new HashMap<>();
    uTypeList = new ArrayList<>();
  }

  public List<PatternRule> getRules() {
    return rules;
  }

  @Override
  public void warning(final SAXParseException e) throws SAXException {
    throw e;
  }

  @Override
  public void error(final SAXParseException e) throws SAXException {
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
      phraseElementList = new ArrayList<>();
    }
  }

  protected void preparePhrase(final Attributes attrs) {
    phraseIdRef = attrs.getValue("idref");
    if (phraseMap.containsKey(phraseIdRef)) {
      for (final List<Element> curPhrEl : phraseMap.get(phraseIdRef)) {
        for (final Element e : curPhrEl) {
          e.setPhraseName(phraseIdRef);
        }
        final List<Element> copy = (List<Element>) ObjectUtils.clone(curPhrEl);
        for (Element element : copy) {
          element.setInsideMarker(inMarker);
        }
        if (elementList.isEmpty()) {
          phraseElementList.add(new ArrayList<>(copy));
        } else {
          final ArrayList<Element> prevList = new ArrayList<>(elementList);
          prevList.addAll(copy);
          phraseElementList.add(new ArrayList<>(prevList));
          prevList.clear();
        }
      }
      lastPhrase = true;
    }
  }

  protected void finalizePhrase() {
    // lazy init
    if (phraseMap == null) {
      phraseMap = new HashMap<>();
    }
    phraseElementInit();
    for (Element element : elementList) {
      element.setInsideMarker(inMarker);
    }
    if (phraseElementList.isEmpty()) {
      phraseElementList.add(new ArrayList<>(elementList));
    } else {
      for (final ArrayList<Element> ph : phraseElementList) {
        ph.addAll(new ArrayList<>(elementList));
      }
    }

    phraseMap.put(phraseId, new ArrayList<List<Element>>(phraseElementList));
    elementList.clear();

    phraseElementList.clear();
  }

  protected void startPattern(final Attributes attrs) throws SAXException {
    tokenCounter = 0;
    inPattern = true;
    caseSensitive = YES.equals(attrs.getValue(CASE_SENSITIVE));
  }

  /**
   * Calculates the offset of the match reference (if any) in case the match
   * element has been used in the group.
   * 
   * @param elList Element list where the match element was used. It is directly changed.
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
      caseConversion = Match.CaseConversion.valueOf(attrs
          .getValue("case_conversion").toUpperCase(Locale.ENGLISH));
    }
    Match.IncludeRange includeRange = Match.IncludeRange.NONE;
    if (attrs.getValue("include_skipped") != null) {
      includeRange = Match.IncludeRange.valueOf(attrs
          .getValue("include_skipped").toUpperCase(Locale.ENGLISH));
    }
    final Match mWorker = new Match(attrs.getValue(POSTAG), attrs.getValue("postag_replace"),
        YES.equals(attrs.getValue(POSTAG_REGEXP)),
        attrs.getValue("regexp_match"), attrs.getValue("regexp_replace"),
        caseConversion, YES.equals(attrs.getValue("setpos")),
        YES.equals(attrs.getValue("suppress_misspelled")),
        includeRange);
    mWorker.setInMessageOnly(!inSuggestion);
    if (inMessage) {
      if (suggestionMatches == null) {
        suggestionMatches = new ArrayList<>();
      }
      suggestionMatches.add(mWorker);
      // add incorrect XML character for simplicity
      message.append("\u0001\\");
      message.append(attrs.getValue("no"));
      checkNumber(attrs);
    } else if (inSuggestion) {
      if (suggestionMatchesOutMsg == null) {
        suggestionMatchesOutMsg = new ArrayList<>();
      }
      suggestionMatchesOutMsg.add(mWorker);
      // add incorrect XML character for simplicity
      suggestionsOutMsg.append("\u0001\\");
      suggestionsOutMsg.append(attrs.getValue("no"));
      checkNumber(attrs);
    } else if (inToken && attrs.getValue("no") != null) {
      final int refNumber = Integer.parseInt(attrs.getValue("no"));
      checkRefNumber(refNumber);
      mWorker.setTokenRef(refNumber);
      tokenReference = mWorker;
      elements.append('\\');
      elements.append(refNumber);
    }
  }

  private void checkNumber(Attributes attrs) throws SAXException {
    if (StringTools.isEmpty(attrs.getValue("no"))) {
      throw new SAXException("References cannot be empty: " + "\n Line: "
          + pLocator.getLineNumber() + ", column: "
          + pLocator.getColumnNumber() + ".");
    } else if (Integer.parseInt(attrs.getValue("no")) < 1) {
      throw new SAXException("References must be larger than 0: "
          + attrs.getValue("no") + "\n Line: " + pLocator.getLineNumber()
          + ", column: " + pLocator.getColumnNumber() + ".");
    }
  }

  private void checkRefNumber(int refNumber) throws SAXException {
    if (refNumber > elementList.size()) {
      throw new SAXException("Only backward references in match elements are possible, tried to specify token "
          + refNumber + "\n" + "Line: " + pLocator.getLineNumber()
          + ", column: " + pLocator.getColumnNumber() + ".");
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
      exceptionSpaceBeforeSet = !IGNORE.equals(attrs.getValue(SPACEBEFORE));
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
    if (!StringTools.isEmpty(exceptions.toString()) || exceptionPosToken != null) {
      tokenElement.setStringPosException(StringTools.trimWhitespace(exceptions
          .toString()), exceptionStringRegExp, exceptionStringInflected,
          exceptionStringNegation, exceptionValidNext, exceptionValidPrev,
          exceptionPosToken, exceptionPosRegExp, exceptionPosNegation);
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
    if (attrs.getValue(SKIP) != null) {
      skipPos = Integer.parseInt(attrs.getValue(SKIP));
    }
    if (attrs.getValue(MIN) != null) {
      minOccurrence = Integer.parseInt(attrs.getValue(MIN));
    }
    if (attrs.getValue(MAX) != null) {
      maxOccurrence = Integer.parseInt(attrs.getValue(MAX));
    }
    elements = new StringBuilder();
    // POSElement creation
    if (attrs.getValue(POSTAG) != null) {
      posToken = attrs.getValue(POSTAG);
      posRegExp = YES.equals(attrs.getValue(POSTAG_REGEXP));
      posNegation = YES.equals(attrs.getValue(NEGATE_POS));
    }
    if (attrs.getValue(CHUNKTAG) != null) {
      chunkTag = new ChunkTag(attrs.getValue(CHUNKTAG));
    }
    regExpression = YES.equals(attrs.getValue(REGEXP));

    if (attrs.getValue(SPACEBEFORE) != null) {
      tokenSpaceBefore = YES.equals(attrs.getValue(SPACEBEFORE));
      tokenSpaceBeforeSet = !IGNORE.equals(attrs.getValue(SPACEBEFORE));
    }

    if (!inAndGroup && !inOrGroup) {
      tokenCounter++;
    }
  }

  /**
   * Adds Match objects for all references to tokens
   * (including '\1' and the like).
   */
  protected List<Match> addLegacyMatches(final List <Match> existingSugMatches, final String messageStr,
      boolean inMessage) {
    if (existingSugMatches == null || existingSugMatches.isEmpty()) {
      return null;
    }
    final List<Match> sugMatch = new ArrayList<>();
    //final String messageStr = message.toString();
    int pos = 0;
    int ind = 0;
    int matchCounter = 0;
    while (pos != -1) {
      pos = messageStr.indexOf('\\', ind + 1);
      if (pos != -1 && messageStr.length() > pos)              {
        if (Character.isDigit(messageStr.charAt(pos + 1))) {
          if (pos == 0 || messageStr.charAt(pos - 1) != '\u0001') {
            final Match mWorker = new Match(null, null, false, null,
                null, Match.CaseConversion.NONE, false, false, Match.IncludeRange.NONE);
            mWorker.setInMessageOnly(true);
            sugMatch.add(mWorker);
          } else if (messageStr.charAt(pos - 1) == '\u0001') { // real suggestion marker
            sugMatch.add(existingSugMatches.get(matchCounter));
            if (inMessage) {
              message.deleteCharAt(pos - 1 - matchCounter);
            }
            else {
              suggestionsOutMsg.deleteCharAt(pos - 1 - matchCounter);
            }
            matchCounter++;
          }
        }
      }
      ind = pos;
    }

    if (sugMatch.isEmpty()) {
      return existingSugMatches;
    }
    return sugMatch;
  }

  protected void finalizeTokens() throws SAXException {
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

    if (minOccurrence == 0) {
      tokenElement.setMinOccurrence(0);
    }
    if (maxOccurrence != 1) {
      tokenElement.setMaxOccurrence(maxOccurrence);
      maxOccurrence = 1;
    }
    if (posToken != null) {
      tokenElement.setPosElement(posToken, posRegExp, posNegation);
      posToken = null;
    }
    if (chunkTag != null) {
      tokenElement.setChunkElement(chunkTag);
      chunkTag = null;
    }

    if (tokenReference != null) {
      tokenElement.setMatch(tokenReference);
    }

    if (inAndGroup && andGroupCounter > 0) {
      elementList.get(elementList.size() - 1).setAndGroupElement(tokenElement);
        if (minOccurrence !=1 || maxOccurrence !=1) {
            throw new SAXException("Please set min and max attributes on the " +
                    "first token in the AND group.\n You attempted to set these " +
                    "attributes on the token no. " + (andGroupCounter + 1) + "." + "\n Line: "
                    + pLocator.getLineNumber() + ", column: "
                    + pLocator.getColumnNumber() + ".");
        }
    } else if (inOrGroup && orGroupCounter > 0) {
      elementList.get(elementList.size() - 1).setOrGroupElement(tokenElement);
    } else {
      if (minOccurrence < 1) {
        elementList.add(tokenElement);
      }
      for (int i = 1; i <= minOccurrence; i ++) {
        elementList.add(tokenElement);
      }
      minOccurrence = 1;
    }
    if (inAndGroup) {
      andGroupCounter++;
    }
    if (inOrGroup) {
      orGroupCounter++;
    }

    if (inUnification) {
      tokenElement.setUnification(equivalenceFeatures);
    }

    tokenElement.setInsideMarker(inMarker);

    if (inUnificationDef) {
      language.getUnifierConfiguration().setEquivalence(uFeature, uType, tokenElement);
      elementList.clear();
    }
    if (tokenSpaceBeforeSet) {
      tokenElement.setWhitespaceBefore(tokenSpaceBefore);
    }
    resetToken();
  }

}
