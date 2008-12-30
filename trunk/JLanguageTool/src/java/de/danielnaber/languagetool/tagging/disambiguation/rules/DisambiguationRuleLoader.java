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

	private static final String NO = "no";
	private static final String MARK_TO = "mark_to";
	private static final String MARK_FROM = "mark_from";
	private static final String MARK = "mark";
	private static final String WD = "wd";
	private static final String PATTERN = "pattern";
	private static final String MATCH = "match";
	private static final String UNIFICATION = "unification";
	private static final String RULEGROUP = "rulegroup";
	private static final String ACTION = "action";
	private static final String DISAMBIG = "disambig";
	private static final String IGNORE = "ignore";
	private static final String SKIP = "skip";
	private static final String TOKEN = "token";
	private static final String TYPE = "type";
	private static final String FEATURE = "feature";
	private static final String UNIFY = "unify";
	private static final String AND = "and";
	private static final String SPACEBEFORE = "spacebefore";
	private static final String REGEXP = "regexp";
	private static final String NEGATE_POS = "negate_pos";
	private static final String POSTAG_REGEXP = "postag_regexp";
	private static final String INFLECTED = "inflected";
	private static final String POSTAG = "postag";
	private static final String SCOPE = "scope";
	private static final String NEGATE = "negate";
	private static final String EXCEPTION = "exception";
	private static final String CASE_SENSITIVE = "case_sensitive";
	private static final String YES = "yes";
	
	private boolean caseSensitive = false;
	private boolean stringRegExp = false;
	private boolean tokenNegated = false;
	private boolean tokenInflected = false;
	private boolean posNegation = false;
	private boolean tokenSpaceBefore = false;
	private boolean tokenSpaceBeforeSet = false;

	private String posToken;

	private String exceptionPosToken;
	private boolean exceptionStringRegExp;
	private boolean exceptionStringNegation;
	private boolean exceptionStringInflected;
	private boolean exceptionPosNegation;
	private boolean exceptionPosRegExp;
	private boolean exceptionValidNext;
	private boolean exceptionValidPrev;
	private boolean exceptionSet;
	private boolean exceptionSpaceBefore;
	private boolean exceptionSpaceBeforeSet;

	private List<Element> elementList;
	private boolean posRegExp;
	private int skipPos;
	private Element tokenElement;

	private String id;
	private String name;
	private Language language;
	private String ruleGroupId;
	private String ruleGroupName;
	private StringBuilder disamb = new StringBuilder();
	private StringBuilder match = new StringBuilder();
	private StringBuilder wd = new StringBuilder();

	private boolean inWord;

	private String disambiguatedPOS;

	private int positionCorrection;
	private int endPositionCorrection;
	private boolean singleTokenCorrection;

	private int andGroupCounter;

	private Match tokenReference;

	private Match posSelector;

	private boolean inUnification;
	private boolean inUnificationDef;
	private boolean uniNegation;

	private String uFeature;
	private String uType = "";

	private int uniCounter;

	private List<AnalyzedToken> newWdList;
	private String wdLemma;
	private String wdPos;

	private Locator dLocator;

	private DisambiguationPatternRule.DisambiguatorAction disambigAction;

	public DisambiguationRuleHandler() {
		elementList = new ArrayList<Element>();
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
		} else if (qName.equals(PATTERN)) {
			inPattern = true;
			if (attrs.getValue(MARK) != null
					&& (attrs.getValue(MARK_FROM) != null)) {
				throw new SAXException(
						"You cannot use both mark and mark_from attributes."
								+ "\n Line: " + dLocator.getLineNumber()
								+ ", column: " + dLocator.getColumnNumber()
								+ ".");
			}
			if (attrs.getValue(MARK) != null
					&& (attrs.getValue(MARK_TO) != null)) {
				throw new SAXException(
						"You cannot use both mark and mark_to attributes."
								+ "\n Line: " + dLocator.getLineNumber()
								+ ", column: " + dLocator.getColumnNumber()
								+ ".");
			}

			if (attrs.getValue(MARK) != null) {
				positionCorrection = Integer.parseInt(attrs.getValue(MARK));
			}
			if (attrs.getValue(MARK_FROM) != null) {
				positionCorrection = Integer
						.parseInt(attrs.getValue(MARK_FROM));
			}
			if (attrs.getValue(MARK_TO) != null) {
				endPositionCorrection = Integer.parseInt(attrs
						.getValue(MARK_TO));
				singleTokenCorrection = false;
			} else {
				singleTokenCorrection = true;
			}
			if (attrs.getValue(CASE_SENSITIVE) != null
					&& YES.equals(attrs.getValue(CASE_SENSITIVE))) {
				caseSensitive = true;
			}
		} else if (qName.equals(EXCEPTION)) {
			inException = true;
			exceptions = new StringBuilder();

			if (attrs.getValue(NEGATE) != null) {
				exceptionStringNegation = attrs.getValue(NEGATE).equals(YES);
			}
			if (attrs.getValue(SCOPE) != null) {
				exceptionValidNext = attrs.getValue(SCOPE).equals("next");
				exceptionValidPrev = attrs.getValue(SCOPE).equals("previous");
			}
			if (attrs.getValue(INFLECTED) != null) {
				exceptionStringInflected = attrs.getValue(INFLECTED)
						.equals(YES);
			}
			if (attrs.getValue(POSTAG) != null) {
				exceptionPosToken = attrs.getValue(POSTAG);
				if (attrs.getValue(POSTAG_REGEXP) != null) {
					exceptionPosRegExp = attrs.getValue(POSTAG_REGEXP).equals(
							YES);
				}
				if (attrs.getValue(NEGATE_POS) != null) {
					exceptionPosNegation = attrs.getValue(NEGATE_POS).equals(
							YES);
				}
			}
			if (attrs.getValue(REGEXP) != null) {
				exceptionStringRegExp = attrs.getValue(REGEXP).equals(YES);
			}
			if (attrs.getValue(SPACEBEFORE) != null) {
				exceptionSpaceBefore = YES.equals(attrs.getValue(SPACEBEFORE));
				exceptionSpaceBeforeSet = IGNORE.equals(attrs
						.getValue(SPACEBEFORE)) ^ true;
			}
		} else if (qName.equals(AND)) {
			inAndGroup = true;
		} else if (qName.equals(UNIFY)) {
			inUnification = true;
			uFeature = attrs.getValue(FEATURE);
			if (attrs.getValue(TYPE) != null) {
				uType = attrs.getValue(TYPE);
			} else {
				uType = "";
			}
			if (attrs.getValue(NEGATE) != null
					&& YES.equals(attrs.getValue(NEGATE))) {
				uniNegation = true;
			}
			uniCounter = 0;
		} else if (qName.equals(TOKEN)) {
			inToken = true;
			if (attrs.getValue(NEGATE) != null) {
				tokenNegated = attrs.getValue(NEGATE).equals(YES);
			}
			if (attrs.getValue(INFLECTED) != null) {
				tokenInflected = attrs.getValue(INFLECTED).equals(YES);
			}
			if (attrs.getValue(SKIP) != null) {
				skipPos = Integer.parseInt(attrs.getValue(SKIP));
			}
			elements = new StringBuilder();
			if (attrs.getValue(POSTAG) != null) {
				posToken = attrs.getValue(POSTAG);
				if (attrs.getValue(POSTAG_REGEXP) != null) {
					posRegExp = attrs.getValue(POSTAG_REGEXP).equals(YES);
				}
				if (attrs.getValue(NEGATE_POS) != null) {
					posNegation = attrs.getValue(NEGATE_POS).equals(YES);
				}

			}
			if (attrs.getValue(REGEXP) != null) {
				stringRegExp = attrs.getValue(REGEXP).equals(YES);
			}
			if (attrs.getValue(SPACEBEFORE) != null) {
				tokenSpaceBefore = YES.equals(attrs.getValue(SPACEBEFORE));
				tokenSpaceBeforeSet = IGNORE
						.equals(attrs.getValue(SPACEBEFORE)) ^ true;
			}

		} else if (qName.equals(DISAMBIG)) {
			inDisamb = true;
			disambiguatedPOS = attrs.getValue(POSTAG);
			if (attrs.getValue(ACTION) != null) {
				disambigAction = DisambiguationPatternRule.DisambiguatorAction
						.toAction(attrs.getValue(ACTION).toUpperCase());
			} else {
				// default mode:
				disambigAction = DisambiguationPatternRule.DisambiguatorAction
						.toAction("REPLACE");
			}
			disamb = new StringBuilder();
		} else if (qName.equals(MATCH)) {
			inMatch = true;
			match = new StringBuilder();
			Match.CaseConversion caseConv = Match.CaseConversion.NONE;
			if (attrs.getValue("case_conversion") != null) {
				caseConv = Match.CaseConversion.toCase(attrs.getValue(
						"case_conversion").toUpperCase());
			}
			final Match mWorker = new Match(attrs.getValue(POSTAG), attrs
					.getValue("postag_replace"), YES.equals(attrs
					.getValue(POSTAG_REGEXP)), attrs.getValue("regexp_match"),
					attrs.getValue("regexp_replace"), caseConv, YES
							.equals(attrs.getValue("setpos")));
			if (inDisamb) {
				if (attrs.getValue(NO) != null) {
					final int refNumber = Integer.parseInt(attrs.getValue(NO));
					if (refNumber > elementList.size()) {
						throw new SAXException(
								"Only backward references in match elements are possible, tried to specify token "
										+ refNumber
										+ "\n Line: "
										+ dLocator.getLineNumber()
										+ ", column: "
										+ dLocator.getColumnNumber() + ".");
					} else {
						mWorker.setTokenRef(refNumber);
						posSelector = mWorker;
					}
				}
			} else if (inToken) {
				if (attrs.getValue(NO) != null) {
					final int refNumber = Integer.parseInt(attrs.getValue(NO));
					if (refNumber > elementList.size()) {
						throw new SAXException(
								"Only backward references in match elements are possible, tried to specify token "
										+ refNumber
										+ "\n Line: "
										+ dLocator.getLineNumber()
										+ ", column: "
										+ dLocator.getColumnNumber() + ".");
					} else {
						mWorker.setTokenRef(refNumber);
						tokenReference = mWorker;
						elements.append("\\" + refNumber);
					}
				}
			}
		} else if (qName.equals(RULEGROUP)) {
			ruleGroupId = attrs.getValue("id");
			ruleGroupName = attrs.getValue("name");
			inRuleGroup = true;
		} else if (qName.equals(UNIFICATION)) {
			uFeature = attrs.getValue(FEATURE);
			inUnificationDef = true;
		} else if (qName.equals("equivalence")) {
			uType = attrs.getValue(TYPE);
		} else if (qName.equals(WD)) {
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
			final DisambiguationPatternRule rule = new DisambiguationPatternRule(
					id, name, language, elementList, disambiguatedPOS,
					posSelector, disambigAction);
			rule.setStartPositionCorrection(positionCorrection);
			if (!singleTokenCorrection) {
				rule.setEndPositionCorrection(endPositionCorrection);
			}
			if (newWdList != null) {
				if (disambigAction == DisambiguatorAction.ADD
						|| disambigAction == DisambiguatorAction.REMOVE) {
					if (newWdList.size() != (elementList.size()
							- positionCorrection + endPositionCorrection)) {
						throw new SAXException(
								"Rule error. The number of interpretations specified with wd: "
										+ newWdList.size()
										+ " must be equal to the number of matched tokens."
										+ "\n Line: "
										+ dLocator.getLineNumber()
										+ ", column: "
										+ dLocator.getColumnNumber() + ".");
					}
					rule.setNewInterpretations(newWdList
							.toArray(new AnalyzedToken[newWdList.size()]));
				}
				newWdList.clear();
			}
			caseSensitive = false;
			rules.add(rule);
			if (disambigAction == DisambiguatorAction.UNIFY
					&& (elementList.size() - positionCorrection + endPositionCorrection) != uniCounter) {
				throw new SAXException(
						"Rule error. The number unified tokens: "
								+ uniCounter
								+ " must be equal to the number of matched tokens."
								+ "\n Line: " + dLocator.getLineNumber()
								+ ", column: " + dLocator.getColumnNumber()
								+ ".");
			}
			if ((!singleTokenCorrection && (disambigAction == DisambiguatorAction.FILTER || disambigAction == DisambiguatorAction.REPLACE))
					&& ((elementList.size() - positionCorrection + endPositionCorrection) > 1)) {
				throw new SAXException(
						"Rule error. Cannot replace or filter more than one token at a time."
								+ "\n Line: " + dLocator.getLineNumber()
								+ ", column: " + dLocator.getColumnNumber()
								+ ".");
			}
			elementList.clear();
			posSelector = null;
		} else if (qName.equals(EXCEPTION)) {
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
						exceptionStringNegation, exceptionValidNext,
						exceptionValidPrev);
			}
			if (exceptionPosToken != null) {
				tokenElement.setPosException(exceptionPosToken,
						exceptionPosRegExp, exceptionPosNegation,
						exceptionValidNext, exceptionValidPrev);
				exceptionPosToken = null;
			}
			if (exceptionSpaceBeforeSet) {
				tokenElement.setExceptionSpaceBefore(exceptionSpaceBefore);
			}
			resetException();
		} else if (qName.equals(AND)) {
			inAndGroup = false;
			andGroupCounter = 0;
		} else if (qName.equals(UNIFY)) {
			inUnification = false;
		} else if (qName.equals(TOKEN)) {
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
				elementList.get(elementList.size() - 1).setAndGroupElement(
						tokenElement);
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
				language.getUnifier().setEquivalence(uFeature, uType,
						tokenElement);
				elementList.clear();
			}
			if (tokenSpaceBeforeSet) {
				tokenElement.setWhitespaceBefore(tokenSpaceBefore);
			}
			resetToken();
		} else if (qName.equals(PATTERN)) {
			inPattern = false;
		} else if (qName.equals(MATCH)) {
			if (inDisamb) {
				posSelector.setLemmaString(match.toString());
			} else if (inToken) {
				tokenReference.setLemmaString(match.toString());
			}
			inMatch = false;
		} else if (qName.equals(DISAMBIG)) {
			inDisamb = false;
		} else if (qName.equals(RULEGROUP)) {
			inRuleGroup = false;
		} else if (qName.equals(UNIFICATION) && inUnificationDef) {
			inUnificationDef = false;
		} else if (qName.equals(UNIFY) && inUnification) {
			inUnification = false;
		} else if (qName.equals(WD)) {
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
	public final void characters(final char[] buf, final int offset,
			final int len) {
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
