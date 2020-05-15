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
 * Find diffs between runs of command-line LT. Matches with the same rule id, doc title, line, column
 * and covered text are considered the "same" match. If there's a difference in message or
 * suggestion of these same matches, then we consider this match to be "modified".
 */
public class RuleMatchDiffFinder {
  
  List<RuleMatchDiff> getDiffs(List<LightRuleMatch> l1, List<LightRuleMatch> l2) {
    System.out.println("Comparing result 1 (" + l1.size() + " matches) to result 2 (" + l2.size() + " matches)");
    //debugList("List 1", l1);
    //debugList("List 2", l2);
    List<RuleMatchDiff> result = new ArrayList<>();
    Map<MatchKey, LightRuleMatch> oldMatches = getMatchMap(l1);
    for (LightRuleMatch match : l2) {
      MatchKey key = new MatchKey(match.getLine(), match.getColumn(), match.getRuleId(), match.getTitle(), match.getCoveredText());
      LightRuleMatch oldMatch = oldMatches.get(key);
      if (oldMatch != null) {
        if (!oldMatch.getSuggestions().equals(match.getSuggestions()) ||
            !oldMatch.getMessage().equals(match.getMessage()) ||
            oldMatch.getStatus() != match.getStatus() ||
            !Objects.equals(oldMatch.getSubId(), match.getSubId()) ||
            !oldMatch.getCoveredText().equals(match.getCoveredText())) {
          result.add(RuleMatchDiff.modified(oldMatch, match));
        }
      } else {
        result.add(RuleMatchDiff.added(match));
      }
    }
    Map<MatchKey, LightRuleMatch> newMatches = getMatchMap(l2);
    for (LightRuleMatch match : l1) {
      MatchKey key = new MatchKey(match.getLine(), match.getColumn(), match.getRuleId(), match.getTitle(), match.getCoveredText());
      LightRuleMatch newMatch = newMatches.get(key);
      if (newMatch == null) {
        // removed
        result.add(RuleMatchDiff.removed(match));
      }
    }
    return result;
  }

  private void debugList(String title, List<LightRuleMatch> l1) {
    System.out.println(title);
    for (LightRuleMatch lightRuleMatch : l1) {
      System.out.println(" *" + lightRuleMatch);
    }
  }

  private Map<MatchKey, LightRuleMatch> getMatchMap(List<LightRuleMatch> list) {
    Map<MatchKey, LightRuleMatch> map = new HashMap<>();
    for (LightRuleMatch match : list) {
      MatchKey key = new MatchKey(match.getLine(), match.getColumn(), match.getRuleId(), match.getTitle(), match.getCoveredText());
      map.put(key, match);
      //System.out.println("-> " + key);
    }
    return map;
  }

  private void print(List<RuleMatchDiff> diffs, FileWriter fw) throws IOException {
    fw.write("Diffs found: " + diffs.size() + "<br>\n");
    printTableBegin(fw);
    for (RuleMatchDiff diff : diffs) {
      if (diff.getStatus() == RuleMatchDiff.Status.ADDED) {
        fw.write("<tr style='background-color: #c7ffd0'>\n");
      } else if (diff.getStatus() == RuleMatchDiff.Status.REMOVED) {
        fw.write("<tr style='background-color: #ffd2d8'>\n");
      } else {
        fw.write("<tr>\n");
      }
      fw.write("  <td>" + diff.getStatus().name().substring(0, 3) + " </td>\n");
      LightRuleMatch oldMatch = diff.getOldMatch();
      LightRuleMatch newMatch = diff.getNewMatch();
      if (diff.getOldMatch() != null) {
        fw.write("<td>" + cleanSource(diff.getOldMatch().getRuleSource()) + "</td>");
      } else if (diff.getNewMatch() != null) {
        fw.write("<td>" + cleanSource(diff.getNewMatch().getRuleSource()) + "</td>");
      } else {
        fw.write("<td></td>");
      }
      if (oldMatch != null && newMatch != null) {
        printRuleIdCol(fw, oldMatch, newMatch);
        printMessage(fw, oldMatch, newMatch);
        if (oldMatch.getSuggestions().equals(newMatch.getSuggestions())) {
          fw.write("  <td>" + oldMatch.getSuggestions() + "</td>\n");
        } else {
          fw.write("  <td>" +
            "  <tt>old:</tt> " + showTrimSpace(oldMatch.getSuggestions()) + "<br>\n" +
            "  <tt>new:</tt> " + showTrimSpace(newMatch.getSuggestions()) +
            "</td>\n");
        }
        fw.write("</tr>\n");
      } else {
        LightRuleMatch match = diff.getOldMatch() != null ? diff.getOldMatch() : diff.getNewMatch();
        printRuleIdCol(fw, null, match);
        printMessage(fw, match, null);
        fw.write("  <td>" + match.getSuggestions() + "</td>\n");
        fw.write("</tr>\n");
      }
    }
    printTableEnd(fw);
  }

  private String cleanSource(String ruleSource) {
    if (ruleSource == null) {
      return "java";
    }
    return ruleSource.replaceFirst("^.*/grammar", "gram.").replaceFirst("gram.-premium", "prem").replaceFirst(".xml", "");
  }

  private void printRuleIdCol(FileWriter fw, LightRuleMatch oldMatch, LightRuleMatch newMatch) throws IOException {
    fw.write("  <td>");
    if (oldMatch != null && !Objects.equals(oldMatch.getSubId(), newMatch.getSubId())) {
      fw.write(oldMatch.getRuleId());
      fw.write("[" + oldMatch.getSubId() + " => " + newMatch.getSubId() + "]");
    } else {
      fw.write(newMatch.getRuleId() + "[" + (newMatch.getSubId() != null ? newMatch.getSubId() : "") + "]");
    }
    if (newMatch.getStatus() != LightRuleMatch.Status.on) {
      fw.write("  <br><span class='status'>[" + newMatch.getStatus() + "]</span>");
    }
    if (oldMatch != null && newMatch.getStatus() != oldMatch.getStatus()) {
      fw.write("  <br><span class='status'>[" + oldMatch.getStatus() + " => " + newMatch.getStatus() + "]</span>");
    }
    fw.write(" </td>\n");
  }

  private void printMessage(FileWriter fw, LightRuleMatch oldMatch, LightRuleMatch newMatch) throws IOException {
    fw.write("  <td>");
    if (newMatch == null) {
      fw.write(oldMatch.getMessage());
    } else if (oldMatch.getMessage().equals(newMatch.getMessage())) {
      fw.write(oldMatch.getMessage());
    } else {
      //System.out.println("old: " + oldMatch.getMessage());
      //System.out.println("new: " + newMatch.getMessage());
      fw.write(
        "<tt>old:</tt> " + showTrimSpace(oldMatch.getMessage()) + "<br>\n" +
        "<tt>new:</tt> " + showTrimSpace(newMatch.getMessage()));
    }
    fw.write("  <br><span class='sentence'>" + oldMatch.getContext() + "</span>");
    fw.write("  </td>\n");
  }

  private String showTrimSpace(String s) {
    s = s.replaceFirst("^\\s", "<span class='whitespace'>&nbsp;</span>");
    s = s.replaceFirst("\\s$", "<span class='whitespace'>&nbsp;</span>");
    return s;
  }

  private void printTableBegin(FileWriter fw) throws IOException {
    fw.write("<table class='sortable_table'>\n");
    fw.write("<thead>\n");
    fw.write("<tr>\n");
    fw.write("  <th>Change</th>\n");
    fw.write("  <th>File</th>\n");
    fw.write("  <th>Rule ID</th>\n");
    fw.write("  <th>Message and Text</th>\n");
    fw.write("  <th>Suggestion</th>\n");
    fw.write("</tr>\n");
    fw.write("</thead>\n");
    fw.write("<tbody>\n");
  }

  private void printTableEnd(FileWriter fw) throws IOException {
    fw.write("</tbody>\n");
    fw.write("</table>\n\n");
  }

  private void run(LightRuleMatchParser parser, File file1, File file2, File file3) throws IOException {
    List<LightRuleMatch> l1 = parser.parseOutput(file1);
    List<LightRuleMatch> l2 = parser.parseOutput(file2);
    String title = "Comparing " + file1.getName() + " to "  + file2.getName();
    System.out.println(title);
    List<RuleMatchDiff> diffs = getDiffs(l1, l2);
    diffs.sort(Comparator.comparing(this::getFullId));
    System.out.println("Total diffs found: " + diffs.size());
    try (FileWriter fw = new FileWriter(file3)) {
      fw.write("<!doctype html>\n");
      fw.write("<html>\n");
      fw.write("<head>\n");
      fw.write("  <title>" + title + "</title>\n");
      fw.write("  <meta charset='utf-8'>\n");
      fw.write("  <script src='/regression-tests/tablefilter/tablefilter.js'></script>\n");  // https://github.com/koalyptus/TableFilter/
      fw.write("  <style>\n");
      fw.write("    .sentence { color: #666; }\n");
      fw.write("    .marker { text-decoration: underline; }\n");
      fw.write("    .source { color: #999; }\n");
      fw.write("    .status { color: #999; }\n");
      fw.write("    .whitespace { background-color: #ccc; }\n");
      fw.write("  </style>\n");
      fw.write("</head>\n");
      fw.write("<body>\n\n");
      print(diffs, fw);
      fw.write("<script>\n" +
               "var tf = new TableFilter(document.querySelector('.sortable_table'), {\n" +
               "    base_path: '../tablefilter/',\n" +
               "    col_0: 'select',\n" +
               "    col_1: 'select',\n" +
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

  private String getFullId(RuleMatchDiff diff) {
    String id = "unknown";
    if (diff.getOldMatch() != null) {
      id = diff.getOldMatch().getFullRuleId();
    } else if (diff.getNewMatch() != null) {
      id = diff.getNewMatch().getFullRuleId();
    }
    return id;
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 3) {
      System.out.println("Usage: " + RuleMatchDiffFinder.class.getSimpleName() + " <matches1> <matches2> <result>");
      System.out.println(" <matches1> and <matches2> are text outputs of different versions of org.languagetool.dev.dumpcheck.SentenceSourceChecker run on the same input");
      System.out.println("                           our JSON outputs from org.languagetool.dev.httpchecker.HttpApiSentenceChecker");
      System.exit(1);
    }
    RuleMatchDiffFinder diffFinder = new RuleMatchDiffFinder();
    LightRuleMatchParser parser = new LightRuleMatchParser();
    if (args[0].contains("XX") && args[1].contains("XX") && args[2].contains("XX")) {
      System.out.println("Running in multi-file mode, replacing 'XX' in filenames with lang codes...");
      String file1 = args[0];
      String file3 = args[2];
      File dir = new File(file1).getParentFile();
      String templateName = new File(file1).getName();
      int varPos = templateName.indexOf("XX");
      for (String file : dir.list()) {
        if (file.length() >= varPos + 1) {
          StringBuilder tempName = new StringBuilder(file).replace(varPos, varPos + 2, "XX");
          if (tempName.toString().equals(templateName)) {
            String langCode = file.substring(varPos, varPos+2);
            String tempNameNew = file.replace(".old", ".new");
            File newFile = new File(dir, tempNameNew);
            if (!newFile.exists()) {
              throw new RuntimeException(tempNameNew + " not found - make sure files are names *.old and *.new in multi-file mode");
            }
            System.out.println("==== " + file + " =================================");
            File oldFile = new File(dir, file);
            String outputFile = file3.replace("XX", langCode);
            diffFinder.run(parser, oldFile, newFile, new File(outputFile));
          }
        }
      }
    } else {
      File file1 = new File(args[0]);
      File file2 = new File(args[1]);
      File file3 = new File(args[2]);
      diffFinder.run(parser, file1, file2, file3);
    }
  }

}
