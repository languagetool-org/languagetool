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
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.ObjectUtils;
import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PatternRuleHandler extends XMLRuleHandler {

  public static final String TYPE = "type";
  
  static final String MARKER_TAG = "<marker>";
  static final String PLEASE_SPELL_ME = "<pleasespellme/>";

  protected Category category;
  protected String categoryIssueType;
  protected String ruleGroupIssueType;
  protected String ruleIssueType;
  protected String name;
  protected String filterClassName;
  protected String filterArgs;

  private int subId;

  private boolean defaultOff;
  private boolean defaultOn;

  private String ruleGroupDescription;
  private int startPos = -1;
  private int endPos = -1;
  private int tokenCountForMarker = 0;

  private int antiPatternCounter = 0;

  private boolean inRule;

  private List<DisambiguationPatternRule> rulegroupAntiPatterns;
  private List<DisambiguationPatternRule> ruleAntiPatterns;

  private boolean relaxedMode = false;
  private boolean inAntiPattern;

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
  public void startElement(final String namespaceURI, final String lName,
                           final String qName, final Attributes attrs) throws SAXException {
    switch (qName) {
      case "category":
        final String catName = attrs.getValue(NAME);
        final String priorityStr = attrs.getValue("priority");
        if (priorityStr == null) {
          category = new Category(catName);
        } else {
          category = new Category(catName, Integer.parseInt(priorityStr));
        }
        if ("off".equals(attrs.getValue(DEFAULT))) {
          category.setDefaultOff();
        }
        if (attrs.getValue(TYPE) != null) {
          categoryIssueType = attrs.getValue(TYPE);
        }
        break;
      case "rules":
        final String languageStr = attrs.getValue("lang");
        language = Language.getLanguageForShortName(languageStr);
        break;
      case RULE:
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

        if (!(inRuleGroup && defaultOff)) {
          defaultOff = "off".equals(attrs.getValue(DEFAULT));
        }
        if (!(inRuleGroup && defaultOn)) {
          defaultOn = "on".equals(attrs.getValue(DEFAULT));
        }

        correctExamples = new ArrayList<>();
        incorrectExamples = new ArrayList<>();
        if (suggestionMatches != null) {
          suggestionMatches.clear();
        }
        if (suggestionMatchesOutMsg != null) {
          suggestionMatchesOutMsg.clear();
        }
        if (attrs.getValue(TYPE) != null) {
          ruleIssueType = attrs.getValue(TYPE);
        }
        break;
      case PATTERN:
        startPattern(attrs);
        tokenCountForMarker = 0;
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
        if (attrs.getValue(TYPE).equals("correct")) {
          inCorrectExample = true;
          correctExample = new StringBuilder();
        } else if (attrs.getValue(TYPE).equals("incorrect")) {
          inIncorrectExample = true;
          incorrectExample = new StringBuilder();
          exampleCorrection = new StringBuilder();
          if (attrs.getValue("correction") != null) {
            exampleCorrection.append(attrs.getValue("correction"));
          }
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
        inShortMessage = true;
        shortMessage = new StringBuilder();
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
        defaultOff = "off".equals(attrs.getValue(DEFAULT));
        defaultOn = "on".equals(attrs.getValue(DEFAULT));
        urlForRuleGroup = new StringBuilder();
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
        phraseElementInit();
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
  public void endElement(final String namespaceURI, final String sName,
      final String qName) throws SAXException {
    switch (qName) {
      case "category":
        categoryIssueType = null;
        break;
      case RULE:
        suggestionMatchesOutMsg = addLegacyMatches(suggestionMatchesOutMsg, suggestionsOutMsg.toString(), false);
        phraseElementInit();
        if (relaxedMode && id == null) {
          id = "";
        }
        if (relaxedMode && name == null) {
          name = "";
        }
        if (phraseElementList.isEmpty()) {
          // Elements contain information whether they are inside a <marker>...</marker>,
          // but for phraserefs this depends on the position where the phraseref is used
          // not where it's defined. Thus we have to copy the elements so each use of
          // the phraseref can carry their own information:

          final List<Element> tmpElements = new ArrayList<>();
          createRules(new ArrayList<>(elementList), tmpElements, 0);

        } else {
          if (!elementList.isEmpty()) {
            for (List<Element> ph : phraseElementList) {
              ph.addAll(new ArrayList<>(elementList));
            }
          }
          for (List<Element> phraseElement : phraseElementList) {
            processElement(phraseElement);
            final List<Element> tmpElements = new ArrayList<>();
            createRules(phraseElement, tmpElements, 0);
          }
        }
        elementList.clear();
        if (phraseElementList != null) {
          phraseElementList.clear();
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
          elementList.clear();
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
        final DisambiguationPatternRule rule = new DisambiguationPatternRule(
            antiId + "_antipattern:" + antiPatternCounter,
            "antipattern", language, elementList, null, null,
            DisambiguationPatternRule.DisambiguatorAction.IMMUNIZE);
        if (startPos != -1 && endPos != -1) {
          rule.setStartPositionCorrection(startPos);
          rule.setEndPositionCorrection(endPos - tokenCountForMarker);
        }
        elementList.clear();
        if (inRule) {
          if (ruleAntiPatterns == null) {
            ruleAntiPatterns = new ArrayList<>();
          }
          ruleAntiPatterns.add(rule);
        } else { // a rulegroup shares all antipatterns not included in a single rule
          if (rulegroupAntiPatterns == null) {
            rulegroupAntiPatterns = new ArrayList<>();
          }
          rulegroupAntiPatterns.add(rule);
        }
        tokenCounter = 0;
        inAntiPattern = false;
        break;
      case EXAMPLE:
        if (inCorrectExample) {
          correctExamples.add(correctExample.toString());
        } else if (inIncorrectExample) {
          final IncorrectExample example;
          final String[] corrections = exampleCorrection.toString().split("\\|");
          if (corrections.length > 0 && corrections[0].length() > 0) {
            example = new IncorrectExample(incorrectExample.toString(), corrections);
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
        inRuleGroup = false;
        ruleGroupIssueType = null;
        if (rulegroupAntiPatterns != null) {
          rulegroupAntiPatterns.clear();
        }
        antiPatternCounter = 0;
        break;
      case MARKER:
        if (inCorrectExample) {
          correctExample.append("</marker>");
        } else if (inIncorrectExample) {
          incorrectExample.append("</marker>");
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
        elementList.clear();
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
        final int lastElement = elementList.size() - 1;
        elementList.get(lastElement).setLastInUnification();
        if (uniNegation) {
          elementList.get(lastElement).setUniNegation();
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
   * @param tmpElements Temporary Element list being created
   * @param numElement Index of elemList being analyzed
   */
  private void createRules(List<Element> elemList,
      List<Element> tmpElements, int numElement) {
    if (numElement >= elemList.size()) {
      final PatternRule rule = new PatternRule(id, language, tmpElements, name,
          message.toString(), shortMessage.toString(),
          suggestionsOutMsg.toString(), phraseElementList.size() > 1);
      if (filterClassName != null && filterArgs != null) {
        RuleFilterCreator creator = new RuleFilterCreator();
        RuleFilter filter = creator.getFilter(filterClassName);
        rule.setFilter(filter);
        rule.setFilterArguments(filterArgs);
      }
      prepareRule(rule);
      rules.add(rule);
    } else {
      Element element = elemList.get(numElement);
      if (element.hasOrGroup()) {
        for (Element elementOfOrGroup : element.getOrGroup()) {
          final List<Element> tmpElements2 = new ArrayList<>();
          tmpElements2.addAll(tmpElements);
          tmpElements2.add((Element) ObjectUtils.clone(elementOfOrGroup));
          createRules(elemList, tmpElements2, numElement + 1);
        }
      }
      tmpElements.add((Element) ObjectUtils.clone(element));
      createRules(elemList, tmpElements, numElement + 1);
    }
  }

  protected void prepareRule(final PatternRule rule) {
    if (startPos != -1 && endPos != -1) {
      rule.setStartPositionCorrection(startPos);
      rule.setEndPositionCorrection(endPos - tokenCountForMarker);
    }
    startPos = -1;
    endPos = -1;
    rule.setCorrectExamples(correctExamples);
    rule.setIncorrectExamples(incorrectExamples);
    rule.setCategory(category);
    if (rulegroupAntiPatterns != null && !rulegroupAntiPatterns.isEmpty()) {
      rule.setAntiPatterns(rulegroupAntiPatterns);
    }
    if (ruleAntiPatterns != null && !ruleAntiPatterns.isEmpty()) {
      rule.setAntiPatterns(ruleAntiPatterns);
      ruleAntiPatterns.clear();
    }
    if (inRuleGroup) {
      rule.setSubId(Integer.toString(subId));
    } else {
      rule.setSubId("1");
    }
    caseSensitive = false;
    if (suggestionMatches != null) {
      for (final Match m : suggestionMatches) {
        rule.addSuggestionMatch(m);
      }
      if (phraseElementList.size() <= 1) {
        suggestionMatches.clear();
      }
    }
    if (suggestionMatchesOutMsg != null) {
      for (final Match m : suggestionMatchesOutMsg) {
        rule.addSuggestionMatchOutMsg(m);
      }
      suggestionMatchesOutMsg.clear();
    }
    if (defaultOff) {
      rule.setDefaultOff();
    }
    if (category == null) {
      throw new RuntimeException("Cannot activate rule '" + id + "', it is outside of a <category>...</category>");
    }
    if (category.isDefaultOff() && !defaultOn) {
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
    } else if (inSuggestion) {  //Suggestion outside message
      suggestionsOutMsg.append(s);
    } else if (inShortMessage) {
      shortMessage.append(s);
    } else if (inUrl) {
      url.append(s);
    } else if (inUrlForRuleGroup) {
      urlForRuleGroup.append(s);
    }
  }

}
