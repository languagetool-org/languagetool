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
package org.languagetool.rules.pt;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.language.BrazilianPortuguese;
import org.languagetool.rules.AbstractAdvancedSynthesizerFilter;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;
import org.languagetool.synthesis.BaseSynthesizer;
import org.languagetool.synthesis.Synthesizer;
import org.languagetool.synthesis.pt.PortugueseSynthesizer;

import java.util.Map;

public class RomanNumeralFilter extends RuleFilter {
  protected PortugueseSynthesizer getSynthesizer() {
    return PortugueseSynthesizer.INSTANCE;
  }

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos,
                                   AnalyzedTokenReadings[] patternTokens) {
    String arabicSource = arguments.get("arabicSource");
    match.setSuggestedReplacement(getSynthesizer().getRomanNumber(arabicSource));
    return match;
  }
}
