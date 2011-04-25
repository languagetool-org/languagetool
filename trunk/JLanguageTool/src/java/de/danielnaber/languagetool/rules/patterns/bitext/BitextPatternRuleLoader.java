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
package de.danielnaber.languagetool.rules.patterns.bitext;

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
import de.danielnaber.languagetool.bitext.StringPair;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.IncorrectExample;
import de.danielnaber.languagetool.rules.bitext.IncorrectBitextExample;
import de.danielnaber.languagetool.rules.patterns.Element;
import de.danielnaber.languagetool.rules.patterns.Match;
import de.danielnaber.languagetool.rules.patterns.PatternRule;

/**
 * Loads {@link PatternRule}s from an XML file.
 * 
 * @author Marcin Miłkowski
 */
public class BitextPatternRuleLoader extends DefaultHandler {

  public final List<BitextPatternRule> getRules(final InputStream is,
      final String filename) throws IOException {
    final List<BitextPatternRule> rules;
    try {
      final PatternRuleHandler handler = new PatternRuleHandler();
      final SAXParserFactory factory = SAXParserFactory.newInstance();
      final SAXParser saxParser = factory.newSAXParser();
      /* saxParser.getXMLReader().setFeature(
          "http://apache.org/xml/features/nonvalidating/load-external-dtd",
          false);
       */
      saxParser.parse(is, handler);
      rules = handler.getBitextRules();
      return rules;
    } catch (final Exception e) {
      throw new IOException("Cannot load or parse '" + filename + "'", e);
    }
  }

}

class PatternRuleHandler extends BitextXMLRuleHandler {

  private int subId;

  private boolean defaultOff;
  private boolean defaultOn;

  private Category category;
  private String description;
  private String ruleGroupDescription;

  private PatternRule srcRule;
  private PatternRule trgRule;

  private IncorrectExample trgExample;
  private IncorrectExample srcExample;

  private Language srcLang;

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

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
      final String languageStr = attrs.getValue("targetLang");
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
      correctExamples = new ArrayList<StringPair>();
      incorrectExamples = new ArrayList<IncorrectBitextExample>();
      if (suggestionMatches != null) {
        suggestionMatches.clear();
      }
    } else if (PATTERN.equals(qName) || "target".equals(qName)) {
      startPattern(attrs);     
    } else if (AND.equals(qName)) {
      inAndGroup = true;
    } else if (UNIFY.equals(qName)) {
      inUnification = true;           
      uniNegation = YES.equals(attrs.getValue(NEGATE));
    } else if (qName.equals("feature")) {
      uFeature = attrs.getValue("id");        
    } else if (qName.equals(TYPE)) {      
      uType = attrs.getValue("id");
      uTypeList.add(uType);
    } else if (qName.equals(TOKEN)) {
      setToken(attrs);
    } else if (qName.equals(EXCEPTION)) {
      setExceptions(attrs);
    } else if (qName.equals(EXAMPLE)
        && attrs.getValue(TYPE).equals("correct")) {
      inCorrectExample = true;
      correctExample = new StringBuilder();
    } else if (EXAMPLE.equals(qName)
        && attrs.getValue(TYPE).equals("incorrect")) {
      inIncorrectExample = true;
      incorrectExample = new StringBuilder();
      exampleCorrection = new StringBuilder();
      if (attrs.getValue("correction") != null) {
        exampleCorrection.append(attrs.getValue("correction"));
      }
    } else if (MESSAGE.equals(qName)) {
      inMessage = true;
      message = new StringBuilder();
    } else if (qName.equals("short")) {
      inShortMessage = true;
      shortMessage = new StringBuilder();
    } else if (qName.equals(RULEGROUP)) {
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
      setMatchElement(attrs);
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
      preparePhrase(attrs);
    } else if (qName.equals("source")) {
      srcLang = Language.getLanguageForShortName(attrs.getValue("lang"));        
    }    
  }

  @Override
  public void endElement(final String namespaceURI, final String sName,
      final String qName) throws SAXException {

    if (qName.equals("source")) {
      checkMarkPositions();
      srcRule = finalizeRule();      
    } else if ("target".equals(qName)) {
      checkMarkPositions();
      trgRule = finalizeRule();
    }  else if ("rule".equals(qName)) {
      trgRule.setMessage(message.toString());
      if (suggestionMatches != null) {
        for (final Match m : suggestionMatches) {
          trgRule.addSuggestionMatch(m);
        }
        if (phraseElementList.size() <= 1) {
          suggestionMatches.clear();
        }
      }      
      final BitextPatternRule bRule = new BitextPatternRule(srcRule, trgRule);
      bRule.setCorrectBitextExamples(correctExamples);
      bRule.setIncorrectBitextExamples(incorrectExamples);
      bRule.setSourceLang(srcLang);      
      rules.add(bRule);
    } else if (qName.equals(EXCEPTION)) {
      finalizeExceptions();
    } else if (qName.equals(AND)) {
      inAndGroup = false;
      andGroupCounter = 0;
      tokenCounter++;
    } else if (qName.equals(TOKEN)) {
        finalizeTokens();
      } else if (qName.equals(PATTERN)) {      
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
    } else if (qName.equals("trgExample")) {
      trgExample = setExample();
    } else if (qName.equals("srcExample")) {
      srcExample = setExample();            
    } else if (qName.equals("example")) {
      if (inCorrectExample) {
        correctExamples.add(new StringPair(srcExample.getExample(), trgExample.getExample()));
      } else if (inIncorrectExample) {
        if (trgExample.getCorrections() == null) {
          incorrectExamples.add(
              new IncorrectBitextExample(
                  new StringPair(
                      srcExample.getExample(), trgExample.getExample())
              ));        
        } else {
          List<String> l = trgExample.getCorrections();
          String str [] = l.toArray (new String [l.size ()]);
          incorrectExamples.add(
              new IncorrectBitextExample(
                  new StringPair(srcExample.getExample(), 
                      trgExample.getExample()), str)
          );  
        }
      }
      inCorrectExample = false;
      inIncorrectExample = false;
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
      finalizePhrase();
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
  
  private IncorrectExample setExample() {
    IncorrectExample example = null;
    if (inCorrectExample) {
      example = new IncorrectExample(correctExample.toString());
    } else if (inIncorrectExample) {
      final String[] corrections = exampleCorrection.toString().split("\\|");
      if (corrections.length > 0 && corrections[0].length() > 0) {
        example = new IncorrectExample(incorrectExample.toString(),
            corrections);
      } else {
        example = new IncorrectExample(incorrectExample.toString());
      }
    }      
    correctExample = new StringBuilder();
    incorrectExample = new StringBuilder();
    exampleCorrection = new StringBuilder();
    return example;
  }

  private PatternRule finalizeRule() {
    PatternRule rule = null;
    phraseElementInit();
    if (phraseElementList.isEmpty()) {
      rule = new PatternRule(id, language, elementList,
          description, "", shortMessage.toString());
      prepareRule(rule);              
    } else {
      if (!elementList.isEmpty()) {
        for (final ArrayList<Element> ph : phraseElementList) {
          ph.addAll(new ArrayList<Element>(elementList));
        }
      }

      for (final ArrayList<Element> phraseElement : phraseElementList) {
        processElement(phraseElement);
        rule = new PatternRule(id, language, phraseElement,
            description, message.toString(), shortMessage.toString(),
            phraseElementList.size() > 1);
        prepareRule(rule);       
      }
    }
    elementList.clear();
    if (phraseElementList != null) {
      phraseElementList.clear();
    }
    startPositionCorrection = 0;
    endPositionCorrection = 0;    
    return rule;
  }
  private void prepareRule(final PatternRule rule) {
    rule.setStartPositionCorrection(startPositionCorrection);
    rule.setEndPositionCorrection(endPositionCorrection);
    startPositionCorrection = 0;
    endPositionCorrection = 0;    
    rule.setCategory(category);
    if (inRuleGroup)
      rule.setSubId(Integer.toString(subId));
    else
      rule.setSubId("1");
    caseSensitive = false;    
    if (defaultOff) {
      rule.setDefaultOff();
    }

    if (category.isDefaultOff() && !defaultOn) {
      rule.setDefaultOff();
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
    } else if (inShortMessage) {
      shortMessage.append(s);
    }
  }

}
