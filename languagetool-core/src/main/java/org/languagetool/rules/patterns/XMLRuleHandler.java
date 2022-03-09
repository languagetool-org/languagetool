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

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.CorrectExample;
import org.languagetool.rules.ErrorTriggeringExample;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.languagetool.tools.StringTools;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;
import java.util.function.Function;

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
  protected static final String PREMIUM = "premium";
  protected static final String YES = "yes";
  protected static final String OFF = "off";
  protected static final String TEMP_OFF = "temp_off";
  protected static final String ON = "on";
  protected static final String POSTAG = "postag";
  protected static final String CHUNKTAG = "chunk";
  protected static final String CHUNKTAG_REGEXP = "chunk_re";
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
  protected static final String MINPREVMATCHES = "min_prev_matches";
  protected static final String DISTANCETOKENS = "distance_tokens";

  protected List<AbstractPatternRule> rules = new ArrayList<>();
  protected Language language;

  protected StringBuilder correctExample = new StringBuilder();
  protected StringBuilder incorrectExample = new StringBuilder();
  protected StringBuilder errorTriggerExample = new StringBuilder();
  protected StringBuilder exampleCorrection = null;
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
  protected boolean isPremiumFile;
  protected boolean isPremiumCategory;
  protected boolean isPremiumRuleGroup;
  protected boolean isPremiumRule;

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

  protected void setMatchElement(Attributes attrs, boolean isSuppressMisspelled) throws SAXException {
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
        isSuppressMisspelled,
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
      exceptionPosToken = internString(attrs.getValue(POSTAG));
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
      patternToken = new PatternToken(tokenInflected, internMatcher(elements.toString().trim(), regExpression, tokenCase));
      exceptionSet = true;
    }
    patternToken.setNegation(tokenNegated);
    if (!StringTools.isEmpty(exceptions.toString()) || exceptionPosToken != null) {
      if (hasPosixCharacterClass(exceptions.toString())) {
        exceptionLevelCaseSensitive = true;
      }
      boolean caseSensitive = exceptionLevelCaseSensitive == null ? patternToken.isCaseSensitive() : exceptionLevelCaseSensitive;
      PatternToken exception = new PatternToken(exceptionStringInflected, internMatcher(exceptions.toString().trim(), exceptionStringRegExp, caseSensitive));
      exception.setNegation(exceptionStringNegation);
      exception.setPosToken(obtainPosToken(exceptionPosToken, exceptionPosRegExp, exceptionPosNegation));
      patternToken.addException(exceptionValidNext, exceptionValidPrev, exception);
      exceptionPosToken = null;
      exceptionLevelCaseSensitive = null;
    }
    if (exceptionSpaceBeforeSet) {
      patternToken.setExceptionSpaceBefore(exceptionSpaceBefore);
    }
    resetException();
  }

  // To be compatible with Java 15, we make p{Lu} and p{Ll} imply case-sensitivity,
  // see https://github.com/languagetool-org/languagetool/issues/4061
  private boolean hasPosixCharacterClass(String s) {
    boolean res = s.contains("\\p{Lu}") || s.contains("\\p{Ll}");
    if (res && s.contains("(?i")) {
      throw new RuntimeException("Contradicting regex contains both '?i' (case-insensitive) and \\p{Lu}/\\p{Ll} (case-sensitive): "
              + s + " in rule " + id);
    }
    return res;
  }

  protected void setToken(Attributes attrs) throws SAXException {
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
      posToken = internString(attrs.getValue(POSTAG));
      posRegExp = YES.equals(attrs.getValue(POSTAG_REGEXP));
      posNegation = YES.equals(attrs.getValue(NEGATE_POS));
    }
    if (attrs.getValue(CHUNKTAG) != null && attrs.getValue(CHUNKTAG_REGEXP) != null) {
      throw new SAXException("You cannot set both 'chunk' and 'chunk_re' for " + id);
    }
    if (attrs.getValue(CHUNKTAG) != null) {
      chunkTag = new ChunkTag(internString(attrs.getValue(CHUNKTAG)));
    } else if (attrs.getValue(CHUNKTAG_REGEXP) != null) {
      chunkTag = new ChunkTag(internString(attrs.getValue(CHUNKTAG_REGEXP)), true);
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

  protected void finalizeTokens(UnifierConfiguration unifierConfiguration) throws SAXException {
    if (!exceptionSet || patternToken == null) {
      boolean tokenCase = caseSensitive || hasPosixCharacterClass(elements.toString());
      if (tokenLevelCaseSet) {
        tokenCase = tokenLevelCaseSensitive;
      }
      patternToken = new PatternToken(tokenInflected, internMatcher(elements.toString().trim(), regExpression, tokenCase));
      patternToken.setNegation(tokenNegated);
    } else {
      boolean caseSensitive = patternToken.isCaseSensitive() || hasPosixCharacterClass(elements.toString());
      patternToken.setTextMatcher(internMatcher(elements.toString().trim(), patternToken.isRegularExpression(), caseSensitive));
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
      patternToken.setPosToken(obtainPosToken(posToken, posRegExp, posNegation));
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
      unifierConfiguration.setEquivalence(uFeature, uType, patternToken);
      patternTokens.clear();
    }
    if (tokenSpaceBeforeSet) {
      patternToken.setWhitespaceBefore(tokenSpaceBefore);
    }
    resetToken();
  }

  private PatternToken.PosToken obtainPosToken(String posToken, boolean regExp, boolean negated) {
    return internedPos.computeIfAbsent(Triple.of(posToken, regExp, negated), t -> {
      StringMatcher matcher = t.getMiddle() ? internMatcher(t.getLeft(), true, true) : null;
      return new PatternToken.PosToken(t.getLeft(), t.getRight(), matcher);
    });
  }

  protected static void setRuleFilter(String filterClassName, String filterArgs, AbstractPatternRule rule) {
    if (filterClassName != null && filterArgs != null) {
      if (rule instanceof RegexPatternRule) {
        RegexRuleFilterCreator creator = new RegexRuleFilterCreator();
        RegexRuleFilter filter = creator.getFilter(filterClassName);
        ((RegexPatternRule) rule).setRegexFilter(filter);
        rule.setFilterArguments(filterArgs);
      } else if (rule instanceof PatternRule || rule instanceof DisambiguationPatternRule) {
        RuleFilterCreator creator = new RuleFilterCreator();
        RuleFilter filter = creator.getFilter(filterClassName);
        rule.setFilter(filter);
        rule.setFilterArguments(filterArgs);
      } else {
        throw new RuntimeException("Rule " + rule.getFullId() + " of type " + rule.getClass() + " cannot have a filter (" + filterClassName + ")");
      }
    }
  }

  private final Map<String, String> internedStrings = new HashMap<>();

  protected String internString(String s) {
    return internedStrings.computeIfAbsent(s, Function.identity());
  }

  private final Map<Triple<String, Boolean, Boolean>, StringMatcher> internedMatchers = new HashMap<>();

  private StringMatcher internMatcher(String text, boolean regexp, boolean caseSensitive) {
    text = internString(PatternToken.normalizeTextPattern(text));
    return internedMatchers.computeIfAbsent(Triple.of(text, regexp, caseSensitive), t ->
      StringMatcher.create(t.getLeft(), t.getMiddle(), t.getRight(), this::internString));
  }

  private final Map<Triple<String, Boolean, Boolean>, PatternToken.PosToken> internedPos = new HashMap<>();

}
