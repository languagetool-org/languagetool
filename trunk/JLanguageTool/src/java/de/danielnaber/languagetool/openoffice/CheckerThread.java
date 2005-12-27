/*
 * Created on 24.12.2005
 */
package de.danielnaber.languagetool.openoffice;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

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
      langTool = new JLanguageTool(docLanguage, baseDir);
      langTool.activateDefaultPatternRules();
      for (Iterator iter = config.getDisabledRuleIds().iterator(); iter.hasNext();) {
        String id = (String) iter.next();
        langTool.disableRule(id);
      }
      ruleMatches = langTool.check(text);
      done = true;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    } catch (SAXException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }
  
}
