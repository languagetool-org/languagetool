/* LanguageTool, a natural language style checker
 * Copyright (C) 2023 Daniel Naber
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
package org.languagetool.rules.de;

import org.jetbrains.annotations.Nullable;
import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.GermanyGerman;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.io.IOException;
import java.util.Map;

/**
 * To be used in LEERZEICHEN_NACH_KLAMMER.
 * @since 6.1
 */
public class ValidWordFilter extends RuleFilter {

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) throws IOException {
    String word1 = arguments.get("word1") + arguments.get("word2");
    String word2 = arguments.get("word1") + arguments.get("word2").toLowerCase();
    GermanSpellerRule speller = GermanyGerman.INSTANCE.getDefaultSpellingRule();
    if (!speller.isMisspelled(word1) || !speller.isMisspelled(word2)) {
      // e.g. "(Promotions)Studierende" -> "Promotionsstudierende" is ok, so no match
      return null;
    }
    // all other cases, e.g.: "Das ist (vielleicht)der bessere Ansatz".
    return match;
  }

}
