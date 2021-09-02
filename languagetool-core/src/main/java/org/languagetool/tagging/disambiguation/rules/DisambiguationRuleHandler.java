/* LanguageTool, a natural language style checker
 * Copyright (C) 2015 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.disambiguation.rules;

import org.languagetool.AnalyzedToken;
import org.languagetool.Languages;
import org.languagetool.rules.patterns.*;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

class DisambiguationRuleHandler extends XMLRuleHandler {

  private static final String WD = "wd";
  private static final String ACTION = "action";
  private static final String DISAMBIG = "disambig";

  private final List<DisambiguationPatternRule> rules = new ArrayList<>();

  private boolean inDisambiguation;
  private int subId;
  private String name;
  private String ruleGroupId;
  private String ruleGroupName;
  protected String filterClassName;
  protected String filterArgs;
  private StringBuilder disamb = new StringBuilder();
  private StringBuilder wd = new StringBuilder();
  private StringBuilder example = new StringBuilder();

  private int antiPatternCounter = 0;
  private boolean inRule;
  private List<DisambiguationPatternRule> rulegroupAntiPatterns = new ArrayList<>();
  private List<DisambiguationPatternRule> ruleAntiPatterns = new ArrayList<>();
  private boolean inAntiPattern;

  private boolean inWord;

  private String disambiguatedPOS;

  private int startPos = -1;
  private int endPos = -1;
  private int tokenCountForMarker;

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

  List<DisambiguationPatternRule> getDisambRules() {
    return rules;
  }

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  public void startElement(String namespaceURI, String lName,
                           String qName, Attributes attrs) throws SAXException {
    switch (qName) {
      case RULE:
        inRule = true;
        id = attrs.getValue(ID);
        if (inRuleGroup) {
          subId++;
        }
        name = attrs.getValue(NAME);
        if (inRuleGroup && id == null) {
          id = ruleGroupId;
        }
        if (inRuleGroup && name == null) {
          name = ruleGroupName;
        }
        break;
      case "rules":
        language = Languages.getLanguageForShortCode(attrs.getValue("lang"));
        break;
      case PATTERN:
        inPattern = true;
        tokenCountForMarker = 0;
        if (attrs.getValue(CASE_SENSITIVE) != null && YES.equals(attrs.getValue(CASE_SENSITIVE))) {
          caseSensitive = true;
        }
        break;
      case ANTIPATTERN:
        inAntiPattern = true;
        antiPatternCounter++;
        caseSensitive = YES.equals(attrs.getValue(CASE_SENSITIVE));
        tokenCounter = 0;
        tokenCountForMarker = 0;
        break;
      case EXCEPTION:
        setExceptions(attrs);
        break;
      case AND:
        inAndGroup = true;
        tokenCountForMarker++;
        if (inUnification) {
          uniCounter++;
        }
        break;
      case UNIFY:
        inUnification = true;
        uniNegation = YES.equals(attrs.getValue(NEGATE));
        uniCounter = 0;
        break;
      case UNIFY_IGNORE:
        inUnificationNeutral = true;
        break;
      case "feature":
        uFeature = attrs.getValue(ID);
        break;
      case TYPE:
        uType = attrs.getValue(ID);
        uTypeList.add(uType);
        break;
      case TOKEN:
        setToken(attrs);
        if (!inAndGroup) {
          tokenCountForMarker++;
        }
        break;
      case DISAMBIG:
        inDisambiguation = true;
        disambiguatedPOS = attrs.getValue(POSTAG);
        if (attrs.getValue(ACTION) == null) {
          // default mode:
          disambigAction = DisambiguationPatternRule.DisambiguatorAction.REPLACE;
        } else {
          disambigAction = DisambiguationPatternRule.DisambiguatorAction
                  .valueOf(attrs.getValue(ACTION).toUpperCase(Locale.ENGLISH));
        }
        disamb = new StringBuilder();
        break;
      case MATCH:
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
        Match mWorker = new Match(attrs.getValue(POSTAG), attrs
                .getValue("postag_replace"), YES
                .equals(attrs.getValue(POSTAG_REGEXP)), attrs
                .getValue("regexp_match"), attrs.getValue("regexp_replace"),
                caseConversion, YES.equals(attrs.getValue("setpos")),
                YES.equals(attrs.getValue("suppress_mispelled")),
                includeRange);
        if (inDisambiguation) {
          if (attrs.getValue(NO) != null) {
            int refNumber = Integer.parseInt(attrs.getValue(NO));
            refNumberSanityCheck(refNumber);
            mWorker.setTokenRef(refNumber);
            posSelector = mWorker;
          }
        } else if (inToken && attrs.getValue(NO) != null) {
          int refNumber = Integer.parseInt(attrs.getValue(NO));
          refNumberSanityCheck(refNumber);
          mWorker.setTokenRef(refNumber);
          tokenReference = mWorker;
          elements.append('\\');
          elements.append(refNumber);
        }
        break;
      case RULEGROUP:
        ruleGroupId = attrs.getValue(ID);
        ruleGroupName = attrs.getValue(NAME);
        inRuleGroup = true;
        subId = 0;
        if (rulegroupAntiPatterns != null) {
          rulegroupAntiPatterns.clear();
        }
        antiPatternCounter = 0;
        break;
      case UNIFICATION:
        uFeature = attrs.getValue(FEATURE);
        inUnificationDef = true;
        break;
      case "equivalence":
        uType = attrs.getValue(TYPE);
        break;
      case WD:
        wdLemma = attrs.getValue("lemma");
        wdPos = attrs.getValue("pos");
        inWord = true;
        wd = new StringBuilder();
        break;
      case EXAMPLE:
        inExample = true;
        if (untouchedExamples == null) {
          untouchedExamples = new ArrayList<>();
        }
        if (disambExamples == null) {
          disambExamples = new ArrayList<>();
        }
        untouched = attrs.getValue(TYPE).equals("untouched");
        if (attrs.getValue(TYPE).equals("ambiguous")) {
          input = attrs.getValue("inputform");
          output = attrs.getValue("outputform");
        }
        example = new StringBuilder();
        break;
      case "filter":
        filterClassName = attrs.getValue("class");
        filterArgs = attrs.getValue("args");
        break;
      case MARKER:
        if (inMarker) {
          throw new IllegalStateException("'<marker>' may not be nested in rule '" + id + "'");
        }
        example.append("<marker>");
        if (inPattern || inAntiPattern) {
          startPos = tokenCounter;
          inMarker = true;
        }
        break;
    }
  }

  private void refNumberSanityCheck(int refNumber) throws SAXException {
    if (refNumber > patternTokens.size()) {
      throw new SAXException("Only backward references in match elements are possible, tried to specify token "
              + refNumber + "\n Line: " + pLocator.getLineNumber()
              + ", column: " + pLocator.getColumnNumber() + ".");
    }
  }

  @Override
  public void endElement(String namespaceURI, String sName,
                         String qName) throws SAXException {
    switch (qName) {
      case RULE:
        DisambiguationPatternRule rule = new DisambiguationPatternRule(id,
                name, language, patternTokens, disambiguatedPOS, posSelector,
                disambigAction);

        endPositionCorrection = endPos - tokenCountForMarker;
        if (startPos != -1 && endPos != -1) {
          rule.setStartPositionCorrection(startPos);
          rule.setEndPositionCorrection(endPositionCorrection);
        } else {
          startPos = 0;
          endPos = tokenCountForMarker;
        }
        rule.setSubId(inRuleGroup ? internString(Integer.toString(subId)) : "1");

        int matchedTokenCount = endPos - startPos;
        if (newWdList != null) {
          if (disambigAction == DisambiguationPatternRule.DisambiguatorAction.ADD || disambigAction == DisambiguationPatternRule.DisambiguatorAction.REMOVE
                  || disambigAction == DisambiguationPatternRule.DisambiguatorAction.REPLACE) {
            if ((!newWdList.isEmpty() && disambigAction == DisambiguationPatternRule.DisambiguatorAction.REPLACE)
                    && newWdList.size() != matchedTokenCount) {
              throw new SAXException(
                      language.getName() + " rule error. The number of interpretations specified with wd: "
                              + newWdList.size()
                              + " must be equal to the number of matched tokens (" + matchedTokenCount + ")"
                              + "\n Line: " + pLocator.getLineNumber() + ", column: "
                              + pLocator.getColumnNumber() + ".");
            }
            rule.setNewInterpretations(newWdList.toArray(new AnalyzedToken[0]));
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
        setRuleFilter(filterClassName, filterArgs, rule);
        if (!rulegroupAntiPatterns.isEmpty()) {
          rule.setAntiPatterns(rulegroupAntiPatterns);
        }
        if (!ruleAntiPatterns.isEmpty()) {
          rule.setAntiPatterns(ruleAntiPatterns);
          ruleAntiPatterns.clear();
        }
        rules.add(rule);
        if (disambigAction == DisambiguationPatternRule.DisambiguatorAction.UNIFY && matchedTokenCount != uniCounter) {
          throw new SAXException(language.getName() + " rule error. The number unified tokens: "
                  + uniCounter + " must be equal to the number of matched tokens: " + matchedTokenCount
                  + "\n Line: " + pLocator.getLineNumber() + ", column: "
                  + pLocator.getColumnNumber() + ".");
        }
        boolean singleTokenCorrection = endPos - startPos > 1;
        if ((!singleTokenCorrection && (disambigAction == DisambiguationPatternRule.DisambiguatorAction.FILTER || disambigAction == DisambiguationPatternRule.DisambiguatorAction.REPLACE))
                && (matchedTokenCount > 1)) {
          throw new SAXException(
                  language.getName() + " rule error. Cannot replace or filter more than one token at a time."
                          + "\n Line: " + pLocator.getLineNumber() + ", column: "
                          + pLocator.getColumnNumber() + ".");
        }
        patternTokens.clear();
        posSelector = null;
        disambExamples = null;
        untouchedExamples = null;
        startPos = -1;
        endPos = -1;
        filterClassName = null;
        filterArgs = null;
        inRule = false;
        break;
      case EXCEPTION:
        finalizeExceptions();
        break;
      case AND:
        inAndGroup = false;
        andGroupCounter = 0;
        tokenCounter++;
        break;
      case TOKEN:
        if (inUnification && !inAndGroup) {
          uniCounter++;
        }
        finalizeTokens(language.getDisambiguationUnifierConfiguration());
        break;
      case PATTERN:
        inPattern = false;
        tokenCounter = 0;
        break;
      case MATCH:
        if (inDisambiguation) {
          posSelector.setLemmaString(match.toString());
        } else if (inToken) {
          tokenReference.setLemmaString(match.toString());
        }
        inMatch = false;
        break;
      case DISAMBIG:
        inDisambiguation = false;
        break;
      case RULEGROUP:
        inRuleGroup = false;
        break;
      case UNIFICATION:
        if (inUnificationDef) {
          inUnificationDef = false;
          tokenCounter = 0;
        }
        break;
      case "feature":
        equivalenceFeatures.put(uFeature, uTypeList);
        uTypeList = new ArrayList<>();
        break;
      case UNIFY:
        inUnification = false;
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
      case WD:
        addNewWord(wd.toString(), wdLemma, wdPos);
        inWord = false;
        break;
      case ANTIPATTERN:
        final DisambiguationPatternRule disRule = new DisambiguationPatternRule(
                id + "_antipattern:" + antiPatternCounter,
                "antipattern", language, patternTokens, null, null,
                DisambiguationPatternRule.DisambiguatorAction.IMMUNIZE);
        if (startPos != -1 && endPos != -1) {
          disRule.setStartPositionCorrection(startPos);
          disRule.setEndPositionCorrection(endPos - tokenCountForMarker);
        }
        patternTokens.clear();
        if (inRule) {
          if (ruleAntiPatterns == null) {
            ruleAntiPatterns = new ArrayList<>();
          }
          ruleAntiPatterns.add(disRule);
        } else { // a rulegroup shares all antipatterns not included in a single rule
          if (rulegroupAntiPatterns == null) {
            rulegroupAntiPatterns = new ArrayList<>();
          }
          rulegroupAntiPatterns.add(disRule);
        }
        tokenCounter = 0;
        inAntiPattern = false;
        break;
      case EXAMPLE:
        inExample = false;
        if (untouched) {
          untouchedExamples.add(example.toString());
        } else {
          disambExamples.add(new DisambiguatedExample(example.toString(), input, output));
        }
        break;
      case MARKER:
        example.append("</marker>");
        if (inPattern || inAntiPattern) {
          endPos = tokenCountForMarker;
          inMarker = false;
        }
        break;
    }
  }

  private void addNewWord(String word, String lemma, String pos) {
    AnalyzedToken newWd = new AnalyzedToken(word, pos, lemma);
    if (newWdList == null) {
      newWdList = new ArrayList<>();
    }
    newWdList.add(newWd);
  }

  @Override
  public final void characters(char[] buf, int offset, int len) {
    String s = new String(buf, offset, len);
    if (inException) {
      exceptions.append(s);
    } else if (inToken && (inPattern || inAntiPattern)) {
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
