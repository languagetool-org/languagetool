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

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.server.HTTPServer;

/**
 * Configuration -- currently this is just a list of disabled rule IDs.
 * Configuration is loaded from and stored to a properties file.
 * 
 * @author Daniel Naber
 */
public class Configuration {

  private static final String CONFIG_FILE = "languagetool.properties";
  private static final String DISABLED_RULES_CONFIG_KEY = "disabledRules";
  private static final String MOTHER_TONGUE_CONFIG_KEY = "motherTongue";
  private static final String SERVER_RUN_CONFIG_KEY = "serverMode";
  private static final String SERVER_PORT_CONFIG_KEY = "serverPort";

  private File configFile = null;

  private Set<String> disabledRuleIds = new HashSet<String>();
  private Language motherTongue;
  private boolean runServer = false;
  private int serverPort = HTTPServer.DEFAULT_PORT;

  public Configuration(File baseDir, String filename) throws IOException {
    if (!baseDir.isDirectory())
      throw new IllegalArgumentException("Not a directory: " + baseDir);
    configFile = new File(baseDir, filename);
    loadConfiguration();
  }
  
  public Configuration(File baseDir) throws IOException {
    this(baseDir, CONFIG_FILE);
  }
  
  public Set<String> getDisabledRuleIds() {
    return disabledRuleIds;
  }

  public void setDisabledRuleIds(Set<String> ruleIDs) {
    disabledRuleIds = ruleIDs;
  }

  public Language getMotherTongue() {
    return motherTongue;
  }

  public void setMotherTongue(Language motherTongue) {
    this.motherTongue = motherTongue;
  }

  public boolean getRunServer() {
    return runServer;
  }

  public void setRunServer(boolean runServer) {
    this.runServer = runServer;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
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
        for (String id : ids) {
          disabledRuleIds.add(id);
        }
      }
      String motherTongueStr = (String)props.get(MOTHER_TONGUE_CONFIG_KEY);
      if (motherTongueStr != null) {
        motherTongue = Language.getLanguageForShortName(motherTongueStr);
      }
      String runServerString = (String)props.get(SERVER_RUN_CONFIG_KEY);
      if (runServerString != null) {
        runServer = runServerString.equals("true");
      }
      String serverPortString = (String)props.get(SERVER_PORT_CONFIG_KEY);
      if (serverPortString != null) {
        serverPort = Integer.parseInt(serverPortString);
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
    StringBuilder sb = new StringBuilder();
    for (Iterator<String> iter = disabledRuleIds.iterator(); iter.hasNext();) {
      String id = iter.next();
      sb.append(id);
      if (iter.hasNext())
        sb.append(",");
    }
    if (disabledRuleIds == null)
      props.setProperty(DISABLED_RULES_CONFIG_KEY, "");
    else
      props.setProperty(DISABLED_RULES_CONFIG_KEY, sb.toString());
    if (motherTongue != null)
      props.setProperty(MOTHER_TONGUE_CONFIG_KEY, motherTongue.getShortName());
    props.setProperty(SERVER_RUN_CONFIG_KEY, Boolean.valueOf(runServer).toString());
    props.setProperty(SERVER_PORT_CONFIG_KEY, Integer.valueOf(serverPort).toString());
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
