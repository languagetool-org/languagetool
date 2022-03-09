/* LanguageTool, a natural language style checker
 * Copyright (C) 2021 Daniel Naber (http://www.danielnaber.de)
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

import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.rules.RuleMatch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Simple script to print only the lines that do not have a match.
 */
public class CorrectLinePrinter {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + CorrectLinePrinter.class.getSimpleName() + " <file> <langCode>");
      System.exit(1);
    }
    List<String> lines = Files.readAllLines(Paths.get(args[0]));
    JLanguageTool lt = new JLanguageTool(Languages.getLanguageForShortCode(args[1]));
    lt.activateLanguageModelRules(new File("/home/languagetool/ngram-data/"));
    for (String line : lines) {
      List<RuleMatch> matches = lt.check(line);
      if (matches.size() == 0) {
        System.out.println(line);
      }
    }
  }
}
