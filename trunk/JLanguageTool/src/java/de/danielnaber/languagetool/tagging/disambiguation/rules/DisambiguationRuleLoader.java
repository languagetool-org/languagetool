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
package de.danielnaber.languagetool.tagging.disambiguation.rules;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.rules.patterns.Element;
import de.danielnaber.languagetool.rules.patterns.Match;
import de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule.DisambiguatorAction;

/**
 * Loads {@link DisambiguationPatternRule}s from a disambiguation rules XML
 * file.
 * 
 * @author Marcin Miłkowski
 */
public class DisambiguationRuleLoader extends DefaultHandler {

  public DisambiguationRuleLoader() {
    super();
  }

  public final List<DisambiguationPatternRule> getRules(final InputStream file)
      throws ParserConfigurationException, SAXException, IOException {
    final DisambiguationRuleHandler handler = new DisambiguationRuleHandler();
    final SAXParserFactory factory = SAXParserFactory.newInstance();
    final SAXParser saxParser = factory.newSAXParser();
    saxParser.parse(file, handler);
    return handler.getDisambRules();
  }

}

class DisambiguationRuleHandler extends DisambXMLRuleHandler {

  private static final String MARK = "mark";
  private static final String WD = "wd";
  private static final String ACTION = "action";
  private static final String DISAMBIG = "disambig";

  private String name;
  private String ruleGroupId;
  private String ruleGroupName;
  private StringBuilder disamb = new StringBuilder();
  private StringBuilder wd = new StringBuilder();
  private StringBuilder example = new StringBuilder();

  private boolean inWord;

  private String disambiguatedPOS;

  private int positionCorrection;
  private int endPositionCorrection;
  private boolean singleTokenCorrection;

  private Match posSelector;

  private int uniCounter;
  
  private List<AnalyzedToken> newWdList;
  private String wdLemma;
  private String wdPos;

  private boolean inExample;
  private boolean untouched;
  private List<String> untouchedExamples;
  private List<DisambiguatedExample> disambExamples;
  private String input;
  private String output;
  
  private DisambiguationPatternRule.DisambiguatorAction disambigAction;

 
  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  public void startElement(final String namespaceURI, final String lName,
      final String qName, final Attributes attrs) throws SAXException {
    if (qName.equals("rule")) {
      id = attrs.getValue("id");
      name = attrs.getValue("name");
      if (inRuleGroup && id == null) {
        id = ruleGroupId;
      }
      if (inRuleGroup && name == null) {
        name = ruleGroupName;
      }
    } else if ("rules".equals(qName)) {
      language = Language.getLanguageForShortName(attrs.getValue("lang"));
    } else if (qName.equals(PATTERN)) {
      inPattern = true;
      if (attrs.getValue(MARK) != null && (attrs.getValue(MARK_FROM) != null)) {
        throw new SAXException(
            "You cannot use both mark and mark_from attributes." + "\n Line: "
                + pLocator.getLineNumber() + ", column: "
                + pLocator.getColumnNumber() + ".");
      }
      if (attrs.getValue(MARK) != null && (attrs.getValue(MARK_TO) != null)) {
        throw new SAXException(
            "You cannot use both mark and mark_to attributes." + "\n Line: "
                + pLocator.getLineNumber() + ", column: "
                + pLocator.getColumnNumber() + ".");
      }

      if (attrs.getValue(MARK) != null) {
        positionCorrection = Integer.parseInt(attrs.getValue(MARK));
      }
      if (attrs.getValue(MARK_FROM) != null) {
        positionCorrection = Integer.parseInt(attrs.getValue(MARK_FROM));
      }
      if (attrs.getValue(MARK_TO) == null) {
        singleTokenCorrection = true;
      } else {
        endPositionCorrection = Integer.parseInt(attrs.getValue(MARK_TO));
        if (endPositionCorrection > 0) {
          throw new SAXException("End position correction (mark_to=" 
              + endPositionCorrection
              + ") cannot be larger than 0: " + "\n Line: "
              + pLocator.getLineNumber() + ", column: "
              + pLocator.getColumnNumber() + ".");
        }        
        singleTokenCorrection = false;
      }
      if (attrs.getValue(CASE_SENSITIVE) != null
          && YES.equals(attrs.getValue(CASE_SENSITIVE))) {
        caseSensitive = true;
      }
    } else if (qName.equals(EXCEPTION)) {
      setExceptions(attrs);
    } else if (qName.equals(AND)) {
      inAndGroup = true;
    } else if (qName.equals(UNIFY)) {
      inUnification = true;           
      uniNegation = YES.equals(attrs.getValue(NEGATE));
      uniCounter = 0;
    } else if ("feature".equals(qName)) {
      uFeature = attrs.getValue("id");        
    } else if (qName.equals(TYPE)) {      
      uType = attrs.getValue("id");
      uTypeList.add(uType);      
    } else if (qName.equals(TOKEN)) {
      setToken(attrs);
    } else if (qName.equals(DISAMBIG)) {
      inDisambiguation = true;
      disambiguatedPOS = attrs.getValue(POSTAG);
      if (attrs.getValue(ACTION) == null) {
        // default mode:
        disambigAction = DisambiguationPatternRule.DisambiguatorAction
            .toAction("REPLACE");
      } else {
        disambigAction = DisambiguationPatternRule.DisambiguatorAction
            .toAction(attrs.getValue(ACTION).toUpperCase());
      }
      disamb = new StringBuilder();
    } else if (qName.equals(MATCH)) {
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
      if (inDisambiguation) {
        if (attrs.getValue(NO) != null) {
          final int refNumber = Integer.parseInt(attrs.getValue(NO));
          if (refNumber > elementList.size()) {
            throw new SAXException(
                "Only backward references in match elements are possible, tried to specify token "
                    + refNumber
                    + "\n Line: "
                    + pLocator.getLineNumber()
                    + ", column: " + pLocator.getColumnNumber() + ".");
          }
          mWorker.setTokenRef(refNumber);
          posSelector = mWorker;
        }
      } else if (inToken) {
        if (attrs.getValue(NO) != null) {
          final int refNumber = Integer.parseInt(attrs.getValue(NO));
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
    } else if (qName.equals(RULEGROUP)) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupName = attrs.getValue("name");
      inRuleGroup = true;
    } else if (qName.equals(UNIFICATION)) {
      uFeature = attrs.getValue(FEATURE);
      inUnificationDef = true;
    } else if ("equivalence".equals(qName)) {
      uType = attrs.getValue(TYPE);
    } else if (qName.equals(WD)) {
      wdLemma = attrs.getValue("lemma");
      wdPos = attrs.getValue("pos");
      inWord = true;
      wd = new StringBuilder();
    } else if (qName.equals(EXAMPLE)) {
      inExample = true;
      if (untouchedExamples == null) {
        untouchedExamples = new ArrayList<String>();
      }
      if (disambExamples == null) {
        disambExamples = new ArrayList<DisambiguatedExample>();
      }
      untouched = attrs.getValue(TYPE).equals("untouched");
      if (attrs.getValue(TYPE).equals("ambiguous")) {
        input = attrs.getValue("inputform");
        output = attrs.getValue("outputform");
      }
      example = new StringBuilder();
    } else if ("marker".equals(qName)) {
      example.append("<marker>");
    }
  }

  @Override
  public void endElement(final String namespaceURI, final String sName,
      final String qName) throws SAXException {
    if ("rule".equals(qName)) {
      final DisambiguationPatternRule rule = new DisambiguationPatternRule(id,
          name, language, elementList, disambiguatedPOS, posSelector,
          disambigAction);
      rule.setStartPositionCorrection(positionCorrection);
      if (singleTokenCorrection) {
        endPositionCorrection = 1 - (elementList.size() - positionCorrection);
        rule.setEndPositionCorrection(endPositionCorrection);
      } else {
        rule.setEndPositionCorrection(endPositionCorrection);
      }
      if (newWdList != null) {
        if (disambigAction == DisambiguatorAction.ADD
            || disambigAction == DisambiguatorAction.REMOVE) {
          if (newWdList.size() != (elementList.size() - positionCorrection + endPositionCorrection)) {
            throw new SAXException(
                language.getName() + " rule error. The number of interpretations specified with wd: "
                    + newWdList.size()
                    + " must be equal to the number of matched tokens (" + (elementList.size() - positionCorrection + endPositionCorrection) + ")" 
                    + "\n Line: " + pLocator.getLineNumber() + ", column: "
                    + pLocator.getColumnNumber() + ".");
          }
          rule.setNewInterpretations(newWdList
              .toArray(new AnalyzedToken[newWdList.size()]));
        }
        newWdList.clear();
      }
      caseSensitive = false;      
      if (disambExamples != null) {
        rule.setExamples(disambExamples);
      }
      if (untouchedExamples != null) {
        rule.setUntouchedExamples(untouchedExamples);
      }
      rules.add(rule);
      if (disambigAction == DisambiguatorAction.UNIFY
          && (elementList.size() - positionCorrection + endPositionCorrection) != uniCounter) {
        throw new SAXException(language.getName() + " rule error. The number unified tokens: "
            + uniCounter + " must be equal to the number of matched tokens."
            + "\n Line: " + pLocator.getLineNumber() + ", column: "
            + pLocator.getColumnNumber() + ".");
      }
      if ((!singleTokenCorrection && (disambigAction == DisambiguatorAction.FILTER || disambigAction == DisambiguatorAction.REPLACE))
          && ((elementList.size() - positionCorrection + endPositionCorrection) > 1)) {
        throw new SAXException(
            language.getName() + " rule error. Cannot replace or filter more than one token at a time."
                + "\n Line: " + pLocator.getLineNumber() + ", column: "
                + pLocator.getColumnNumber() + ".");
      }
      elementList.clear();
      posSelector = null;
      disambExamples = null;
      untouchedExamples = null;
    } else if (qName.equals(EXCEPTION)) {
      finalizeExceptions();
    } else if (qName.equals(AND)) {
      inAndGroup = false;
      andGroupCounter = 0;
      tokenCounter++;            
    } else if (qName.equals(TOKEN)) {
      if (!exceptionSet || tokenElement == null) {
        tokenElement = new Element(elements.toString(), caseSensitive,
            regExpression, tokenInflected);
        tokenElement.setNegation(tokenNegated);
      } else {
        tokenElement.setStringElement(elements.toString());
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
        uniCounter++;
      }
      if (inUnificationDef) {
        language.getDisambiguationUnifier().setEquivalence(uFeature, uType, tokenElement);
        elementList.clear();
      }
      if (tokenSpaceBeforeSet) {
        tokenElement.setWhitespaceBefore(tokenSpaceBefore);
      }
      resetToken();
    } else if (qName.equals(PATTERN)) {
      inPattern = false;
      if (positionCorrection >= tokenCounter) {
        throw new SAXException(
            "Attempt to mark a token no. ("+ positionCorrection +") that is outside the pattern (" + tokenCounter + "). Pattern elements are numbered starting from 0!" + "\n Line: "
                + pLocator.getLineNumber() + ", column: "
                + pLocator.getColumnNumber() + ".");
      }
      if (tokenCounter - endPositionCorrection < 0 ) {
        throw new SAXException(
            "Attempt to mark a token no. ("+ endPositionCorrection +") that is outside the pattern (" + tokenCounter + "). Pattern elements are numbered starting from 0!" + "\n Line: "
                + pLocator.getLineNumber() + ", column: "
                + pLocator.getColumnNumber() + ".");
      }
      tokenCounter = 0;
    } else if (qName.equals(MATCH)) {
      if (inDisambiguation) {
        posSelector.setLemmaString(match.toString());
      } else if (inToken) {
        tokenReference.setLemmaString(match.toString());
      }
      inMatch = false;
    } else if (qName.equals(DISAMBIG)) {
      inDisambiguation = false;
    } else if (qName.equals(RULEGROUP)) {
      inRuleGroup = false;
    } else if (qName.equals(UNIFICATION) && inUnificationDef) {
      inUnificationDef = false;
    } else if ("feature".equals(qName)) {      
      equivalenceFeatures.put(uFeature, uTypeList);
      uTypeList = new ArrayList<String>();
    } else if (qName.equals(UNIFY)) {
      inUnification = false;
      equivalenceFeatures = new HashMap<String, List<String>>();
    } else if (qName.equals(WD)) {
      addNewWord(wd.toString(), wdLemma, wdPos);
      inWord = false;
    } else if (EXAMPLE.equals(qName)) {
      inExample = false;
      if (untouched) {
        untouchedExamples.add(example.toString());
      } else {
        disambExamples.add(new DisambiguatedExample(example.toString(), input, output));
      }
    } else if ("marker".equals(qName)) {
      example.append("</marker>");
    }
  }

  private void addNewWord(final String word, final String lemma,
      final String pos) {
    final AnalyzedToken newWd = new AnalyzedToken(word, pos, lemma);
    if (newWdList == null) {
      newWdList = new ArrayList<AnalyzedToken>();
    }
    newWdList.add(newWd);
  }

  @Override
  public final void characters(final char[] buf, final int offset, final int len) {
    final String s = new String(buf, offset, len);
    if (inException) {
      exceptions.append(s);
    } else if (inToken && inPattern) {
      elements.append(s);
    } else if (inMatch) {
      match.append(s);
    } else if (inWord) {
      wd.append(s);
    } else if (inDisambiguation) {
      disamb.append(s);
    } else if (inExample) {
      example.append(s);
    }
  }

}
