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

import java.io.File;
import java.util.Iterator;
import java.util.List;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;

class CheckerThread extends Thread {

  private String text;
  private Language docLanguage;
  private Configuration config;
  private File baseDir;
  
  private JLanguageTool langTool; 
  private List ruleMatches;
  private boolean done = false;
  
  CheckerThread(String text, Language docLanguage, Configuration config, File baseDir) {
    this.text = text;
    this.docLanguage = docLanguage;
    this.config = config;
    this.baseDir = baseDir;
  }
  
  public boolean done() {
    return done;
  }

  List getRuleMatches() {
    return ruleMatches;
  }

  JLanguageTool getLanguageTool() {
    return langTool;
  }

  public void run() {
    try {
      langTool = new JLanguageTool(docLanguage, config.getMotherTongue(), baseDir);
      langTool.activateDefaultPatternRules();
      langTool.activateDefaultFalseFriendRules();
      if (config.getDisabledRuleIds() != null) {
        for (Iterator iter = config.getDisabledRuleIds().iterator(); iter.hasNext();) {
          String id = (String) iter.next();
          langTool.disableRule(id);
        }
      }
      ruleMatches = langTool.check(text);
      done = true;
    } catch (Exception e) {
      done = true;
      Main.showError(e);
    }
  }
  
}
