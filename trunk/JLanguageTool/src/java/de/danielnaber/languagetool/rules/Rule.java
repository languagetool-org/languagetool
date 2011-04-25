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
package de.danielnaber.languagetool.rules;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import de.danielnaber.languagetool.AnalyzedSentence;
import de.danielnaber.languagetool.Language;

/**
 * Abstract rule class. A Rule describes a language error and can test whether a
 * given pre-analyzed text contains that error using the {@link Rule#match(de.danielnaber.languagetool.AnalyzedSentence)}
 * method.
 * 
 * @author Daniel Naber
 */
public abstract class Rule {

  private List<String> correctExamples;
  private List<IncorrectExample> incorrectExamples;
  private Category category;
  /** If true, then the rule is turned off by default. */
  private boolean defaultOff;
  /** Used by paragraph rules to signal that they can remove previous rule matches */
  private boolean paragraphBackTrack;
  /** The final list of RuleMatches, without removed matches. */
  private List<RuleMatch> previousMatches;
  private List<RuleMatch> removedMatches;

  protected ResourceBundle messages;

  /**
   * Called by language-dependent rules.
   */
  public Rule() {
  }

  /**
   * Called by language-independent rules.
   */
  public Rule(final ResourceBundle messages) {
    this.messages = messages;
  }

  /**
   * An ASCII-only string used to identify the rule in e.g. configuration files.
   * This string is supposed to be unique and to stay the same in all upcoming
   * versions of LanguageTool.
   */
  public abstract String getId();

  public abstract String getDescription();

  /**
   * Check whether the given text matches this error rule, i.e. whether the text
   * contains this error.
   *
   * @param text
   *          a pre-analyzed sentence
   * @return an array of RuleMatch object for each match.
   */
  public abstract RuleMatch[] match(AnalyzedSentence text) throws IOException;

  /**
   * If a rule keeps its state over more than the check of one sentence, this
   * must be implemented so the internal state is reset. It will be called
   * before a new text is going to be checked.
   */
  public abstract void reset();

  /**
   * Whether this rule can be used for text in the given language.
   */
  public final boolean supportsLanguage(final Language language) {
    final List<Class<? extends Rule>> relevantRuleClasses = language.getRelevantRules();
    return relevantRuleClasses != null && relevantRuleClasses.contains(this.getClass());
  }

  /**
   * Set the examples that are correct and thus do not trigger the rule.
   */
  public final void setCorrectExamples(final List<String> correctExamples) {
    this.correctExamples = correctExamples;
  }

  /**
   * Get example sentences that are correct and thus will not match this rule.
   */
  public final List<String> getCorrectExamples() {
    return correctExamples;
  }

  /**
   * Set the examples that are incorrect and thus do trigger the rule.
   */
  public final void setIncorrectExamples(
      final List<IncorrectExample> incorrectExamples) {
    this.incorrectExamples = incorrectExamples;
  }

  /**
   * Get example sentences that are incorrect and thus will match this rule.
   */
  public final List<IncorrectExample> getIncorrectExamples() {
    return incorrectExamples;
  }

  public final Category getCategory() {
    return category;
  }

  public final void setCategory(final Category category) {
    this.category = category;
  }

  protected final RuleMatch[] toRuleMatchArray(final List<RuleMatch> ruleMatches) {
    return ruleMatches.toArray(new RuleMatch[ruleMatches.size()]);
  }

  public final boolean isParagraphBackTrack() {
    return paragraphBackTrack;
  }

  public final void setParagraphBackTrack(final boolean backTrack) {
    paragraphBackTrack = backTrack;
  }

  /**
   * Method to add matches.
   * 
   * @param ruleMatch
   *          RuleMatch - matched rule added by check()
   */
  public final void addRuleMatch(final RuleMatch ruleMatch) {
    if (previousMatches == null) {
      previousMatches = new ArrayList<RuleMatch>();
    }
    previousMatches.add(ruleMatch);
  }

  /**
   * Deletes (or disables) previously matched rule.
   * 
   * @param index
   *          Index of the rule that should be deleted.
   */
  public final void setAsDeleted(final int index) {
    if (removedMatches == null) {
      removedMatches = new ArrayList<RuleMatch>();
    }
    removedMatches.add(previousMatches.get(index));
  }

  public final boolean isInRemoved(final RuleMatch ruleMatch) {
    if (removedMatches == null) {
      return false;
    }
    return removedMatches.contains(ruleMatch);
  }

  public final boolean isInMatches(final int index) {
    if (previousMatches == null) {
      return false;
    }
    if (previousMatches.size() > index) {
      return previousMatches.get(index) != null;
    }
    return false;
  }

  public final void clearMatches() {
    if (previousMatches != null) {
      previousMatches.clear();
    }
  }

  public final int getMatchesIndex() {
    if (previousMatches == null) {
      return 0;
    }
    return previousMatches.size();
  }

  public final List<RuleMatch> getMatches() {
    return previousMatches;
  }

  /**
   * Checks whether the rule has been turned off by default by the rule author.
   * @return True if the rule is turned off by default.
   */
  public final boolean isDefaultOff() {
    return defaultOff;
  }

  /**
   * Turns the rule off by default.
   **/
  public final void setDefaultOff() {
    defaultOff = true;
  }

}
