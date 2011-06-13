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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.Category;
import de.danielnaber.languagetool.rules.IncorrectExample;

/**
 * Loads {@link PatternRule}s from an XML file.
 * 
 * @author Daniel Naber
 */
public class PatternRuleLoader extends DefaultHandler {

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
      throw new IOException("Cannot load or parse '" + filename + "'", e);
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

  private int subId;

  private boolean defaultOff;
  private boolean defaultOn;

  private Category category;
  private String name;
  private String ruleGroupDescription;

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  public void startElement(final String namespaceURI, final String lName,
      final String qName, final Attributes attrs) throws SAXException {
    if ("category".equals(qName)) {
      final String catName = attrs.getValue("name");
      final String priorityStr = attrs.getValue("priority");
      // int prio = 0;
      if (priorityStr == null) {
        category = new Category(catName);        
      } else {
        category = new Category(catName, Integer.parseInt(priorityStr));
      }

      if ("off".equals(attrs.getValue(DEFAULT))) {
        category.setDefaultOff();
      }

    } else if ("rules".equals(qName)) {
      final String languageStr = attrs.getValue("lang");
      language = Language.getLanguageForShortName(languageStr);
      if (language == null) {
        throw new SAXException("Unknown language '" + languageStr + "'");
      }
    } else if ("rule".equals(qName)) {
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
    } else if (PATTERN.equals(qName)) {
      startPattern(attrs);     
    } else if (AND.equals(qName)) {
      inAndGroup = true;
    } else if ("unify".equals(qName)) {
        inUnification = true;           
        uniNegation = YES.equals(attrs.getValue(NEGATE));
    } else if ("feature".equals(qName)) {
        uFeature = attrs.getValue("id");        
    } else if (qName.equals(TYPE)) {      
        uType = attrs.getValue("id");
        uTypeList.add(uType);
    } else if (qName.equals(TOKEN)) {
      setToken(attrs);
    } else if (EXCEPTION.equals(qName)) {
      setExceptions(attrs);
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
    } else if ("message".equals(qName)) {
      inMessage = true;
      inSuggestion = false;
      message = new StringBuilder();
    } else if ("short".equals(qName)) {
      inShortMessage = true;
      shortMessage = new StringBuilder();
    } else if ("rulegroup".equals(qName)) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupDescription = attrs.getValue("name");
      defaultOff = "off".equals(attrs.getValue(DEFAULT));
      defaultOn = "on".equals(attrs.getValue(DEFAULT));
      inRuleGroup = true;
      subId = 0;
    } else if ("suggestion".equals(qName) && inMessage) {
      message.append("<suggestion>");
      inSuggestion = true;
    } else if ("match".equals(qName)) {
      setMatchElement(attrs);
    } else if (qName.equals(MARKER) && inCorrectExample) {
      correctExample.append("<marker>");
    } else if (qName.equals(MARKER) && inIncorrectExample) {
      incorrectExample.append("<marker>");
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
    }    
  }

  @Override
  public void endElement(final String namespaceURI, final String sName,
      final String qName) throws SAXException {
    if ("rule".equals(qName)) {
      phraseElementInit();
      if (phraseElementList.isEmpty()) {
        final PatternRule rule = new PatternRule(id, language, elementList,
                name, message.toString(), shortMessage.toString());
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
                  name, message.toString(), shortMessage.toString(),
              phraseElementList.size() > 1);
          prepareRule(rule);
          rules.add(rule);
        }
      }
      elementList.clear();
      if (phraseElementList != null) {
        phraseElementList.clear();
      }

    } else if (qName.equals(EXCEPTION)) {
      finalizeExceptions();
    } else if (qName.equals(AND)) {
      inAndGroup = false;
      andGroupCounter = 0;
      tokenCounter++;
    } else if (qName.equals(TOKEN)) {
      finalizeTokens();
    } else if (qName.equals(PATTERN)) {
      checkMarkPositions();
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
    } else if ("message".equals(qName)) {
      suggestionMatches = addLegacyMatches();
      inMessage = false;
    } else if ("short".equals(qName)) {
      inShortMessage = false;
    } else if ("match".equals(qName)) {
      if (inMessage) {
        suggestionMatches.get(suggestionMatches.size() - 1).setLemmaString(
            match.toString());
      } else if (inToken) {
        tokenReference.setLemmaString(match.toString());
      }
      inMatch = false;
    } else if ("rulegroup".equals(qName)) {
      inRuleGroup = false;
    } else if ("suggestion".equals(qName) && inMessage) {
      message.append("</suggestion>");
      inSuggestion = false;
    } else if (qName.equals(MARKER) && inCorrectExample) {
      correctExample.append("</marker>");
    } else if (qName.equals(MARKER) && inIncorrectExample) {
      incorrectExample.append("</marker>");
    } else if ("phrase".equals(qName) && inPhrases) {
      finalizePhrase();
    } else if ("includephrases".equals(qName)) {
        elementList.clear();
    } else if (PHRASES.equals(qName) && inPhrases) {
        inPhrases = false;
    } else if (UNIFICATION.equals(qName)) {
        inUnificationDef = false;
    } else if ("feature".equals(qName)) {        
        equivalenceFeatures.put(uFeature, uTypeList);
        uTypeList = new ArrayList<String>();
    } else if ("unify".equals(qName)) {      
      inUnification = false;
      //clear the features...
      equivalenceFeatures = new HashMap<String, List<String>>();
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
    if (inRuleGroup) {
      rule.setSubId(Integer.toString(subId));
    }
    else {
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
