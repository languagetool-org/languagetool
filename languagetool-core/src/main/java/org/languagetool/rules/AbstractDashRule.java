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

package org.languagetool.rules;

import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.rules.patterns.PatternRule;
import org.languagetool.rules.patterns.PatternToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Another use of the compounds file -- check for compounds written with
 * dashes instead of hyphens (for example, Rabka — Zdrój).
 * NOTE: this slows down checking a lot when used with large compound lists
 * @since 3.8
 */
public abstract class AbstractDashRule extends Rule {

  private final List<PatternRule> dashRules;

  public AbstractDashRule(List<PatternRule> dashRules) throws IOException {
    this.dashRules = Objects.requireNonNull(dashRules);
  }

  @Override
  public String getId() {
    return "DASH_RULE";
  }

  @Override
  public abstract String getDescription();

  @Override
  public int estimateContextForSureMatch() {
    return 2;
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    List<RuleMatch> matches = new ArrayList<>();
    for (PatternRule dashRule : dashRules) {
      for (RuleMatch match : dashRule.match(sentence)) {
        RuleMatch rm = new RuleMatch
            (this, match.getSentence(), match.getFromPos(), match.getToPos(), match.getMessage(),
                match.getShortMessage(), false, "");
        matches.add(rm);
      }
    }
    return matches.toArray(new RuleMatch[0]);
  }

  protected static List<PatternRule> loadCompoundFile(String path, String msg, Language lang) {
    List<PatternRule> rules = new ArrayList<>();
    try (
        InputStream stream = JLanguageTool.getDataBroker().getFromResourceDirAsStream(path);
        InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
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
        List<PatternToken> tokList = new ArrayList<>();
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
            ("DASH_RULE" + counter, lang, tokList,
                "", msg + "<suggestion>"+line.replaceAll("[–—]", "-")+"</suggestion>.", line.replaceAll("[–—]", "-"));
        rules.add(dashRule);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return rules;
  }

  private static String removeLastCharacter(String str) {
    return str.substring(0, str.length() - 1);
  }

}
