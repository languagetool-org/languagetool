/* LanguageTool, a natural language style checker
 * Copyright (C) 2014 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.dev.eval;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

import java.io.*;
import java.util.List;

/**
 * Simple command line tool to compare spell checker suggestions. Run it,
 * change the source code, run it again and diff the output to see what
 * has changed.
 * @since 2.7
 */
public class SpellCheckEvaluation {
  
  private static final int MAX_SUGGESTIONS = 5;

  private void run(Language language, File file) throws IOException {
    JLanguageTool lt = getLanguageToolForSpellCheck(language);
    checkFile(file, lt);
  }

  private JLanguageTool getLanguageToolForSpellCheck(Language language) {
    JLanguageTool lt = new JLanguageTool(language);
    for (Rule rule : lt.getAllActiveRules()) {
      if (!rule.isDictionaryBasedSpellingRule()) {
        lt.disableRule(rule.getId());
      }
    }
    return lt;
  }

  private void checkFile(File file, JLanguageTool lt) throws IOException {
    try (
      FileInputStream fis = new FileInputStream(file);
      InputStreamReader reader = new InputStreamReader(fis, "utf-8");
      BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      while ((line = br.readLine()) != null) {
        List<RuleMatch> matches = lt.check(line);
        for (RuleMatch match : matches) {
          String covered = line.substring(match.getFromPos(), match.getToPos());
          List<String> suggestions = match.getSuggestedReplacements();
          List<String> limitedSuggestions = suggestions.subList(0, Math.min(MAX_SUGGESTIONS, suggestions.size()));
          System.out.println(covered + ": " + limitedSuggestions);
        }
      }
    }
  }

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println("Usage: " + SpellCheckEvaluation.class.getSimpleName() + " <langCode> <textFile>");
      System.exit(1);
    }
    SpellCheckEvaluation eval = new SpellCheckEvaluation();
    eval.run(Languages.getLanguageForShortCode(args[0]), new File(args[1]));
  }
}
