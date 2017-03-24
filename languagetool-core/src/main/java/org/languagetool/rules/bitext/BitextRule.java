/* LanguageTool, a natural language style checker 
 * Copyright (C) 2010 Marcin Miłkowski (www.languagetool.org)
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

package org.languagetool.rules.bitext;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.bitext.StringPair;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

/**
 * Abstract bitext rule class. A BitextRule describes a language error and 
 * can test whether a given pre-analyzed pair of source and target text 
 * contains that error using the {@link Rule#match} method.
 * 
 * @author Marcin Miłkowski
 */
public abstract class BitextRule extends Rule {

  public static List<Class<? extends BitextRule>> getRelevantRules() {
    return Arrays.asList(
            DifferentLengthRule.class,
            SameTranslationRule.class,
            DifferentPunctuationRule.class
    );
  }

  public abstract String getMessage();

  public abstract RuleMatch[] match(AnalyzedSentence sourceText,
      AnalyzedSentence targetText) throws IOException;

  private List<StringPair> correctExamples;
  private List<IncorrectBitextExample> incorrectExamples;
  private Language sourceLanguage;

  /**
   * This method makes no sense for bitext, thus it always returns {@code null}.
   */
  @Nullable
  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    return null;
  }

  /**
   * Set the source language. If the language is not supported
   * by LT, you need to use the default tokenizers etc.
   * @param lang Source Language
   */
  public final void setSourceLanguage(Language lang) {
    sourceLanguage = lang;
  }

  public final Language getSourceLanguage() {
    return sourceLanguage;
  }

  /**
   * Set the examples that are correct and thus do not trigger the rule.
   */
  public final void setCorrectBitextExamples(List<StringPair> correctExamples) {
    this.correctExamples = correctExamples;
  }

  /**
   * Get example sentences that are correct and thus will not match this rule.
   */
  public final List<StringPair> getCorrectBitextExamples() {
    return correctExamples;
  }

  /**
   * Set the examples that are incorrect and thus do trigger the rule.
   */
  public final void setIncorrectBitextExamples(
      final List<IncorrectBitextExample> incorrectExamples) {
    this.incorrectExamples = Collections.unmodifiableList(incorrectExamples);
  }

  /**
   * Get example sentences that are incorrect and thus will match this rule.
   */
  public final List<IncorrectBitextExample> getIncorrectBitextExamples() {
    return incorrectExamples;
  }

}
