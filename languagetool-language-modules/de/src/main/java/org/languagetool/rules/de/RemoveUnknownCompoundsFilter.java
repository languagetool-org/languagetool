/* LanguageTool, a natural language style checker
 * Copyright (C) 2020 Daniel Naber (http://www.danielnaber.de)
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
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.language.German;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.io.IOException;
import java.util.Map;

public class RemoveUnknownCompoundsFilter extends RuleFilter {

  private final static GermanSpellerRule spellerRule = new GermanSpellerRule(JLanguageTool.getMessageBundle(), (German) Languages.getLanguageForShortCode("de-DE"), null, null);

  @Nullable
  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) throws IOException {
    String compound = arguments.get("part1") + arguments.get("part2").toLowerCase();
    if (spellerRule.isMisspelled(compound)) {
      //System.err.println("Ignoring match for " + compound + ": " + match.getRule().getFullId());
      return null;
    }
    // TODO: use ngrams. no occurrences for compound = don't show error
    return match;
  }
}
