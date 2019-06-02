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
package org.languagetool.dev;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.language.Dutch;
import org.languagetool.rules.nl.MorfologikDutchSpellerRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class DutchWordSplitter {

  public static void main(String[] args) throws IOException {
    // exported as documented at http://wiki.languagetool.org/developing-a-tagger-dictionary#toc2,
    // then taking only the full form: awk '{print $1}' dictionary-nl.dump
    String filename = "/home/dnaber/lt/dictionary-nl.dump";
    Language dutch = new Dutch();
    JLanguageTool lt = new JLanguageTool(dutch);
    List<String> lines = Files.readAllLines(Paths.get(filename));
    MorfologikDutchSpellerRule spellerRule = new MorfologikDutchSpellerRule(JLanguageTool.getMessageBundle(), dutch, null);
    int lineCount = 0;
    for (String line : lines) {
      if (isValidSpelling(line, spellerRule, lt)) {
        for (int i = 1; i < line.length(); i++) {
          String part1 = line.substring(0, i);
          String part2 = line.substring( i);
          if (isValidSpelling(part1, spellerRule, lt) && isValidSpelling(part2, spellerRule, lt)) {
            System.out.println(line + " => " + part1 + " " + part2);
          }
        }
      }
      lineCount++;
      if (lineCount % 100 == 0) {
        System.out.println("lineCount: " + lineCount);
      }
    }
  }

  private static boolean isValidSpelling(String word, MorfologikDutchSpellerRule spellerRule, JLanguageTool lt) throws IOException {
    AnalyzedSentence as = lt.getAnalyzedSentence(word);
    return spellerRule.match(as).length <= 0;
  }

}
