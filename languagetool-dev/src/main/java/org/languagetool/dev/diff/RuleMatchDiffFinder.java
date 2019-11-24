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
package org.languagetool.dev.diff;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.*;

/**
 * Find diffs between runs of command-line LT. Matches with the same rule id, line, and column
 * are considered the "same" match. If there's a difference in message or suggestion of these
 * same matches, then we consider this match to be "modified".
 */
public class RuleMatchDiffFinder {
  
  List<RuleMatchDiff> getDiffs(List<LightRuleMatch> l1, List<LightRuleMatch> l2) {
    List<RuleMatchDiff> result = new ArrayList<>();
    Map<MatchKey, LightRuleMatch> oldMatches = getMatchMap(l1);
    for (LightRuleMatch match : l2) {
      MatchKey key = new MatchKey(match.getLine(), match.getColumn(), match.getRuleId());
      LightRuleMatch oldMatch = oldMatches.get(key);
      if (oldMatch != null) {
        // no change or modified
        if (!oldMatch.getSuggestions().equals(match.getSuggestions()) ||
            !oldMatch.getMessage().equals(match.getMessage()) ||
            !oldMatch.getCoveredText().equals(match.getCoveredText())) {
          result.add(RuleMatchDiff.modified(oldMatch, match));
        }
      } else {
        // added
        result.add(RuleMatchDiff.added(match));
      }
    }
    Map<MatchKey, LightRuleMatch> newMatches = getMatchMap(l2);
    for (LightRuleMatch match : l1) {
      MatchKey key = new MatchKey(match.getLine(), match.getColumn(), match.getRuleId());
      LightRuleMatch newMatch = newMatches.get(key);
      if (newMatch == null) {
        // removed
        result.add(RuleMatchDiff.removed(match));
      }
    }
    return result;
  }

  private Map<MatchKey, LightRuleMatch> getMatchMap(List<LightRuleMatch> list) {
    Map<MatchKey, LightRuleMatch> map = new HashMap<>();
    for (LightRuleMatch match : list) {
      MatchKey key = new MatchKey(match.getLine(), match.getColumn(), match.getRuleId());
      map.put(key, match);
    }
    return map;
  }

  private void print(RuleMatchDiff.Status status, List<RuleMatchDiff> diffs) {
    System.out.println("=============================================================");
    System.out.println(status + ":");
    System.out.println("=============================================================");
    int count = 0;
    for (RuleMatchDiff diff : diffs) {
      if (diff.getStatus() == status) {
        count++;
      }
    }
    System.out.println("Diffs found: " + count);
    System.out.println("");
    for (RuleMatchDiff diff : diffs) {
      if (diff.getStatus() != status) {
        continue;
      }
      System.out.println(diff.getStatus() + ":");
      System.out.println("OLD: " + diff.getOldMatch());
      System.out.println("NEW: " + diff.getNewMatch());
      System.out.println("-------------------------------------------------------------");
    }
  }

  public static void main(String[] args) throws FileNotFoundException {
    if (args.length != 2) {
      System.out.println("Usage: " + RuleMatchDiffFinder.class.getSimpleName() + " <output1> <output2>");
      System.out.println(" output1 and output2 are text outputs of different versions of org.languagetool.commandline.Main run on the same input");
      System.exit(1);
    }
    RuleMatchDiffFinder diffFinder = new RuleMatchDiffFinder();
    LightRuleMatchParser parser = new LightRuleMatchParser();
    File file1 = new File(args[0]);
    File file2 = new File(args[1]);
    List<LightRuleMatch> l1 = parser.parse(new FileReader(file1));
    List<LightRuleMatch> l2 = parser.parse(new FileReader(file2));
    List<RuleMatchDiff> diffs = diffFinder.getDiffs(l1, l2);
    System.out.println("Comparing " + file1 + " to "  + file2);
    diffFinder.print(RuleMatchDiff.Status.ADDED, diffs);
    diffFinder.print(RuleMatchDiff.Status.REMOVED, diffs);
    diffFinder.print(RuleMatchDiff.Status.MODIFIED, diffs);
  }

}
