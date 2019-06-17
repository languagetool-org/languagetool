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

import org.apache.commons.lang3.ObjectUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.ErrorTriggeringExample;
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
  
  enum RegexpMode {
    SMART, EXACT
  }

  public static final String ID = "id";
  public static final String NAME = "name";

  /** Definitions of values in XML files. */
  protected static final String YES = "yes";
  protected static final String OFF = "off";
  protected static final String ON = "on";
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
  protected static final String UNIFY_IGNORE = "unify-ignore";
  protected static final String AND = "and";
  protected static final String OR = "or";
  protected static final String EXCEPTION = "exception";
  protected static final String CASE_SENSITIVE = "case_sensitive";
  protected static final String MARK = "mark";
  protected static final String PATTERN = "pattern";
  protected static final String ANTIPATTERN = "antipattern";
  protected static final String MATCH = "match";
  protected static final String UNIFICATION = "unification";
  protected static final String RULE = "rule";
  protected static final String RULES = "rules";
  protected static final String RULEGROUP = "rulegroup";
  protected static final String NO = "no";
  protected static final String PHRASES = "phrases";
  protected static final String MESSAGE = "message";
  protected static final String SUGGESTION = "suggestion";
  protected static final String TABNAME = "tab";

  protected List<AbstractPatternRule> rules = new ArrayList<>();
  protected Language language;

  protected StringBuilder correctExample = new StringBuilder();
  protected StringBuilder incorrectExample = new StringBuilder();
  protected StringBuilder errorTriggerExample = new StringBuilder();
  protected StringBuilder exampleCorrection = new StringBuilder();
  protected StringBuilder message = new StringBuilder();
  protected StringBuilder suggestionsOutMsg = new StringBuilder();
  protected StringBuilder match = new StringBuilder();
  protected StringBuilder elements;
  protected StringBuilder exceptions;

  protected List<CorrectExample> correctExamples = new ArrayList<>();
  protected List<IncorrectExample> incorrectExamples = new ArrayList<>();
  protected List<ErrorTriggeringExample> errorTriggeringExamples = new ArrayList<>();

  protected boolean inPattern;
  protected boolean inCorrectExample;
  protected boolean inIncorrectExample;
  protected boolean inErrorTriggerExample;
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

  protected boolean tokenLevelCaseSensitive;
  protected boolean tokenLevelCaseSet;

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

  protected Boolean exceptionLevelCaseSensitive;
  protected boolean exceptionLevelCaseSet;

  /** List of elements as specified by tokens. */
  protected List<PatternToken> patternTokens = new ArrayList<>();

  /** true when phraseref is the last element in the rule. */
  protected boolean lastPhrase;

  /** ID reference to the phrase. */
  protected String phraseIdRef;

  /** Current phrase ID. */
  protected String phraseId;
  protected int skipPos;
  protected int minOccurrence = 1;
  protected int maxOccurrence = 1;
  protected String ruleGroupId;
  protected String id;
  protected PatternToken patternToken;
  protected Match tokenReference;
  protected List<Match> suggestionMatches = new ArrayList<>();
  protected List<Match> suggestionMatchesOutMsg = new ArrayList<>();
  protected Locator pLocator;

  protected int startPositionCorrection;
  protected int endPositionCorrection;
  protected int tokenCounter;

  /** Phrase store - elementLists keyed by phraseIds. */
  protected Map<String, List<List<PatternToken>>> phraseMap;

  /**
   * Logically forking element list, used for including multiple phrases in the
   * current one.
   */
  protected List<ArrayList<PatternToken>> phrasePatternTokens = new ArrayList<>();

  protected int andGroupCounter;
  protected int orGroupCounter;

  protected boolean inUrl;
  protected boolean inUrlForRuleGroup;
  protected StringBuilder url = new StringBuilder();
  protected StringBuilder urlForRuleGroup = new StringBuilder();
  
  protected boolean inRegex;
  protected StringBuilder regex = new StringBuilder();
  protected RegexpMode regexMode = RegexpMode.SMART;
  protected boolean regexCaseSensitive = false;
  protected int regexpMark = 0;

  protected boolean inShortMessage;
  protected boolean inShortMessageForRuleGroup;
  protected StringBuilder shortMessage = new StringBuilder();
  protected StringBuilder shortMessageForRuleGroup = new StringBuilder();

  protected boolean inUnification;
  protected boolean inMarker;
  protected boolean inUnificationDef;
  protected boolean uniNegation;
  protected boolean inUnificationNeutral;

  protected String uFeature;
  protected String uType = "";

  protected List<String> uTypeList = new ArrayList<>();
  protected Map<String, List<String>> equivalenceFeatures = new HashMap<>();

  public XMLRuleHandler() {
  }

  public List<AbstractPatternRule> getRules() {
    return rules;
  }

  @Override
  public void warning(SAXParseException e) throws SAXException {
    throw e;
  }

  @Override
  public void error(SAXParseException e) throws SAXException {
    throw e;
  }

  @Override
  public void setDocumentLocator(Locator locator) {
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

  protected void preparePhrase(Attributes attrs) {
    phraseIdRef = attrs.getValue("idref");
    if (phraseMap.containsKey(phraseIdRef)) {
      for (List<PatternToken> curPhrTokens : phraseMap.get(phraseIdRef)) {
        for (PatternToken pToken : curPhrTokens) {
          pToken.setPhraseName(phraseIdRef);
        }
        List<PatternToken> copy = ObjectUtils.clone(curPhrTokens);
        for (PatternToken patternToken : copy) {
          patternToken.setInsideMarker(inMarker);
        }
        if (patternTokens.isEmpty()) {
          phrasePatternTokens.add(new ArrayList<>(copy));
        } else {
          List<PatternToken> prevList = new ArrayList<>(patternTokens);
          prevList.addAll(copy);
          phrasePatternTokens.add(new ArrayList<>(prevList));
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
    for (PatternToken patternToken : patternTokens) {
      patternToken.setInsideMarker(inMarker);
    }
    if (phrasePatternTokens.isEmpty()) {
      phrasePatternTokens.add(new ArrayList<>(patternTokens));
    } else {
      for (List<PatternToken> ph : phrasePatternTokens) {
        ph.addAll(new ArrayList<>(patternTokens));
      }
    }

    phraseMap.put(phraseId, new ArrayList<>(phrasePatternTokens));
    patternTokens.clear();

    phrasePatternTokens.clear();
  }

  protected void startPattern(Attributes attrs) throws SAXException {
    tokenCounter = 0;
    inPattern = true;
    caseSensitive = YES.equals(attrs.getValue(CASE_SENSITIVE));
  }

  /**
   * Calculates the offset of the match reference (if any) in case the match
   * element has been used in the group.
   * 
   * @param patternTokens token list where the match element was used. It is directly changed.
   */
  protected void processElement(List<PatternToken> patternTokens) {
    int counter = 0;
    for (PatternToken pToken : patternTokens) {
        if (pToken.getPhraseName() != null && counter > 0 && pToken.isReferenceElement()) {
            int tokRef = pToken.getMatch().getTokenRef();
            pToken.getMatch().setTokenRef(tokRef + counter - 1);
            String offsetToken = pToken.getString().replace("\\" + tokRef,
                    "\\" + (tokRef + counter - 1));
            pToken.setStringElement(offsetToken);
        }
      counter++;
    }
  }

  protected void setMatchElement(Attributes attrs) throws SAXException {
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
    Match mWorker = new Match(attrs.getValue(POSTAG), attrs.getValue("postag_replace"),
        YES.equals(attrs.getValue(POSTAG_REGEXP)),
        attrs.getValue("regexp_match"), attrs.getValue("regexp_replace"),
        caseConversion, YES.equals(attrs.getValue("setpos")),
        YES.equals(attrs.getValue("suppress_misspelled")),
        includeRange);
    mWorker.setInMessageOnly(!inSuggestion);
    if (inMessage) {
      suggestionMatches.add(mWorker);
      // add incorrect XML character for simplicity
      message.append("\u0001\\");
      message.append(attrs.getValue("no"));
      checkNumber(attrs);
    } else if (inSuggestion) {
      suggestionMatchesOutMsg.add(mWorker);
      // add incorrect XML character for simplicity
      suggestionsOutMsg.append("\u0001\\");
      suggestionsOutMsg.append(attrs.getValue("no"));
      checkNumber(attrs);
    } else if (inToken && attrs.getValue("no") != null) {
      int refNumber = Integer.parseInt(attrs.getValue("no"));
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
    } else if (Integer.parseInt(attrs.getValue("no")) < 1 && regex.length() == 0) {
      throw new SAXException("References must be larger than 0: "
          + attrs.getValue("no") + "\n Line: " + pLocator.getLineNumber()
          + ", column: " + pLocator.getColumnNumber() + ".");
    }
  }

  private void checkRefNumber(int refNumber) throws SAXException {
    if (refNumber > patternTokens.size()) {
      throw new SAXException("Only backward references in match elements are possible, tried to specify token "
          + refNumber + "\n" + "Line: " + pLocator.getLineNumber()
          + ", column: " + pLocator.getColumnNumber() + ".");
    }
  }

  protected void setExceptions(Attributes attrs) {
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

    if (attrs.getValue(CASE_SENSITIVE) != null) {
      exceptionLevelCaseSet = true;
      exceptionLevelCaseSensitive = YES.equals(attrs.getValue(CASE_SENSITIVE));
    } else {
      exceptionLevelCaseSet = false;
    }
  }

  protected void finalizeExceptions() {
    inException = false;
    if (!exceptionSet) {
      boolean tokenCase = caseSensitive;
      if (tokenLevelCaseSet) {
        tokenCase = tokenLevelCaseSensitive;
      }
      patternToken = new PatternToken(elements.toString(), tokenCase, regExpression, tokenInflected);
      exceptionSet = true;
    }
    patternToken.setNegation(tokenNegated);
    if (!StringTools.isEmpty(exceptions.toString()) || exceptionPosToken != null) {
      patternToken.setStringPosException(exceptions.toString(), exceptionStringRegExp,
          exceptionStringInflected, exceptionStringNegation, exceptionValidNext, exceptionValidPrev,
          exceptionPosToken, exceptionPosRegExp, exceptionPosNegation, exceptionLevelCaseSensitive);
      exceptionPosToken = null;
      exceptionLevelCaseSensitive = null;
    }
    if (exceptionSpaceBeforeSet) {
      patternToken.setExceptionSpaceBefore(exceptionSpaceBefore);
    }
    resetException();
  }

  protected void setToken(Attributes attrs) {
    inToken = true;

    if (lastPhrase) {
      patternTokens.clear();
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

    if (attrs.getValue(CASE_SENSITIVE) != null) {
      tokenLevelCaseSet = true;
      tokenLevelCaseSensitive = YES.equals(attrs.getValue(CASE_SENSITIVE));
    } else {
      tokenLevelCaseSensitive = false;
      tokenLevelCaseSet = false;
    }
  }

  /**
   * Adds Match objects for all references to tokens
   * (including '\1' and the like).
   */
  @Nullable
  protected List<Match> addLegacyMatches(List <Match> existingSugMatches, String messageStr,
      boolean inMessage) {
    List<Match> sugMatch = new ArrayList<>();
    int pos = 0;
    int ind = 0;
    int matchCounter = 0;
    while (pos != -1) {
      pos = messageStr.indexOf('\\', ind);
      if (pos != -1 && messageStr.length() > pos && Character.isDigit(messageStr.charAt(pos + 1))) {
        if (pos == 0 || messageStr.charAt(pos - 1) != '\u0001') {
          Match mWorker = new Match(null, null, false, null,
              null, Match.CaseConversion.NONE, false, false, Match.IncludeRange.NONE);
          mWorker.setInMessageOnly(true);
          sugMatch.add(mWorker);
        } else if (messageStr.charAt(pos - 1) == '\u0001') { // real suggestion marker
          sugMatch.add(existingSugMatches.get(matchCounter));
          if (inMessage) {
            message.deleteCharAt(pos - 1 - matchCounter);
          } else {
            suggestionsOutMsg.deleteCharAt(pos - 1 - matchCounter);
          }
          matchCounter++;
        }
      }
      ind = pos + 1;
    }

    if (sugMatch.isEmpty()) {
      return existingSugMatches;
    }
    return sugMatch;
  }

  protected void finalizeTokens() throws SAXException {
    if (!exceptionSet || patternToken == null) {
      boolean tokenCase = caseSensitive;
      if (tokenLevelCaseSet) {
        tokenCase = tokenLevelCaseSensitive;
      }
      patternToken = new PatternToken(elements.toString(),
          tokenCase, regExpression, tokenInflected);
      patternToken.setNegation(tokenNegated);
    } else {
      patternToken.setStringElement(elements.toString());
    }
    if (skipPos != 0) {
      patternToken.setSkipNext(skipPos);
      skipPos = 0;
    }
    if (minOccurrence == 0) {
      patternToken.setMinOccurrence(0);
    }
    if (maxOccurrence != 1) {
      patternToken.setMaxOccurrence(maxOccurrence);
      maxOccurrence = 1;
    }
    if (posToken != null) {
      patternToken.setPosToken(new PatternToken.PosToken(posToken, posRegExp, posNegation));
      posToken = null;
    }
    if (chunkTag != null) {
      patternToken.setChunkTag(chunkTag);
      chunkTag = null;
    }
    if (tokenReference != null) {
      patternToken.setMatch(tokenReference);
    }
    if (inAndGroup && andGroupCounter > 0) {
      patternTokens.get(patternTokens.size() - 1).setAndGroupElement(patternToken);
        if (minOccurrence !=1 || maxOccurrence !=1) {
            throw new SAXException("Please set min and max attributes on the " +
                    "first token in the AND group.\n You attempted to set these " +
                    "attributes on the token no. " + (andGroupCounter + 1) + "." + "\n Line: "
                    + pLocator.getLineNumber() + ", column: "
                    + pLocator.getColumnNumber() + ".");
        }
    } else if (inOrGroup && orGroupCounter > 0) {
      patternTokens.get(patternTokens.size() - 1).setOrGroupElement(patternToken);
    } else {
      if (minOccurrence < 1) {
        patternTokens.add(patternToken);
      }
      for (int i = 1; i <= minOccurrence; i ++) {
        patternTokens.add(patternToken);
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
      patternToken.setUnification(equivalenceFeatures);
    }
    if (inUnificationNeutral) {
      patternToken.setUnificationNeutral();
    }
    patternToken.setInsideMarker(inMarker);
    if (inUnificationDef) {
      language.getUnifierConfiguration().setEquivalence(uFeature, uType, patternToken);
      patternTokens.clear();
    }
    if (tokenSpaceBeforeSet) {
      patternToken.setWhitespaceBefore(tokenSpaceBefore);
    }
    resetToken();
  }
  
  protected void setRuleFilter(String filterClassName, String filterArgs, AbstractPatternRule rule) {
    if (filterClassName != null && filterArgs != null) {
      RuleFilterCreator creator = new RuleFilterCreator();
      RuleFilter filter = creator.getFilter(filterClassName);
      rule.setFilter(filter);
      rule.setFilterArguments(filterArgs);
    }
  }

}
