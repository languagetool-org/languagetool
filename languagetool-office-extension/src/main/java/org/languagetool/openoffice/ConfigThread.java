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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
  private final SwJLanguageTool lt;
  private final MultiDocumentsHandler documents;
  private final ConfigurationDialog cfgDialog;
  
  ConfigThread(Language docLanguage, Configuration config, SwJLanguageTool lt, MultiDocumentsHandler documents) {
    if (config.getDefaultLanguage() == null) {
      this.docLanguage = docLanguage;
    } else {
      this.docLanguage = config.getDefaultLanguage();
    }
    this.config = config;
    this.lt = lt;
    this.documents = documents; 
    cfgDialog = new ConfigurationDialog(null, true, config);
  }

  @Override
  public void run() {
    if(!documents.javaVersionOkay()) {
      return;
    }
    try {
      List<Rule> allRules = lt.getAllRules();
      Set<String> disabledRulesUI = documents.getDisabledRules(docLanguage.getShortCodeWithCountryAndVariant());
      config.addDisabledRuleIds(disabledRulesUI);
      boolean configChanged = cfgDialog.show(allRules);
      if (configChanged) {
        Set<String> disabledRules = config.getDisabledRuleIds();
        Set<String> tmpDisabledRules = new HashSet<>(disabledRulesUI);
        for (String ruleId : tmpDisabledRules) {
          if(!disabledRules.contains(ruleId)) {
            disabledRulesUI.remove(ruleId);
          }
        }
        documents.setDisabledRules(docLanguage.getShortCodeWithCountryAndVariant(), disabledRulesUI);
        config.removeDisabledRuleIds(disabledRulesUI);
        config.saveConfiguration(docLanguage);
        documents.resetDocumentCaches();
        documents.resetConfiguration();
      } else {
        config.removeDisabledRuleIds(documents.getDisabledRules(docLanguage.getShortCodeWithCountryAndVariant()));
      }
    } catch (Throwable e) {
      MessageHandler.showError(e);
    }
  }
  
}
