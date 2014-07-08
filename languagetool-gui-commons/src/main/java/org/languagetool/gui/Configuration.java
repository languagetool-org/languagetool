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

import org.languagetool.Language;
import org.languagetool.tools.StringTools;

import java.io.*;
import java.util.*;

/**
 * Configuration like list of disabled rule IDs, server mode etc.
 * Configuration is loaded from and stored to a properties file.
 * 
 * @author Daniel Naber
 */
public class Configuration {
  
  static final int DEFAULT_SERVER_PORT = 8081;  // should be HTTPServerConfig.DEFAULT_PORT but we don't have that dependency
  static final int FONT_STYLE_INVALID = -1;
  static final int FONT_SIZE_INVALID = -1;

  private static final String CONFIG_FILE = "languagetool.properties";

  private static final String DISABLED_RULES_CONFIG_KEY = "disabledRules";
  private static final String ENABLED_RULES_CONFIG_KEY = "enabledRules";
  private static final String DISABLED_CATEGORIES_CONFIG_KEY = "disabledCategories";
  private static final String LANGUAGE_CONFIG_KEY = "language";
  private static final String MOTHER_TONGUE_CONFIG_KEY = "motherTongue";
  private static final String AUTO_DETECT_CONFIG_KEY = "autoDetect";
  private static final String SERVER_RUN_CONFIG_KEY = "serverMode";
  private static final String SERVER_PORT_CONFIG_KEY = "serverPort";
  private static final String USE_GUI_CONFIG_KEY = "useGUIConfig";
  private static final String FONT_NAME_CONFIG_KEY = "font.name";
  private static final String FONT_STYLE_CONFIG_KEY = "font.style";
  private static final String FONT_SIZE_CONFIG_KEY = "font.size";
  private static final String LF_NAME_CONFIG_KEY = "lookAndFeelName";

  private static final String DELIMITER = ",";
  private static final String EXTERNAL_RULE_DIRECTORY = "extRulesDirectory";

  private File configFile;
  private final HashMap<String, String> configForOtherLangs = new HashMap<>();

  private Set<String> disabledRuleIds = new HashSet<>();
  private Set<String> enabledRuleIds = new HashSet<>();
  private Set<String> disabledCategoryNames = new HashSet<>();
  private Language language;
  private Language motherTongue;
  private boolean runServer;
  private boolean autoDetect;
  private boolean guiConfig;
  private String fontName;
  private int fontStyle;
  private int fontSize;
  private int serverPort = DEFAULT_SERVER_PORT;
  private String externalRuleDirectory;
  private String lookAndFeelName;

  /**
   * Uses the configuration file from the default location.
   * @param lang The language for the configuration, used to distinguish 
   * rules that are enabled or disabled per language.
   */
  public Configuration(final Language lang) throws IOException {
    this(new File(System.getProperty("user.home")), CONFIG_FILE, lang);
  }
  
  public Configuration(final File baseDir, final String filename, final Language lang)
      throws IOException {
    this();
    if (!baseDir.isDirectory()) {
      throw new IllegalArgumentException("Not a directory: " + baseDir);
    }
    configFile = new File(baseDir, filename);
    loadConfiguration(lang);
  }

  public Configuration(final File baseDir, final Language lang) throws IOException {
    this(baseDir, CONFIG_FILE, lang);
  }

  Configuration() {
    fontStyle = FONT_STYLE_INVALID;
    fontSize = FONT_SIZE_INVALID;
  }

  /**
   *
   * Returns a copy of the given configuration.
   *
   * @param configuration the object to copy.
   * @return the copy.
   * @since 2.6
   */
  Configuration copy(Configuration configuration) {
    Configuration copy = new Configuration();
    copy.restoreState(configuration);
    return copy;
  }

  /**
   * Restore the state of this object from configuration
   *
   * @param configuration the object from which we will read the state
   * @since 2.6
   */
  void restoreState(Configuration configuration) {
    this.configFile = configuration.configFile;
    this.language = configuration.language;
    this.motherTongue = configuration.motherTongue;
    this.runServer = configuration.runServer;
    this.autoDetect = configuration.autoDetect;
    this.guiConfig = configuration.guiConfig;
    this.fontName = configuration.fontName;
    this.fontStyle = configuration.fontStyle;
    this.fontSize = configuration.fontSize;    
    this.serverPort = configuration.serverPort;
    this.lookAndFeelName = configuration.lookAndFeelName;
    this.externalRuleDirectory = configuration.externalRuleDirectory;
    this.disabledRuleIds.clear();
    this.disabledRuleIds.addAll(configuration.disabledRuleIds);
    this.enabledRuleIds.clear();
    this.enabledRuleIds.addAll(configuration.enabledRuleIds);
    this.disabledCategoryNames.clear();
    this.disabledCategoryNames.addAll(configuration.disabledCategoryNames);
    this.configForOtherLangs.clear();
    for (String key : configuration.configForOtherLangs.keySet()) {
      this.configForOtherLangs.put(key, configuration.configForOtherLangs.get(key));
    }
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

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(final Language language) {
    this.language = language;
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

  public String getExternalRuleDirectory() {
    return externalRuleDirectory;
  }

  public void setExternalRuleDirectory(final String path) {
    externalRuleDirectory = path;
  }

  /**
   *
   * Returns the name of the GUI's editing textarea font.
   *
   * @return the name of the font.
   * @since 2.6
   * 
   * @see java.awt.Font#getFamily()
   */
  public String getFontName() {
    return fontName;
  }

  /**
   *
   * Sets the name of the GUI's editing textarea font.
   *
   * @param fontName the name of the font.
   * @since 2.6
   * 
   * @see java.awt.Font#getFamily()
   */
  public void setFontName(String fontName) {
    this.fontName = fontName;
  }

  /**
   *
   * Returns the style of the GUI's editing textarea font.
   *
   * @return the style of the font.
   * @since 2.6
   * 
   * @see java.awt.Font#getStyle()
   */
  public int getFontStyle() {
    return fontStyle;
  }

  /**
   *
   * Sets the style of the GUI's editing textarea font.
   *
   * @param fontStyle the style of the font.
   * @since 2.6
   * 
   * @see java.awt.Font#getStyle()
   */
  public void setFontStyle(int fontStyle) {
    this.fontStyle = fontStyle;
  }

  /**
   *
   * Returns the size of the GUI's editing textarea font.
   *
   * @return the size of the font.
   * @since 2.6
   * 
   * @see java.awt.Font#getSize()
   */
  public int getFontSize() {
    return fontSize;
  }

  /**
   *
   * Sets the size of the GUI's editing textarea font.
   *
   * @param fontSize the size of the font.
   * @since 2.6
   * 
   * @see java.awt.Font#getSize()
   */
  public void setFontSize(int fontSize) {
    this.fontSize = fontSize;
  }

  /**
   *
   * Returns the name of the GUI's LaF.
   *
   * @return the name of the LaF.
   * @since 2.6
   * 
   * @see javax.swing.UIManager.LookAndFeelInfo#getName()
   */
  public String getLookAndFeelName() {
    return this.lookAndFeelName;
  }

  /**
   *
   * Sets the name of the GUI's LaF.
   *
   * @param lookAndFeelName the name of the LaF.
   * @since 2.6 @see
   * 
   * @see javax.swing.UIManager.LookAndFeelInfo#getName()
   */
  public void setLookAndFeelName(String lookAndFeelName) {
    this.lookAndFeelName = lookAndFeelName;
  }

  private void loadConfiguration(final Language lang) throws IOException {

    final String qualifier = getQualifier(lang);

    try (FileInputStream fis = new FileInputStream(configFile)) {

      final Properties props = new Properties();
      props.load(fis);

      disabledRuleIds.addAll(getListFromProperties(props, DISABLED_RULES_CONFIG_KEY + qualifier));
      enabledRuleIds.addAll(getListFromProperties(props, ENABLED_RULES_CONFIG_KEY + qualifier));
      disabledCategoryNames.addAll(getListFromProperties(props, DISABLED_CATEGORIES_CONFIG_KEY + qualifier));
      
      final String languageStr = (String) props.get(LANGUAGE_CONFIG_KEY);
      if (languageStr != null) {
        language = Language.getLanguageForShortName(languageStr);
      }
      final String motherTongueStr = (String) props.get(MOTHER_TONGUE_CONFIG_KEY);
      if (motherTongueStr != null && !motherTongueStr.equals("xx")) {
        motherTongue = Language.getLanguageForShortName(motherTongueStr);
      }
            
      autoDetect = "true".equals(props.get(AUTO_DETECT_CONFIG_KEY));
      guiConfig = "true".equals(props.get(USE_GUI_CONFIG_KEY));
      runServer = "true".equals(props.get(SERVER_RUN_CONFIG_KEY));

      fontName = (String) props.get(FONT_NAME_CONFIG_KEY);
      if (props.get(FONT_STYLE_CONFIG_KEY) != null) {
        try {
          fontStyle = Integer.parseInt((String) props.get(FONT_STYLE_CONFIG_KEY));
        } catch (NumberFormatException e) {
          // Ignore
        }
      }
      if (props.get(FONT_SIZE_CONFIG_KEY) != null) {
        try {
          fontSize = Integer.parseInt((String) props.get(FONT_SIZE_CONFIG_KEY));
        } catch (NumberFormatException e) {
          // Ignore
        }
      }
      lookAndFeelName = (String) props.get(LF_NAME_CONFIG_KEY);

      final String serverPortString = (String) props.get(SERVER_PORT_CONFIG_KEY);
      if (serverPortString != null) {
        serverPort = Integer.parseInt(serverPortString);
      }
      final String extRules = (String) props.get(EXTERNAL_RULE_DIRECTORY);
      if (extRules != null) {
        externalRuleDirectory = extRules;
      }
      
      //store config for other languages
      loadConfigForOtherLanguages(lang, props);
      
    } catch (final FileNotFoundException e) {
      // file not found: okay, leave disabledRuleIds empty
    }
  }

  private String getQualifier(final Language lang) {
    String qualifier = "";
    if (lang != null) {
      qualifier = "." + lang.getShortNameWithCountryAndVariant();
    }
    return qualifier;
  }

  private void loadConfigForOtherLanguages(final Language lang, final Properties prop) {
    for (Language otherLang : Language.getAllLanguages()) {
      if (!otherLang.equals(lang)) {
        final String languageSuffix = "." + otherLang.getShortNameWithCountryAndVariant();
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
    final List<String> list = new ArrayList<>();
    if (value != null && !value.isEmpty()) {
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
    if (language != null && !language.isExternal()) {  // external languages won't be known at startup, so don't save them
      props.setProperty(LANGUAGE_CONFIG_KEY, language.getShortNameWithCountryAndVariant());
    }
    if (motherTongue != null) {
      props.setProperty(MOTHER_TONGUE_CONFIG_KEY, motherTongue.getShortName());
    }
    props.setProperty(AUTO_DETECT_CONFIG_KEY, Boolean.toString(autoDetect));
    props.setProperty(USE_GUI_CONFIG_KEY, Boolean.toString(guiConfig));
    props.setProperty(SERVER_RUN_CONFIG_KEY, Boolean.toString(runServer));
    props.setProperty(SERVER_PORT_CONFIG_KEY, Integer.toString(serverPort));
    if (fontName != null) {
      props.setProperty(FONT_NAME_CONFIG_KEY, fontName);
    }
    if (fontStyle != FONT_STYLE_INVALID) {
      props.setProperty(FONT_STYLE_CONFIG_KEY, Integer.toString(fontStyle));
    }
    if (fontSize != FONT_SIZE_INVALID) {
      props.setProperty(FONT_SIZE_CONFIG_KEY, Integer.toString(fontSize));
    }
    if (this.lookAndFeelName != null) {
      props.setProperty(LF_NAME_CONFIG_KEY, lookAndFeelName);
    }    
    if (externalRuleDirectory != null) {
      props.setProperty(EXTERNAL_RULE_DIRECTORY, externalRuleDirectory);
    }

    for (final String key : configForOtherLangs.keySet()) {
      props.setProperty(key, configForOtherLangs.get(key));
    }

    try (FileOutputStream fos = new FileOutputStream(configFile)) {
      props.store(fos, "LanguageTool configuration");
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
