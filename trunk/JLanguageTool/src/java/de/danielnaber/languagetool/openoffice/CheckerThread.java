/* LanguageTool, a natural language style checker 
 * Copyright (C) 2005 Daniel Naber (http://www.danielnaber.de)
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
package de.danielnaber.languagetool.openoffice;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.rules.RuleMatch;

class CheckerThread extends Thread {

  private List<String> paragraphs;
  private Language docLanguage;
  private Configuration config;

  private ProgressInformation progressInfo;

  private JLanguageTool langTool; 
  private List<CheckedParagraph> checkedParagraphs = new ArrayList<CheckedParagraph>();
  private boolean done = false;

  CheckerThread(final List<String> paragraphs, final Language docLanguage, final Configuration config,
      final ProgressInformation progressInfo) {
    this.paragraphs = paragraphs;
    this.docLanguage = docLanguage;
    this.config = config;
    this.progressInfo = progressInfo;
    progressInfo.setMaxProgress(paragraphs.size());
  }

  public boolean done() {
    return done;
  }

  List<CheckedParagraph> getRuleMatches() {
    return checkedParagraphs;
  }

  JLanguageTool getLanguageTool() {
    return langTool;
  }

  public void run() {
    try {
      langTool = new JLanguageTool(docLanguage, config.getMotherTongue());
      langTool.activateDefaultPatternRules();
      langTool.activateDefaultFalseFriendRules();
      if (config.getDisabledRuleIds() != null) {
        for (String id : config.getDisabledRuleIds()) {
          langTool.disableRule(id);
        }
      }
      Set<String> disabledCategories = config.getDisabledCategoryNames();
      if (disabledCategories != null) {
        for (String categoryName : disabledCategories) {
          langTool.disableCategory(categoryName);
        }
      }
      int paraCount = 0;
      for (String para : paragraphs) {
        List<RuleMatch> ruleMatches = langTool.check(para);
        if (ruleMatches.size() > 0) {
          checkedParagraphs.add(new CheckedParagraph(paraCount, ruleMatches));
        }
        paraCount++;
        progressInfo.setProgress(paraCount);
      }
      done = true;
    } catch (Exception e) {
      done = true;
      Main.showError(e);
    }
  }

}
