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

import java.io.*;
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

  private void printAddedOrRemoved(RuleMatchDiff.Status status, List<RuleMatchDiff> diffs, FileWriter fw) throws IOException {
    fw.write("<h1>" + status + "</h1>\n\n");
    fw.write("Diffs found: " + getDiffCount(status, diffs) + "<br>\n");
    printTableBegin(fw);
    for (RuleMatchDiff diff : diffs) {
      if (diff.getStatus() == status) {
        LightRuleMatch match = diff.getOldMatch() != null ? diff.getOldMatch() : diff.getNewMatch();
        fw.write("<tr>\n");
        fw.write("  <td>" + match.getRuleId() + " </td>\n");
        fw.write("  <td>" + match.getMessage() + " </td>\n");
        fw.write("  <td>" + match.getCoveredText() + " </td>\n");
        fw.write("</tr>\n");
      }
    }
    printTableEnd(fw);
  }

  private void printModified(RuleMatchDiff.Status status, List<RuleMatchDiff> diffs, FileWriter fw) throws IOException {
    fw.write("<h1>" + status + "</h1>\n\n");
    fw.write("Diffs found: " + getDiffCount(status, diffs) + "<br>\n");
    printTableBegin(fw);
    for (RuleMatchDiff diff : diffs) {
      if (diff.getStatus() == status) {
        LightRuleMatch oldMatch = diff.getOldMatch();
        LightRuleMatch newMatch = diff.getNewMatch();
        fw.write("<tr>\n");
        fw.write("  <td>" + oldMatch.getRuleId() + " </td>\n");
        fw.write("  <td>" +
          "old: " + oldMatch.getMessage() + "<br>" + 
          "new: " + newMatch.getMessage() + 
          "</td>\n");
        fw.write("  <td>" +
          "old: " + oldMatch.getCoveredText() + "<br>" +
          "new: " + newMatch.getCoveredText() +
          "</td>\n");
        fw.write("</tr>\n");
      }
    }
    printTableEnd(fw);
  }

  private void printTableBegin(FileWriter fw) throws IOException {
    fw.write("<table class='sortable_table'>\n");
    fw.write("<thead>\n");
    fw.write("<tr>\n");
    fw.write("  <th>Rule ID</th>\n");
    fw.write("  <th>Message</th>\n");
    fw.write("  <th>Context</th>\n");
    fw.write("</tr>\n");
    fw.write("</thead>\n");
    fw.write("<tbody>\n");
  }

  private void printTableEnd(FileWriter fw) throws IOException {
    fw.write("</tbody>\n");
    fw.write("</table>\n\n");
  }

  private int getDiffCount(RuleMatchDiff.Status status, List<RuleMatchDiff> diffs) {
    int count = 0;
    for (RuleMatchDiff diff : diffs) {
      if (diff.getStatus() == status) {
        count++;
      }
    }
    return count;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("Usage: " + RuleMatchDiffFinder.class.getSimpleName() + " <matches1> <matches2> <result>");
      System.out.println(" <matches1> and <matches2> are text outputs of different versions of org.languagetool.commandline.Main run on the same input");
      System.exit(1);
    }
    RuleMatchDiffFinder diffFinder = new RuleMatchDiffFinder();
    LightRuleMatchParser parser = new LightRuleMatchParser();
    File file1 = new File(args[0]);
    File file2 = new File(args[1]);
    File file3 = new File(args[2]);
    List<LightRuleMatch> l1 = parser.parse(new FileReader(file1));
    List<LightRuleMatch> l2 = parser.parse(new FileReader(file2));
    List<RuleMatchDiff> diffs = diffFinder.getDiffs(l1, l2);
    String title = "Comparing " + file1.getName() + " to "  + file2.getName();
    System.out.println(title);
    System.out.println("Total diffs found: " + diffs.size());
    try (FileWriter fw = new FileWriter(file3)) {
      fw.write("<!doctype html>\n");
      fw.write("<html>\n");
      fw.write("<head>\n");
      fw.write("  <title>" + title + "</title>\n");
      fw.write("  <meta charset='utf-8'>\n");
      fw.write("  <script src='tablefilter/tablefilter.js'></script>");  // https://github.com/koalyptus/TableFilter/
      fw.write("</head>\n");
      fw.write("<body>\n\n");
      diffFinder.printAddedOrRemoved(RuleMatchDiff.Status.ADDED, diffs, fw);
      diffFinder.printAddedOrRemoved(RuleMatchDiff.Status.REMOVED, diffs, fw);
      diffFinder.printModified(RuleMatchDiff.Status.MODIFIED, diffs, fw);
      fw.write("<script>\n" +
               "var tf = new TableFilter(document.querySelector('.sortable_table'), {\n" +
               "    base_path: './tablefilter/',\n" +
               "    col_0: 'select',\n" +
               "    alternate_rows: true,\n" +
               "    auto_filter: { delay: 100 },\n" +
               "    grid_layout: false,\n" +
               "    col_types: ['string', 'string', 'string'],\n" +
               "    extensions: [{ name: 'sort' }]\n" +
               "});\n" +
               "tf.init();\n" +
               "</script>");
      fw.write("</body>\n");
      fw.write("</html>\n");
    }
  }

}
