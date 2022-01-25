/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber
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

import org.languagetool.UserConfig;
import org.languagetool.markup.AnnotatedText;

import java.util.*;

/**
 * Filter spelling error with phrases the users wants to have accepted.
 * Needed so words with spaces (i.e. phrases) can be added to a user's dictionary
 * without LT creating internal anti patterns for each phrase.
 */
public class DictionarySpellMatchFilter implements RuleMatchFilter {

  private final UserConfig userConfig;

  public DictionarySpellMatchFilter(UserConfig userConfig) {
    this.userConfig = userConfig;
  }

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches, AnnotatedText text) {
    Set<String> dictionary = userConfig.getAcceptedPhrases();
    if (dictionary.size() > 0) {
      Map<String, List<RuleMatch>> phraseToMatches = getPhrases(ruleMatches, text);
      List<RuleMatch> cleanMatches = new ArrayList<>(ruleMatches);
      for (Map.Entry<String, List<RuleMatch>> entry : phraseToMatches.entrySet()) {
        if (dictionary.contains(entry.getKey())) {
          cleanMatches.removeAll(entry.getValue());
        }
      }
      return cleanMatches;
    }
    return ruleMatches;
  }

  public Map<String, List<RuleMatch>> getPhrases(List<RuleMatch> ruleMatches, AnnotatedText text) {
    Map<String, List<RuleMatch>> phraseToMatches = new HashMap<>();
    int prevToPos = Integer.MIN_VALUE;
    List<RuleMatch> collectedMatches = new ArrayList<>();
    List<String> collectedTerms = new ArrayList<>();
    for (RuleMatch match : ruleMatches) {
      if (match.getRule().isDictionaryBasedSpellingRule()) {
        String covered = text.getPlainText().substring(match.getFromPos(), match.getToPos());
        if (match.getFromPos() == prevToPos + 1) {
          String key = String.join(" ", collectedTerms) + " " + covered;
          ArrayList<RuleMatch> l = new ArrayList<>(collectedMatches);
          l.add(match);
          phraseToMatches.put(key, l);
        } else {
          collectedTerms.clear();
          collectedMatches.clear();
        }
        collectedTerms.add(covered);
        collectedMatches.add(match);
        prevToPos = match.getToPos();
      }
    }
    return phraseToMatches;
  }

}
