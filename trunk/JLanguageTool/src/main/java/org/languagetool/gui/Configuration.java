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
package org.languagetool.gui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.languagetool.Language;
import org.languagetool.server.HTTPServerConfig;
import org.languagetool.tools.StringTools;

/**
 * Configuration like list of disabled rule IDs, server mode etc.
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
  private static final String USE_GUI_CONFIG_KEY = "useGUIConfig";
  private static final String DELIMITER = ",";

  private final File configFile;
  private final HashMap<String, String> configForOtherLangs;

  private Set<String> disabledRuleIds = new HashSet<String>();
  private Set<String> enabledRuleIds = new HashSet<String>();
  private Set<String> disabledCategoryNames = new HashSet<String>();
  private Language motherTongue;
  private boolean runServer;
  private boolean autoDetect;
  private boolean guiConfig;
  private int serverPort = HTTPServerConfig.DEFAULT_PORT;

  /**
   * Uses the configuration file from the default location.
   * @param lang The language for the configuration, used to distinguish 
   * rules that are enabled or disabled per language.
   *  
   * @throws IOException
   */
  public Configuration(final Language lang) throws IOException {
    this(new File(System.getProperty("user.home")), CONFIG_FILE, lang);
  }
  
  public Configuration(final File baseDir, final String filename, final Language lang)
      throws IOException {
    if (!baseDir.isDirectory()) {
      throw new IllegalArgumentException("Not a directory: " + baseDir);
    }
    configFile = new File(baseDir, filename);
    configForOtherLangs = new HashMap<String, String>();
    loadConfiguration(lang);
  }

  public Configuration(final File baseDir, final Language lang) throws IOException {
    this(baseDir, CONFIG_FILE, lang);
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

  public void setUseGUIConfig(final boolean useGUIConfig) {
    this.guiConfig = useGUIConfig;
  }

 public boolean getUseGUIConfig() {
    return guiConfig;
}

  
  public void setServerPort(final int serverPort) {
    this.serverPort = serverPort;
  }

  private void loadConfiguration(final Language lang) throws IOException {

    final String qualifier = getQualifier(lang);

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(configFile);
      final Properties props = new Properties();
      props.load(fis);

      disabledRuleIds.addAll(getListFromProperties(props, DISABLED_RULES_CONFIG_KEY + qualifier));
      enabledRuleIds.addAll(getListFromProperties(props, ENABLED_RULES_CONFIG_KEY + qualifier));
      disabledCategoryNames.addAll(getListFromProperties(props, DISABLED_CATEGORIES_CONFIG_KEY + qualifier));
      
      final String motherTongueStr = (String) props.get(MOTHER_TONGUE_CONFIG_KEY);
      if (motherTongueStr != null) {
        motherTongue = Language.getLanguageForShortName(motherTongueStr);
      }
            
      autoDetect = "true".equals(props.get(AUTO_DETECT_CONFIG_KEY));
      guiConfig = "true".equals(props.get(USE_GUI_CONFIG_KEY));
      runServer = "true".equals(props.get(SERVER_RUN_CONFIG_KEY));

      final String serverPortString = (String) props.get(SERVER_PORT_CONFIG_KEY);
      if (serverPortString != null) {
        serverPort = Integer.parseInt(serverPortString);
      }
      
      //store config for other languages
      loadConfigForOtherLanguages(lang, props);
      
    } catch (final FileNotFoundException e) {
      // file not found: okay, leave disabledRuleIds empty
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }

  private String getQualifier(final Language lang) {
    String qualifier = "";
    if (lang != null) {
      qualifier = "." + lang.getShortNameWithVariant();
    }
    return qualifier;
  }

  private void loadConfigForOtherLanguages(final Language lang, final Properties prop) {
    for (Language otherLang : Language.getAllLanguages()) {
      if (!otherLang.equals(lang)) {
        final String languageSuffix = "." + otherLang.getShortNameWithVariant();
        storeConfigKeyFromProp(prop, DISABLED_RULES_CONFIG_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, ENABLED_RULES_CONFIG_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, DISABLED_CATEGORIES_CONFIG_KEY + languageSuffix);
      }
    }
  }

  private void storeConfigKeyFromProp(final Properties prop, final String key) {
    if (prop.containsKey(key)) {
      configForOtherLangs.put(key, prop.getProperty(key));
    }
  }

  private Collection<? extends String> getListFromProperties(final Properties props, final String key) {
    final String value = (String) props.get(key);
    final List<String> list = new ArrayList<String>();
    if (value != null) {
      final String[] names = value.split(DELIMITER);
      list.addAll(Arrays.asList(names));
    }
    return list;
  }

  public void saveConfiguration(final Language lang) throws IOException {
    final Properties props = new Properties();
    
    final String qualifier = getQualifier(lang);
    
    addListToProperties(props, DISABLED_RULES_CONFIG_KEY + qualifier, disabledRuleIds);
    addListToProperties(props, ENABLED_RULES_CONFIG_KEY + qualifier, enabledRuleIds);
    addListToProperties(props, DISABLED_CATEGORIES_CONFIG_KEY + qualifier, disabledCategoryNames);
    if (motherTongue != null) {
      props.setProperty(MOTHER_TONGUE_CONFIG_KEY, motherTongue.getShortName());
    }
    props.setProperty(AUTO_DETECT_CONFIG_KEY, Boolean.valueOf(autoDetect).toString());
    props.setProperty(USE_GUI_CONFIG_KEY, Boolean.valueOf(guiConfig).toString());
    props.setProperty(SERVER_RUN_CONFIG_KEY, Boolean.valueOf(runServer).toString());
    props.setProperty(SERVER_PORT_CONFIG_KEY, Integer.valueOf(serverPort).toString());

    for (final String key : configForOtherLangs.keySet()) {
      props.setProperty(key, configForOtherLangs.get(key));
    }

    final FileOutputStream fos = new FileOutputStream(configFile);
    try {
      props.store(fos, "LanguageTool configuration");
    } finally {
      fos.close();
    }
  }

  private void addListToProperties(final Properties props, final String key, final Set<String> list) {
    if (list == null) {
      props.setProperty(key, "");
    } else {
      props.setProperty(key, StringTools.listToString(list, DELIMITER));
    }
  }

}
