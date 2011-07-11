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
import java.util.*;

import de.danielnaber.languagetool.Language;
import de.danielnaber.languagetool.server.HTTPServer;
import de.danielnaber.languagetool.tools.StringTools;

/**
 * Configuration -- currently this is mostly a list of disabled rule IDs.
 * Configuration is loaded from and stored to a properties file.
 * 
 * @author Daniel Naber
 */
public class Configuration {

  private static final String CONFIG_FILE = "languagetool.properties";
  private static final String DISABLED_RULES_CONFIG_KEY = "disabledRules";
  private static final String ENABLED_RULES_CONFIG_KEY = "enabledRules";
  private static final String DISABLED_CATEGORIES_CONFIG_KEY = "disabledCategories";
  private static final String MOTHER_TONGUE_CONFIG_KEY = "motherTongue";
  private static final String AUTO_DETECT_CONFIG_KEY = "autoDetect";
  private static final String SERVER_RUN_CONFIG_KEY = "serverMode";
  private static final String SERVER_PORT_CONFIG_KEY = "serverPort";
  private static final String DELIMITER = ",";

  private File configFile;
  private Set<String> disabledRuleIds = new HashSet<String>();
  private Set<String> enabledRuleIds = new HashSet<String>();
  private Set<String> disabledCategoryNames = new HashSet<String>();
  private Language motherTongue;
  private boolean runServer;
  private boolean autoDetect;
  private int serverPort = HTTPServer.DEFAULT_PORT;

  public Configuration(final File baseDir, final String filename)
      throws IOException {
    if (!baseDir.isDirectory()) {
      throw new IllegalArgumentException("Not a directory: " + baseDir);
    }
    configFile = new File(baseDir, filename);
    loadConfiguration();
  }

  public Configuration(final File baseDir) throws IOException {
    this(baseDir, CONFIG_FILE);
  }

  public Set<String> getDisabledRuleIds() {
    return disabledRuleIds;
  }

  public Set<String> getEnabledRuleIds() {
    return enabledRuleIds;
  }

  public Set<String> getDisabledCategoryNames() {
    return disabledCategoryNames;
  }

  public void setDisabledRuleIds(final Set<String> ruleIDs) {
    disabledRuleIds = ruleIDs;
  }

  public void setEnabledRuleIds(final Set<String> ruleIDs) {
    enabledRuleIds = ruleIDs;
  }

  public void setDisabledCategoryNames(final Set<String> categoryNames) {
    disabledCategoryNames = categoryNames;
  }

  public Language getMotherTongue() {
    return motherTongue;
  }

  public void setMotherTongue(final Language motherTongue) {
    this.motherTongue = motherTongue;
  }
  
  public boolean getAutoDetect() {
      return autoDetect;
  }
  
  public void setAutoDetect(final boolean autoDetect) {
      this.autoDetect = autoDetect;
  }
  
  public boolean getRunServer() {
    return runServer;
  }

  public void setRunServer(final boolean runServer) {
    this.runServer = runServer;
  }

  public int getServerPort() {
    return serverPort;
  }

  public void setServerPort(final int serverPort) {
    this.serverPort = serverPort;
  }

  private void loadConfiguration() throws IOException {

    // FIXME: disabling a rule X in language Y should not disable it in all
    // languages - need to add a language parameter

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(configFile);
      final Properties props = new Properties();
      props.load(fis);

      disabledRuleIds.addAll(getListFromProperties(props, DISABLED_RULES_CONFIG_KEY));
      enabledRuleIds.addAll(getListFromProperties(props, ENABLED_RULES_CONFIG_KEY));
      disabledCategoryNames.addAll(getListFromProperties(props, DISABLED_CATEGORIES_CONFIG_KEY));
      
      final String motherTongueStr = (String) props.get(MOTHER_TONGUE_CONFIG_KEY);
      if (motherTongueStr != null) {
        motherTongue = Language.getLanguageForShortName(motherTongueStr);
      }
      
      final String autoDetectStr = (String) props.get(AUTO_DETECT_CONFIG_KEY);
      if (autoDetectStr != null) {
          autoDetect = autoDetectStr.equals("true");
      }
      
      final String runServerString = (String) props.get(SERVER_RUN_CONFIG_KEY);
      if (runServerString != null) {
        runServer = runServerString.equals("true");
      }
      final String serverPortString = (String) props.get(SERVER_PORT_CONFIG_KEY);
      if (serverPortString != null) {
        serverPort = Integer.parseInt(serverPortString);
      }
    } catch (final FileNotFoundException e) {
      // file not found: okay, leave disabledRuleIds empty
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }

  private Collection<? extends String> getListFromProperties(Properties props, String key) {
    final String value = (String) props.get(key);
    final List<String> list = new ArrayList<String>();
    if (value != null) {
      final String[] names = value.split(DELIMITER);
      list.addAll(Arrays.asList(names));
    }
    return list;
  }

  public void saveConfiguration() throws IOException {
    final Properties props = new Properties();
    addListToProperties(props, DISABLED_RULES_CONFIG_KEY, disabledRuleIds);
    addListToProperties(props, ENABLED_RULES_CONFIG_KEY, enabledRuleIds);
    addListToProperties(props, DISABLED_CATEGORIES_CONFIG_KEY, disabledCategoryNames);
    if (motherTongue != null) {
      props.setProperty(MOTHER_TONGUE_CONFIG_KEY, motherTongue.getShortName());
    }
    props.setProperty(AUTO_DETECT_CONFIG_KEY, Boolean.valueOf(autoDetect).toString());
    props.setProperty(SERVER_RUN_CONFIG_KEY, Boolean.valueOf(runServer).toString());
    props.setProperty(SERVER_PORT_CONFIG_KEY, Integer.valueOf(serverPort).toString());
    final FileOutputStream fos = new FileOutputStream(configFile);
    try {
      props.store(fos, "LanguageTool configuration");
    } finally {
      fos.close();
    }
  }

  private void addListToProperties(Properties props, String key, Set<String> list) {
    if (list == null) {
      props.setProperty(key, "");
    } else {
      props.setProperty(key, StringTools.listToString(list, DELIMITER));
    }
  }

}
