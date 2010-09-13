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
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.rules.IncorrectExample;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * XML rule handler that loads rules from XML and throws
 * exceptions on errors and warnings.
 * 
 * @author Daniel Naber
 */
public class XMLRuleHandler extends DefaultHandler {

  List<PatternRule> rules = new ArrayList<PatternRule>();

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

  protected int skipPos;

  protected String ruleGroupId;

  protected String id;

  protected Element tokenElement;

  protected Match tokenReference;

  protected List<Match> suggestionMatches;

  protected Locator pLocator;

  /** Defines "yes" value in XML files. */
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

  public List<PatternRule> getRules() {
    return rules;
  }

  public void warning (final SAXParseException e) throws SAXException {
    throw e;
  }

  public void error (final SAXParseException e) throws SAXException {
    throw e;
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
      elements.append("\\");
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

  
}
