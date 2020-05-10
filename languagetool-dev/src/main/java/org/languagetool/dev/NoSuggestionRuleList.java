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

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.IncorrectExample;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.AbstractPatternRule;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * List rules that offer no suggestion.
 */
public class NoSuggestionRuleList {
  
  // format: rule_id,match_propability_as_float (and optionally more columns)
  // e.g.:
  // MORFOLOGIK_RULE_NL_NL,0.596809797834639
  // EINDE_ZIN_ONVERWACHT,0.2137227907162415
  private final static String POPULARITY_FILE = "/home/dnaber/Downloads/rule_matches_nl_1w_detailed.csv";  // set to null to skip

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.out.println("Usage: " + NoSuggestionRuleList.class.getSimpleName() + " <langCodes>");
      System.exit(1);
    }
    Map<String,Float> popularity = new HashMap<>();
    if (POPULARITY_FILE != null) {
      List<String> lines = Files.readAllLines(Paths.get(POPULARITY_FILE));
      for (String line : lines) {
        String[] parts = line.split(",");
        try {
          popularity.put(parts[0], Float.parseFloat(parts[1]));
        } catch (NumberFormatException e) {
          System.err.println("Ignoring line: " + line + ", " + e.getMessage());
        }
      }
    }
    System.out.println("Loaded " + popularity.size() + " popularity mappings");
    for (String langCode : args) {
      Language lang = Languages.getLanguageForShortCode(langCode);
      JLanguageTool lt = new JLanguageTool(lang);
      for (Rule rule : lt.getAllActiveRules()) {
        lt.disableRule(rule.getId());
      }
      int suggestion = 0;
      int noSuggestion = 0;
      for (Rule rule : lt.getAllRules()) {
        if (rule.isDefaultOff()) {
          continue;
        }
        List<IncorrectExample> incorrectExamples = rule.getIncorrectExamples();
        if (incorrectExamples.isEmpty()) {
          //System.err.println("Skipping " + rule.getId() + " (no example)");
          continue;
        }
        String incorrectExample = incorrectExamples.get(0).getExample().replaceAll("<marker>", "").replaceAll("</marker>", "");
        lt.enableRule(rule.getId());
        List<RuleMatch> matches = lt.check(incorrectExample);
        for (RuleMatch match : matches) {
          if (match.getSuggestedReplacements().isEmpty()) {
            //if (rule instanceof AbstractPatternRule) {
            //  printRule(((AbstractPatternRule)rule).getFullId(), rule, incorrectExample, popularity);
            //} else {
            //  printRule(rule.getId(), rule, incorrectExample, popularity);
            //}
            printRule(rule.getId(), rule, incorrectExample, popularity);
            noSuggestion++;
          } else {
            suggestion++;
          }
          break;
        }
        lt.disableRule(rule.getId());
      }
      System.out.println(lang + ":");
      System.out.printf("With suggestion   : %d\n", suggestion);
      System.out.printf("Without suggestion: %d (%.2f%%)\n", noSuggestion, ((float)noSuggestion / (suggestion + noSuggestion))*100.0);
      System.out.println();
    }
  }

  private static void printRule(String id, Rule rule, String incorrectExample, Map<String, Float> popularity) {
    Float pop = popularity.get(rule.getId());
    if (pop != null) {
      System.out.printf(Locale.ENGLISH, "%.4f " + id + "\n", pop);
    } else {
      System.out.println("0 " + id);
    }
    //System.out.println("--> "+incorrectExample);
  }
}
