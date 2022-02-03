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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Experimental;
import org.languagetool.Language;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.tagging.disambiguation.rules.DisambiguationPatternRule;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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
  protected List<Match> suggestionMatches;
  protected List<Match> suggestionMatchesOutMsg;
  protected List<DisambiguationPatternRule> antiPatterns;

  protected String subId; // because there can be more than one rule in a rule group
  protected int startPositionCorrection;
  protected int endPositionCorrection;
  protected String suggestionsOutMsg; // extra suggestions outside message
  protected RuleFilter filter;
  protected String filterArgs;
  protected String message;
  protected String sourceFile = null;
  protected RuleMatch.Type type = null; // allow setting custom match types without relying on IssueType

  private final String id;
  private final String description;
  private final boolean getUnified;
  private final boolean groupsOrUnification;

  protected AbstractPatternRule(String id, String description, Language language) {
    this(id, description, language, null, false);
  }

  public AbstractPatternRule(String id, String description, Language language, List<PatternToken> patternTokens, boolean getUnified, String message) {
    this(id, description, language, patternTokens, getUnified);
    this.message = message;
  }

  public AbstractPatternRule(String id, String description, Language language, List<PatternToken> patternTokens, boolean getUnified) {
    this.id = Objects.requireNonNull(id, "id cannot be null");
    this.description = Objects.requireNonNull(description, "description ('name' in XML) cannot be null");
    this.language = Objects.requireNonNull(language, "language cannot be null");
    this.getUnified = getUnified;
    if (patternTokens != null) {
      this.patternTokens = new ArrayList<>(patternTokens);
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
    } else {
      this.patternTokens = null;
      groupsOrUnification = false;
      sentStart = false;
      testUnification = false;
    }
  }

  @Override
  public boolean supportsLanguage(Language language) {
    return language.equalsConsiderVariantsIfSpecified(this.language);
  }

  private boolean initUnifier() {
    for (PatternToken pToken : patternTokens) {
      if (pToken.isUnified()) {
        return true;
      }
    }
    return false;
  }

  @Override
  public String toString() {
    return id + "[" + subId + "]" + (sourceFile != null ? "@" + sourceFile : "" ) + ":" + patternTokens + ":" + description;
  }

  @Override
  public String getDescription() {
    return description;
  }


  @Nullable
  public String getSourceFile() {
    return sourceFile;
  }

  void setSourceFile(String sourceFile) {
    this.sourceFile = sourceFile;
  }

  /**
   * @see #getFullId() 
   */
  @Override
  public String getId() {
    return id;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    return null;
  }

  /**
   * @since 2.3
   */
  public final Language getLanguage() {
    return language;
  }

  public final void setStartPositionCorrection(int startPositionCorrection) {
    this.startPositionCorrection = startPositionCorrection;
  }

  public final int getStartPositionCorrection() {
    return startPositionCorrection;
  }

  public final void setEndPositionCorrection(int endPositionCorrection) {
    this.endPositionCorrection = endPositionCorrection;
  }

  public final int getEndPositionCorrection() {
    return endPositionCorrection;
  }

  /**
   * The rule id and its sub id, if any. The format is like {@code RULE_ID[SUB_ID]}, e.g. {@code WANT_TO[2]}.
   * @since 3.2
   * @see #getId()
   */
  @Override
  public String getFullId() {
    if (subId != null) {
      return id + "[" + subId + "]";
    } else {
      return id;
    }
  }

  /**
   * The rule id and its sub id, if any.
   * @since 3.2
   */
  public PatternRuleId getPatternRuleId() {
    if (subId != null) {
      return new PatternRuleId(id, subId);
    } else {
      return new PatternRuleId(id);
    }
  }

  public final String getSubId() {
    return subId;
  }

  public final void setSubId(String subId) {
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

  /** Add formatted suggestion elements. */
  public final void addSuggestionMatch(Match m) {
    if (suggestionMatches == null) {
      suggestionMatches = new ArrayList<>(0);
    }
    suggestionMatches.add(m);
  }

  /** Add formatted suggestion elements outside message. */
  public final void addSuggestionMatchOutMsg(Match m) {
    if (suggestionMatchesOutMsg == null) {
      suggestionMatchesOutMsg = new ArrayList<>(0);
    }
    suggestionMatchesOutMsg.add(m);
  }
  
  List<Match> getSuggestionMatches() {
    return suggestionMatches == null ? Collections.emptyList() : suggestionMatches;
  }

  List<Match> getSuggestionMatchesOutMsg() {
    return suggestionMatchesOutMsg == null ? Collections.emptyList() : suggestionMatchesOutMsg;
  }

  @NotNull
  public final String getSuggestionsOutMsg() {
    return suggestionsOutMsg;
  }
  
  /**
   * Get the message shown to the user if this rule matches.
   */
  public final String getMessage() {
    return message;
  }

  /**
   * Set the message shown to the user if this rule matches.
   */
  public final void setMessage(String message) {
    this.message = message;
  }

  /** @since 2.7 (public since 3.2) */
  public void setFilter(RuleFilter filter) {
    this.filter = filter;
  }

  /** @since 2.7 (public since 3.2) */
  @Nullable
  public RuleFilter getFilter() {
    return filter;
  }

  /** @since 2.7 (public since 3.2) */
  public void setFilterArguments(String filterArgs) {
    this.filterArgs = filterArgs;
  }

  /** @since 2.7 (public since 3.2) */
  @Nullable
  public String getFilterArguments() {
    return filterArgs;
  }

  /**
   * Set up the list of antipatterns used to immunize tokens, i.e., make them
   * non-matchable by the current rule. Useful for multi-word complex exceptions,
   * such as multi-word idiomatic expressions.
   * @param antiPatterns A list of antiPatterns, implemented as {@code DisambiguationPatternRule}.
   * @since 2.5
   */
  public void setAntiPatterns(List<DisambiguationPatternRule> antiPatterns) {
    if (this.antiPatterns == null) {
      this.antiPatterns = new ArrayList<>(0);
    }
    this.antiPatterns.addAll(antiPatterns);
  }

  /**
   * @since 3.1
   */
  @Override
  public final List<DisambiguationPatternRule> getAntiPatterns() {
    return antiPatterns == null ? Collections.emptyList() : Collections.unmodifiableList(antiPatterns);
  }

  /**
   * @return String of short message as specified in &lt;short&gt;...&lt;/short&gt;
   * @since 4.4
   */
  String getShortMessage() {
  	return StringUtils.EMPTY;
  }

  /**
   * Determines the match type, based on the type variable if set (to allow overriding) or {@link org.languagetool.rules.Rule#getLocQualityIssueType()}
   * @since 5.7
   * @return The match type for the matches created by this rule
   */
  @Nullable
  @Experimental
  public RuleMatch.Type getType() {
    if (type == null) {
      ITSIssueType issueType = getLocQualityIssueType();
      if (issueType == ITSIssueType.Style || issueType == ITSIssueType.LocaleViolation || issueType == ITSIssueType.Register) {
        // interpret the issue type - this is what the clients have done so far before there was RuleMatch.Type
        return RuleMatch.Type.Hint;
      } else {
        // default type as defined in RuleMatch
        return RuleMatch.Type.Other;
      }
    }
    return type;
  }

  /**
   * Allows overriding the match type, otherwise determined by {@link org.languagetool.rules.Rule#getLocQualityIssueType()}
   * @since 5.7
   * @param type the desired match type
   */
  @Experimental
  public void setType(RuleMatch.Type type) {
    this.type = type;
  }

}
