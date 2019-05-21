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

import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.LinguServices;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;

/**
 * Configuration like list of disabled rule IDs, server mode etc.
 * Configuration is loaded from and stored to a properties file.
 *
 * @author Daniel Naber
 */
public class Configuration {

  static final int DEFAULT_SERVER_PORT = 8081;  // should be HTTPServerConfig.DEFAULT_PORT but we don't have that dependency
  static final int DEFAULT_NUM_CHECK_PARAS = 5;  //  default number of parameters to be checked by TextLevelRules in LO/OO 
  static final int FONT_STYLE_INVALID = -1;
  static final int FONT_SIZE_INVALID = -1;
  static final Color STYLE_COLOR = new Color(0, 175, 0);

  private static final String CONFIG_FILE = ".languagetool.cfg";

  private static final String DISABLED_RULES_KEY = "disabledRules";
  private static final String ENABLED_RULES_KEY = "enabledRules";
  private static final String DISABLED_CATEGORIES_KEY = "disabledCategories";
  private static final String ENABLED_CATEGORIES_KEY = "enabledCategories";
  private static final String ENABLED_RULES_ONLY_KEY = "enabledRulesOnly";
  private static final String LANGUAGE_KEY = "language";
  private static final String MOTHER_TONGUE_KEY = "motherTongue";
  private static final String NGRAM_DIR_KEY = "ngramDir";
  private static final String WORD2VEC_DIR_KEY = "word2vecDir";
  private static final String AUTO_DETECT_KEY = "autoDetect";
  private static final String TAGGER_SHOWS_DISAMBIG_LOG_KEY = "taggerShowsDisambigLog";
  private static final String SERVER_RUN_KEY = "serverMode";
  private static final String SERVER_PORT_KEY = "serverPort";
  private static final String PARA_CHECK_KEY = "numberParagraphs";
  private static final String RESET_CHECK_KEY = "doResetCheck";
  private static final String NO_MULTI_RESET_KEY = "noMultiReset";
  private static final String USE_DOC_LANG_KEY = "useDocumentLanguage";
  private static final String USE_GUI_KEY = "useGUIConfig";
  private static final String FONT_NAME_KEY = "font.name";
  private static final String FONT_STYLE_KEY = "font.style";
  private static final String FONT_SIZE_KEY = "font.size";
  private static final String LF_NAME_KEY = "lookAndFeelName";
  private static final String ERROR_COLORS_KEY = "errorColors";
  private static final String UNDERLINE_COLORS_KEY = "underlineColors";
  private static final String CONFIGURABLE_RULE_VALUES_KEY = "configurableRuleValues";
  private static final String LT_SWITCHED_OFF_KEY = "ltSwitchedOff";
  private static final String IS_MULTI_THREAD_LO_KEY = "isMultiThread";

  private static final String DELIMITER = ",";
  // find all comma followed by zero or more white space characters that are preceded by ":" AND a valid 6-digit hex code
  // example: ":#44ffee,"
  private static final String COLOR_SPLITTER_REGEXP = "(?<=:#[0-9A-Fa-f]{6}),\\s*";
  //find all colon followed by a valid 6-digit hex code, e.g., ":#44ffee"
  private static final String COLOR_SPLITTER_REGEXP_COLON = ":(?=#[0-9A-Fa-f]{6})";
  // find all comma followed by zero or more white space characters that are preceded by at least one digit
  // example: "4,"
  private static final String CONFIGURABLE_RULE_SPLITTER_REGEXP = "(?<=[0-9]),\\s*";
  private static final String EXTERNAL_RULE_DIRECTORY = "extRulesDirectory";

  private final Map<String, String> configForOtherLanguages = new HashMap<>();
  private final Map<ITSIssueType, Color> errorColors = new EnumMap<>(ITSIssueType.class);
  private final Map<String, Color> underlineColors = new HashMap<>();
  private final Map<String, Integer> configurableRuleValues = new HashMap<>();
  private final Set<String> styleLikeCategories = new HashSet<>();
  private final Map<String, String> specialTabCategories = new HashMap<>();

  private File configFile;
  private Set<String> disabledRuleIds = new HashSet<>();
  private Set<String> enabledRuleIds = new HashSet<>();
  private Set<String> disabledCategoryNames = new HashSet<>();
  private Set<String> enabledCategoryNames = new HashSet<>();
  private boolean enabledRulesOnly = false;
  private Language language;
  private Language motherTongue;
  private File ngramDirectory;
  private File word2vecDirectory;
  private boolean runServer;
  private boolean autoDetect;
  private boolean taggerShowsDisambigLog;
  private boolean guiConfig;
  private String fontName;
  private int fontStyle = FONT_STYLE_INVALID;
  private int fontSize = FONT_SIZE_INVALID;
  private int serverPort = DEFAULT_SERVER_PORT;
  private int numParasToCheck = DEFAULT_NUM_CHECK_PARAS;
  private boolean doResetCheck = false;
  private boolean noMultiReset = true;
  private String externalRuleDirectory;
  private String lookAndFeelName;
  private boolean switchOff = false;
  private boolean useDocLanguage = true;
  private boolean isMultiThreadLO = false;

  /**
   * Uses the configuration file from the default location.
   *
   * @param lang The language for the configuration, used to distinguish
   *             rules that are enabled or disabled per language.
   */
  public Configuration(Language lang) throws IOException {
    this(new File(System.getProperty("user.home")), CONFIG_FILE, lang);
  }

  public Configuration(File baseDir, Language lang) throws IOException {
    this(baseDir, CONFIG_FILE, lang);
  }

  public Configuration(File baseDir, String filename, Language lang) throws IOException {
    this(baseDir, filename, lang, null);
  }

  public Configuration(File baseDir, String filename, Language lang, LinguServices linguServices) throws IOException {
    if (baseDir == null || !baseDir.isDirectory()) {
      throw new IllegalArgumentException("Cannot open file " + filename + " in directory " + baseDir);
    }
    configFile = new File(baseDir, filename);
    loadConfiguration(lang);
  }

  private Configuration() {
  }

  /**
   * Returns a copy of the given configuration.
   * @param configuration the object to copy.
   * @since 2.6
   */
  Configuration copy(Configuration configuration) {
    Configuration copy = new Configuration();
    copy.restoreState(configuration);
    return copy;
  }

  /**
   * Restore the state of this object from configuration.
   * @param configuration the object from which we will read the state
   * @since 2.6
   */
  void restoreState(Configuration configuration) {
    this.configFile = configuration.configFile;
    this.language = configuration.language;
    this.motherTongue = configuration.motherTongue;
    this.ngramDirectory = configuration.ngramDirectory;
    this.word2vecDirectory = configuration.word2vecDirectory;
    this.runServer = configuration.runServer;
    this.autoDetect = configuration.autoDetect;
    this.taggerShowsDisambigLog = configuration.taggerShowsDisambigLog;
    this.guiConfig = configuration.guiConfig;
    this.fontName = configuration.fontName;
    this.fontStyle = configuration.fontStyle;
    this.fontSize = configuration.fontSize;
    this.serverPort = configuration.serverPort;
    this.numParasToCheck = configuration.numParasToCheck;
    this.doResetCheck = configuration.doResetCheck;
    this.noMultiReset = configuration.noMultiReset;
    this.isMultiThreadLO = configuration.isMultiThreadLO;
    this.useDocLanguage = configuration.useDocLanguage;
    this.lookAndFeelName = configuration.lookAndFeelName;
    this.externalRuleDirectory = configuration.externalRuleDirectory;
    this.disabledRuleIds.clear();
    this.disabledRuleIds.addAll(configuration.disabledRuleIds);
    this.enabledRuleIds.clear();
    this.enabledRuleIds.addAll(configuration.enabledRuleIds);
    this.disabledCategoryNames.clear();
    this.disabledCategoryNames.addAll(configuration.disabledCategoryNames);
    this.enabledCategoryNames.clear();
    this.enabledCategoryNames.addAll(configuration.enabledCategoryNames);
    this.configForOtherLanguages.clear();
    for (String key : configuration.configForOtherLanguages.keySet()) {
      this.configForOtherLanguages.put(key, configuration.configForOtherLanguages.get(key));
    }
    this.underlineColors.clear();
    for (Map.Entry<String, Color> entry : configuration.underlineColors.entrySet()) {
      this.underlineColors.put(entry.getKey(), entry.getValue());
    }
    this.configurableRuleValues.clear();
    for (Map.Entry<String, Integer> entry : configuration.configurableRuleValues.entrySet()) {
      this.configurableRuleValues.put(entry.getKey(), entry.getValue());
    }
    this.styleLikeCategories.clear();
    this.styleLikeCategories.addAll(configuration.styleLikeCategories);
    this.specialTabCategories.clear();
    for (Map.Entry<String, String> entry : configuration.specialTabCategories.entrySet()) {
      this.specialTabCategories.put(entry.getKey(), entry.getValue());
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

  public Set<String> getEnabledCategoryNames() {
    return enabledCategoryNames;
  }

  public void setDisabledRuleIds(Set<String> ruleIds) {
    disabledRuleIds = ruleIds;
    enabledRuleIds.removeAll(ruleIds);
  }

  public void setEnabledRuleIds(Set<String> ruleIds) {
    enabledRuleIds = ruleIds;
  }

  public void setDisabledCategoryNames(Set<String> categoryNames) {
    disabledCategoryNames = categoryNames;
  }

  public void setEnabledCategoryNames(Set<String> categoryNames) {
    enabledCategoryNames = categoryNames;
  }

  public boolean getEnabledRulesOnly() {
    return enabledRulesOnly;
  }

  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  public Language getMotherTongue() {
    return motherTongue;
  }

  public void setMotherTongue(Language motherTongue) {
    this.motherTongue = motherTongue;
  }

  public Language getDefaultLanguage() {
    if(useDocLanguage) {
      return null;
    }
    return motherTongue;
  }

  public void setUseDocLanguage(boolean useDocLang) {
    useDocLanguage = useDocLang;
  }

  public boolean getUseDocLanguage() {
    return useDocLanguage;
  }

  public boolean getAutoDetect() {
    return autoDetect;
  }

  public void setAutoDetect(boolean autoDetect) {
    this.autoDetect = autoDetect;
  }

  /**
   * Determines whether the tagger window will also print the disambiguation
   * log.
   * @return true if the tagger window will print the disambiguation log,
   * false otherwise
   * @since 3.3
   */
  public boolean getTaggerShowsDisambigLog() {
    return taggerShowsDisambigLog;
  }

  /**
   * Enables or disables the disambiguation log on the tagger window,
   * depending on the value of the parameter taggerShowsDisambigLog.
   * @param taggerShowsDisambigLog If true, the tagger window will print the
   * @since 3.3
   */
  public void setTaggerShowsDisambigLog(boolean taggerShowsDisambigLog) {
    this.taggerShowsDisambigLog = taggerShowsDisambigLog;
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

  public void setUseGUIConfig(boolean useGUIConfig) {
    this.guiConfig = useGUIConfig;
  }

  public boolean getUseGUIConfig() {
    return guiConfig;
  }

  public void setServerPort(int serverPort) {
    this.serverPort = serverPort;
  }

  public String getExternalRuleDirectory() {
    return externalRuleDirectory;
  }

  public void setExternalRuleDirectory(String path) {
    externalRuleDirectory = path;
  }

  /**
   * get the number of paragraphs to be checked for TextLevelRules
   * @since 4.0
   */
  public int getNumParasToCheck() {
    return numParasToCheck;
  }

  /**
   * set the number of paragraphs to be checked for TextLevelRules
   * @since 4.0
   */
  public void setNumParasToCheck(int numParas) {
    this.numParasToCheck = numParas;
  }

  /**
   * will all paragraphs check after every change of text?
   * @since 4.2
   */
  public boolean isResetCheck() {
    return doResetCheck;
  }

  /**
   * set all paragraphs to be checked after every change of text
   * @since 4.2
   */
  public void setDoResetCheck(boolean resetCheck) {
    this.doResetCheck = resetCheck;
  }

  /**
   * will all paragraphs not checked after every change of text 
   * if more than one document loaded?
   * @since 4.5
   */
  public boolean isNoMultiReset() {
    return noMultiReset;
  }

  /**
   * set all paragraphs to be not checked after every change of text
   * if more than one document loaded?
   * @since 4.5
   */
  public void setNoMultiReset(boolean noMultiReset) {
    this.noMultiReset = noMultiReset;
  }

  /**
   * run LO in multi thread mode
   * @since 4.6
   */
  public void setMultiThreadLO(boolean isMultiThread) {
    this.isMultiThreadLO = isMultiThread;
  }

  /**
   * shall LO run in multi thread mode 
   * @since 4.6
   */
  public boolean isMultiThread() {
    return isMultiThreadLO;
  }

  /**
   * Returns the name of the GUI's editing textarea font.
   * @return the name of the font.
   * @see Font#getFamily()
   * @since 2.6
   */
  public String getFontName() {
    return fontName;
  }

  /**
   * Sets the name of the GUI's editing textarea font.
   * @param fontName the name of the font.
   * @see Font#getFamily()
   * @since 2.6
   */
  public void setFontName(String fontName) {
    this.fontName = fontName;
  }

  /**
   * Returns the style of the GUI's editing textarea font.
   * @return the style of the font.
   * @see Font#getStyle()
   * @since 2.6
   */
  public int getFontStyle() {
    return fontStyle;
  }

  /**
   * Sets the style of the GUI's editing textarea font.
   * @param fontStyle the style of the font.
   * @see Font#getStyle()
   * @since 2.6
   */
  public void setFontStyle(int fontStyle) {
    this.fontStyle = fontStyle;
  }

  /**
   * Returns the size of the GUI's editing textarea font.
   * @return the size of the font.
   * @see Font#getSize()
   * @since 2.6
   */
  public int getFontSize() {
    return fontSize;
  }

  /**
   * Sets the size of the GUI's editing textarea font.
   * @param fontSize the size of the font.
   * @see Font#getSize()
   * @since 2.6
   */
  public void setFontSize(int fontSize) {
    this.fontSize = fontSize;
  }

  /**
   * Returns the name of the GUI's LaF.
   * @return the name of the LaF.
   * @see javax.swing.UIManager.LookAndFeelInfo#getName()
   * @since 2.6
   */
  public String getLookAndFeelName() {
    return this.lookAndFeelName;
  }

  /**
   * Sets the name of the GUI's LaF.
   * @param lookAndFeelName the name of the LaF.
   * @see javax.swing.UIManager.LookAndFeelInfo#getName()
   * @since 2.6 @see
   */
  public void setLookAndFeelName(String lookAndFeelName) {
    this.lookAndFeelName = lookAndFeelName;
  }

  /**
   * Directory with ngram data or null.
   * @since 3.0
   */
  @Nullable
  public File getNgramDirectory() {
    return ngramDirectory;
  }

  /**
   * Sets the directory with ngram data (may be null).
   * @since 3.0
   */
  public void setNgramDirectory(File dir) {
    this.ngramDirectory = dir;
  }

  /**
   * Directory with word2vec data or null.
   * @since 4.0
   */
  @Nullable
  public File getWord2VecDirectory() {
    return word2vecDirectory;
  }

  /**
   * Sets the directory with word2vec data (may be null).
   * @since 4.0
   */
  public void setWord2VecDirectory(File dir) {
    this.word2vecDirectory = dir;
  }

  /**
   * @since 2.8
   */
  public Map<ITSIssueType, Color> getErrorColors() {
    return errorColors;
  }

  /**
   * @since 4.3
   * Returns true if category is style like
   */
  public boolean isStyleCategory(String category) {
    return styleLikeCategories.contains(category);
  }

  /**
   * @since 4.4
   * Initialize set of style like categories
   */
  public void initStyleCategories(List<Rule> allRules) {
    for (Rule rule : allRules) {
      if (rule.getCategory().getTabName() != null && !specialTabCategories.containsKey(rule.getCategory().getName())) {
        specialTabCategories.put(rule.getCategory().getName(), rule.getCategory().getTabName());
      }
      if (rule.getLocQualityIssueType().toString().equalsIgnoreCase("STYLE")
              || rule.getLocQualityIssueType().toString().equalsIgnoreCase("REGISTER")
              || rule.getCategory().getId().toString().equals("STYLE")
              || rule.getCategory().getId().toString().equals("TYPOGRAPHY")) {
        if (!styleLikeCategories.contains(rule.getCategory().getName())) {
          styleLikeCategories.add(rule.getCategory().getName());
        }
      }
    }
  }

  /**
   * @since 4.3
   * Returns true if category is a special Tab category
   */
  public boolean isSpecialTabCategory(String category) {
    return specialTabCategories.containsKey(category);
  }

  /**
   * @since 4.3
   * Returns true if category is member of named special Tab
   */
  public boolean isInSpecialTab(String category, String tabName) {
    if (specialTabCategories.containsKey(category)) {
      return specialTabCategories.get(category).equals(tabName);
    }
    return false;
  }

  /**
   * @since 4.3
   * Returns all special tab names
   */
  public String[] getSpecialTabNames() {
    Set<String> tabNames = new HashSet<>();
    for (Map.Entry<String, String> entry : specialTabCategories.entrySet()) {
      if (!tabNames.contains(entry.getValue())) {
        tabNames.add(entry.getValue());
      }
    }
    return tabNames.toArray(new String[tabNames.size()]);
  }

  /**
   * @since 4.3
   * Returns all categories for a named special tab
   */
  public Set<String> getSpecialTabCategories(String tabName) {
    Set<String> tabCategories = new HashSet<>();
    for (Map.Entry<String, String> entry : specialTabCategories.entrySet()) {
      if (entry.getKey().equals(tabName)) {
        tabCategories.add(entry.getKey());
      }
    }
    return tabCategories;
  }

  /**
   * @since 4.2
   */
  public Map<String, Color> getUnderlineColors() {
    return underlineColors;
  }

  /**
   * @since 4.2
   * Get the color to underline a rule match by the Name of its category
   */
  public Color getUnderlineColor(String category) {
    if (underlineColors.containsKey(category)) {
      return underlineColors.get(category);
    }
    if (styleLikeCategories.contains(category)) {
      return STYLE_COLOR;
    }
    return Color.blue;
  }

  /**
   * @since 4.2
   * Set the color to underline a rule match for its category
   */
  public void setUnderlineColor(String category, Color col) {
    underlineColors.put(category, col);
  }

  /**
   * @since 4.2
   * Set the color back to default (removes category from map)
   */
  public void setDefaultUnderlineColor(String category) {
    underlineColors.remove(category);
  }

  /**
   * returns all configured values
   * @since 4.2
   */
  public Map<String, Integer> getConfigurableValues() {
    return configurableRuleValues;
  }

  /**
   * @since 4.2
   * Get the configurable value of a rule by ruleID
   * returns -1 if no value is set by configuration
   */
  public int getConfigurableValue(String ruleID) {
    if (configurableRuleValues.containsKey(ruleID)) {
      return configurableRuleValues.get(ruleID);
    }
    return -1;
  }

  /**
   * @since 4.2
   * Set the value for a rule with ruleID
   */
  public void setConfigurableValue(String ruleID, int value) {
    configurableRuleValues.put(ruleID, value);
  }

  /**
   * @since 4.4
   * if true: LT is switched Off, else: LT is switched On
   */
  public boolean isSwitchedOff() {
    return switchOff;
  }

  /**
   * @throws IOException 
   * @since 4.4
   * Set LT is switched Off or On
   * save configuration
   */
  public void setSwitchedOff(boolean switchOff, Language lang) throws IOException {
    this.switchOff = switchOff;
    saveConfiguration(lang);
  }

  private void loadConfiguration(Language lang) throws IOException {

    String qualifier = getQualifier(lang);

    try (FileInputStream fis = new FileInputStream(configFile)) {

      Properties props = new Properties();
      props.load(fis);

      disabledRuleIds.addAll(getListFromProperties(props, DISABLED_RULES_KEY + qualifier));
      enabledRuleIds.addAll(getListFromProperties(props, ENABLED_RULES_KEY + qualifier));
      disabledCategoryNames.addAll(getListFromProperties(props, DISABLED_CATEGORIES_KEY + qualifier));
      enabledCategoryNames.addAll(getListFromProperties(props, ENABLED_CATEGORIES_KEY + qualifier));
      enabledRulesOnly = "true".equals(props.get(ENABLED_RULES_ONLY_KEY));

      String languageStr = (String) props.get(LANGUAGE_KEY);
      if (languageStr != null) {
        language = Languages.getLanguageForShortCode(languageStr);
      }
      String motherTongueStr = (String) props.get(MOTHER_TONGUE_KEY);
      if (motherTongueStr != null && !motherTongueStr.equals("xx")) {
        motherTongue = Languages.getLanguageForShortCode(motherTongueStr);
      }
      String ngramDir = (String) props.get(NGRAM_DIR_KEY);
      if (ngramDir != null) {
        ngramDirectory = new File(ngramDir);
      }
      String word2vecDir = (String) props.get(WORD2VEC_DIR_KEY);
      if (word2vecDir != null) {
        word2vecDirectory = new File(word2vecDir);
      }

      autoDetect = "true".equals(props.get(AUTO_DETECT_KEY));
      taggerShowsDisambigLog = "true".equals(props.get(TAGGER_SHOWS_DISAMBIG_LOG_KEY));
      guiConfig = "true".equals(props.get(USE_GUI_KEY));
      runServer = "true".equals(props.get(SERVER_RUN_KEY));

      fontName = (String) props.get(FONT_NAME_KEY);
      if (props.get(FONT_STYLE_KEY) != null) {
        try {
          fontStyle = Integer.parseInt((String) props.get(FONT_STYLE_KEY));
        } catch (NumberFormatException e) {
          // Ignore
        }
      }
      if (props.get(FONT_SIZE_KEY) != null) {
        try {
          fontSize = Integer.parseInt((String) props.get(FONT_SIZE_KEY));
        } catch (NumberFormatException e) {
          // Ignore
        }
      }
      lookAndFeelName = (String) props.get(LF_NAME_KEY);

      String serverPortString = (String) props.get(SERVER_PORT_KEY);
      if (serverPortString != null) {
        serverPort = Integer.parseInt(serverPortString);
      }
      String extRules = (String) props.get(EXTERNAL_RULE_DIRECTORY);
      if (extRules != null) {
        externalRuleDirectory = extRules;
      }

      String paraCheckString = (String) props.get(PARA_CHECK_KEY);
      if (paraCheckString != null) {
        numParasToCheck = Integer.parseInt(paraCheckString);
      }

      String resetCheckString = (String) props.get(RESET_CHECK_KEY);
      if (resetCheckString != null) {
        doResetCheck = Boolean.parseBoolean(resetCheckString);
      }

      String noMultiResetString = (String) props.get(NO_MULTI_RESET_KEY);
      if (noMultiResetString != null) {
        noMultiReset = Boolean.parseBoolean(noMultiResetString);
      }

      String useDocLangString = (String) props.get(USE_DOC_LANG_KEY);
      if (useDocLangString != null) {
        useDocLanguage = Boolean.parseBoolean(useDocLangString);
      }

      String switchOffString = (String) props.get(LT_SWITCHED_OFF_KEY);
      if (switchOffString != null) {
        switchOff = Boolean.parseBoolean(switchOffString);
      }

      String isMultiThreadString = (String) props.get(IS_MULTI_THREAD_LO_KEY);
      if (isMultiThreadString != null) {
        isMultiThreadLO = Boolean.parseBoolean(isMultiThreadString);
      }
      
      String rulesValuesString = (String) props.get(CONFIGURABLE_RULE_VALUES_KEY + qualifier);
      if(rulesValuesString == null) {
        rulesValuesString = (String) props.get(CONFIGURABLE_RULE_VALUES_KEY);
      }
      parseConfigurableRuleValues(rulesValuesString);

      String colorsString = (String) props.get(ERROR_COLORS_KEY);
      parseErrorColors(colorsString);

      String underlineColorsString = (String) props.get(UNDERLINE_COLORS_KEY);
      parseUnderlineColors(underlineColorsString);

      //store config for other languages
      loadConfigForOtherLanguages(lang, props);

    } catch (FileNotFoundException e) {
      // file not found: okay, leave disabledRuleIds empty
    }

  }

  private void parseErrorColors(String colorsString) {
    if (StringUtils.isNotEmpty(colorsString)) {
      String[] typeToColorList = colorsString.split(COLOR_SPLITTER_REGEXP);
      for (String typeToColor : typeToColorList) {
        String[] typeAndColor = typeToColor.split(COLOR_SPLITTER_REGEXP_COLON);
        if (typeAndColor.length != 2) {
          throw new RuntimeException("Could not parse type and color, colon expected: '" + typeToColor + "'");
        }
        ITSIssueType type = ITSIssueType.getIssueType(typeAndColor[0]);
        String hexColor = typeAndColor[1];
        errorColors.put(type, Color.decode(hexColor));
      }
    }
  }

  private void parseUnderlineColors(String colorsString) {
    if (StringUtils.isNotEmpty(colorsString)) {
      String[] typeToColorList = colorsString.split(COLOR_SPLITTER_REGEXP);
      for (String typeToColor : typeToColorList) {
        String[] typeAndColor = typeToColor.split(COLOR_SPLITTER_REGEXP_COLON);
        if (typeAndColor.length != 2) {
          throw new RuntimeException("Could not parse type and color, colon expected: '" + typeToColor + "'");
        }
        underlineColors.put(typeAndColor[0], Color.decode(typeAndColor[1]));
      }
    }
  }

  private void parseConfigurableRuleValues(String rulesValueString) {
    if (StringUtils.isNotEmpty(rulesValueString)) {
      String[] ruleToValueList = rulesValueString.split(CONFIGURABLE_RULE_SPLITTER_REGEXP);
      for (String ruleToValue : ruleToValueList) {
        String[] ruleAndValue = ruleToValue.split(":");
        if (ruleAndValue.length != 2) {
          throw new RuntimeException("Could not parse rule and value, colon expected: '" + ruleToValue + "'");
        }
        configurableRuleValues.put(ruleAndValue[0], Integer.parseInt(ruleAndValue[1]));
      }
    }
  }

  private String getQualifier(Language lang) {
    String qualifier = "";
    if (lang != null) {
      qualifier = "." + lang.getShortCodeWithCountryAndVariant();
    }
    return qualifier;
  }

  private void loadConfigForOtherLanguages(Language lang, Properties prop) {
    for (Language otherLang : Languages.get()) {
      if (!otherLang.equals(lang)) {
        String languageSuffix = "." + otherLang.getShortCodeWithCountryAndVariant();
        storeConfigKeyFromProp(prop, DISABLED_RULES_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, ENABLED_RULES_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, DISABLED_CATEGORIES_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, ENABLED_CATEGORIES_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, CONFIGURABLE_RULE_VALUES_KEY + languageSuffix);
      }
    }
  }

  private void storeConfigKeyFromProp(Properties prop, String key) {
    if (prop.containsKey(key)) {
      configForOtherLanguages.put(key, prop.getProperty(key));
    }
  }

  private Collection<? extends String> getListFromProperties(Properties props, String key) {
    String value = (String) props.get(key);
    List<String> list = new ArrayList<>();
    if (value != null && !value.isEmpty()) {
      String[] names = value.split(DELIMITER);
      list.addAll(Arrays.asList(names));
    }
    return list;
  }

  public void saveConfiguration(Language lang) throws IOException {
    Properties props = new Properties();
    String qualifier = getQualifier(lang);

    addListToProperties(props, DISABLED_RULES_KEY + qualifier, disabledRuleIds);
    addListToProperties(props, ENABLED_RULES_KEY + qualifier, enabledRuleIds);
    addListToProperties(props, DISABLED_CATEGORIES_KEY + qualifier, disabledCategoryNames);
    addListToProperties(props, ENABLED_CATEGORIES_KEY + qualifier, enabledCategoryNames);
    if (language != null && !language.isExternal()) {  // external languages won't be known at startup, so don't save them
      props.setProperty(LANGUAGE_KEY, language.getShortCodeWithCountryAndVariant());
    }
    if (motherTongue != null) {
      props.setProperty(MOTHER_TONGUE_KEY, motherTongue.getShortCode());
    }
    if (ngramDirectory != null) {
      props.setProperty(NGRAM_DIR_KEY, ngramDirectory.getAbsolutePath());
    }
    if (word2vecDirectory != null) {
      props.setProperty(WORD2VEC_DIR_KEY, word2vecDirectory.getAbsolutePath());
    }
    props.setProperty(AUTO_DETECT_KEY, Boolean.toString(autoDetect));
    props.setProperty(TAGGER_SHOWS_DISAMBIG_LOG_KEY, Boolean.toString(taggerShowsDisambigLog));
    props.setProperty(USE_GUI_KEY, Boolean.toString(guiConfig));
    props.setProperty(SERVER_RUN_KEY, Boolean.toString(runServer));
    props.setProperty(SERVER_PORT_KEY, Integer.toString(serverPort));
    props.setProperty(PARA_CHECK_KEY, Integer.toString(numParasToCheck));
    props.setProperty(RESET_CHECK_KEY, Boolean.toString(doResetCheck));
    props.setProperty(NO_MULTI_RESET_KEY, Boolean.toString(noMultiReset));
    if(!useDocLanguage) {
      props.setProperty(USE_DOC_LANG_KEY, Boolean.toString(useDocLanguage));
    }
    if(switchOff) {
      props.setProperty(LT_SWITCHED_OFF_KEY, Boolean.toString(switchOff));
    }
    if(isMultiThreadLO) {
      props.setProperty(IS_MULTI_THREAD_LO_KEY, Boolean.toString(isMultiThreadLO));
    }
    if (fontName != null) {
      props.setProperty(FONT_NAME_KEY, fontName);
    }
    if (fontStyle != FONT_STYLE_INVALID) {
      props.setProperty(FONT_STYLE_KEY, Integer.toString(fontStyle));
    }
    if (fontSize != FONT_SIZE_INVALID) {
      props.setProperty(FONT_SIZE_KEY, Integer.toString(fontSize));
    }
    if (this.lookAndFeelName != null) {
      props.setProperty(LF_NAME_KEY, lookAndFeelName);
    }
    if (externalRuleDirectory != null) {
      props.setProperty(EXTERNAL_RULE_DIRECTORY, externalRuleDirectory);
    }
    StringBuilder sbRV = new StringBuilder();
    for (Map.Entry<String, Integer> entry : configurableRuleValues.entrySet()) {
      sbRV.append(entry.getKey()).append(":").append(Integer.toString(entry.getValue())).append(", ");
    }
    props.setProperty(CONFIGURABLE_RULE_VALUES_KEY + qualifier, sbRV.toString());

    StringBuilder sb = new StringBuilder();
    for (Map.Entry<ITSIssueType, Color> entry : errorColors.entrySet()) {
      String rgb = Integer.toHexString(entry.getValue().getRGB());
      rgb = rgb.substring(2, rgb.length());
      sb.append(entry.getKey()).append(":#").append(rgb).append(", ");
    }
    props.setProperty(ERROR_COLORS_KEY, sb.toString());

    StringBuilder sbUC = new StringBuilder();
    for (Map.Entry<String, Color> entry : underlineColors.entrySet()) {
      String rgb = Integer.toHexString(entry.getValue().getRGB());
      rgb = rgb.substring(2, rgb.length());
      sbUC.append(entry.getKey()).append(":#").append(rgb).append(", ");
    }
    props.setProperty(UNDERLINE_COLORS_KEY, sbUC.toString());

    for (String key : configForOtherLanguages.keySet()) {
      props.setProperty(key, configForOtherLanguages.get(key));
    }

    try (FileOutputStream fos = new FileOutputStream(configFile)) {
      props.store(fos, "LanguageTool configuration (" + JLanguageTool.VERSION + "/" + JLanguageTool.BUILD_DATE + ")");
    }
  }

  private void addListToProperties(Properties props, String key, Set<String> list) {
    if (list == null) {
      props.setProperty(key, "");
    } else {
      props.setProperty(key, String.join(DELIMITER, list));
    }
  }

}
