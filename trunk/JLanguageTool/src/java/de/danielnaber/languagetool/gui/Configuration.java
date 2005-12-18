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
package de.danielnaber.languagetool.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * Configuration -- currently this is just a list of disabled rule IDs.
 * Configuration is loaded from and stored to a properties file.
 * 
 * @author Daniel Naber
 */
public class Configuration {

  private static final String CONFIG_FILE = "languagetool.properties";
  private static final String DISABLED_RULES_CONFIG_KEY = "disabledRules";
  private File configFile = null;

  private Set disabledRuleIds = new HashSet();

  public Configuration(File baseDir) throws IOException {
    if (!baseDir.isDirectory())
      throw new IllegalArgumentException("Not a directory: " + baseDir);
    configFile = new File(baseDir, CONFIG_FILE);
    loadConfiguration();
  }
  
  public Set getDisabledRuleIds() {
    return disabledRuleIds;
  }

  public void setDisabledRuleIds(Set ruleIDs) {
    disabledRuleIds = ruleIDs;
  }

  private void loadConfiguration() throws IOException {
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(configFile);
      Properties props = new Properties();
      props.load(fis);
      String val = (String)props.get(DISABLED_RULES_CONFIG_KEY);
      if (val != null) {
        String[] ids = val.split(",");
        for (int i = 0; i < ids.length; i++) {
          disabledRuleIds.add(ids[i]);
        }
      }
    } catch (FileNotFoundException e) {
      // file not found: okay, leave disabledRuleIds empty
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }

  public void saveConfiguration() throws IOException {
    Properties props = new Properties();
    StringBuffer sb = new StringBuffer();
    for (Iterator iter = disabledRuleIds.iterator(); iter.hasNext();) {
      String id = (String) iter.next();
      sb.append(id);
      if (iter.hasNext())
        sb.append(",");
    }
    if (disabledRuleIds == null)
      props.setProperty(DISABLED_RULES_CONFIG_KEY, "");
    else
      props.setProperty(DISABLED_RULES_CONFIG_KEY, sb.toString());
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(configFile);
      props.store(fos, "LanguageTool configuration");
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }
  
}
