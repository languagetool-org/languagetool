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

import org.languagetool.Language;
import org.languagetool.rules.Category;
import org.languagetool.rules.IncorrectExample;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class PatternRuleHandler extends XMLRuleHandler {

  private int subId;

  private boolean defaultOff;
  private boolean defaultOn;

  protected Category category;
  protected String categoryIssueType;
  protected String ruleGroupIssueType;
  protected String ruleIssueType;
  protected String name;
  private String ruleGroupDescription;
  private int startPos = -1;
  private int endPos = -1;
  private int tokenCountForMarker = 0;

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  public void startElement(final String namespaceURI, final String lName,
      final String qName, final Attributes attrs) throws SAXException {
    if ("category".equals(qName)) {
      final String catName = attrs.getValue("name");
      final String priorityStr = attrs.getValue("priority");
      if (priorityStr == null) {
        category = new Category(catName);
      } else {
        category = new Category(catName, Integer.parseInt(priorityStr));
      }
      if ("off".equals(attrs.getValue(DEFAULT))) {
        category.setDefaultOff();
      }
      if (attrs.getValue("type") != null) {
        categoryIssueType = attrs.getValue("type");
      }
    } else if ("rules".equals(qName)) {
      final String languageStr = attrs.getValue("lang");
      language = Language.getLanguageForShortName(languageStr);
    } else if (RULE.equals(qName)) {
      shortMessage = new StringBuilder();
      message = new StringBuilder();
      suggestionsOutMsg = new StringBuilder();
      url = new StringBuilder();
      id = attrs.getValue("id");
      if (inRuleGroup) {
        subId++;
      }
      if (!(inRuleGroup && defaultOff)) {
        defaultOff = "off".equals(attrs.getValue(DEFAULT));
      }
      if (!(inRuleGroup && defaultOn)) {
        defaultOn = "on".equals(attrs.getValue(DEFAULT));
      }
      if (inRuleGroup && id == null) {
        id = ruleGroupId;
      }
      name = attrs.getValue("name");
      if (inRuleGroup && name == null) {
        name = ruleGroupDescription;
      }
      correctExamples = new ArrayList<String>();
      incorrectExamples = new ArrayList<IncorrectExample>();
      if (suggestionMatches != null) {
        suggestionMatches.clear();
      }
      if (suggestionMatchesOutMsg != null) {
        suggestionMatchesOutMsg.clear();
      }
      if (attrs.getValue("type") != null) {
        ruleIssueType = attrs.getValue("type");
      }
    } else if (PATTERN.equals(qName)) {
      startPattern(attrs);
      tokenCountForMarker = 0;
    } else if (AND.equals(qName)) {
      inAndGroup = true;
      tokenCountForMarker++;
    } else if (UNIFY.equals(qName)) {
      inUnification = true;
      uniNegation = YES.equals(attrs.getValue(NEGATE));
    } else if (FEATURE.equals(qName)) {
      uFeature = attrs.getValue("id");
    } else if (TYPE.equals(qName)) {
      uType = attrs.getValue("id");
      uTypeList.add(uType);
    } else if (TOKEN.equals(qName)) {
      setToken(attrs);
      if (!inAndGroup) {
        tokenCountForMarker++;
      }
    } else if (EXCEPTION.equals(qName)) {
      setExceptions(attrs);
    } else if (EXAMPLE.equals(qName) && attrs.getValue(TYPE).equals("correct")) {
      inCorrectExample = true;
      correctExample = new StringBuilder();
    } else if (EXAMPLE.equals(qName) && attrs.getValue(TYPE).equals("incorrect")) {
      inIncorrectExample = true;
      incorrectExample = new StringBuilder();
      exampleCorrection = new StringBuilder();
      if (attrs.getValue("correction") != null) {
        exampleCorrection.append(attrs.getValue("correction"));
      }
    } else if (MESSAGE.equals(qName)) {
      inMessage = true;
      inSuggestion = false;
      message = new StringBuilder();
    } else if ("suggestion".equals(qName) && !inMessage) {  //suggestions outside message      
        if (YES.equals(attrs.getValue("suppress_misspelled"))) {
          suggestionsOutMsg.append("<pleasespellme/>");
        }
        suggestionsOutMsg.append("<suggestion>");
        inSuggestion = true;
    } else if ("short".equals(qName)) {
      inShortMessage = true;
      shortMessage = new StringBuilder();
    } else if ("url".equals(qName)) {
      inUrl = true;
      url = new StringBuilder();
    } else if (RULEGROUP.equals(qName)) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupDescription = attrs.getValue("name");
      defaultOff = "off".equals(attrs.getValue(DEFAULT));
      defaultOn = "on".equals(attrs.getValue(DEFAULT));
      inRuleGroup = true;
      subId = 0;
      if (attrs.getValue("type") != null) {
        ruleGroupIssueType = attrs.getValue("type");
      }
    } else if ("suggestion".equals(qName) && inMessage) {      
      if (YES.equals(attrs.getValue("suppress_misspelled"))) {
        message.append("<pleasespellme/>");
      }
      message.append("<suggestion>");
      inSuggestion = true;
    } else if (MATCH.equals(qName)) {
      setMatchElement(attrs);
    } else if (MARKER.equals(qName) && inCorrectExample) {
      correctExample.append("<marker>");
    } else if (MARKER.equals(qName) && inIncorrectExample) {
      incorrectExample.append("<marker>");
    } else if (MARKER.equals(qName) && inPattern) {
      startPos = tokenCounter;
    } else if (UNIFICATION.equals(qName)) {
      uFeature = attrs.getValue("feature");
      inUnificationDef = true;
    } else if ("equivalence".equals(qName)) {
      uType = attrs.getValue(TYPE);
    } else if (PHRASES.equals(qName)) {
      inPhrases = true;
    } else if ("includephrases".equals(qName)) {
      phraseElementInit();
    } else if ("phrase".equals(qName) && inPhrases) {
      phraseId = attrs.getValue("id");
    } else if ("phraseref".equals(qName) && (attrs.getValue("idref") != null)) {
      preparePhrase(attrs);
      tokenCountForMarker++;
    }
  }

  @Override
  public void endElement(final String namespaceURI, final String sName,
      final String qName) throws SAXException {
    if ("category".equals(qName)) {
      categoryIssueType = null;
    } else if (RULE.equals(qName)) {
      suggestionMatchesOutMsg = addLegacyMatches(suggestionMatchesOutMsg,suggestionsOutMsg.toString(),false);
      phraseElementInit();
      if (phraseElementList.isEmpty()) {
        final PatternRule rule = new PatternRule(id, language, elementList,
                name, message.toString(), shortMessage.toString(), suggestionsOutMsg.toString());
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
              name, message.toString(), shortMessage.toString(), suggestionsOutMsg.toString(),
              phraseElementList.size() > 1);
          prepareRule(rule);
          rules.add(rule);
        }
      }
      elementList.clear();
      if (phraseElementList != null) {
        phraseElementList.clear();
      }
      ruleIssueType = null;

    } else if (EXCEPTION.equals(qName)) {
      finalizeExceptions();
    } else if (AND.equals(qName)) {
      inAndGroup = false;
      andGroupCounter = 0;
      tokenCounter++;
    } else if (TOKEN.equals(qName)) {
      finalizeTokens();
    } else if (PATTERN.equals(qName)) {
      inPattern = false;
      if (lastPhrase) {
        elementList.clear();
      }
      tokenCounter = 0;
    } else if (EXAMPLE.equals(qName)) {
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
    } else if (MESSAGE.equals(qName)) {
      suggestionMatches = addLegacyMatches(suggestionMatches,message.toString(),true);
      inMessage = false;        
    } else if ("suggestion".equals(qName) && !inMessage) { //suggestion outside message
      suggestionsOutMsg.append("</suggestion>");      
      inSuggestion = false;      
    } else if ("short".equals(qName)) {
      inShortMessage = false;
    } else if ("url".equals(qName)) {
      inUrl = false;
    } else if (MATCH.equals(qName)) {
      if (inMessage) {
        suggestionMatches.get(suggestionMatches.size() - 1).
            setLemmaString(match.toString());
      } else if (inSuggestion && !inMessage) {
          suggestionMatchesOutMsg.get(suggestionMatchesOutMsg.size() - 1).
              setLemmaString(match.toString());
      } else if (inToken) {
        tokenReference.setLemmaString(match.toString());
      }
      inMatch = false;
    } else if (RULEGROUP.equals(qName)) {
      inRuleGroup = false;
      ruleGroupIssueType = null;
    } else if ("suggestion".equals(qName) && inMessage) {
      message.append("</suggestion>");      
      inSuggestion = false;
    } else if (MARKER.equals(qName) && inCorrectExample) {
      correctExample.append("</marker>");
    } else if (MARKER.equals(qName) && inIncorrectExample) {
      incorrectExample.append("</marker>");
    } else if (MARKER.equals(qName) && inPattern) {
      endPos = tokenCountForMarker;
    } else if ("phrase".equals(qName) && inPhrases) {
      finalizePhrase();
    } else if ("includephrases".equals(qName)) {
      elementList.clear();
    } else if (PHRASES.equals(qName) && inPhrases) {
      inPhrases = false;
    } else if (UNIFICATION.equals(qName)) {
      inUnificationDef = false;
    } else if (FEATURE.equals(qName)) {
      equivalenceFeatures.put(uFeature, uTypeList);
      uTypeList = new ArrayList<String>();
    } else if (UNIFY.equals(qName)) {
      inUnification = false;
      //clear the features...
      equivalenceFeatures = new HashMap<String, List<String>>();
      //set negation on the last token only!
      final int lastElement = elementList.size() - 1;
      elementList.get(lastElement).setLastInUnification();
      if (uniNegation) {            
    	  elementList.get(lastElement).setUniNegation();
      }
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
      //if (phraseElementList.size() <= 1) {
      suggestionMatchesOutMsg.clear();
      //}
    }
    if (defaultOff) {
      rule.setDefaultOff();
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
    }
    // inheritance of values - if no type value is defined for a rule, take the rule group's value etc:
    if (ruleIssueType != null) {
      rule.setLocQualityIssueType(ruleIssueType);
    } else if (ruleGroupIssueType != null) {
      rule.setLocQualityIssueType(ruleGroupIssueType);
    } else if (categoryIssueType != null) {
      rule.setLocQualityIssueType(categoryIssueType);
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
    } else if (inSuggestion && !inMessage) {  //Suggestion outside message
      suggestionsOutMsg.append(s);
    } else if (inShortMessage) {
      shortMessage.append(s);
    } else if (inUrl) {
      url.append(s);
    }
  }

}
