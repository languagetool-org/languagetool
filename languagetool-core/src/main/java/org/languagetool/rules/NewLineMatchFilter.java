/*
 * LanguageTool, a natural language style checker
 * Copyright (C) 2025 Stefan Viol (https://stevio.de)
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

package org.languagetool.rules;

import org.languagetool.markup.AnnotatedText;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NewLineMatchFilter implements RuleMatchFilter {

  private static final String INVISIBLE_SEPERATOR = "\u2063";

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches, AnnotatedText text) {
    return ruleMatches.stream().filter(ruleMatch -> {

      var from = ruleMatch.getFromPos();
      var to = ruleMatch.getToPos();

      // without some old tests failing with ArrayOutOfBoundExceptions. Should be safe to just keep the matches.
      if (text.getOriginalText().length() < from || text.getOriginalText().length() < to) {
        return true;
      }

      var matchText = text.getOriginalText().substring(from, to);

      // remove tailing and leading \n and \u2063 from the matchText and update the from and to positions of the match
      while (matchText.endsWith("\n") || matchText.endsWith(INVISIBLE_SEPERATOR)) {
        matchText = matchText.substring(0, matchText.length() - 1);
        to--;
        if (to < from) {
          //only new line match can be removed
          return false;
        }
      }
      while (matchText.startsWith("\n") || matchText.startsWith(INVISIBLE_SEPERATOR)) {
        matchText = matchText.substring(1);
        from++;
      }
      var newSuggestionReplacements = new ArrayList<String>();
      ruleMatch.getSuggestedReplacements().forEach(replacement -> {
        var newRepacement = replacement;
        while (newRepacement.endsWith("\n") || newRepacement.endsWith("\u2063")) {
          newRepacement = newRepacement.substring(0, newRepacement.length() - 1);
        }
        while (newRepacement.startsWith("\n") || newRepacement.startsWith("\u2063")) {
          newRepacement = newRepacement.substring(1);
        }
        newSuggestionReplacements.add(newRepacement);
      });
      if (newSuggestionReplacements.size() == 1 && newSuggestionReplacements.get(0).equals(matchText)) {
        return false;
      } else {
        ruleMatch.setOffsetPosition(from, to);
        ruleMatch.setSuggestedReplacements(newSuggestionReplacements);
        return true;
      }
    }).collect(Collectors.toCollection(ArrayList::new));
  }
}
