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

import java.util.Set;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.gui.ConfigurationDialog;

/**
 * A thread that shows the configuration dialog which lets the
 * user enable/disable rules.
 * 
 * @author Marcin Miłkowski
 * @author Daniel Naber
 */
class ConfigThread extends Thread {

  private final Language docLanguage;
  private final Configuration config;
  private final de.danielnaber.languagetool.openoffice.Main mainThread;
  
  private final ConfigurationDialog cfgDialog;
  
  ConfigThread(final Language docLanguage, final Configuration config,
      final de.danielnaber.languagetool.openoffice.Main main) {
    this.docLanguage = docLanguage;
    this.config = config;
    mainThread = main; 
    cfgDialog = new ConfigurationDialog(null, true);
    cfgDialog.setDisabledRules(config.getDisabledRuleIds());
    cfgDialog.setEnabledRules(config.getEnabledRuleIds());
    cfgDialog.setDisabledCategories(config.getDisabledCategoryNames());
    cfgDialog.setMotherTongue(config.getMotherTongue());    
  }
    
  public Set<String> getDisabledRuleIds() {
    return cfgDialog.getDisabledRuleIds();
  }  

  @Override
  public void run() {    
    try {
      final JLanguageTool langTool = new JLanguageTool(docLanguage, cfgDialog.getMotherTongue());
      langTool.activateDefaultPatternRules();
      langTool.activateDefaultFalseFriendRules();
      cfgDialog.show(langTool.getAllRules());
      config.setDisabledRuleIds(cfgDialog.getDisabledRuleIds());
      config.setEnabledRuleIds(cfgDialog.getEnabledRuleIds());
      config.setDisabledCategoryNames(cfgDialog.getDisabledCategoryNames());
      config.setMotherTongue(cfgDialog.getMotherTongue());
      config.saveConfiguration();
      if (mainThread != null) {
        mainThread.resetDocument();
      }
    } catch (Throwable e) {
      Main.showError(e);
    }
  }
  
}
