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
package org.languagetool.openoffice;

import java.util.List;

import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.gui.Configuration;
import org.languagetool.gui.ConfigurationDialog;
import org.languagetool.rules.Rule;

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
  private final Main mainThread;
  
  private final ConfigurationDialog cfgDialog;
  
  ConfigThread(Language docLanguage, Configuration config, Main main) {
    this.docLanguage = docLanguage;
    this.config = config;
    mainThread = main; 
    cfgDialog = new ConfigurationDialog(null, true, config);
  }

  @Override
  public void run() {
    try {
      JLanguageTool langTool = new JLanguageTool(docLanguage, config.getMotherTongue());
      List<Rule> allRules = langTool.getAllRules();
      for (Rule rule : allRules) {
        if(rule.isOfficeDefaultOn()) rule.setDefaultOn();
        else if(rule.isOfficeDefaultOff()) rule.setDefaultOff();
      }
      cfgDialog.show(allRules);
      config.saveConfiguration(docLanguage);
      if (mainThread != null) {
        mainThread.resetDocument();
      }
    } catch (Throwable e) {
      Main.showError(e);
    }
  }
  
}
