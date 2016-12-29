/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Marcin Miłkowski (http://www.languagetool.org)
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

package org.languagetool.rules.pl;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Languages;
import org.languagetool.language.Polish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Another use of the compounds file -- check for compounds written with
 * dashes instead of hyphens (for example, Rabka — Zdrój). Not sure if this is generalizable for other
 * languages than Polish.
 * @since 3.6
 */
public class DashRule extends Rule {

  private final List<PatternRule> dashRules;

  public DashRule() throws IOException {
    dashRules = new ArrayList<>();
    loadCompoundFile("/pl/compounds.txt");
  }

  @Override
  public String getId() {
    return "DASH_RULE";
  }

  @Override
  public String getDescription() {
    return "Sprawdza, czy wyrazy pisane z łącznikiem zapisano z myślnikami (np. „Lądek — Zdrój” zamiast „Lądek-Zdrój”).";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    for (PatternRule dashRule : dashRules) {
      for (RuleMatch ruleMatch : dashRule.match(sentence)) {
        RuleMatch rm = new RuleMatch
            (this, ruleMatch.getFromPos(), ruleMatch.getToPos(), ruleMatch.getMessage(),
                ruleMatch.getShortMessage(), false, "");
        matches.add(rm);
      }
    }
    return matches.toArray(new RuleMatch[matches.size()]);
  }

  @Override
  public void reset() {
  }

  private void loadCompoundFile(String path) throws IOException {
    try (
        InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
        InputStreamReader reader = new InputStreamReader(stream, "utf-8");
        BufferedReader br = new BufferedReader(reader)
    ) {
      String line;
      int counter = 0;
      while ((line = br.readLine()) != null) {
        counter++;
        if (line.isEmpty() || line.charAt(0) == '#') {
          continue;     // ignore comments
        }
        if (line.endsWith("+")) {
          continue; // skip non-hyphenated suggestions
        } else if (line.endsWith("*")) {
          line = removeLastCharacter(line);
        }

        List<PatternToken> tokList = new ArrayList<PatternToken>();
        String[] tokens = line.split("-");
        int tokenCounter = 0;
        for (String token : tokens) {
          tokenCounter++;
            // token
          tokList.add(new PatternToken(token, true, false, false));
          if (tokenCounter < tokens.length) {
            // add dash
            tokList.add(new PatternToken("[—–]", false, true, false));
          }
        }
        PatternRule dashRule = new PatternRule
            ("DASH_RULE" + counter, Languages.getLanguageForName("Polish"), tokList,
                "", "Błędne użycie myślnika zamiast myślnika. " +
                "Poprawnie: <suggestion>"+line.replaceAll("[–—]", "-")+"</suggestion>.", line.replaceAll("[–—]", "-"));
        dashRules.add(dashRule);
      }
    }
  }

  private String removeLastCharacter(String str) {
    return str.substring(0, str.length() - 1);
  }

}
