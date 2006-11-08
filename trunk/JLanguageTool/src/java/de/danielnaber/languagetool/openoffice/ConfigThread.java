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
import java.util.Set;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.gui.ConfigurationDialog;

class ConfigThread extends Thread {

  private Language docLanguage;
  private Configuration config;
  private File baseDir;
  private boolean done = false;
  
  private JLanguageTool langTool; 
  private ConfigurationDialog cfgDialog;
  
  ConfigThread(final Language docLanguage, final Configuration config, final File baseDir) {
    this.docLanguage = docLanguage;
    this.config = config;
    this.baseDir = baseDir;
    cfgDialog = new ConfigurationDialog(true, true);
    cfgDialog.setDisabledRules(config.getDisabledRuleIds());
    cfgDialog.setMotherTongue(config.getMotherTongue());
  }
  
  public boolean done() {
    return done || cfgDialog.isClosed();
  }
  
  public Set<String> getDisabledRuleIds() {
    return cfgDialog.getDisabledRuleIds();
  }

  JLanguageTool getLanguageTool() {
    return langTool;
  }

  public void run() {
    try {
      JLanguageTool langTool = new JLanguageTool(docLanguage, baseDir);
      langTool.activateDefaultPatternRules();
      cfgDialog.show(langTool.getAllRules());
      config.setDisabledRuleIds(cfgDialog.getDisabledRuleIds());
      config.setMotherTongue(cfgDialog.getMotherTongue());
      config.saveConfiguration();
    } catch (Throwable e) {
      done = true;
      Main.showError(e);
    }
  }
  
}
