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
import java.util.List;

import org.languagetool.AnalyzedSentence;
import org.languagetool.Language;
import org.languagetool.rules.AbstractSuppressMisspelledSuggestionsFilter;
import org.languagetool.rules.spelling.SpellingCheckRule;

public class CatalanSuppressMisspelledSuggestionsFilter extends AbstractSuppressMisspelledSuggestionsFilter {

  public CatalanSuppressMisspelledSuggestionsFilter() throws IOException {
  }

  @Override
  public boolean isMisspelled(String s, Language language) throws IOException {
    SpellingCheckRule spellerRule = language.getDefaultSpellingRule();
    if (spellerRule == null) {
      return false;
    }
    List<AnalyzedSentence> sentences = language.createDefaultJLanguageTool().analyzeText(s);
    return spellerRule.match(sentences.get(0)).length > 0;
  }

}
