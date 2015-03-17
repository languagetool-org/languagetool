/* LanguageTool, a natural language style checker 
 * Copyright (C) 2008 Daniel Naber (http://www.danielnaber.de)
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
import java.util.List;
import java.util.Objects;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * An Abstract Pattern Rule that describes a pattern of words or part-of-speech tags 
 * used for PatternRule and DisambiguationPatternRule.
 * 
 * Introduced to minimize code duplication between those classes.
 * 
 * @author Marcin Miłkowski
 */
public abstract class AbstractPatternRule extends Rule {

  protected final Language language;
  protected final List<PatternToken> patternTokens;
  protected final boolean testUnification;
  protected final boolean sentStart;

  protected String subId; // because there can be more than one rule in a rule group
  protected int startPositionCorrection;
  protected int endPositionCorrection;

  private final String id;
  private final String description;
  private final boolean getUnified;
  private final boolean groupsOrUnification;

  public AbstractPatternRule(final String id, 
      final String description,
      final Language language,
      final List<PatternToken> patternTokens,
      final boolean getUnified) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.description = Objects.requireNonNull(description, "description cannot be null");
    this.patternTokens = new ArrayList<>(Objects.requireNonNull(patternTokens, "patternTokens cannot be null")); // copy elements
    this.language = Objects.requireNonNull(language, "language cannot be null");
    this.getUnified = getUnified;
    testUnification = initUnifier();
    sentStart = this.patternTokens.size() > 0 && this.patternTokens.get(0).isSentenceStart();
    if (!testUnification) {
      boolean found = false;
      for (PatternToken elem : this.patternTokens) {
        if (elem.hasAndGroup()) {
          found = true;
          break;
        }
      }
      groupsOrUnification = found;
    } else {
      groupsOrUnification = true;
    }
  }

  @Override
  public boolean supportsLanguage(final Language language) {
    return language.equalsConsiderVariantsIfSpecified(this.language);
  }

  private boolean initUnifier() {
    for (final PatternToken pToken : patternTokens) {
      if (pToken.isUnified()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return id + "[" + subId + "]:" + patternTokens + ":" + description;
  }

  @Override
  public String getDescription() {
    return description;
  }

  @Override
  public String getId() {
    return id;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    return null;
  }

  @Override
  public void reset() {
  }

  /**
   * @since 2.3
   */
  public final Language getLanguage() {
    return language;
  }

  public final void setStartPositionCorrection(final int startPositionCorrection) {
    this.startPositionCorrection = startPositionCorrection;
  }

  public final int getStartPositionCorrection() {
    return startPositionCorrection;
  }

  public final void setEndPositionCorrection(final int endPositionCorrection) {
    this.endPositionCorrection = endPositionCorrection;
  }

  public final int getEndPositionCorrection() {
    return endPositionCorrection;
  }

  public final String getSubId() {
    return subId;
  }

  public final void setSubId(final String subId) {
    this.subId = subId;
  }

  /**
   * @since 2.3
   */
  public boolean isGroupsOrUnification() {
    return groupsOrUnification;
  }

  /**
   * @since 2.3
   */
  public boolean isGetUnified() {
    return getUnified;
  }

  /**
   * @since 2.3
   */
  public boolean isSentStart() {
    return sentStart;
  }

  /**
   * @since 2.3
   */
  public boolean isTestUnification() {
    return testUnification;
  }

  /**
   * @since 2.3
   */
  public List<PatternToken> getPatternTokens() {
    return patternTokens;
  }
}
