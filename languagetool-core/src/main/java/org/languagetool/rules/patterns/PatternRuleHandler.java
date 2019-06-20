/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ObjectUtils;
import org.languagetool.Languages;
import org.languagetool.rules.*;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PatternRuleHandler extends XMLRuleHandler {

  public static final String TYPE = "type";

  static final String MARKER_TAG = "<marker>";
  static final String RAW_TAG = "raw_pos";
  static final String PLEASE_SPELL_ME = "<pleasespellme/>";

  private static final String EXTERNAL = "external";

  protected final String sourceFile;

  protected Category category;
  protected String categoryIssueType;
  protected String ruleGroupIssueType;
  protected String ruleIssueType;
  protected String name;
  protected String filterClassName;
  protected String filterArgs;

  private final List<DisambiguationPatternRule> rulegroupAntiPatterns = new ArrayList<>();
  private final List<DisambiguationPatternRule> ruleAntiPatterns = new ArrayList<>();

  private int subId;
  private boolean interpretPosTagsPreDisambiguation;

  private boolean defaultOff;
  private boolean ruleGroupDefaultOff;

  private String ruleGroupDescription;
  private int startPos = -1;
  private int endPos = -1;
  private int tokenCountForMarker;

  private int antiPatternCounter;

  private boolean inRule;

  private boolean relaxedMode = false;
  private boolean inAntiPattern;

  private String idPrefix;

  public PatternRuleHandler() {
    this.sourceFile = null;
  }

  public PatternRuleHandler(String sourceFile) {
    this.sourceFile = sourceFile;
  }

  /**
   * If set to true, don't throw an exception if id or name is not set.
   * Used for online rule editor.
   * @since 2.1
   */
  void setRelaxedMode(boolean relaxedMode) {
    this.relaxedMode = relaxedMode;
  }

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  public void startElement(String namespaceURI, String lName,
                           String qName, Attributes attrs) throws SAXException {
    switch (qName) {
      case "category":
        String catName = attrs.getValue(NAME);
        String catId = attrs.getValue(ID);
        Category.Location location = YES.equals(attrs.getValue(EXTERNAL)) ?
                Category.Location.EXTERNAL : Category.Location.INTERNAL;
        boolean onByDefault = !OFF.equals(attrs.getValue(DEFAULT));
        String tabName = attrs.getValue(TABNAME);
        category = new Category(catId != null ? new CategoryId(catId) : null, catName, location, onByDefault, tabName);
        if (attrs.getValue(TYPE) != null) {
          categoryIssueType = attrs.getValue(TYPE);
        }
        break;
      case "rules":
        String languageStr = attrs.getValue("lang");
        idPrefix = attrs.getValue("idprefix");
        language = Languages.getLanguageForShortCode(languageStr);
        break;
      case "regexp":
        inRegex = true;
        regexMode = "exact".equals(attrs.getValue("type")) ? RegexpMode.EXACT : RegexpMode.SMART;
        regexCaseSensitive = attrs.getValue(CASE_SENSITIVE) != null && YES.equals(attrs.getValue(CASE_SENSITIVE));
        regexpMark = attrs.getValue(MARK) != null ? Integer.parseInt(attrs.getValue(MARK)) : 0;
        break;
      case RULE:
        regex = new StringBuilder();
        inRule = true;
        shortMessage = new StringBuilder();
        message = new StringBuilder();
        suggestionsOutMsg = new StringBuilder();
        url = new StringBuilder();
        id = attrs.getValue(ID);
        name = attrs.getValue(NAME);
        if (inRuleGroup) {
          subId++;
          if (id == null) {
            id = ruleGroupId;
          }
          if (name == null) {
            name = ruleGroupDescription;
          }
        }
        if (id == null) {
          throw new RuntimeException("id is null for rule with name '" + name + "'");
        }
        id = idPrefix != null ? idPrefix + id : id;

        if (inRuleGroup && ruleGroupDefaultOff && attrs.getValue(DEFAULT) != null) {
          throw new RuntimeException("Rule group " + ruleGroupId + " is off by default, thus rule " + id + " cannot specify 'default=...'");
        }
        if (inRuleGroup && ruleGroupDefaultOff) {
          defaultOff = true;
        } else {
          defaultOff = OFF.equals(attrs.getValue(DEFAULT));
        }

        correctExamples = new ArrayList<>();
        incorrectExamples = new ArrayList<>();
        errorTriggeringExamples = new ArrayList<>();
        suggestionMatches.clear();
        suggestionMatchesOutMsg.clear();
        if (attrs.getValue(TYPE) != null) {
          ruleIssueType = attrs.getValue(TYPE);
        }
        break;
      case PATTERN:
        startPattern(attrs);
        tokenCountForMarker = 0;
        interpretPosTagsPreDisambiguation = YES.equals(attrs.getValue(RAW_TAG));
        break;
      case ANTIPATTERN:
        inAntiPattern = true;
        antiPatternCounter++;
        caseSensitive = YES.equals(attrs.getValue(CASE_SENSITIVE));
        tokenCounter = 0;
        tokenCountForMarker = 0;
        break;
      case AND:
        inAndGroup = true;
        tokenCountForMarker++;
        break;
      case OR:
        inOrGroup = true;
        tokenCountForMarker++;
        break;
      case UNIFY:
        inUnification = true;
        uniNegation = YES.equals(attrs.getValue(NEGATE));
        break;
      case UNIFY_IGNORE:
        inUnificationNeutral = true;
        break;
      case FEATURE:
        uFeature = attrs.getValue(ID);
        break;
      case TYPE:
        uType = attrs.getValue(ID);
        uTypeList.add(uType);
        break;
      case TOKEN:
        setToken(attrs);
        if (!inAndGroup && !inOrGroup) {
          tokenCountForMarker++;
        }
        break;
      case EXCEPTION:
        setExceptions(attrs);
        break;
      case EXAMPLE:
        String typeVal = attrs.getValue(TYPE);
        if ("incorrect".equals(typeVal) || attrs.getValue("correction") != null) {
          inIncorrectExample = true;
          incorrectExample = new StringBuilder();
          exampleCorrection = new StringBuilder();
          if (attrs.getValue("correction") != null) {
            exampleCorrection.append(attrs.getValue("correction"));
          }
        } else if ("triggers_error".equals(typeVal)) {
          inErrorTriggerExample = true;
          errorTriggerExample = new StringBuilder();
        } else {
          // no attribute implies the sentence is a correct example
          inCorrectExample = true;
          correctExample = new StringBuilder();
        }
        break;
      case "filter":
        filterClassName = attrs.getValue("class");
        filterArgs = attrs.getValue("args");
        break;
      case MESSAGE:
        inMessage = true;
        inSuggestion = false;
        message = new StringBuilder();
        break;
      case SUGGESTION:
        if (YES.equals(attrs.getValue("suppress_misspelled"))) {
          message.append(PLEASE_SPELL_ME);
        }
        if (inMessage) {
          message.append("<suggestion>");
        } else {  //suggestions outside message
          suggestionsOutMsg.append("<suggestion>");
        }
        inSuggestion = true;
        break;
      case "short":
        if (inRule) {
          inShortMessage = true;
          shortMessage = new StringBuilder();
        } else {
          inShortMessageForRuleGroup = true;
          shortMessageForRuleGroup = new StringBuilder();
        }
        break;
      case "url":
        if (inRule) {
          inUrl = true;
          url = new StringBuilder();
        } else {
          inUrlForRuleGroup = true;
          urlForRuleGroup = new StringBuilder();
        }
        break;
      case RULEGROUP:
        ruleGroupId = attrs.getValue(ID);
        ruleGroupDescription = attrs.getValue(NAME);
        ruleGroupDefaultOff = OFF.equals(attrs.getValue(DEFAULT));
        urlForRuleGroup = new StringBuilder();
        shortMessageForRuleGroup = new StringBuilder();
        inRuleGroup = true;
        subId = 0;
        if (attrs.getValue(TYPE) != null) {
          ruleGroupIssueType = attrs.getValue(TYPE);
        }
        break;
      case MATCH:
        setMatchElement(attrs);
        break;
      case MARKER:
        if (inIncorrectExample) {
          incorrectExample.append(MARKER_TAG);
        } else if (inCorrectExample) {
          correctExample.append(MARKER_TAG);
        } else if (inErrorTriggerExample) {
          errorTriggerExample.append(MARKER_TAG);
        } else if (inPattern || inAntiPattern) {
          startPos = tokenCounter;
          inMarker = true;
        }
        break;
      case UNIFICATION:
        uFeature = attrs.getValue("feature");
        inUnificationDef = true;
        break;
      case "equivalence":
        uType = attrs.getValue(TYPE);
        break;
      case PHRASES:
        inPhrases = true;
        break;
      case "includephrases":
        break;
      case "phrase":
        if (inPhrases) {
          phraseId = attrs.getValue(ID);
        }
        break;
      case "phraseref":
        if (attrs.getValue("idref") != null) {
          preparePhrase(attrs);
          tokenCountForMarker++;
        }
        break;
    }
  }

  @Override
  public void endElement(String namespaceURI, String sName,
      String qName) throws SAXException {
    switch (qName) {
      case "category":
        categoryIssueType = null;
        break;
      case "regexp":
        inRegex = false;
        break;
      case RULE:
        suggestionMatchesOutMsg = addLegacyMatches(suggestionMatchesOutMsg, suggestionsOutMsg.toString(), false);
        if (relaxedMode && id == null) {
          id = "";
        }
        if (relaxedMode && name == null) {
          name = "";
        }
        if (phrasePatternTokens.isEmpty()) {
          // Elements contain information whether they are inside a <marker>...</marker>,
          // but for phraserefs this depends on the position where the phraseref is used
          // not where it's defined. Thus we have to copy the elements so each use of
          // the phraseref can carry their own information:

          List<PatternToken> tmpPatternTokens = new ArrayList<>();
          createRules(new ArrayList<>(patternTokens), tmpPatternTokens, 0);

        } else {
          if (!patternTokens.isEmpty()) {
            for (List<PatternToken> ph : phrasePatternTokens) {
              ph.addAll(new ArrayList<>(patternTokens));
            }
          }
          for (List<PatternToken> phrasePatternToken : phrasePatternTokens) {
            processElement(phrasePatternToken);
            List<PatternToken> tmpPatternTokens = new ArrayList<>();
            createRules(phrasePatternToken, tmpPatternTokens, 0);
          }
        }
        patternTokens.clear();
        if (phrasePatternTokens != null) {
          phrasePatternTokens.clear();
        }
        ruleIssueType = null;
        inRule = false;
        filterClassName = null;
        filterArgs = null;
        break;
      case EXCEPTION:
        finalizeExceptions();
        break;
      case AND:
        inAndGroup = false;
        andGroupCounter = 0;
        tokenCounter++;
        break;
      case OR:
        inOrGroup = false;
        orGroupCounter = 0;
        tokenCounter++;
        break;
      case TOKEN:
        finalizeTokens();
        break;
      case PATTERN:
        inPattern = false;
        if (lastPhrase) {
          patternTokens.clear();
        }
        tokenCounter = 0;
        break;
      case ANTIPATTERN:
        String antiId = id;
        if (inRuleGroup) {
          if (subId > 0) {
            antiId = ruleGroupId + "[" + subId + "]";
          } else {
            antiId = ruleGroupId;
          }
        }
        DisambiguationPatternRule rule = new DisambiguationPatternRule(
            antiId + "_antipattern:" + antiPatternCounter,
            "antipattern", language, patternTokens, null, null,
            DisambiguationPatternRule.DisambiguatorAction.IMMUNIZE);
        if (startPos != -1 && endPos != -1) {
          rule.setStartPositionCorrection(startPos);
          rule.setEndPositionCorrection(endPos - tokenCountForMarker);
        } else {
          // No '<marker>'? Then add artificial <marker>s around all tokens to work
          // around issue https://github.com/languagetool-org/languagetool/issues/189:
          for (PatternToken patternToken : patternTokens) {
            patternToken.setInsideMarker(true);
          }
        }
        patternTokens.clear();
        if (inRule) {
          ruleAntiPatterns.add(rule);
        } else { // a rulegroup shares all antipatterns not included in a single rule
          rulegroupAntiPatterns.add(rule);
        }
        tokenCounter = 0;
        inAntiPattern = false;
        break;
      case EXAMPLE:
        if (inCorrectExample) {
          correctExamples.add(new CorrectExample(correctExample.toString()));
        } else if (inIncorrectExample) {
          IncorrectExample example;
          List<String> corrections = new ArrayList<>();
          corrections.addAll(Arrays.asList(exampleCorrection.toString().split("\\|")));
          if (corrections.size() > 0) {
            if (exampleCorrection.toString().endsWith("|")) {  // split() will ignore trailing empty items
              corrections.add("");
            }
            example = new IncorrectExample(incorrectExample.toString(), corrections);
          } else {
            example = new IncorrectExample(incorrectExample.toString());
          }
          incorrectExamples.add(example);
        } else if (inErrorTriggerExample) {
          errorTriggeringExamples.add(new ErrorTriggeringExample(errorTriggerExample.toString()));
        }
        inCorrectExample = false;
        inIncorrectExample = false;
        inErrorTriggerExample = false;
        correctExample = new StringBuilder();
        incorrectExample = new StringBuilder();
        errorTriggerExample = new StringBuilder();
        exampleCorrection = new StringBuilder();
        break;
      case MESSAGE:
        suggestionMatches = addLegacyMatches(suggestionMatches, message.toString(), true);
        inMessage = false;
        break;
      case SUGGESTION:
        if (inMessage) {
          message.append("</suggestion>");
        } else { //suggestion outside message
          suggestionsOutMsg.append("</suggestion>");
        }
        inSuggestion = false;
        break;
      case "short":
        inShortMessage = false;
        inShortMessageForRuleGroup = false;
        break;
      case "url":
        inUrl = false;
        inUrlForRuleGroup = false;
        break;
      case MATCH:
        if (inMessage) {
          suggestionMatches.get(suggestionMatches.size() - 1).
              setLemmaString(match.toString());
        } else if (inSuggestion) {
          suggestionMatchesOutMsg.get(suggestionMatchesOutMsg.size() - 1).
              setLemmaString(match.toString());
        } else if (inToken) {
          tokenReference.setLemmaString(match.toString());
        }
        inMatch = false;
        break;
      case RULEGROUP:
        urlForRuleGroup = new StringBuilder();
        shortMessageForRuleGroup = new StringBuilder();
        inRuleGroup = false;
        ruleGroupIssueType = null;
        rulegroupAntiPatterns.clear();
        antiPatternCounter = 0;
        ruleGroupDefaultOff = false;
        defaultOff = false;
        break;
      case MARKER:
        if (inCorrectExample) {
          correctExample.append("</marker>");
        } else if (inIncorrectExample) {
          incorrectExample.append("</marker>");
        } else if (inErrorTriggerExample) {
          errorTriggerExample.append("</marker>");
        } else if (inPattern || inAntiPattern) {
          endPos = tokenCountForMarker;
          inMarker = false;
        }
        break;
      case "phrase":
        if (inPhrases) {
          finalizePhrase();
        }
        break;
      case "includephrases":
        patternTokens.clear();
        break;
      case PHRASES:
        if (inPhrases) {
          inPhrases = false;
        }
        break;
      case UNIFICATION:
        inUnificationDef = false;
        break;
      case FEATURE:
        equivalenceFeatures.put(uFeature, uTypeList);
        uTypeList = new ArrayList<>();
        break;
      case UNIFY:
        inUnification = false;
        //clear the features...
        equivalenceFeatures = new HashMap<>();
        //set negation on the last token only!
        int lastElement = patternTokens.size() - 1;
        patternTokens.get(lastElement).setLastInUnification();
        if (uniNegation) {
          patternTokens.get(lastElement).setUniNegation();
        }
        break;
      case UNIFY_IGNORE:
        inUnificationNeutral = false;
        break;
    }
  }

  /**
   * Create rule from an Element list.
   * In case of OR groups, several rules are created recursively.
   * @since 2.3
   * 
   * @param elemList The complete original Element list
   * @param tmpPatternTokens Temporary list being created
   * @param numElement Index of elemList being analyzed
   */
  private void createRules(List<PatternToken> elemList,
    List<PatternToken> tmpPatternTokens, int numElement) {
    String shortMessage = "";
    if (this.shortMessage != null && this.shortMessage.length() > 0) {
      shortMessage = this.shortMessage.toString();
    } else if (shortMessageForRuleGroup != null && shortMessageForRuleGroup.length() > 0) {
      shortMessage = this.shortMessageForRuleGroup.toString();
    }
    if (numElement >= elemList.size()) {
      AbstractPatternRule rule;
      if (tmpPatternTokens.size() > 0) {
        rule = new PatternRule(id, language, tmpPatternTokens, name,
                message.toString(), shortMessage,
                suggestionsOutMsg.toString(), phrasePatternTokens.size() > 1, interpretPosTagsPreDisambiguation);
        rule.setSourceFile(sourceFile);
      } else if (regex.length() > 0) {
        int flags = regexCaseSensitive ? 0 : Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE;
        String regexStr = regex.toString();
        if (regexMode == RegexpMode.SMART) {
          // Note: it's not that easy to add \b because the regex might look like '(foo)' or '\d' so we cannot just look at the last character
          regexStr = replaceSpacesInRegex(regexStr);
        }
        if (ruleAntiPatterns.size() > 0 || rulegroupAntiPatterns.size() > 0) {
          throw new RuntimeException("<regexp> rules currently cannot be used together with <antipattern>. Rule id: " + id + "[" + subId + "]");
        }
        rule = new RegexPatternRule(id, name, message.toString(), shortMessage, suggestionsOutMsg.toString(), language, Pattern.compile(regexStr, flags), regexpMark);
        rule.setSourceFile(sourceFile);
      } else {
        throw new IllegalStateException("Neither '<pattern>' tokens nor '<regex>' is set in rule '" + id + "'");
      }
      setRuleFilter(filterClassName, filterArgs, rule);
      prepareRule(rule);
      rules.add(rule);
    } else {
      PatternToken patternToken = elemList.get(numElement);
      if (patternToken.hasOrGroup()) {
        // When creating a new rule, we finally clear the backed-up variables. All the elements in
        // the OR group should share the values of backed-up variables. That's why these variables
        // are backed-up.
        List<Match> suggestionMatchesBackup = new ArrayList<>(suggestionMatches);
        List<Match> suggestionMatchesOutMsgBackup =  new ArrayList<>(suggestionMatchesOutMsg);
        int startPosBackup = startPos;
        int endPosBackup = endPos;
        List<DisambiguationPatternRule> ruleAntiPatternsBackup = new ArrayList<>(ruleAntiPatterns);
        for (PatternToken patternTokenOfOrGroup : patternToken.getOrGroup()) {
          List<PatternToken> tmpElements2 = new ArrayList<>();
          tmpElements2.addAll(tmpPatternTokens);
          tmpElements2.add(ObjectUtils.clone(patternTokenOfOrGroup));
          createRules(elemList, tmpElements2, numElement + 1);
          startPos = startPosBackup;
          endPos = endPosBackup;
          suggestionMatches = suggestionMatchesBackup;
          suggestionMatchesOutMsg = suggestionMatchesOutMsgBackup;
          ruleAntiPatterns.addAll(ruleAntiPatternsBackup);
        }
      }
      tmpPatternTokens.add(ObjectUtils.clone(patternToken));
      createRules(elemList, tmpPatternTokens, numElement + 1);
    }
  }

  String replaceSpacesInRegex(String s) {
    StringBuilder sb = new StringBuilder();
    boolean inBracket = false;
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c == '[') {
        inBracket = true;
      } else if (c == ']') {
        inBracket = false;
      }
      if (c == ' ' && !inBracket) {
        sb.append("(?:[\\s\u00A0\u202F]+)");
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  protected void prepareRule(AbstractPatternRule rule) {
    rule.setSourceFile(sourceFile);
    if (startPos != -1 && endPos != -1) {
      rule.setStartPositionCorrection(startPos);
      rule.setEndPositionCorrection(endPos - tokenCountForMarker);
    }
    startPos = -1;
    endPos = -1;
    rule.setCorrectExamples(correctExamples);
    rule.setIncorrectExamples(incorrectExamples);
    rule.setErrorTriggeringExamples(errorTriggeringExamples);
    rule.setCategory(category);
    if (!rulegroupAntiPatterns.isEmpty()) {
      rule.setAntiPatterns(rulegroupAntiPatterns);
    }
    if (!ruleAntiPatterns.isEmpty()) {
      rule.setAntiPatterns(ruleAntiPatterns);
      ruleAntiPatterns.clear();
    }
    if (inRuleGroup) {
      rule.setSubId(Integer.toString(subId));
    } else {
      rule.setSubId("1");
    }
    caseSensitive = false;
    for (Match m : suggestionMatches) {
      rule.addSuggestionMatch(m);
    }
    if (phrasePatternTokens.size() <= 1) {
      suggestionMatches.clear();
    }
    for (Match m : suggestionMatchesOutMsg) {
      rule.addSuggestionMatchOutMsg(m);
    }
    suggestionMatchesOutMsg.clear();
    if (category == null) {
      throw new RuntimeException("Cannot activate rule '" + id + "', it is outside of a <category>...</category>");
    }
    if (defaultOff) {
      rule.setDefaultOff();
    }
    if (url != null && url.length() > 0) {
      try {
        rule.setUrl(new URL(url.toString()));
      } catch (MalformedURLException e) {
        throw new RuntimeException("Could not parse URL for rule: " + rule + ": '" + url + "'", e);
      }
    } else if (urlForRuleGroup != null && urlForRuleGroup.length() > 0) {
      try {
        rule.setUrl(new URL(urlForRuleGroup.toString()));
      } catch (MalformedURLException e) {
        throw new RuntimeException("Could not parse URL for rule: " + rule + ": '" + urlForRuleGroup + "'", e);
      }
    }
    // inheritance of values - if no type value is defined for a rule, take the rule group's value etc:
    if (ruleIssueType != null) {
      rule.setLocQualityIssueType(ITSIssueType.getIssueType(ruleIssueType));
    } else if (ruleGroupIssueType != null) {
      rule.setLocQualityIssueType(ITSIssueType.getIssueType(ruleGroupIssueType));
    } else if (categoryIssueType != null) {
      rule.setLocQualityIssueType(ITSIssueType.getIssueType(categoryIssueType));
    }
  }

  @Override
  public void characters(char[] buf, int offset, int len) {
    String s = new String(buf, offset, len);
    if (inException) {
      exceptions.append(s);
    } else if (inToken) {
      elements.append(s);
    } else if (inCorrectExample) {
      correctExample.append(s);
    } else if (inIncorrectExample) {
      incorrectExample.append(s);
    } else if (inErrorTriggerExample) {
      errorTriggerExample.append(s);
    } else if (inMatch) {
      match.append(s);
    } else if (inMessage) {
      message.append(s);
    } else if (inSuggestion) {  //Suggestion outside message
      suggestionsOutMsg.append(s);
    } else if (inShortMessage) {
      shortMessage.append(s);
    } else if (inShortMessageForRuleGroup) {
      shortMessageForRuleGroup.append(s);
    } else if (inUrl) {
      url.append(s);
    } else if (inUrlForRuleGroup) {
      urlForRuleGroup.append(s);
    } else if (inRegex) {
      regex.append(s);
    }
  }

}
