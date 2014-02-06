/* LanguageTool, a natural language style checker
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
 * Copyright (C) 2013 Stefan Lotties
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.languagetool.AnalyzedToken;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.Language;

/**
 * @since 2.3
 */
public class ElementMatcher {

  private final Element baseElement;
  private Element element;
  private List<ElementMatcher> andGroup;
  private boolean[] andGroupCheck;

  public ElementMatcher(Element element) {
    baseElement = element;
    this.element = baseElement;
    resolveGroup();
  }

  private void resolveGroup() {
    if (baseElement.hasAndGroup()) {
      List<Element> elementAndGroup = baseElement.getAndGroup();
      andGroup = new ArrayList<>(elementAndGroup.size());

      for (Element el : elementAndGroup) {
        ElementMatcher matcher = new ElementMatcher(el);
        andGroup.add(matcher);
      }
    }
  }

  // TODO: add .compile for all exceptions of the element?
  public void resolveReference(final int firstMatchToken,
      final AnalyzedTokenReadings[] tokens, Language language)
          throws IOException {
    if (baseElement.isReferenceElement()) {
      final int refPos = firstMatchToken
          + baseElement.getMatch().getTokenRef();
      if (refPos < tokens.length) {
        element = baseElement.compile(tokens[refPos],
            language.getSynthesizer());
      }
    }
  }

  public Element getElement() {
    return baseElement;
  }

  /**
   * Checks whether the rule element matches the token given as a parameter.
   *
   * @param token AnalyzedToken to check matching against
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
    if (baseElement.hasAndGroup()) {
      for (ElementMatcher andMatcher : andGroup) {
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
   * {@link #prepareAndGroup(int, org.languagetool.AnalyzedTokenReadings[], org.languagetool.Language)}, and followed by {@link #checkAndGroup(boolean)}
   *
   * @param token the token checked.
   */
  public final void addMemberAndGroup(final AnalyzedToken token) {
    if (element.hasAndGroup()) {
      List<ElementMatcher> andGroupList = andGroup;
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
    return element.isMatchedByScopeNextException(token);
  }

  /**
   * Used to test exceptions with scope="optional".
   * @param token {@link AnalyzedToken} to check.
   * @return True if exception matches.
   * 
   * @since 2.5
   * 
   * 
   */
  public final boolean isMatchedByOptionalException(final AnalyzedToken token) {
    return element.isMatchedByOptionalException(token);
  }

  public final boolean isExceptionMatchedCompletely(final AnalyzedToken token) {
    return element.isExceptionMatchedCompletely(token);
  }

  public boolean hasPreviousException() {
    return element.hasPreviousException();
  }

  public boolean hasOptionalException() {
    boolean hasSubEx = false;
    if (element.getExceptionList() != null) {
      for (Element e : element.getExceptionList()) {
        if (e.hasOptionalException()) {
          return true;
        }
      }
    }
    return hasSubEx;
  }

  public boolean isMatchedByPreviousException(AnalyzedTokenReadings token) {
    return element.isMatchedByPreviousException(token);
  }

  @Override
  public String toString() {
    return "ElementMatcher for " + baseElement;
  }
}
