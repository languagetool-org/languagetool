/*
 * Created on 24.12.2005
 */
package de.danielnaber.languagetool.openoffice;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import de.danielnaber.languagetool.JLanguageTool;
import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.gui.Configuration;
import de.danielnaber.languagetool.gui.ConfigurationDialog;

class ConfigThread extends Thread {

  private Language docLanguage;
  private Configuration config;
  private File baseDir;
  
  private JLanguageTool langTool; 
  private ConfigurationDialog cfgDialog;
  
  ConfigThread(Language docLanguage, Configuration config, File baseDir) {
    this.docLanguage = docLanguage;
    this.config = config;
    this.baseDir = baseDir;
    cfgDialog = new ConfigurationDialog(true);
    cfgDialog.setDisabledRules(config.getDisabledRuleIds());
  }
  
  public boolean done() {
    return cfgDialog.isClosed();
  }
  
  public Set getDisabledRuleIds() {
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
      config.saveConfiguration();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (ParserConfigurationException e) {
      throw new RuntimeException(e);
    } catch (SAXException e) {
      throw new RuntimeException(e);
    }
  }
  
}
