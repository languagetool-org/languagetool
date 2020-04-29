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
package org.languagetool.rules.nl;

import org.languagetool.AnalyzedTokenReadings;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.RuleFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.languagetool.rules.nl.Tools.glueParts;

public class CompoundFilter extends RuleFilter {

  @Override
  public RuleMatch acceptRuleMatch(RuleMatch match, Map<String, String> arguments, int patternTokenPos, AnalyzedTokenReadings[] patternTokens) {
    List<String> words = new ArrayList<>();
    for (int i = 1; i < 6; i++) {
      String arg = arguments.get("word" + i);
      if (arg == null) {
        break;
      }
      words.add(arguments.get("word" + i));
    }
    String repl = glueParts(words);
    String message = match.getMessage().replaceAll("<suggestion>.*?</suggestion>", "<suggestion>" + repl + "</suggestion>");
    String shortMessage = match.getShortMessage().replaceAll("<suggestion>.*?</suggestion>", "<suggestion>" + repl + "</suggestion>");
    RuleMatch newMatch = new RuleMatch(match.getRule(), match.getSentence(), match.getFromPos(), match.getToPos(), message, shortMessage);
    newMatch.setSuggestedReplacement(repl);
    return newMatch;
  }

}
