/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.languagetool.rules.Rule;

import java.util.List;
import java.util.Objects;

/**
 * Detect language per token and add that to the analysis.
 * @since 4.6
 */
class LanguageAnnotator {

  private final Language mainLang;
  private final Language secondLang;

  public LanguageAnnotator(Language mainLang, Language secondLang) {
    this.mainLang = Objects.requireNonNull(mainLang);
    this.secondLang = Objects.requireNonNull(secondLang);
  }

  void annotateWithLanguage(List<AnalyzedSentence> analyzedSentences) {
    Rule mainSpeller = getSpellerRule(mainLang);
    Rule secondSpeller = getSpellerRule(mainLang);
    // TODO: what if the languages use different tokenizers?
    for (AnalyzedSentence sentence : analyzedSentences) {
      for (AnalyzedTokenReadings tokens : sentence.getTokensWithoutWhitespace()) {
        //mainSpeller.match(new AnalyzedSentence(new AnalyzedTokenReadings[] { tokens.getToken() }));
        // TODO: if (!mainSpeller.isMisspelled(token))
        if (true) {
          // TODO: if (!secondSpeller.isMisspelled(token))
          tokens.setLanguage(secondLang);
        }
      }
    }
  }

  private Rule getSpellerRule(Language lang) {
    List<Rule> rules = new JLanguageTool(lang).getAllActiveRules();
    for (Rule rule : rules) {
      if (rule.isDictionaryBasedSpellingRule()) {
        // TODO: there could me more than one!!
        return rule;
      }
    }
    return null;
  }

}
