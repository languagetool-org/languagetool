/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Jaume Ortolà
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

import org.languagetool.Language;
import org.languagetool.Premium;
import org.languagetool.Tag;

import java.util.ArrayList;
import java.util.List;

/* 
 * Remove overlapping errors according to the priorities established for the language.  
 * (It assumes the input list is ordered by start position)
 * 
 * @since 3.6
 */
public class CleanOverlappingFilter implements RuleMatchFilter {

  private static final int negativeConstant = Integer.MIN_VALUE + 10000;

  private final Language language;
  private final boolean hidePremiumMatches;

  public CleanOverlappingFilter(Language lang, boolean hidePremiumMatches) {
    this.language = lang;
    this.hidePremiumMatches = hidePremiumMatches;
  }
  
  @Override
  public final List<RuleMatch> filter(List<RuleMatch> ruleMatches) {
    List<RuleMatch> cleanList = new ArrayList<>();
    RuleMatch prevRuleMatch = null;
    for (RuleMatch ruleMatch: ruleMatches) {
      if (prevRuleMatch == null) {  // first item
        prevRuleMatch = ruleMatch;
        continue;
      }
      if (ruleMatch.getFromPos() < prevRuleMatch.getFromPos()) {
        throw new IllegalArgumentException(
            "The list of rule matches is not ordered. Make sure it is sorted by start position.");
      }

      boolean isDuplicateSuggestion = false;
      if (ruleMatch.getSuggestedReplacements().size() > 0
        && prevRuleMatch.getSuggestedReplacements().size() > 0) {
        String suggestion = ruleMatch.getSuggestedReplacements().get(0);
        String prevSuggestion = prevRuleMatch.getSuggestedReplacements().get(0);
        // juxtaposed errors adding a comma in the same place
        if (ruleMatch.getFromPos() == prevRuleMatch.getToPos()) {
          if (prevSuggestion.endsWith(",") && suggestion.startsWith(", ")) {
            isDuplicateSuggestion = true;
          }
        }
        // duplicate suggestion for the same position
        if (suggestion.indexOf(" ") > 0 && prevSuggestion.indexOf(" ") > 0
          && ruleMatch.getFromPos() == prevRuleMatch.getToPos() + 1) {
          String parts[] = suggestion.split(" ");
          String partsPrev[] = prevSuggestion.split(" ");
          if (partsPrev.length > 1 && parts.length > 1 && partsPrev[1].equals(parts[0])) {
            isDuplicateSuggestion = true;
          }
        }
      }

      // no overlapping (juxtaposed errors are not removed)
      if (ruleMatch.getFromPos() >= prevRuleMatch.getToPos() && !isDuplicateSuggestion) {
        cleanList.add(prevRuleMatch);
        prevRuleMatch = ruleMatch;
        continue;
      }
      // overlapping
      int currentPriority = language.getRulePriority(ruleMatch.getRule());
      if (isPremiumRule(ruleMatch) && hidePremiumMatches) {
        // non-premium match should win, so the premium match does *not* become a hidden match
        // (we'd show hidden matches for errors covered by an Open Source match)
        currentPriority = Integer.MIN_VALUE;
      }
      if (ruleMatch.getRule().getTags().contains(Tag.picky) && currentPriority != Integer.MIN_VALUE) {
        currentPriority += negativeConstant;
      }
      int prevPriority = language.getRulePriority(prevRuleMatch.getRule());
      if (isPremiumRule(prevRuleMatch) && hidePremiumMatches) {
        prevPriority = Integer.MIN_VALUE;
      }
      if (prevRuleMatch.getRule().getTags().contains(Tag.picky) && prevPriority != Integer.MIN_VALUE) {
        prevPriority += negativeConstant;
      }
      // If both matches only change punctuation,
      // prefer the rule that participates in "correct all errors at once".
      boolean currentIsPunctuationOnly = isPunctuationOnlyChange(ruleMatch);
      boolean prevIsPunctuationOnly = isPunctuationOnlyChange(prevRuleMatch);
      if (currentIsPunctuationOnly && prevIsPunctuationOnly) {
        boolean currentIncludedAllAtOnce = ruleMatch.getRule().isIncludedInErrorsCorrectedAllAtOnce();
        boolean prevIncludedAllAtOnce = prevRuleMatch.getRule().isIncludedInErrorsCorrectedAllAtOnce();
        if (currentIncludedAllAtOnce != prevIncludedAllAtOnce) {
          if (currentIncludedAllAtOnce) {
            if (currentPriority < prevPriority) {
              currentPriority = prevPriority + 1;
            }
          } else if (prevPriority < currentPriority) {
            prevPriority = currentPriority + 1;
          }
        }
      }
      if (currentPriority == prevPriority) {
        // take the longest error:
        currentPriority = ruleMatch.getToPos() - ruleMatch.getFromPos();
        prevPriority = prevRuleMatch.getToPos() - prevRuleMatch.getFromPos();
      }
      if (currentPriority == prevPriority) {
        currentPriority++; // take the last one (to keep the current results in the web UI) 
      }
      if (currentPriority > prevPriority) {
        prevRuleMatch = ruleMatch;
      }
    }
    //last match
    if (prevRuleMatch != null) {
      cleanList.add(prevRuleMatch);
    }
    return cleanList;
  }

  protected boolean isPremiumRule(RuleMatch ruleMatch) {
    return Premium.get().isPremiumRule(ruleMatch.getRule());
  }

  private boolean isPunctuationOnlyChange(RuleMatch match) {
    if (match == null) {
      return false;
    }
    List<String> suggestions = match.getSuggestedReplacements();
    if (suggestions == null || suggestions.isEmpty()) {
      return false;
    }
    String replacement = suggestions.get(0);
    if (replacement == null) {
      return false;
    }
    String original = match.getOriginalErrorStr();
    if (original == null || original.isEmpty()) {
      String sentenceStr = match.getSentence() != null ? match.getSentence().getText() : null;
      if (sentenceStr == null || sentenceStr.isEmpty()) {
        return false;
      }
      int from = match.getFromPosSentence();
      int to = match.getToPosSentence();
      if (from > -1 && to > -1 && to <= sentenceStr.length() && from < to) {
        original = sentenceStr.substring(from, to);
      } else {
        from = match.getFromPos();
        to = match.getToPos();
        if (from > -1 && to > -1 && to <= sentenceStr.length() && from < to) {
          original = sentenceStr.substring(from, to);
        } else {
          return false;
        }
      }
    }
    // if strings are identical, it's not a change at all
    if (replacement.equals(original)) {
      return false;
    }
    String normalizedOriginal = keepLettersAndDigits(original);
    String normalizedReplacement = keepLettersAndDigits(replacement);
    return normalizedOriginal.equals(normalizedReplacement);
  }

  private String keepLettersAndDigits(String s) {
    StringBuilder sb = new StringBuilder(s.length());
    for (int i = 0; i < s.length(); i++) {
      char ch = s.charAt(i);
      if (Character.isLetterOrDigit(ch)) {
        sb.append(ch);
      }
    }
    return sb.toString();
  }

}
