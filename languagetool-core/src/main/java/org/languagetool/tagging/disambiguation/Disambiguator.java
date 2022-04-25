/* LanguageTool, a natural language style checker 
 * Copyright (C) 2007 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.tagging.disambiguation;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;

import java.io.IOException;

/**
 * Disambiguator interface. Particular implementations are language-dependent.
 * 
 * <p>The POS tagger might assign multiple tags to the token.
 * The goal is to filter out the incorrect tags and leave ideally only one per token.
 */
public interface Disambiguator {
  
  /**
   * If possible, filters out the wrong POS tags.
   * This code will run before disambiguation rules from xml are called.
   * This allows to have some initial disambiguation logic in Java.
   * 
   * @param input
   *          The sentence with already tagged words. The words are expected to
   *          have multiple tags.
   * @return Analyzed sentence, where each word has only one (possibly the most
   *         correct) tag.
   * @since 3.7
   */
  AnalyzedSentence preDisambiguate(AnalyzedSentence input);

  /**
   * If possible, filters out the wrong POS tags.
   * 
   * @param input
   *          The sentence with already tagged words. The words are expected to
   *          have multiple tags.
   * @return Analyzed sentence, where each word has only one (possibly the most
   *         correct) tag.
   */
  AnalyzedSentence disambiguate(AnalyzedSentence input) throws IOException;

  /**
   * The same as {@link #disambiguate(AnalyzedSentence)},
   * but may call {@code checkCanceled} (if it's non-null) to allow for better interruptibility.
   */
  default AnalyzedSentence disambiguate(
    AnalyzedSentence input, @Nullable JLanguageTool.CheckCancelledCallback checkCanceled
  ) throws IOException {
    return disambiguate(input);
  }
}
