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
package org.languagetool.tagging.disambiguation.rules;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.AnalyzedToken;
import org.languagetool.Language;
import org.languagetool.rules.patterns.AbstractTokenBasedRule;
import org.languagetool.rules.patterns.Match;
import org.languagetool.rules.patterns.PatternToken;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * A Rule that describes a pattern of words or part-of-speech tags used for
 * disambiguation.
 * 
 * @author Marcin Miłkowski
 */
public class DisambiguationPatternRule extends AbstractTokenBasedRule {

  /** Possible disambiguator actions. **/
  public enum DisambiguatorAction {
    ADD, FILTER, REMOVE, REPLACE, UNIFY, IMMUNIZE, IGNORE_SPELLING, FILTERALL
  }

  private final String disambiguatedPOS;
  private final Match matchElement;
  private final DisambiguatorAction disAction;

  private AnalyzedToken[] newTokenReadings;
  private List<DisambiguatedExample> examples = Collections.emptyList();
  private List<String> untouchedExamples = Collections.emptyList();

  /**
   * @param id Id of the Rule
   * @param language Language of the Rule
   * @param description Description to be shown (name)
   * @param disambAction the action to be executed on found token(s)
   * @since public since 2.5
   */
  public DisambiguationPatternRule(String id, String description,
                                   Language language, List<PatternToken> patternTokens,
                                   String disambiguatedPOS, Match posSelect,
                                   DisambiguatorAction disambAction) {
    super(id, description, language, patternTokens, true);
    if (disambiguatedPOS == null && posSelect == null
        && disambAction != DisambiguatorAction.UNIFY
        && disambAction != DisambiguatorAction.ADD
        && disambAction != DisambiguatorAction.REMOVE
        && disambAction != DisambiguatorAction.IMMUNIZE
        && disambAction != DisambiguatorAction.REPLACE
        && disambAction != DisambiguatorAction.FILTERALL
        && disambAction != DisambiguatorAction.IGNORE_SPELLING) {
      throw new NullPointerException("disambiguated POS cannot be null with posSelect == null and " + disambAction);
    }
    this.disambiguatedPOS = disambiguatedPOS;
    this.matchElement = posSelect;
    this.disAction = Objects.requireNonNull(disambAction);
    setMessage("");
    suggestionsOutMsg = "";
  }

  /**
   * Used to add new interpretations.
   * @param newReadings
   *          An array of AnalyzedTokens. The length of the array should be the
   *          same as the number of the tokens matched and selected by
   *          {@code <marker>...</marker>} elements.
   */
  public final void setNewInterpretations(AnalyzedToken[] newReadings) {
    newTokenReadings = newReadings.clone();
  }

  /**
   * Performs disambiguation on the source sentence.
   * @param sentence {@link AnalyzedSentence} Sentence to be disambiguated.
   * @return {@link AnalyzedSentence} Disambiguated sentence (might be unchanged).
   */
  public final AnalyzedSentence replace(AnalyzedSentence sentence) throws IOException {
    return canBeIgnoredFor(sentence) ? sentence : new DisambiguationPatternRuleReplacer(this).replace(sentence);
  }

  public void setExamples(List<DisambiguatedExample> examples) {
    this.examples = Objects.requireNonNull(examples);
  }

  public List<DisambiguatedExample> getExamples() {
    return Collections.unmodifiableList(examples);
  }

  public void setUntouchedExamples(List<String> untouchedExamples) {
    this.untouchedExamples = Objects.requireNonNull(untouchedExamples);
  }

  public List<String> getUntouchedExamples() {
    return Collections.unmodifiableList(untouchedExamples);
  }

  /**
   * @since 2.3
   */
  public DisambiguatorAction getAction() {
    return disAction;
  }

  /**
   * @since 2.3
   */
  public AnalyzedToken[] getNewTokenReadings() {
    return newTokenReadings;
  }

  /**
   * @since 2.3
   */
  @Nullable
  public Match getMatchElement() {
    return matchElement;
  }

  /**
   * @since 2.3
   */
  @Nullable
  public String getDisambiguatedPOS() {
    return disambiguatedPOS;
  }

}
