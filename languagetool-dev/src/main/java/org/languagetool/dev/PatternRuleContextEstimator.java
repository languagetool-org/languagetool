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
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleInformation;
import org.languagetool.rules.patterns.PatternRule;

/**
 * Manual testing of org.languagetool.rules.patterns.PatternRule#estimateContextForSureMatch().
 */
public class PatternRuleContextEstimator {

  public static void main2(String[] args) {
    Language german = Languages.getLanguageForShortCode("de");
    JLanguageTool lt = new JLanguageTool(german);
    int i = 0;
    for (Rule rule : lt.getAllActiveRules()) {
      if (rule instanceof PatternRule/* && rule.getId().equals("ER_LIES")*/) {
        int estimatedContext = rule.estimateContextForSureMatch();
        String ruleId = ((PatternRule) rule).getFullId();
        System.out.println(ruleId + " -> " + estimatedContext);
      }
    }
  }

  public static void main(String[] args) {
    Language german = Languages.getLanguageForShortCode("de");
    JLanguageTool lt = new JLanguageTool(german);
    int i = 0;
    for (Rule rule : lt.getAllActiveRules()) {
      if (rule instanceof PatternRule) {
        int estimatedContext = rule.estimateContextForSureMatch();
        String ruleId = ((PatternRule) rule).getFullId();
        //System.out.println(ruleId + " -> " + estimatedContext);
        boolean ignoreWhenIncomplete = RuleInformation.ignoreForIncompleteSentences(rule.getId(), german);
        if (ignoreWhenIncomplete && estimatedContext == 0) {
          System.out.println(++i + ". ignored for incomplete sentences but context = 0: " + ruleId);
        }
      }
    }
  }

}
