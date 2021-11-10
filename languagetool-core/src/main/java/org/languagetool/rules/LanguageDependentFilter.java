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
import org.languagetool.rules.patterns.RuleSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/* 
 * Adjust rule matches for some languages  
 * 
 * @since 4.6
 */
public class LanguageDependentFilter implements RuleMatchFilter {

  protected Language language;
  protected Set<String> enabledRules;
  protected Set<CategoryId> disabledCategories;
  
  private static final Pattern CA_OLD_DIACRITICS = Pattern.compile(".*\\b(dóna|vénen|véns|fóra)\\b.*",Pattern.CASE_INSENSITIVE|Pattern.UNICODE_CASE);
  private static final Pattern ES_CONTRACTIONS = Pattern.compile("\\b([Aa]|[Dd]e) e(l)\\b");

  public LanguageDependentFilter(Language lang, RuleSet rules) {
    language = lang;
    enabledRules = rules.allRuleIds();
  }

  @Override
  public List<RuleMatch> filter(List<RuleMatch> ruleMatches) {
    if (language.getShortCode().equals("ca")) {
      // Use typographic apostrophe in suggestions
      if (enabledRules.contains("APOSTROF_TIPOGRAFIC") || !enabledRules.contains("DIACRITICS_TRADITIONAL_RULES")) {
        List<RuleMatch> newRuleMatches = new ArrayList<>();
        for (RuleMatch rm : ruleMatches) {
          List<String> replacements = rm.getSuggestedReplacements();
          List<String> newReplacements = new ArrayList<>();
          for (String s : replacements) {
            if (enabledRules.contains("APOSTROF_TIPOGRAFIC") && s.length() > 1) {
              s = s.replace("'", "’");
            }
            Matcher m = CA_OLD_DIACRITICS.matcher(s);
            if (!enabledRules.contains("DIACRITICS_TRADITIONAL_RULES") && m.matches()) {
              // skip this suggestion with traditional diacritics
            } else {
              newReplacements.add(s);
            }
          }
          RuleMatch newMatch = new RuleMatch(rm, newReplacements);
          newRuleMatches.add(newMatch);
        }
        return newRuleMatches;
      }
    } else if (language.getShortCode().equals("fr")) {
      if (this.enabledRules.contains("APOS_TYP")) {
        List<RuleMatch> newRuleMatches = new ArrayList<>();
        for (RuleMatch rm : ruleMatches) {
          List<String> replacements = rm.getSuggestedReplacements();
          List<String> newReplacements = new ArrayList<>();
          for (String s : replacements) {
            if (s.length() > 1) {
              s = s.replace("'", "’");
            }
            newReplacements.add(s);
          }
          RuleMatch newMatch = new RuleMatch(rm, newReplacements);
          newRuleMatches.add(newMatch);
        }
        return newRuleMatches;
      }
    } else if (language.getShortCode().equals("es")) {
        List<RuleMatch> newRuleMatches = new ArrayList<>();
        for (RuleMatch rm : ruleMatches) {
          List<String> replacements = rm.getSuggestedReplacements();
          List<String> newReplacements = new ArrayList<>();
          for (String s : replacements) {
            Matcher m = ES_CONTRACTIONS.matcher(s);
            s= m.replaceAll("$1$2");
            newReplacements.add(s);
          }
          RuleMatch newMatch = new RuleMatch(rm, newReplacements);
          newRuleMatches.add(newMatch);
        }
        return newRuleMatches;
    }
    return ruleMatches;
  }

}