/* LanguageTool, a natural language style checker
 * Copyright (C) 2022 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.remote;

import org.jetbrains.annotations.NotNull;
import org.languagetool.tools.Tools;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * German only for now. Takes sentences, removes commas, sees if LT finds the missing
 * commas and suggests adding them at the correct location.
 */
public class MissingCommaEval {

  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.out.println("Usage: " + MissingCommaEval.class.getSimpleName() + " <file>");
      System.out.println("         <file> is a file with sentences, once sentence per line; commas will be " +
        "removed and each line will be checked");
      System.exit(1);
    }
    int totalCommas = 0;
    int totalCommasFound = 0;
    int lineCount = 0;
    RemoteLanguageTool lt = new RemoteLanguageTool(Tools.getUrl("http://localhost:8081"));
    CheckConfiguration config = new CheckConfigurationBuilder().disabledRuleIds("WHITESPACE_RULE").build();
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    for (String line : lines) {
      if (!line.contains(", ")) {
        System.out.println("No comma (', '), skipping: " + line);
        continue;
      }
      List<Integer> commaPositions = getCommaPositions(line);
      List<Integer> foundCommaPositions = new ArrayList<>();
      String noCommas = line.replace(", ", "  ");  // 2 spaces! to keep positions
      System.out.println(++lineCount + ".");
      System.out.println("I: " + noCommas + " -> " + commaPositions);
      RemoteResult result;
      try {
        result = lt.check(noCommas, config);
      } catch (RuntimeException e) {
        System.out.println("Exception, skipping '" + line + "': ");
        e.printStackTrace();
        continue;
      }
      for (RemoteRuleMatch match : result.getMatches()) {
        for (String repl : match.getReplacements().get()) {
          String fixedSentence = new StringBuilder(noCommas).replace(match.getErrorOffset(),
            match.getErrorOffset()+match.getErrorLength(), repl).toString();
          if (repl.contains(",")) {
            List<Integer> commaPosInFixedSentence = getCommaPositions(fixedSentence);
            for (Integer commaPos : commaPosInFixedSentence) {
              System.out.println("F: " + fixedSentence + " - " + match.getRuleId() + ", comma at " + commaPos);
              // be more generous due to issues with offset problems (as we replace ", " by two spaces...):
              boolean commaProperlyAdded = commaPositions.contains(commaPos) || commaPositions.contains(commaPos-1) || commaPositions.contains(commaPos+1);
              if (commaProperlyAdded && !foundCommaPositions.contains(commaPos)) {
                foundCommaPositions.add(commaPos);
              }
            }
          }
        }
      }
      System.out.println("   Found " + foundCommaPositions.size() + " of " + commaPositions.size() + " commas");
      totalCommas += commaPositions.size();
      totalCommasFound += foundCommaPositions.size();
    }
    System.out.println("Total commas originally   : " + totalCommas);
    float percent = (float)totalCommasFound / totalCommas * 100.0f;
    System.out.printf(Locale.ENGLISH, "Total removed commas found: %d (%.2f%%)", totalCommasFound, percent);
  }

  @NotNull
  private static List<Integer> getCommaPositions(String line) {
    List<Integer> commaPositions = new ArrayList<>();
    int oldPos = 0;
    while (true) {
      int pos = line.indexOf(",", oldPos);
      if (pos == -1) {
        break;
      }
      commaPositions.add(pos);
      oldPos = pos + 1;
    }
    return commaPositions;
  }

}
