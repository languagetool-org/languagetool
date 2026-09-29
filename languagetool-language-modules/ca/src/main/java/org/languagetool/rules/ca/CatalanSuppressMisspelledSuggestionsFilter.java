/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2021 Jaume Ortolà
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
package org.languagetool.rules.ca;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.chunking.ChunkTag;
import org.languagetool.rules.AbstractSuppressMisspelledSuggestionsFilter;
import org.languagetool.rules.spelling.SpellingCheckRule;

public class CatalanSuppressMisspelledSuggestionsFilter extends AbstractSuppressMisspelledSuggestionsFilter {

  public CatalanSuppressMisspelledSuggestionsFilter() throws IOException {
  }

  private final ChunkTag incorrectVerbChunk = new ChunkTag("_incorrect_verb_");

  @Override
  public boolean isMisspelled(String s, Language language) throws IOException {
    SpellingCheckRule spellerRule = language.getDefaultSpellingRule();
    if (spellerRule == null) {
      return true;
    }
    // Fast path: run the speller rule without the (expensive) disambiguator. If it already
    // finds a problem, there is no need for a full analysis. Only suggestions that pass this
    // cheap check need the full analysis to look for the "_incorrect_verb_" chunk tag.
    // Restricted to single tokens: multi-token suggestions may be accepted thanks to chunk
    // tags added by the disambiguator, so they need the full analysis.
    if (s.indexOf(' ') < 0) {
      AnalyzedSentence rawSentence = language.createDefaultJLanguageTool().getRawAnalyzedSentence(s);
      if (spellerRule.match(rawSentence).length > 0) {
        return true;
      }
    }
    List<AnalyzedSentence> sentences = language.createDefaultJLanguageTool().analyzeText(s);
    AnalyzedSentence sentence = sentences.get(0);
    boolean hasIncorrectVerb = Arrays.stream(sentence.getTokensWithoutWhitespace())
      .anyMatch(x -> x.getChunkTags().contains(incorrectVerbChunk));
    return hasIncorrectVerb || spellerRule.match(sentence).length > 0;
  }

}
