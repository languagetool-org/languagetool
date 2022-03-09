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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Find diffs between runs of command-line LT. Matches with the same rule id, doc title, line, column
 * and covered text are considered the "same" match. If there's a difference in message or
 * suggestion of these same matches, then we consider this match to be "modified".
 */
public class RuleMatchDiffFinder {

  private static final String MARKER_START = "<span class='marker'>";
  private static final String MARKER_END = "</span>";
  private static final int IFRAME_MAX = -1;

  private boolean fullMode;

  List<RuleMatchDiff> getDiffs(List<LightRuleMatch> l1, List<LightRuleMatch> l2) {
    System.out.println("Comparing result 1 (" + l1.size() + " matches) to result 2 (" + l2.size() + " matches), step 1");
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
          //!Objects.equals(oldMatch.getSubId(), match.getSubId()) ||   -- sub id change = other sub-rule added or removed, this is usually not relevant
          !oldMatch.getCoveredText().equals(match.getCoveredText())) {
          result.add(RuleMatchDiff.modified(oldMatch, match));
        }
      } else {
        result.add(RuleMatchDiff.added(match));
      }
    }
    System.out.println("Comparing result 1 (" + l1.size() + " matches) to result 2 (" + l2.size() + " matches), step 2");
    Map<String, List<RuleMatchDiff>> addedToMatch = getAddedMatchesMap(result);
    Map<MatchKey, LightRuleMatch> newMatches = getMatchMap(l2);
    for (LightRuleMatch match : l1) {
      MatchKey key = new MatchKey(match.getLine(), match.getColumn(), match.getRuleId(), match.getTitle(), match.getCoveredText());
      LightRuleMatch newMatch = newMatches.get(key);
      if (newMatch == null) {
        // removed
        String lookupKey = cleanSpan(match.getContext()) + "_" + match.getTitle();
        List<RuleMatchDiff> addedMatches = addedToMatch.get(lookupKey);
        LightRuleMatch replacedBy = null;
        if (addedMatches != null) {
          for (RuleMatchDiff addedMatch : addedMatches) {
            LightRuleMatch tmp = addedMatch.getNewMatch();
            boolean overlaps = tmp.getColumn() < match.getColumn() + match.getCoveredText().length() &&
              tmp.getColumn() + tmp.getCoveredText().length() > match.getColumn();
            if (overlaps && !tmp.getFullRuleId().equals(match.getFullRuleId())) {
              /*System.out.println(tmp + "\noverlaps\n" + match);
              System.out.println("tmp " + tmp.getTitle());
              System.out.println("match " + match.getTitle());
              System.out.println("  " + tmp.getColumn() + " < " + match.getColumn() +" + "+ match.getCoveredText().length()  + " &&");
              System.out.println("  " + tmp.getColumn() + " + " + tmp.getCoveredText().length() +" >  "+ match.getColumn());
              System.out.println("   old covered: " + match.getCoveredText());
              System.out.println("   new covered: " + tmp.getCoveredText());
              System.out.println("");*/
              replacedBy = addedMatch.getNewMatch();
              addedMatch.setReplaces(match);
              break;
            }
          }
        }
        result.add(RuleMatchDiff.removed(match, replacedBy));
      }
    }
    return result;
  }

  @NotNull
  private Map<String, List<RuleMatchDiff>> getAddedMatchesMap(List<RuleMatchDiff> result) {
    Map<String, List<RuleMatchDiff>> addedToMatch = new HashMap<>();
    for (RuleMatchDiff diff : result) {
      if (diff.getStatus() == RuleMatchDiff.Status.ADDED) {
        String key = cleanSpan(diff.getNewMatch().getContext()) + "_" + diff.getNewMatch().getTitle();
        List<RuleMatchDiff> val = addedToMatch.get(key);
        if (val == null) {
          List<RuleMatchDiff> diffs = new ArrayList<>();
          diffs.add(diff);
          addedToMatch.put(key, diffs);
        } else {
          val.add(diff);
        }
      }
    }
    return addedToMatch;
  }

  private String cleanSpan(String s) {
    return s.replaceFirst(MARKER_START, "").replaceFirst("</span>", "");
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

  private void printDiffs(List<RuleMatchDiff> diffs, FileWriter fw, String langCode, String date, String filename, String ruleId) throws IOException {
    fw.write("Diffs found: " + diffs.size());
    if (diffs.size() > 0) {
      RuleMatchDiff diff1 = diffs.get(0);
      if (diff1.getOldMatch() != null) {
        fw.write(". Category: " + diff1.getOldMatch().getCategoryName());
      } else if (diff1.getNewMatch() != null) {
        fw.write(". Category: " + diff1.getNewMatch().getCategoryName());
      }
    }
    if (fullMode) {
      fw.write(". <a href='../" + langCode + "/" + filename + "'>Today's list</a>");
    } else {
      fw.write(". <a href='../" + langCode + "_full/" + filename + "'>Full list</a>");
    }
    String shortRuleId = ruleId.replaceFirst("^.* / ", "").replaceFirst("\\[[0-9]+\\]", "");
    fw.write(".  " + getAnalyticsLink(shortRuleId, langCode));
    fw.write("<br>\n");
    printTableBegin(fw);
    int iframeCount = 0;
    int i = 1;
    for (RuleMatchDiff diff : diffs) {
      if (diff.getStatus() == RuleMatchDiff.Status.ADDED) {
        fw.write("<tr style='background-color: #c7ffd0'>\n");
      } else if (diff.getStatus() == RuleMatchDiff.Status.REMOVED) {
        fw.write("<tr style='background-color: #ffd2d8'>\n");
      } else {
        fw.write("<tr>\n");
      }
      fw.write("  <td>" + diff.getStatus().name().substring(0, 3) + "<br>#" + i + " </td>\n");
      i++;
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
        iframeCount += printMessage(fw, oldMatch, newMatch, diff.getReplaces(), diff.getReplacedBy(), langCode, date, diff.getStatus(), iframeCount);
        printMarkerCol(fw, oldMatch, newMatch);
        if (oldMatch.getSuggestions().equals(newMatch.getSuggestions())) {
          fw.write("<td>");
          fw.write(oldMatch.getSuggestions().stream().map(k -> showTrimSpace(k)).collect(Collectors.joining(", ")));
          fw.write("</td>\n");
        } else {
          fw.write("<td>\n");
          fw.write("  <tt>old: </tt>" + oldMatch.getSuggestions().stream().map(k -> showTrimSpace(k)).collect(Collectors.joining(", ")));
          fw.write("  <br>");
          fw.write("  <tt>new: </tt>" + newMatch.getSuggestions().stream().map(k -> showTrimSpace(k)).collect(Collectors.joining(", ")));
          fw.write("</td>\n");
        }
      } else {
        LightRuleMatch match = diff.getOldMatch() != null ? diff.getOldMatch() : diff.getNewMatch();
        printRuleIdCol(fw, null, match);
        iframeCount += printMessage(fw, match, null, diff.getReplaces(), diff.getReplacedBy(), langCode, date, diff.getStatus(), iframeCount);
        printMarkerCol(fw, null, match);
        fw.write("  <td>" + match.getSuggestions().stream().map(k -> showTrimSpace(k)).collect(Collectors.joining(", ")) + "</td>\n");
      }
      fw.write("</tr>\n");
    }
    printTableEnd(fw);
  }

  private String getAnalyticsLink(String ruleId, String langCode) {
    String shortId = ruleId.replaceFirst("\\[[0-9]+\\]", "");
    String shortLangCode = langCode.replaceFirst("-.*", "");
    return "[<a href='https://internal1.languagetool.org/grafana/d/BY_CNDHGz/rule-events-analysis?orgId=1&var-rule_id=" +
      shortId + "&from=now-30d&var-language=" + shortLangCode + "' target='grafana' title='Grafana'>g</a>/" +
      "<a href='https://analytics.languagetoolplus.com/matomo/index.php?module=Widgetize&action=iframe&secondaryDimension=eventName&moduleToWidgetize=Events&actionToWidgetize=getAction&idSite=18&period=day&date=yesterday&flat=1&filter_column=label&show_dimensions=1&filter_pattern=" +
      shortId + "' target='disables' title='disables in Matomo'>m</a>]";
  }

  private String cleanSource(String ruleSource) {
    if (ruleSource == null) {
      return "java";
    }
    return ruleSource.replaceFirst("^.*/grammar", "gram.").replaceFirst("gram.-premium", "prem").replaceFirst(".xml", "");
  }

  private void printRuleIdCol(FileWriter fw, LightRuleMatch oldMatch, LightRuleMatch newMatch) throws IOException {
    fw.write("  <td class='small'>");
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
    if (newMatch.getTags().size() > 0) {
      fw.write("  <br><span class='status'>" + newMatch.getTags() + "</span>");
    }
    if (oldMatch != null && !newMatch.getTags().equals(oldMatch.getTags())) {
      fw.write("  <br><span class='status'>" + oldMatch.getTags() + " => " + newMatch.getTags() + "</span>");
    }
    fw.write(" </td>\n");
  }

  private void printMarkerCol(FileWriter fw, LightRuleMatch oldMatch, LightRuleMatch newMatch) throws IOException {
    fw.write("  <td>");
    String markedText = newMatch == null ? oldMatch.getCoveredText() : newMatch.getCoveredText();
    fw.write(markedText);
    fw.write(" </td>\n");
  }

  private int printMessage(FileWriter fw, LightRuleMatch oldMatch, LightRuleMatch newMatch,
                           LightRuleMatch replaces, LightRuleMatch replacedBy, String langCode, String date,
                           RuleMatchDiff.Status status, int iframeCount) throws IOException {
    fw.write("  <td>");
    String message;
    boolean canOverlap;
    if (newMatch == null) {
      fw.write(oldMatch.getMessage());
      message = oldMatch.getMessage();
      canOverlap = canOverlap(oldMatch);
    } else if (oldMatch.getMessage().equals(newMatch.getMessage())) {
      fw.write(oldMatch.getMessage());
      message = oldMatch.getMessage();
      canOverlap = canOverlap(oldMatch);
    } else {
      //System.out.println("old: " + oldMatch.getMessage());
      //System.out.println("new: " + newMatch.getMessage());
      fw.write(
        "<tt>old:</tt> " + showTrimSpace(oldMatch.getMessage()) + "<br>\n" +
          "<tt>new:</tt> " + showTrimSpace(newMatch.getMessage()));
      message = newMatch.getMessage();
      canOverlap = canOverlap(newMatch);
    }
    fw.write("  <br><span class='sentence'>" + showTrimSpace(escapeSentence(oldMatch.getContext())) + "</span>");
    boolean withIframe = false;
    if (status == RuleMatchDiff.Status.ADDED || status == RuleMatchDiff.Status.MODIFIED) {
      int markerFrom = oldMatch.getContext().indexOf(MARKER_START);
      int markerTo = oldMatch.getContext().replace(MARKER_START, "").indexOf(MARKER_END);
      String params = "sentence=" + enc(oldMatch.getContext().replace(MARKER_START, "").replace(MARKER_END, ""), 300) +
        "&rule_id=" + enc(oldMatch.getFullRuleId()) +
        "&filename=" + enc(cleanSource(oldMatch.getRuleSource())) +
        "&message=" + enc(message, 300) +
        "&suggestions=" + enc(String.join(", ", oldMatch.getSuggestions()), 300) +
        "&marker_from=" + markerFrom +
        "&marker_to=" + markerTo +
        "&language=" + enc(langCode) +
        "&day=" + enc(date);
      if (iframeCount > IFRAME_MAX) {
        // rendering 2000 iframes into a page isn't fun...
        fw.write("    <a target='regression_feedback' href=\"https://languagetoolplus.com/regression/button?" + params + "\">FA?</a>\n\n");
      } else {
        fw.write("    <iframe scrolling=\"no\" style=\"border: none; width: 165px; height: 30px\"\n" +
          "src=\"https://languagetoolplus.com/regression/button?" +
          //"src=\"http://127.0.0.1:8000/regression/button" +
          params + "\"></iframe>\n\n");
        withIframe = true;
      }
    }
    if (replaces != null) {
      if (canOverlap) {
        // can be ignored, sentence length rule can overlap other matches
      } else {
        fw.write("<br><br><i>Maybe replaces old match:</i><br>");
        fw.write(replaces.getMessage());
        fw.write("  <br><span class='sentence'>" + escapeSentence(replaces.getContext()) + "</span>");
        fw.write("  <br><span class='suggestions'>Suggestions: " + replaces.getSuggestions() + "</span>");
        fw.write("  <br><span class='id'>" + replaces.getFullRuleId() + "</span>");
      }
    }
    if (replacedBy != null) {
      if (canOverlap(replacedBy)) {
        // can be ignored, sentence length rule can overlap other matches
      } else {
        fw.write("<br><br><i>Maybe replaced by new match:</i><br>");
        fw.write(replacedBy.getMessage());
        fw.write("  <br><span class='sentence'>" + escapeSentence(replacedBy.getContext()) + "</span>");
        fw.write("  <br><span class='suggestions'>Suggestions: " + replacedBy.getSuggestions() + "</span>");
        fw.write("  <br><span class='id'>" + replacedBy.getFullRuleId() + "</span>\n");
      }
    }
    fw.write("  </td>\n");
    return withIframe ? 1 : 0;
  }

  private boolean canOverlap(LightRuleMatch match) {
    return match.getRuleId().equals("TOO_LONG_SENTENCE") || match.getRuleId().equals("TOO_LONG_SENTENCE_DE");
  }

  private String escapeSentence(String s) {
    return StringTools.escapeHTML(s).
      replace("&lt;span class='marker'&gt;", "<span class='marker'>").
      replace("&lt;/span&gt;", "</span>");
  }

  private String enc(String s) {
    return enc(s, Integer.MAX_VALUE);
  }

  private String enc(String s, int maxLen) {
    try {
      return URLEncoder.encode(StringUtils.abbreviate(s, maxLen), "utf-8");
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  private String showTrimSpace(String s) {
    s = s.replaceAll("\n", "<span class='whitespace'>\\\\n</span>");
    s = s.replaceFirst("^\\s", "<span class='whitespace'>&nbsp;</span>");
    s = s.replaceFirst("\\s$", "<span class='whitespace'>&nbsp;</span>");
    s = s.replaceAll("\u00A0", "<span class='nbsp' title='non-breaking space'>&nbsp;</span>");
    return s;
  }

  private void printTableBegin(FileWriter fw) throws IOException {
    fw.write("<table class='sortable_table'>\n");
    fw.write("<thead>\n");
    fw.write("<tr>\n");
    fw.write("  <th style='width:60px'>Change</th>\n");
    fw.write("  <th>File</th>\n");
    fw.write("  <th class='small'>Rule ID</th>\n");
    fw.write("  <th>Message and Text</th>\n");
    fw.write("  <th>Marked</th>\n");
    fw.write("  <th>Suggestions</th>\n");
    fw.write("</tr>\n");
    fw.write("</thead>\n");
    fw.write("<tbody>\n");
  }

  private void printTableEnd(FileWriter fw) throws IOException {
    fw.write("</tbody>\n");
    fw.write("</table>\n\n");
  }

  private void run(LightRuleMatchParser parser, File file1, File file2, File outputDir, String langCode, String date) throws IOException {
    if (file1.getName().equals("empty.json")) {
      fullMode = true;
    }
    LightRuleMatchParser.JsonParseResult jsonParseResult1 = parser.parseOutput(file1);
    List<LightRuleMatch> l1 = jsonParseResult1.result;
    LightRuleMatchParser.JsonParseResult jsonParseResult2 = parser.parseOutput(file2);
    List<LightRuleMatch> l2 = jsonParseResult2.result;
    String title = "Comparing " + file1.getName() + " to " + file2.getName();
    System.out.println(title);
    List<RuleMatchDiff> diffs = getDiffs(l1, l2);
    diffs.sort((k, j) -> {
        int idDiff = getFullId(k).compareTo(getFullId(j));
        if (idDiff == 0) {
          int diff2 = k.getStatus().compareTo(j.getStatus());
          if (diff2 == 0) {
            return k.getMarkedText().compareTo(j.getMarkedText());
          } else {
            return diff2;
          }
        }
        return idDiff;
      }
    );
    System.out.println("Total diffs found: " + diffs.size());
    Map<String, List<RuleMatchDiff>> keyToDiffs = groupDiffs(diffs);
    List<OutputFile> outputFiles = new ArrayList<>();
    for (Map.Entry<String, List<RuleMatchDiff>> entry : keyToDiffs.entrySet()) {
      String filename = "result_" + entry.getKey().replaceAll("/", "_").replaceAll("[\\s_]+", "_") + ".html";
      File outputFile = new File(outputDir, filename);
      if (entry.getValue().size() > 0) {
        outputFiles.add(new OutputFile(outputFile, entry.getValue()));
      }
      try (FileWriter fw = new FileWriter(outputFile)) {
        System.out.println("Writing result to " + outputFile);
        printHeader(title, fw);
        printDiffs(entry.getValue(), fw, langCode, date, filename, entry.getKey());
        printFooter(fw);
      }
    }
    try (FileWriter fw = new FileWriter(new File(outputDir, "index.html"))) {
      printHeader("Overview of regression results", fw);
      fw.write("<table class='sortable_table'>\n");
      fw.write("<thead>");
      fw.write("<tr>");
      fw.write("  <td>Total</td>");
      fw.write("  <td>ADD</td>");
      fw.write("  <td>REM</td>");
      fw.write("  <td>MOD</td>");
      fw.write("  <td>Source</td>");
      fw.write("  <td title='Picky'>P</td>");
      fw.write("  <td title='temp_off'>T</td>");
      fw.write("  <td>ID</td>");
      fw.write("  <td>Message of first match</td>");
      fw.write("</tr>");
      fw.write("</thead>");
      fw.write("<tbody>\n");
      for (OutputFile outputFile : outputFiles) {
        String file = outputFile.file.getName();
        fw.write("<tr>");
        fw.write("<td>" + outputFile.items.size() + "</td>");
        long added = outputFile.items.stream().filter(k -> k.getStatus() == RuleMatchDiff.Status.ADDED).count();
        fw.write("<td " + (added > 0 ? "style='background-color: #c7ffd0'" : "") + ">" + added + "</td>");
        long removed = outputFile.items.stream().filter(k -> k.getStatus() == RuleMatchDiff.Status.REMOVED).count();
        fw.write("<td " + (removed > 0 ? "style='background-color: #ffd2d8'" : "") + ">" + removed + "</td>");
        fw.write("<td>" + outputFile.items.stream().filter(k -> k.getStatus() == RuleMatchDiff.Status.MODIFIED).count() + "</td>");
        fw.write("<td>");
        fw.write(file.replaceFirst("result_", "").replaceFirst("_.*", ""));
        fw.write("</td>");
        if (outputFile.items.size() > 0 && outputFile.items.get(0).getNewMatch() != null) {
          fw.write("<td>" + (outputFile.items.get(0).getNewMatch().getTags().contains("picky") ? "p" : "") + "</td>");
        } else if (outputFile.items.size() > 0 && outputFile.items.get(0).getOldMatch() != null) {
          fw.write("<td>" + (outputFile.items.get(0).getOldMatch().getTags().contains("picky") ? "p" : "") + "</td>");
        } else {
          fw.write("<td></td>");
        }
        if (outputFile.items.size() > 0 && outputFile.items.get(0).getNewMatch() != null) {
          fw.write("<td>" + (outputFile.items.get(0).getNewMatch().getStatus() == LightRuleMatch.Status.temp_off ? "t" : "") + "</td>");
        } else if (outputFile.items.size() > 0 && outputFile.items.get(0).getOldMatch() != null) {
          fw.write("<td>" + (outputFile.items.get(0).getOldMatch().getStatus() == LightRuleMatch.Status.temp_off ? "t" : "") + "</td>");
        } else {
          fw.write("<td></td>");
        }
        fw.write("<td>");
        String id = file.replaceFirst("result_.*?_", "").replace(".html", "");
        fw.write("  <a href='" + file + "'>" + id + "</a>");
        fw.write("  " + getAnalyticsLink(id, langCode));
        fw.write("</td>");
        if (outputFile.items.size() > 0 && outputFile.items.get(0).getNewMatch() != null) {
          fw.write("<td class='msg'>" + escapeSentence(outputFile.items.get(0).getNewMatch().getMessage()) + "</td>");
        } else if (outputFile.items.size() > 0 && outputFile.items.get(0).getOldMatch() != null) {
          fw.write("<td class='msg'>" + escapeSentence(outputFile.items.get(0).getOldMatch().getMessage()) + "</td>");
        } else {
          fw.write("<td></td>");
        }
        fw.write("</tr>\n");
      }
      fw.write("</tbody>");
      fw.write("</table>\n\n");
      fw.write("<br><table class='meta'>\n");
      fw.write("  <tr><td>Old API:</td> <td>" + jsonParseResult1.buildDates + "</td></tr>\n");
      fw.write("  <tr><td>New API:</td> <td>" + jsonParseResult2.buildDates + "</td></tr>\n");
      fw.write("</table>\n");
      printFooterForIndex(fw);
    }
  }

  static class OutputFile {
    File file;
    List<RuleMatchDiff> items;

    OutputFile(File file, List<RuleMatchDiff> items) {
      this.file = file;
      this.items = items;
    }
  }

  private Map<String, List<RuleMatchDiff>> groupDiffs(List<RuleMatchDiff> diffs) {
    Map<String, List<RuleMatchDiff>> keyToDiffs = new TreeMap<>();
    String key;
    String prevKey = "";
    List<RuleMatchDiff> l = new ArrayList<>();
    for (RuleMatchDiff diff : diffs) {
      if (diff.getOldMatch() != null) {
        key = cleanSource(diff.getOldMatch().getRuleSource()) + " / " + diff.getOldMatch().getFullRuleId();
      } else {
        key = cleanSource(diff.getNewMatch().getRuleSource()) + " / " + diff.getNewMatch().getFullRuleId();
      }
      if (!key.equals(prevKey) && l.size() > 0) {
        keyToDiffs.put(prevKey, l);
        l = new ArrayList<>();
      }
      l.add(diff);
      prevKey = key;
    }
    if (l.size() > 0) {
      keyToDiffs.put(prevKey, l);
    }
    return keyToDiffs;
  }

  private void printHeader(String title, FileWriter fw) throws IOException {
    fw.write("<!doctype html>\n");
    fw.write("<html>\n");
    fw.write("<head>\n");
    fw.write("  <title>" + title + "</title>\n");
    fw.write("  <meta charset='utf-8'>\n");
    fw.write("  <script src='https://unpkg.com/tablefilter@0.7.0/dist/tablefilter/tablefilter.js'></script>\n");  // https://github.com/koalyptus/TableFilter/
    fw.write("  <style>\n");
    fw.write("    td { vertical-align: top; }\n");
    fw.write("    .small { font-size: small }\n");
    fw.write("    .sentence { color: #666; }\n");
    fw.write("    .marker { text-decoration: underline; background-color: rgba(200, 200, 200, 0.4) }\n");
    fw.write("    .source { color: #999; }\n");
    fw.write("    .status { color: #999; }\n");
    fw.write("    .whitespace { background-color: rgba(200, 200, 200, 0.3) }\n");
    fw.write("    .nbsp { background-color: rgba(200, 200, 200, 0.3) }\n");
    fw.write("    .id { color: #666; }\n");
    fw.write("    .msg { color: #666; }\n");
    fw.write("    .meta { color: #666; }\n");
    fw.write("  </style>\n");
    fw.write("</head>\n");
    fw.write("<body>\n\n");
  }

  private void printFooter(FileWriter fw) throws IOException {
    fw.write("<script>\n" +
      "var tf = new TableFilter(document.querySelector('.sortable_table'), {\n" +
      "    base_path: 'https://unpkg.com/tablefilter@0.7.0/dist/tablefilter/',\n" +
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

  private void printFooterForIndex(FileWriter fw) throws IOException {
    fw.write("<script>\n" +
      "var tf = new TableFilter(document.querySelector('.sortable_table'), {\n" +
      "    base_path: 'https://unpkg.com/tablefilter@0.7.0/dist/tablefilter/',\n" +
      "    auto_filter: { delay: 100 },\n" +
      "    col_0: 'none',\n" +
      "    col_1: 'none',\n" +
      "    col_2: 'none',\n" +
      "    col_3: 'none',\n" +
      "    col_4: 'select',\n" +
      "    col_5: 'none',\n" +
      "    grid_layout: false,\n" +
      "    col_types: ['number', 'number', 'number', 'number', 'string', 'string'],\n" +
      "    extensions: [{ name: 'sort' }]\n" +
      "});\n" +
      "tf.init();\n" +
      "</script>");
    fw.write("</body>\n");
    fw.write("</html>\n");
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

  private static void printUsageAndExit() {
    System.out.println("Usage: " + RuleMatchDiffFinder.class.getSimpleName() + " <matches1> <matches2> <resultDir> <date>");
    System.out.println(" <matches1> and <matches2> are text outputs of different versions of org.languagetool.dev.dumpcheck.SentenceSourceChecker run on the same input");
    System.out.println("                           or JSON outputs from org.languagetool.dev.httpchecker.HttpApiSentenceChecker");
    System.exit(1);
  }

  public static void main(String[] args) throws IOException {
    RuleMatchDiffFinder diffFinder = new RuleMatchDiffFinder();
    LightRuleMatchParser parser = new LightRuleMatchParser();
    if (args.length == 0) {
      printUsageAndExit();
    }
    if (args[0].contains("XX") && args[1].contains("XX") && args[2].contains("XX")) {
      if (args.length != 4) {
        printUsageAndExit();
      }
      System.out.println("Running in multi-file mode, replacing 'XX' in filenames with lang codes...");
      String file1 = args[0];
      String file3 = args[2];
      String date = args[3];
      File dir = new File(file1).getParentFile();
      String templateName = new File(file1).getName();
      int varPos = templateName.indexOf("XX");
      for (String file : dir.list()) {
        if (file.length() >= varPos + 1) {
          StringBuilder tempName = new StringBuilder(file).replace(varPos, varPos + 2, "XX");
          if (tempName.toString().equals(templateName)) {
            String langCode = file.substring(varPos, varPos + 2);
            String tempNameNew = file.replace(".old", ".new");
            File newFile = new File(dir, tempNameNew);
            if (!newFile.exists()) {
              throw new RuntimeException(tempNameNew + " not found - make sure files are names *.old and *.new in multi-file mode");
            }
            System.out.println("==== " + file + " =================================");
            File oldFile = new File(dir, file);
            String outputDir = file3.replace("XX", langCode);
            diffFinder.run(parser, oldFile, newFile, new File(outputDir), langCode, date);
          }
        }
      }
    } else {
      if (args.length != 5) {
        System.out.println("Usage: " + RuleMatchDiffFinder.class.getSimpleName() + " <matches1> <matches2> <resultDir> <langCode> <date>");
        System.out.println(" <matches1> and <matches2> are text outputs of different versions of org.languagetool.dev.dumpcheck.SentenceSourceChecker run on the same input");
        System.out.println("                           or JSON outputs from org.languagetool.dev.httpchecker.HttpApiSentenceChecker");
        System.exit(1);
      }
      File file1 = new File(args[0]);
      File file2 = new File(args[1]);
      File outputDir = new File(args[2]);
      String langCode = args[3];
      String date = args[4];
      if (outputDir.exists() && outputDir.isFile()) {
        throw new IOException("<resultDir> already exists, but is a file: " + outputDir);
      }
      if (!outputDir.exists()) {
        boolean mkdir = outputDir.mkdir();
        if (!mkdir) {
          throw new IOException("Could not create directory " + outputDir);
        }
      }
      diffFinder.run(parser, file1, file2, outputDir, langCode, date);
    }
  }

}
