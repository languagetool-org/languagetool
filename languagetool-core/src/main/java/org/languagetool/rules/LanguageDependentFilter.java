/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Jaume Ortolà (http://www.languagetool.org)
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

import org.languagetool.Language;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/* 
 * Adjust rule matches for some languages  
 * 
 * @since 4.6
 */
public class LanguageDependentFilter implements RuleMatchFilter {

  protected Language language;
  protected Set<String> enabledRules;
  protected Set<CategoryId> disabledCategories;
  
  public LanguageDependentFilter(Language lang, Set<String> enabledRules, Set<CategoryId> disabledRuleCategories) {
    this.language = lang;
    this.enabledRules = enabledRules;
    this.disabledCategories = disabledRuleCategories;
  }

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches) {
    if (language.getShortCode() == "ca") {
      // Use typographic apostrophe in suggestions
      CategoryId catID = new CategoryId("DIACRITICS_TRADITIONAL");
      if (this.enabledRules.contains("APOSTROF_TIPOGRAFIC") 
          || this.disabledCategories.contains(catID)) {
        List<RuleMatch> newRuleMatches = new ArrayList<>();
        for (RuleMatch rm : ruleMatches) {
          List<String> replacements = rm.getSuggestedReplacements();
          List<String> newReplacements = new ArrayList<>();
          for (String s: replacements) {
            if (this.enabledRules.contains("APOSTROF_TIPOGRAFIC") && s.length() > 1) {
              s = s.replace("'", "’");
            }
            if (this.disabledCategories.contains(catID) && s.matches(".*\\b([Dd]óna|[Vv]énen|[Vv]éns|[Ff]óra)\\b.*")) {
              // skip this suggestion with traditional diacritics
            } else {
              newReplacements.add(s);
            }
          }
          RuleMatch newrm = new RuleMatch(rm, newReplacements);
          newRuleMatches.add(newrm);
        }
        return newRuleMatches;
      }
    }
    return ruleMatches;
  }  
  
}