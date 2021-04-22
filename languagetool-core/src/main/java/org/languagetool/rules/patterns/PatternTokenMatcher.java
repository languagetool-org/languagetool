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
public class PatternTokenMatcher {

  private final PatternToken basePatternToken;

  private PatternToken patternToken;
  private List<PatternTokenMatcher> andGroup;
  private boolean[] andGroupCheck;

  public PatternTokenMatcher(PatternToken patternToken) {
    basePatternToken = patternToken;
    this.patternToken = basePatternToken;
    if (basePatternToken.hasAndGroup()) {
      List<PatternToken> patternTokenAndGroup = basePatternToken.getAndGroup();
      andGroup = new ArrayList<>(patternTokenAndGroup.size());
      for (PatternToken el : patternTokenAndGroup) {
        PatternTokenMatcher matcher = new PatternTokenMatcher(el);
        andGroup.add(matcher);
      }
    }
  }

  public void resolveReference(int firstMatchToken,
      AnalyzedTokenReadings[] tokens, Language language) throws IOException {
    if (basePatternToken.isReferenceElement()) {
      int refPos = firstMatchToken + basePatternToken.getMatch().getTokenRef();
      if (refPos < tokens.length) {
        patternToken = basePatternToken.compile(tokens[refPos], language.getSynthesizer());
      }
    }
  }

  public PatternToken getPatternToken() {
    return basePatternToken;
  }

  /**
   * Checks whether the rule element matches the token given as a parameter.
   *
   * @param token AnalyzedToken to check matching against
   * @return True if token matches, false otherwise.
   */
  public final boolean isMatched(AnalyzedToken token) {
    boolean matched = patternToken.isMatched(token);
    if (patternToken.hasAndGroup()) {
      andGroupCheck[0] |= matched;
    }
    return matched;
  }

  void prepareAndGroup(int firstMatchToken, AnalyzedTokenReadings[] tokens, Language language) throws IOException {
    if (basePatternToken.hasAndGroup()) {
      for (PatternTokenMatcher andMatcher : andGroup) {
        andMatcher.resolveReference(firstMatchToken, tokens, language);
      }
      andGroupCheck = new boolean[patternToken.getAndGroup().size() + 1];
      Arrays.fill(andGroupCheck, false);
    }
  }

  /**
   * Enables testing multiple conditions specified by different elements.
   * Doesn't test exceptions.
   *
   * Works as logical AND operator only if preceded with
   * {@link #prepareAndGroup(int, AnalyzedTokenReadings[], Language)}, and followed by {@link #checkAndGroup(boolean)}
   *
   * @param token the token checked.
   */
  public final void addMemberAndGroup(AnalyzedToken token) {
    if (patternToken.hasAndGroup()) {
      List<PatternTokenMatcher> andGroupList = andGroup;
      for (int i = 0; i < andGroupList.size(); i++) {
        if (!andGroupCheck[i + 1]) {
          PatternTokenMatcher testAndGroup = andGroupList.get(i);
          if (testAndGroup.isMatched(token)) {
            andGroupCheck[i + 1] = true;
          }
        }
      }
    }
  }

  public final boolean checkAndGroup(boolean previousValue) {
    if (patternToken.hasAndGroup()) {
      boolean allConditionsMatch = true;
      for (boolean testValue : andGroupCheck) {
        allConditionsMatch &= testValue;
      }
      return allConditionsMatch;
    }
    return previousValue;
  }

  public final boolean isMatchedByScopeNextException(AnalyzedToken token) {
    return patternToken.isMatchedByScopeNextException(token);
  }

  public final boolean isExceptionMatchedCompletely(AnalyzedToken token) {
    return patternToken.isExceptionMatchedCompletely(token);
  }

  public boolean hasPreviousException() {
    return patternToken.hasPreviousException();
  }

  public boolean isMatchedByPreviousException(AnalyzedTokenReadings token) {
    return patternToken.isMatchedByPreviousException(token);
  }

  @Override
  public String toString() {
    return "PatternTokenMatcher for " + basePatternToken;
  }
}
