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
package org.languagetool.rules.patterns;

import java.util.regex.Pattern;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.tools.StringTools;

/**
 * A {@link Match} is the configuration of an algorithm used to match {@link AnalyzedTokenReadings}s.
 * Use {@link #createState(Synthesizer, AnalyzedTokenReadings)} and {@link #createState(Synthesizer, AnalyzedTokenReadings[], int, int)}
 * to create a {@link MatchState} used to actually match {@link AnalyzedTokenReadings}.
 *
 * @author Marcin Miłkowski
 */
public class Match {

  /** Possible string case conversions. **/
  public enum CaseConversion {
    NONE, STARTLOWER, STARTUPPER, ALLLOWER, ALLUPPER, PRESERVE
  }

  public enum IncludeRange {
    NONE, FOLLOWING, ALL
  }

  private final String posTag;
  private final boolean suppressMisspelled;
  private final String regexReplace;
  private final String posTagReplace;
  private final CaseConversion caseConversionType;
  private final IncludeRange includeSkipped;
  // Pattern used to define parts of the matched token:
  private final Pattern pRegexMatch;
  // True if this match element is used for formatting POS token:
  private final boolean setPos;

  private boolean postagRegexp;
  // True if this match element formats a statically defined lemma which is
  // enclosed by the element, e.g., <match...>word</match>:
  private boolean staticLemma;
  private String lemma;
  private int tokenRef;
  // Word form generator for POS tags:
  private Synthesizer synthesizer;
  // Pattern used to define parts of the matched POS token:
  private Pattern pPosRegexMatch;
  // True when the match is not in the suggestion:
  private boolean inMessageOnly;

  public Match(final String posTag, final String posTagReplace,
      final boolean postagRegexp, final String regexMatch,
      final String regexReplace, final CaseConversion caseConversionType,
      final boolean setPOS,
      final boolean suppressMisspelled,
      final IncludeRange includeSkipped) {
    this.posTag = posTag;
    this.postagRegexp = postagRegexp;
    this.caseConversionType = caseConversionType;
    pRegexMatch = regexMatch != null ? Pattern.compile(regexMatch) : null;
    if (postagRegexp && posTag != null) {
      pPosRegexMatch = Pattern.compile(posTag);
    }
    this.regexReplace = regexReplace;
    this.posTagReplace = posTagReplace;
    this.setPos = setPOS;
    this.includeSkipped = includeSkipped;
    this.suppressMisspelled = suppressMisspelled;
  }

  /**
   * Creates a state used for actually matching a token.
   * @since 2.3
   */
  public MatchState createState(final Synthesizer synthesizer, final AnalyzedTokenReadings token) {
    final MatchState state = new MatchState(this, synthesizer);
    state.setToken(token);
    return state;
  }

  /**
   * Creates a state used for actually matching a token.
   * @since 2.3
   */
  public MatchState createState(final Synthesizer synthesizer, final AnalyzedTokenReadings[] tokens, final int index, final int next) {
    final MatchState state = new MatchState(this, synthesizer);
    state.setToken(tokens, index, next);
    return state;
  }

  /**
   * Checks if the Match element is used for setting the part of speech Element.
   *
   * @return True if Match sets POS.
   */
  public final boolean setsPos() {
    return setPos;
  }

  /**
   * Checks if the Match element uses regexp-based form of the POS tag.
   *
   * @return True if regexp is used in POS.
   */
  public final boolean posRegExp() {
    return postagRegexp;
  }

  /**
   * Sets a base form (lemma) that will be formatted, or synthesized, using the
   * specified POS regular expressions.
   *
   * @param lemmaString String that specifies the base form.
   */
  public final void setLemmaString(final String lemmaString) {
    if (!StringTools.isEmpty(lemmaString)) {
      lemma = lemmaString;
      staticLemma = true;
      postagRegexp = true;
      if (posTag != null) {
        pPosRegexMatch = Pattern.compile(posTag);
      }
    }
  }

  /**
   * @since 2.3
   */
  public String getLemma() {
    return lemma;
  }

  /**
   * @since 2.3
   */
  public boolean isStaticLemma() {
    return staticLemma;
  }

  /**
   * Sets a synthesizer used for grammatical synthesis of forms based on
   * formatted POS values.
   *
   * @param synth Synthesizer class.
   */
  public final void setSynthesizer(final Synthesizer synth) {
    synthesizer = synth;
  }

  /**
   * @since 2.3
   */
  public Synthesizer getSynthesizer() {
    return synthesizer;
  }

  /**
   * Used to tell whether the Match class will spell-check the result so
   * that misspelled suggestions are suppressed.
   * @return True if this is so.
   */
  public final boolean checksSpelling() {
    return suppressMisspelled;
  }

  /**
   * Sets the token number referenced by the match.
   *
   * @param i Token number.
   */
  public final void setTokenRef(final int i) {
    tokenRef = i;
  }

  /**
   * Gets the token number referenced by the match.
   *
   * @return int - token number.
   */
  public final int getTokenRef() {
    return tokenRef;
  }

  /**
   * Used to let LT know that it should change the case of the match.
   *
   * @return true if match converts the case of the token.
   */
  public final boolean convertsCase() {
    return !caseConversionType.equals(CaseConversion.NONE);
  }

  /**
   * @param inMessageOnly the inMessageOnly to set
   */
  public void setInMessageOnly(final boolean inMessageOnly) {
    this.inMessageOnly = inMessageOnly;
  }

  /**
   * @return the inMessageOnly
   */
  public boolean isInMessageOnly() {
    return inMessageOnly;
  }


  /**
   * @since 2.3
   */
  public String getPosTag() {
    return posTag;
  }

  /**
   * @since 2.3
   */
  public Pattern getRegexMatch() {
    return pRegexMatch;
  }

  /**
   * @since 2.3
   */
  public String getRegexReplace() {
    return regexReplace;
  }

  /**
   * @since 2.3
   */
  public CaseConversion getCaseConversionType() {
    return caseConversionType;
  }

  /**
   * @since 2.3
   */
  public Pattern getPosRegexMatch() {
    return pPosRegexMatch;
  }

  /**
   * @since 2.3
   */
  public boolean isPostagRegexp() {
    return postagRegexp;
  }

  /**
   * @since 2.3
   */
  public String getPosTagReplace() {
    return posTagReplace;
  }

  /**
   * @since 2.3
   */
  public IncludeRange getIncludeSkipped() {
    return includeSkipped;
  }

}
