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

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses the output of org.languagetool.commandline.Main. Parsing JSON would
 * be more robust, but this way we can use the same output both here and for humans to read.
 */
class LightRuleMatchParser {

  private final Pattern startPattern = Pattern.compile("^(?:\\d+\\.\\) )?Line (\\d+), column (\\d+), Rule ID: (.*)");
  private final Pattern coverPattern = Pattern.compile("^([ ^]+)$");

  List<LightRuleMatch> parse(Reader reader) {
    List<LightRuleMatch> result = new ArrayList<>();
    int lineNum = -1;
    int columnNum = -1;
    String ruleId = null;
    String message = null;
    String context = null;
    String suggestion = null;
    String source = null;
    String title = null;
    Scanner sc = new Scanner(reader);
    while (sc.hasNextLine()) {
      String line = sc.nextLine();
      //System.out.println("L:" + line + " [ctx=" + context + "]");
      Matcher startMatcher = startPattern.matcher(line);
      Matcher coverMatcher = coverPattern.matcher(line);
      if (line.startsWith("Message: ")) {
        message = line.substring("Message: ".length());
      } else if (line.startsWith("Suggestion: ")) {
        suggestion = line.substring("Suggestion: ".length());
      } else if (line.startsWith("Rule source: ")) {
        source = line.substring("Rule source: ".length());
      } else if (line.startsWith("Title: ")) {
        title = line.substring("Title: ".length());
      } else if (startMatcher.matches()) {
        lineNum = Integer.parseInt(startMatcher.group(1));
        columnNum = Integer.parseInt(startMatcher.group(2));
        ruleId = startMatcher.group(3);
      } else if ((suggestion != null || message != null) && context == null) {
        // context comes directly after suggestion (if any)
        context = line;
      } else if (coverMatcher.matches() && line.contains("^")) {
        String cover = coverMatcher.group(1);
        int startMarkerPos = cover.indexOf("^");
        int endMarkerPos = cover.lastIndexOf("^") + 1;
        String coveredText;
        String origContext = context;
        try {
          int maxEnd = Math.min(endMarkerPos, context.length());
          coveredText = context.substring(startMarkerPos, maxEnd);
          context = context.substring(0, startMarkerPos) +
            "<span class='marker'> " +
            context.substring(startMarkerPos, maxEnd) +
            "</span>" +
            context.substring(maxEnd);
        } catch (StringIndexOutOfBoundsException e) {
          System.err.println("Cannot get context, setting to '???':");
          System.err.println(origContext);
          System.err.println(cover);
          //e.printStackTrace();
          coveredText = "???";
        }
        boolean isTempOff = ruleId.contains("[temp_off]"); 
        String cleanId = ruleId.replace("[off]", "").replace("[temp_off]", ""); 
        result.add(makeMatch(lineNum, columnNum, ruleId, cleanId, message, suggestion, context, coveredText, title, source, isTempOff));
        lineNum = -1;
        columnNum = -1;
        ruleId = null;
        message = null;
        context = null;
        suggestion = null;
        source = null;
        // don't reset title, can appear more than once per sentence
      }
    }
    return result;
  }
  
  private LightRuleMatch makeMatch(int line, int column, String ruleId, String cleanId, String message, String suggestions,
                                   String context, String coveredText, String title, String source, boolean isTempOff) {
    LightRuleMatch.Status s = ruleId.contains("[temp_off]") ? LightRuleMatch.Status.temp_off : LightRuleMatch.Status.on;
    return new LightRuleMatch(line, column, cleanId, message, context, coveredText, suggestions, source, title, s, isTempOff);
  }
  
}
