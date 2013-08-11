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
  protected final List<Element> patternElements;
  protected final boolean testUnification;
  protected final boolean sentStart;

  protected String subId; // because there can be more than one rule in a rule group
  protected int startPositionCorrection;
  protected int endPositionCorrection;

  private final String id;
  private final String description;
  private final boolean getUnified;

  private boolean groupsOrUnification;

  public AbstractPatternRule(final String id, 
      final String description,
      final Language language,
      final List<Element> elements,
      boolean getUnified) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.description = Objects.requireNonNull(description, "description cannot be null");
    this.patternElements = new ArrayList<>(Objects.requireNonNull(elements, "elements cannot be null")); // copy elements
    this.language = Objects.requireNonNull(language, "language cannot be null");
    this.getUnified = getUnified;
    testUnification = initUnifier();
    sentStart = patternElements.size() > 0 && patternElements.get(0).isSentStart();    
    if (!testUnification) {
      for (Element elem : patternElements) {
        if (elem.hasAndGroup()) {
          groupsOrUnification = true;
          break;
        }
      }
    } else {
      groupsOrUnification = true;
    }
  }

  private boolean initUnifier() {
    for (final Element elem : patternElements) {
      if (elem.isUnified()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return id + "[" + subId + "]:" + patternElements + ":" + description;
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
  public RuleMatch[] match(AnalyzedSentence text) throws IOException {
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
    return this.startPositionCorrection;
  }

  public final void setEndPositionCorrection(final int endPositionCorrection) {
    this.endPositionCorrection = endPositionCorrection;
  }

  public final int getEndPositionCorrection() {
    return this.endPositionCorrection;
  }

  public final String getSubId() {
    return subId;
  }

  public final void setSubId(final String subId) {
    this.subId = subId;
  }

  public boolean isGroupsOrUnification() {
	return groupsOrUnification;
  }
  
  public boolean isGetUnified() {
	return getUnified;
  }
  
  public boolean isSentStart() {
	return sentStart;
  }
  
  public boolean isTestUnification() {
	return testUnification;
  }
  
  public List<Element> getPatternElements() {
	return patternElements;
  }
}
