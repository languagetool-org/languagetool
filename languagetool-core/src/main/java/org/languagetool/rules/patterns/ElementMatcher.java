package org.languagetool.rules.patterns;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;

public class ElementMatcher {
	private final Element baseElement;
	private Element element;

	private List<ElementMatcher> andGroup;
	private boolean[] andGroupCheck;

	public ElementMatcher(Element element) {
		this.baseElement = element;
		this.element = this.baseElement;
		
		this.resolveGroup();
	}
	
	private void resolveGroup() {
		if (this.baseElement.hasAndGroup()) {
			List<Element> elementAndGroup = this.baseElement.getAndGroup(); 
			this.andGroup = new ArrayList<ElementMatcher>(elementAndGroup.size());
			
			for (Element el : elementAndGroup) {
				ElementMatcher matcher = new ElementMatcher(el);
				this.andGroup.add(matcher);
			}
		}
	}

	// TODO: add .compile for all exceptions of the element?
	public void resolveReference(final int firstMatchToken,
			final AnalyzedTokenReadings[] tokens, Language language)
			throws IOException {
		if (this.baseElement.isReferenceElement()) {
			final int refPos = firstMatchToken
					+ this.baseElement.getMatch().getTokenRef();
			if (refPos < tokens.length) {
				this.element = this.baseElement.compile(tokens[refPos],
						language.getSynthesizer());
			}
		}
	}

	public Element getElement() {
		return this.baseElement;
	}

	/**
	 * Checks whether the rule element matches the token given as a parameter.
	 * 
	 * @param token
	 *            AnalyzedToken to check matching against
	 * @return True if token matches, false otherwise.
	 */
	public final boolean isMatched(final AnalyzedToken token) {
		boolean matched = element.isMatched(token);
		if (element.hasAndGroup()) {
			andGroupCheck[0] |= matched;
		}
		return matched;
	}
	
	void prepareAndGroup(int firstMatchToken, AnalyzedTokenReadings[] tokens, Language language) throws IOException {
		if (this.baseElement.hasAndGroup()) {
			for (ElementMatcher andMatcher : this.andGroup) {
				andMatcher.resolveReference(firstMatchToken, tokens, language);
			}
			
			andGroupCheck = new boolean[element.getAndGroup().size() + 1];
			Arrays.fill(andGroupCheck, false);
		}
	}

	/**
	 * Enables testing multiple conditions specified by different elements.
	 * Doesn't test exceptions.
	 * 
	 * Works as logical AND operator only if preceded with
	 * {@link #setupAndGroup()}, and followed by {@link #checkAndGroup(boolean)}
	 * .
	 * 
	 * @param token
	 *            the token checked.
	 */
	public final void addMemberAndGroup(final AnalyzedToken token) {
		if (element.hasAndGroup()) {
			List<ElementMatcher> andGroupList = this.andGroup;
			for (int i = 0; i < andGroupList.size(); i++) {
				if (!andGroupCheck[i + 1]) {
					final ElementMatcher testAndGroup = andGroupList.get(i);
					if (testAndGroup.isMatched(token)) {
						andGroupCheck[i + 1] = true;
					}
				}
			}
		}
	}

	public final boolean checkAndGroup(final boolean previousValue) {
		if (element.hasAndGroup()) {
			boolean allConditionsMatch = true;
			for (final boolean testValue : andGroupCheck) {
				allConditionsMatch &= testValue;
			}
			return allConditionsMatch;
		}
		return previousValue;
	}
	
	public final boolean isMatchedByScopeNextException(final AnalyzedToken token) {
		return this.element.isMatchedByScopeNextException(token);
	}
	
	public final boolean isExceptionMatchedCompletely(final AnalyzedToken token) {
		return this.element.isExceptionMatchedCompletely(token);
	}
	
	public boolean hasPreviousException() {
		return this.element.hasPreviousException();
	}
	
	public boolean isMatchedByPreviousException(AnalyzedTokenReadings token) {
		return this.element.isMatchedByPreviousException(token);
	}
}
