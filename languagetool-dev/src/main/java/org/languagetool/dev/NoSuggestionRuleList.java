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
import java.util.List;

/**
 * List rules that offer no suggestion.
 */
public class NoSuggestionRuleList {

  public static void main(String[] args) throws IOException {
    if (args.length < 1) {
      System.out.println("Usage: " + NoSuggestionRuleList.class.getSimpleName() + " <langCodes>");
      System.exit(1);
    }
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
        if (incorrectExamples.size() == 0) {
          //System.err.println("Skipping " + rule.getId() + " (no example)");
          continue;
        }
        String incorrectExample = incorrectExamples.get(0).getExample().replaceAll("<marker>", "").replaceAll("</marker>", "");
        lt.enableRule(rule.getId());
        List<RuleMatch> matches = lt.check(incorrectExample);
        for (RuleMatch match : matches) {
          if (match.getSuggestedReplacements().size() == 0) {
            if (rule instanceof AbstractPatternRule) {
              //System.out.println("No suggestion for: " + ((AbstractPatternRule) rule).getFullId());
              //System.out.println("--> "+incorrectExample);
            } else {
              //System.out.println("No suggestion for: " + rule.getId());
              //System.out.println("--> "+incorrectExample);
            }
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
}
