/* LanguageTool, a natural language style checker 
 * Copyright (C) 2011 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.openoffice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.languagetool.gui.Configuration;
import org.languagetool.rules.Rule;
import org.languagetool.rules.TextLevelRule;

/**
 * class to store all text level rules sorted by the minimum to check paragraphs
 * (currently only full text check and all other text level rules)
 * @since 5.3
 * @author Fred Kruse
 */
class SortedTextRules { 

  private static boolean debugMode = false;   //  should be false except for testing
  
  List<Integer> minToCheckParagraph;
  List<List<String>> textLevelRules;

  SortedTextRules (SwJLanguageTool lt, Configuration config, Set<String> disabledRulesUI) {
    minToCheckParagraph = new ArrayList<>(OfficeTools.NUMBER_TEXTLEVEL_CACHE);
    textLevelRules = new ArrayList<>(OfficeTools.NUMBER_TEXTLEVEL_CACHE);
    minToCheckParagraph.add(0,0);
    minToCheckParagraph.add(1,1);
    minToCheckParagraph.add(2,-1);
    minToCheckParagraph.add(3,-2);
    for (int i = 0; i < OfficeTools.NUMBER_TEXTLEVEL_CACHE; i++) {
      textLevelRules.add(i, new ArrayList<>());
      debugMode = OfficeTools.DEBUG_MODE_SR;
    }
    List<Rule> rules = lt.getAllActiveOfficeRules();
    int numParasToCheck = config.getNumParasToCheck();
    for (Rule rule : rules) {
      if (rule instanceof TextLevelRule && !lt.getDisabledRules().contains(rule.getId()) 
          && !disabledRulesUI.contains(rule.getId())) {
        insertRule(((TextLevelRule) rule).minToCheckParagraph(), numParasToCheck, rule.getId());
      }
    }
    if (debugMode) {
      MessageHandler.printToLogFile("SortedTextRules: Number different minToCheckParagraph: " + minToCheckParagraph.size());
      for ( int i = 0; i < minToCheckParagraph.size(); i++) {
        MessageHandler.printToLogFile("SortedTextRules: minToCheckParagraph: " + minToCheckParagraph.get(i));
        for (int j = 0; j < textLevelRules.get(i).size(); j++) {
          MessageHandler.printToLogFile("RuleId: " + textLevelRules.get(i).get(j));
        }
      }
    }
  }

  /**
   * Insert a rule to list of text level rules
   */
  private void insertRule (int minPara, int numParasToCheck, String ruleId) {
    if (minPara == 0) {
        textLevelRules.get(0).add(ruleId);
    } else {
      if (numParasToCheck >= 0) {
        textLevelRules.get(1).add(ruleId);
        if (minPara < 0 && minToCheckParagraph.get(1) < numParasToCheck) {
          minToCheckParagraph.set(1, numParasToCheck);
        } else if (minPara <= numParasToCheck && minPara > minToCheckParagraph.get(1)) {
          minToCheckParagraph.set(1, minPara);
        }
      } else if (minPara > 0) {
        textLevelRules.get(1).add(ruleId);
        if (minPara > minToCheckParagraph.get(1)) {
          minToCheckParagraph.set(1, minPara);
        }
      } else if (minPara == -2 && numParasToCheck == -2) {
        textLevelRules.get(3).add(ruleId);
      } else {
        textLevelRules.get(2).add(ruleId);
      }
    }
  }

  /**
   * Get the minimum of paragraphs that should be checked
   */
  public List<Integer> getMinToCheckParas() {
    return minToCheckParagraph;
  }

  /**
   * Activate the text level rules for a specified cache 
   */
  public void activateTextRulesByIndex(int nCache, SwJLanguageTool lt) {
    for (int i = 0; i < textLevelRules.size(); i++) {
      if (i == nCache) {
        for (String ruleId : textLevelRules.get(i)) {
          lt.enableRule(ruleId);
        }
      } else {
        for (String ruleId : textLevelRules.get(i)) {
          lt.disableRule(ruleId);
        }
      }
    }
  }

  /**
   * Reactivate the text level rules which was deactivated for a specified cache 
   */
  public void reactivateTextRules(SwJLanguageTool lt) {
    for (List<String> textRules : textLevelRules) {
      for (String ruleId : textRules) {
        lt.enableRule(ruleId);
      }
    }
  }

}

