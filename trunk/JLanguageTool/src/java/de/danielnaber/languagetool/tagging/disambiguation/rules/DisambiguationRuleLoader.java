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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.helpers.DefaultHandler;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.AnalyzedToken;
import de.danielnaber.languagetool.rules.patterns.Element;
import de.danielnaber.languagetool.rules.patterns.Match;
import de.danielnaber.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule.DisambiguatorAction;
import de.danielnaber.languagetool.tools.StringTools;

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
    return handler.getRules();
  }

}

class DisambiguationRuleHandler extends XMLRuleHandler {

  private boolean caseSensitive = false;
  private boolean stringRegExp = false;
  private boolean tokenNegated = false;
  private boolean tokenInflected = false;
  private boolean posNegation = false;
  private boolean tokenSpaceBefore = false;
  private boolean tokenSpaceBeforeSet = false;

  private String posToken;

  private String exceptionPosToken;
  private boolean exceptionStringRegExp = false;
  private boolean exceptionStringNegation = false;
  private boolean exceptionStringInflected = false;
  private boolean exceptionPosNegation = false;
  private boolean exceptionPosRegExp = false;
  private boolean exceptionValidNext = false;
  private boolean exceptionValidPrev = false;
  private boolean exceptionSet = false;
  private boolean exceptionSpaceBefore = false;
  private boolean exceptionSpaceBeforeSet = false;

  private List<Element> elementList = null;
  private boolean posRegExp = false;
  private int skipPos = 0;
  private Element tokenElement = null;

  private String id;
  private String name;
  private Language language;
  private String ruleGroupId;
  private String ruleGroupName;
  private StringBuilder disamb = new StringBuilder();
  private StringBuilder match = new StringBuilder();
  private StringBuilder wd = new StringBuilder();

  private boolean inWord = false;

  private String disambiguatedPOS;

  private int positionCorrection = 0;
  private int endPositionCorrection = 0;
  private boolean singleTokenCorrection = true;

  private int andGroupCounter = 0;

  private Match tokenReference = null;

  private Match posSelector = null;

  private boolean inUnification = false;
  private boolean inUnificationDef = false;
  private boolean uniNegation = false;

  private String uFeature;
  private String uType = "";

  private int uniCounter = 0;

  private List<AnalyzedToken> newWdList;
  private String wdLemma;
  private String wdPos;

  private Locator dLocator;

  private DisambiguationPatternRule.DisambiguatorAction disambigAction;

  public DisambiguationRuleHandler() {
  }

  // ===========================================================
  // SAX DocumentHandler methods
  // ===========================================================

  @Override
  public void setDocumentLocator(final Locator locator) {
    dLocator = locator;
    super.setDocumentLocator(locator);
  }

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
    } else if (qName.equals("rules")) {
      language = Language.getLanguageForShortName(attrs.getValue("lang"));
    } else if (qName.equals("pattern")) {
      inPattern = true;
      if (attrs.getValue("mark") != null
          && (attrs.getValue("mark_from") != null)) {
        throw new SAXException(
            "You cannot use both mark and mark_from attributes." + "\n Line: "
                + dLocator.getLineNumber() + ", column: "
                + dLocator.getColumnNumber() + ".");
      }
      if (attrs.getValue("mark") != null && (attrs.getValue("mark_to") != null)) {
        throw new SAXException(
            "You cannot use both mark and mark_to attributes." + "\n Line: "
                + dLocator.getLineNumber() + ", column: "
                + dLocator.getColumnNumber() + ".");
      }

      if (attrs.getValue("mark") != null) {
        positionCorrection = Integer.parseInt(attrs.getValue("mark"));
      }
      if (attrs.getValue("mark_from") != null) {
        positionCorrection = Integer.parseInt(attrs.getValue("mark_from"));
      }
      if (attrs.getValue("mark_to") != null) {
        endPositionCorrection = Integer.parseInt(attrs.getValue("mark_to"));
        singleTokenCorrection = false;
      } else {
        singleTokenCorrection = true;
      }
      if (attrs.getValue("case_sensitive") != null
          && "yes".equals(attrs.getValue("case_sensitive"))) {
        caseSensitive = true;
      }
    } else if (qName.equals("exception")) {
      inException = true;
      exceptions = new StringBuilder();

      if (attrs.getValue("negate") != null) {
        exceptionStringNegation = attrs.getValue("negate").equals("yes");
      }
      if (attrs.getValue("scope") != null) {
        exceptionValidNext = attrs.getValue("scope").equals("next");
        exceptionValidPrev = attrs.getValue("scope").equals("previous");
      }
      if (attrs.getValue("inflected") != null) {
        exceptionStringInflected = attrs.getValue("inflected").equals("yes");
      }
      if (attrs.getValue("postag") != null) {
        exceptionPosToken = attrs.getValue("postag");
        if (attrs.getValue("postag_regexp") != null) {
          exceptionPosRegExp = attrs.getValue("postag_regexp").equals("yes");
        }
        if (attrs.getValue("negate_pos") != null) {
          exceptionPosNegation = attrs.getValue("negate_pos").equals("yes");
        }
      }
      if (attrs.getValue("regexp") != null) {
        exceptionStringRegExp = attrs.getValue("regexp").equals("yes");
      }
      if (attrs.getValue("spacebefore") != null) {
        exceptionSpaceBefore = "yes".equals(attrs.getValue("spacebefore"));
        exceptionSpaceBeforeSet = "ignore"
            .equals(attrs.getValue("spacebefore")) ^ true;
      }
    } else if (qName.equals("and")) {
      inAndGroup = true;
    } else if (qName.equals("unify")) {
      inUnification = true;
      uFeature = attrs.getValue("feature");
      if (attrs.getValue("type") != null) {
        uType = attrs.getValue("type");
      } else {
        uType = "";
      }
      if (attrs.getValue("negate") != null
          && "yes".equals(attrs.getValue("negate"))) {
        uniNegation = true;
      }
      uniCounter = 0;
    } else if (qName.equals("token")) {
      inToken = true;
      if (attrs.getValue("negate") != null) {
        tokenNegated = attrs.getValue("negate").equals("yes");
      }
      if (attrs.getValue("inflected") != null) {
        tokenInflected = attrs.getValue("inflected").equals("yes");
      }
      if (attrs.getValue("skip") != null) {
        skipPos = Integer.parseInt(attrs.getValue("skip"));
      }
      elements = new StringBuilder();
      if (elementList == null) {
        elementList = new ArrayList<Element>();
      }
      if (attrs.getValue("postag") != null) {
        posToken = attrs.getValue("postag");
        if (attrs.getValue("postag_regexp") != null) {
          posRegExp = attrs.getValue("postag_regexp").equals("yes");
        }
        if (attrs.getValue("negate_pos") != null) {
          posNegation = attrs.getValue("negate_pos").equals("yes");
        }

      }
      if (attrs.getValue("regexp") != null) {
        stringRegExp = attrs.getValue("regexp").equals("yes");
      }
      if (attrs.getValue("spacebefore") != null) {
        tokenSpaceBefore = "yes".equals(attrs.getValue("spacebefore"));
        tokenSpaceBeforeSet = "ignore".equals(attrs.getValue("spacebefore")) ^ true;
      }

    } else if (qName.equals("disambig")) {
      inDisamb = true;
      disambiguatedPOS = attrs.getValue("postag");
      if (attrs.getValue("action") != null) {
        disambigAction = DisambiguationPatternRule.DisambiguatorAction
            .toAction(attrs.getValue("action").toUpperCase());
      } else {
        // default mode:
        disambigAction = DisambiguationPatternRule.DisambiguatorAction
            .toAction("REPLACE");
      }
      disamb = new StringBuilder();
    } else if (qName.equals("match")) {
      inMatch = true;
      match = new StringBuilder();
      Match.CaseConversion caseConv = Match.CaseConversion.NONE;
      if (attrs.getValue("case_conversion") != null) {
        caseConv = Match.CaseConversion.toCase(attrs
            .getValue("case_conversion").toUpperCase());
      }
      final Match mWorker = new Match(attrs.getValue("postag"), attrs
          .getValue("postag_replace"), "yes".equals(attrs
          .getValue("postag_regexp")), attrs.getValue("regexp_match"), attrs
          .getValue("regexp_replace"), caseConv, "yes".equals(attrs
          .getValue("setpos")));
      if (inDisamb) {
        if (attrs.getValue("no") != null) {
          final int refNumber = Integer.parseInt(attrs.getValue("no"));
          if (refNumber > elementList.size()) {
            throw new SAXException(
                "Only backward references in match elements are possible, tried to specify token "
                    + refNumber
                    + "\n Line: "
                    + dLocator.getLineNumber()
                    + ", column: " + dLocator.getColumnNumber() + ".");
          } else {
            mWorker.setTokenRef(refNumber);
            posSelector = mWorker;
          }
        }
      } else if (inToken) {
        if (attrs.getValue("no") != null) {
          final int refNumber = Integer.parseInt(attrs.getValue("no"));
          if (refNumber > elementList.size()) {
            throw new SAXException(
                "Only backward references in match elements are possible, tried to specify token "
                    + refNumber
                    + "\n Line: "
                    + dLocator.getLineNumber()
                    + ", column: " + dLocator.getColumnNumber() + ".");
          } else {
            mWorker.setTokenRef(refNumber);
            tokenReference = mWorker;
            elements.append("\\" + refNumber);
          }
        }
      }
    } else if (qName.equals("rulegroup")) {
      ruleGroupId = attrs.getValue("id");
      ruleGroupName = attrs.getValue("name");
      inRuleGroup = true;
    } else if (qName.equals("unification")) {
      uFeature = attrs.getValue("feature");
      inUnificationDef = true;
    } else if (qName.equals("equivalence")) {
      uType = attrs.getValue("type");
    } else if (qName.equals("wd")) {
      wdLemma = attrs.getValue("lemma");
      wdPos = attrs.getValue("pos");
      inWord = true;
      wd = new StringBuilder();
    }
  }

  @Override
  public void endElement(final String namespaceURI, final String sName,
      final String qName) throws SAXException {
    if (qName.equals("rule")) {
      final DisambiguationPatternRule rule = new DisambiguationPatternRule(id,
          name, language, elementList, disambiguatedPOS, posSelector,
          disambigAction);
      rule.setStartPositionCorrection(positionCorrection);
      if (!singleTokenCorrection) {
        rule.setEndPositionCorrection(endPositionCorrection);
      }
      if (newWdList != null) {
        if (disambigAction == DisambiguatorAction.ADD
            || disambigAction == DisambiguatorAction.REMOVE) {
          if (newWdList.size() != (elementList.size() - positionCorrection + endPositionCorrection)) {
            throw new SAXException(
                "Rule error. The number of interpretations specified with wd: "
                    + newWdList.size()
                    + " must be equal to the number of matched tokens."
                    + "\n Line: " + dLocator.getLineNumber() + ", column: "
                    + dLocator.getColumnNumber() + ".");
          }
          rule.setNewInterpretations(newWdList
              .toArray(new AnalyzedToken[newWdList.size()]));
        }
        newWdList.clear();
      }
      caseSensitive = false;
      rules.add(rule);
      if (elementList != null) {
        if (disambigAction == DisambiguatorAction.UNIFY
            && (elementList.size() - positionCorrection + endPositionCorrection) != uniCounter) {
          throw new SAXException("Rule error. The number unified tokens: "
              + uniCounter + " must be equal to the number of matched tokens."
              + "\n Line: " + dLocator.getLineNumber() + ", column: "
              + dLocator.getColumnNumber() + ".");
        }
        if ((!singleTokenCorrection && (disambigAction == DisambiguatorAction.FILTER || disambigAction == DisambiguatorAction.REPLACE))
            && ((elementList.size() - positionCorrection + endPositionCorrection) > 1)) {
          throw new SAXException(
              "Rule error. Cannot replace or filter more than one token at a time."
                  + "\n Line: " + dLocator.getLineNumber() + ", column: "
                  + dLocator.getColumnNumber() + ".");
        }

        elementList.clear();
      }
      posSelector = null;
    } else if (qName.equals("exception")) {
      inException = false;
      if (!exceptionSet) {
        tokenElement = new Element(elements.toString(), caseSensitive,
            stringRegExp, tokenInflected);
        exceptionSet = true;
      }
      tokenElement.setNegation(tokenNegated);
      if (!StringTools.isEmpty(exceptions.toString())) {
        tokenElement.setStringException(exceptions.toString(),
            exceptionStringRegExp, exceptionStringInflected,
            exceptionStringNegation, exceptionValidNext, exceptionValidPrev);
      }
      if (exceptionPosToken != null) {
        tokenElement.setPosException(exceptionPosToken, exceptionPosRegExp,
            exceptionPosNegation, exceptionValidNext, exceptionValidPrev);
        exceptionPosToken = null;
      }
      if (exceptionSpaceBeforeSet) {
        tokenElement.setExceptionSpaceBefore(exceptionSpaceBefore);
      }
      resetException();
    } else if (qName.equals("and")) {
      inAndGroup = false;
      andGroupCounter = 0;
    } else if (qName.equals("unify")) {
      inUnification = false;
    } else if (qName.equals("token")) {
      if (!exceptionSet || tokenElement == null) {
        tokenElement = new Element(elements.toString(), caseSensitive,
            stringRegExp, tokenInflected);
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
        tokenElement.setUnification(uFeature, uType);
        if (uniNegation) {
          tokenElement.setUniNegation();
        }
        uniCounter++;
      }
      if (inUnificationDef) {
        language.getUnifier().setEquivalence(uFeature, uType, tokenElement);
        if (elementList != null) {
          elementList.clear();
        }
      }
      if (tokenSpaceBeforeSet) {
        tokenElement.setWhitespaceBefore(tokenSpaceBefore);
      }
      resetToken();
    } else if (qName.equals("pattern")) {
      inPattern = false;
    } else if (qName.equals("match")) {
      if (inDisamb) {
        posSelector.setLemmaString(match.toString());
      } else if (inToken) {
        tokenReference.setLemmaString(match.toString());
      }
      inMatch = false;
    } else if (qName.equals("disambig")) {
      inDisamb = false;
    } else if (qName.equals("rulegroup")) {
      inRuleGroup = false;
    } else if (qName.equals("unification") && inUnificationDef) {
      inUnificationDef = false;
    } else if (qName.equals("unify") && inUnification) {
      inUnification = false;
    } else if (qName.equals("wd")) {
      addNewWord(wd.toString(), wdLemma, wdPos);
      inWord = false;
    }
  }

  private void resetToken() {
    tokenNegated = false;
    tokenInflected = false;
    posNegation = false;
    posRegExp = false;
    inToken = false;
    stringRegExp = false;
    tokenSpaceBefore = false;
    tokenSpaceBeforeSet = false;

    resetException();
    exceptionSet = false;
    tokenReference = null;
  }

  private void resetException() {
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
    } else if (inDisamb) {
      disamb.append(s);
    }
  }

}
