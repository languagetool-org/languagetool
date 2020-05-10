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
import java.util.Map.Entry;
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
  
  public final static short UNDERLINE_WAVE = 10;
  public final static short UNDERLINE_BOLDWAVE = 18;
  public final static short UNDERLINE_BOLD = 12;
  public final static short UNDERLINE_DASH = 5;

  static final int DEFAULT_SERVER_PORT = 8081;  // should be HTTPServerConfig.DEFAULT_PORT but we don't have that dependency
  static final int DEFAULT_NUM_CHECK_PARAS = -1;  //  default number of parameters to be checked by TextLevelRules in LO/OO 
  static final int FONT_STYLE_INVALID = -1;
  static final int FONT_SIZE_INVALID = -1;
  static final boolean DEFAULT_DO_RESET = false;
  static final boolean DEFAULT_MULTI_THREAD = false;
  static final boolean DEFAULT_FULL_CHECK_FIRST = true;
  static final boolean DEFAULT_USE_QUEUE = true;
  static final boolean DEFAULT_USE_DOC_LANGUAGE = true;
  static final boolean DEFAULT_DO_REMOTE_CHECK = false;
  static final boolean DEFAULT_USE_OTHER_SERVER = false;
  static final boolean DEFAULT_MARK_SINGLE_CHAR_BOLD = false;

  static final Color STYLE_COLOR = new Color(0, 175, 0);

  private static final String CONFIG_FILE = ".languagetool.cfg";

  private static final String CURRENT_PROFILE_KEY = "currentProfile";
  private static final String DEFINED_PROFILES_KEY = "definedProfiles";
  
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
  private static final String NO_DEFAULT_CHECK_KEY = "noDefaultCheck";
  private static final String PARA_CHECK_KEY = "numberParagraphs";
  private static final String RESET_CHECK_KEY = "doResetCheck";
  private static final String USE_QUEUE_KEY = "useTextLevelQueue";
  private static final String DO_FULL_CHECK_AT_FIRST_KEY = "doFullCheckAtFirst";
  private static final String USE_DOC_LANG_KEY = "useDocumentLanguage";
  private static final String USE_GUI_KEY = "useGUIConfig";
  private static final String FONT_NAME_KEY = "font.name";
  private static final String FONT_STYLE_KEY = "font.style";
  private static final String FONT_SIZE_KEY = "font.size";
  private static final String LF_NAME_KEY = "lookAndFeelName";
  private static final String ERROR_COLORS_KEY = "errorColors";
  private static final String UNDERLINE_COLORS_KEY = "underlineColors";
  private static final String UNDERLINE_TYPES_KEY = "underlineTypes";
  private static final String CONFIGURABLE_RULE_VALUES_KEY = "configurableRuleValues";
  private static final String LT_SWITCHED_OFF_KEY = "ltSwitchedOff";
  private static final String IS_MULTI_THREAD_LO_KEY = "isMultiThread";
  private static final String EXTERNAL_RULE_DIRECTORY = "extRulesDirectory";
  private static final String DO_REMOTE_CHECK_KEY = "doRemoteCheck";
  private static final String OTHER_SERVER_URL_KEY = "otherServerUrl";
  private static final String USE_OTHER_SERVER_KEY = "useOtherServer";
  private static final String MARK_SINGLE_CHAR_BOLD_KEY = "markSingleCharBold";
  private static final String LOG_LEVEL_KEY = "logLevel";

  private static final String DELIMITER = ",";
  // find all comma followed by zero or more white space characters that are preceded by ":" AND a valid 6-digit hex code
  // example: ":#44ffee,"
  private static final String COLOR_SPLITTER_REGEXP = "(?<=:#[0-9A-Fa-f]{6}),\\s*";
  //find all colon followed by a valid 6-digit hex code, e.g., ":#44ffee"
  private static final String COLOR_SPLITTER_REGEXP_COLON = ":(?=#[0-9A-Fa-f]{6})";
  // find all comma followed by zero or more white space characters that are preceded by at least one digit
  // example: "4,"
  private static final String CONFIGURABLE_RULE_SPLITTER_REGEXP = "(?<=[0-9]),\\s*";

  private static final String BLANK = "[ \t]";
  private static final String BLANK_REPLACE = "_";
  private static final String PROFILE_DELIMITER = "__";
  
  // For new Maps, Sets or Lists add a clear to initOptions
  private final Map<String, String> configForOtherProfiles = new HashMap<>();
  private final Map<String, String> configForOtherLanguages = new HashMap<>();
  private final Map<ITSIssueType, Color> errorColors = new EnumMap<>(ITSIssueType.class);
  private final Map<String, Color> underlineColors = new HashMap<>();
  private final Map<String, Short> underlineTypes = new HashMap<>();
  private final Map<String, Integer> configurableRuleValues = new HashMap<>();
  private final Set<String> styleLikeCategories = new HashSet<>();
  private final Map<String, String> specialTabCategories = new HashMap<>();

  // For new Maps, Sets or Lists add a clear to initOptions
  private Set<String> disabledRuleIds = new HashSet<>();
  private Set<String> enabledRuleIds = new HashSet<>();
  private Set<String> disabledCategoryNames = new HashSet<>();
  private Set<String> enabledCategoryNames = new HashSet<>();
  private List<String> definedProfiles = new ArrayList<String>();
  private List<String> allProfileKeys = new ArrayList<String>();
  private List<String> allProfileLangKeys = new ArrayList<String>();

  // Add new option default parameters to initOptions
  private Language lang;
  private File configFile;
  private File oldConfigFile;
  private boolean enabledRulesOnly = false;
  private Language language;
  private Language motherTongue = null;
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
  private boolean doResetCheck = DEFAULT_DO_RESET;
  private boolean isMultiThreadLO = DEFAULT_MULTI_THREAD;
  private boolean doFullCheckAtFirst = DEFAULT_FULL_CHECK_FIRST;
  private boolean useTextLevelQueue = DEFAULT_USE_QUEUE;
  private boolean useDocLanguage = DEFAULT_USE_DOC_LANGUAGE;
  private boolean doRemoteCheck = DEFAULT_DO_REMOTE_CHECK;
  private boolean useOtherServer = DEFAULT_USE_OTHER_SERVER;
  private boolean markSingleCharBold = DEFAULT_MARK_SINGLE_CHAR_BOLD;
  private String externalRuleDirectory;
  private String lookAndFeelName;
  private String currentProfile = null;
  private String otherServerUrl = null;
  private String logLevel = null;
  private boolean switchOff = false;

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
    this(baseDir, filename, null, lang, linguServices);
  }

  public Configuration(File baseDir, String filename, File oldConfigFile, Language lang, LinguServices linguServices) throws IOException {
    // already fails silently if file doesn't exist in loadConfiguration, don't fail here either
    // can cause problem when starting LanguageTool server as a user without a home directory because of default arguments
    //if (baseDir == null || !baseDir.isDirectory()) {
    //  throw new IllegalArgumentException("Cannot open file " + filename + " in directory " + baseDir);
    //}
    initOptions();
    this.lang = lang;
    configFile = new File(baseDir, filename);
    this.oldConfigFile = oldConfigFile;
    setAllProfileKeys();
    loadConfiguration();
  }

  private Configuration() {
    lang = null;
  }

  /**
   * Initialize variables and clears Maps, Sets and Lists
   */
  
  public void initOptions() {
    configForOtherLanguages.clear();
    underlineColors.clear();
    underlineTypes.clear();
    configurableRuleValues.clear();

    disabledRuleIds.clear();
    enabledRuleIds.clear();
    disabledCategoryNames.clear();
    enabledCategoryNames.clear();
    definedProfiles.clear();

    enabledRulesOnly = false;
    ngramDirectory = null;
    word2vecDirectory = null;
    runServer = false;
    autoDetect = false;
    taggerShowsDisambigLog = false;
    guiConfig = false;
    fontName = null;
    fontStyle = FONT_STYLE_INVALID;
    fontSize = FONT_SIZE_INVALID;
    serverPort = DEFAULT_SERVER_PORT;
    numParasToCheck = DEFAULT_NUM_CHECK_PARAS;
    doResetCheck = DEFAULT_DO_RESET;
    isMultiThreadLO = DEFAULT_MULTI_THREAD;
    doFullCheckAtFirst = DEFAULT_FULL_CHECK_FIRST;
    useTextLevelQueue = DEFAULT_USE_QUEUE;
    useDocLanguage = DEFAULT_USE_DOC_LANGUAGE;
    doRemoteCheck = DEFAULT_DO_REMOTE_CHECK;
    useOtherServer = DEFAULT_USE_OTHER_SERVER;
    markSingleCharBold = DEFAULT_MARK_SINGLE_CHAR_BOLD;
    externalRuleDirectory = null;
    lookAndFeelName = null;
    currentProfile = null;
    otherServerUrl = null;
    logLevel = null;
    switchOff = false;
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
    this.lang = configuration.lang;
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
    this.useTextLevelQueue = configuration.useTextLevelQueue;
    this.doFullCheckAtFirst = configuration.doFullCheckAtFirst;
    this.isMultiThreadLO = configuration.isMultiThreadLO;
    this.useDocLanguage = configuration.useDocLanguage;
    this.lookAndFeelName = configuration.lookAndFeelName;
    this.externalRuleDirectory = configuration.externalRuleDirectory;
    this.currentProfile = configuration.currentProfile;
    this.doRemoteCheck = configuration.doRemoteCheck;
    this.useOtherServer = configuration.useOtherServer;
    this.markSingleCharBold = configuration.markSingleCharBold;
    this.otherServerUrl = configuration.otherServerUrl;
    this.logLevel = configuration.logLevel;
    
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
    this.underlineTypes.clear();
    for (Map.Entry<String, Short> entry : configuration.underlineTypes.entrySet()) {
      this.underlineTypes.put(entry.getKey(), entry.getValue());
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
    this.definedProfiles.clear();
    this.definedProfiles.addAll(configuration.definedProfiles);
    this.allProfileLangKeys.clear();
    this.allProfileLangKeys.addAll(configuration.allProfileLangKeys);
    this.allProfileKeys.clear();
    this.allProfileKeys.addAll(configuration.allProfileKeys);
    this.configForOtherProfiles.clear();
    for (Entry<String, String> entry : configuration.configForOtherProfiles.entrySet()) {
      this.configForOtherProfiles.put(entry.getKey(), entry.getValue());
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

  public void addDisabledRuleIds(Set<String> ruleIds) {
    disabledRuleIds.addAll(ruleIds);
    enabledRuleIds.removeAll(ruleIds);
  }

  public void removeDisabledRuleIds(Set<String> ruleIds) {
    disabledRuleIds.removeAll(ruleIds);
    enabledRuleIds.addAll(ruleIds);
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

  public void setRemoteCheck(boolean doRemoteCheck) {
    this.doRemoteCheck = doRemoteCheck;
  }

  public boolean doRemoteCheck() {
    return doRemoteCheck;
  }

  public void setUseOtherServer(boolean useOtherServer) {
    this.useOtherServer = useOtherServer;
  }

  public boolean useOtherServer() {
    return useOtherServer;
  }

  public void setOtherServerUrl(String otherServerUrl) {
    this.otherServerUrl = otherServerUrl;
  }

  public String getServerUrl() {
    return useOtherServer ? otherServerUrl : null;
  }

  public String getlogLevel() {
    return logLevel;
  }

  public void setMarkSingleCharBold(boolean markSingleCharBold) {
    this.markSingleCharBold = markSingleCharBold;
  }

  public boolean markSingleCharBold() {
    return markSingleCharBold;
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
  public boolean useTextLevelQueue() {
    return useTextLevelQueue;
  }

  /**
   * set all paragraphs to be not checked after every change of text
   * if more than one document loaded?
   * @since 4.5
   */
  public void setUseTextLevelQueue(boolean useTextLevelQueue) {
    this.useTextLevelQueue = useTextLevelQueue;
  }

  /**
   * set option to do a full check at first iteration
   * @since 4.7
   */
  public void setFullCheckAtFirst(boolean doFullCheckAtFirst) {
    this.doFullCheckAtFirst = doFullCheckAtFirst;
  }

  /**
   * do a full check at first iteration?
   * @since 4.7
   */
  public boolean doFullCheckAtFirst() {
    return doFullCheckAtFirst;
  }
  
  /**
   * get the current profile
   * @since 4.7
   */
  public String getCurrentProfile() {
    return currentProfile;
  }
  
  /**
   * set the current profile
   * @since 4.7
   */
  public void setCurrentProfile(String profile) {
    currentProfile = profile;
  }
  
  /**
   * get the current profile
   * @since 4.7
   */
  public List<String> getDefinedProfiles() {
    return definedProfiles;
  }
  
  /**
   * add a new profile
   * @since 4.7
   */
  public void addProfile(String profile) {
    definedProfiles.add(profile);
  }
  
  /**
   * add a list of profiles
   * @since 4.7
   */
  public void addProfiles(List<String> profiles) {
    definedProfiles.clear();
    definedProfiles.addAll(profiles);
  }
  
  /**
   * remove an existing profile
   * @since 4.7
   */
  public void removeProfile(String profile) {
    definedProfiles.remove(profile);
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
        styleLikeCategories.add(rule.getCategory().getName());
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
      tabNames.add(entry.getValue());
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
   * @since 4.9
   */
  public Map<String, Short> getUnderlineTypes() {
    return underlineTypes;
  }

  /**
   * @since 4.9
   * Get the type to underline a rule match by the Name of its category
   */
  public Short getUnderlineType(String category) {
    if (underlineTypes.containsKey(category)) {
      return underlineTypes.get(category);
    }
    return UNDERLINE_WAVE;
  }

  /**
   * @since 4.9
   * Set the type to underline a rule match for its category
   */
  public void setUnderlineType(String category, short type) {
    underlineTypes.put(category, type);
  }

  /**
   * @since 4.9
   * Set the type back to default (removes category from map)
   */
  public void setDefaultUnderlineType(String category) {
    underlineTypes.remove(category);
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

  private void loadConfiguration() throws IOException {
    loadConfiguration(null);
  }

  public void loadConfiguration(String profile) throws IOException {


    String qualifier = getQualifier(lang);
    
    File cfgFile;
    if(configFile.exists() || oldConfigFile == null) {
      cfgFile = configFile;
    } else {
      cfgFile = oldConfigFile;
    }

    try (FileInputStream fis = new FileInputStream(cfgFile)) {

      Properties props = new Properties();
      props.load(fis);
      
      if(profile == null) {
        String curProfileStr = (String) props.get(CURRENT_PROFILE_KEY);
        if (curProfileStr != null) {
          currentProfile = curProfileStr;
        }
      } else {
        currentProfile = profile;
      }
      definedProfiles.addAll(getListFromProperties(props, DEFINED_PROFILES_KEY));
      
      logLevel = (String) props.get(LOG_LEVEL_KEY);
      
      storeConfigforAllProfiles(props);
      
      String prefix;
      if(currentProfile == null) {
        prefix = "";
      } else {
        prefix = currentProfile;
      }
      if(!prefix.isEmpty()) {
        prefix = prefix.replaceAll(BLANK, BLANK_REPLACE);
        prefix += PROFILE_DELIMITER;
      }

      String useDocLangString = (String) props.get(prefix + USE_DOC_LANG_KEY);
      if (useDocLangString != null) {
        useDocLanguage = Boolean.parseBoolean(useDocLangString);
      }
      String motherTongueStr = (String) props.get(prefix + MOTHER_TONGUE_KEY);
      if (motherTongueStr != null && !motherTongueStr.equals("xx")) {
        motherTongue = Languages.getLanguageForShortCode(motherTongueStr);
      }
      if(!useDocLanguage && motherTongue != null) {
        qualifier = getQualifier(motherTongue);
      }

      disabledRuleIds.addAll(getListFromProperties(props, prefix + DISABLED_RULES_KEY + qualifier));
      enabledRuleIds.addAll(getListFromProperties(props, prefix + ENABLED_RULES_KEY + qualifier));
      disabledCategoryNames.addAll(getListFromProperties(props, prefix + DISABLED_CATEGORIES_KEY + qualifier));
      enabledCategoryNames.addAll(getListFromProperties(props, prefix + ENABLED_CATEGORIES_KEY + qualifier));
      enabledRulesOnly = "true".equals(props.get(prefix + ENABLED_RULES_ONLY_KEY));

      String languageStr = (String) props.get(prefix + LANGUAGE_KEY);
      if (languageStr != null) {
        language = Languages.getLanguageForShortCode(languageStr);
      }
      String ngramDir = (String) props.get(prefix + NGRAM_DIR_KEY);
      if (ngramDir != null) {
        ngramDirectory = new File(ngramDir);
      }
      String word2vecDir = (String) props.get(prefix + WORD2VEC_DIR_KEY);
      if (word2vecDir != null) {
        word2vecDirectory = new File(word2vecDir);
      }

      autoDetect = "true".equals(props.get(prefix + AUTO_DETECT_KEY));
      taggerShowsDisambigLog = "true".equals(props.get(prefix + TAGGER_SHOWS_DISAMBIG_LOG_KEY));
      guiConfig = "true".equals(props.get(prefix + USE_GUI_KEY));
      runServer = "true".equals(props.get(prefix + SERVER_RUN_KEY));

      fontName = (String) props.get(prefix + FONT_NAME_KEY);
      if (props.get(prefix + FONT_STYLE_KEY) != null) {
        try {
          fontStyle = Integer.parseInt((String) props.get(prefix + FONT_STYLE_KEY));
        } catch (NumberFormatException e) {
          // Ignore
        }
      }
      if (props.get(prefix + FONT_SIZE_KEY) != null) {
        try {
          fontSize = Integer.parseInt((String) props.get(prefix + FONT_SIZE_KEY));
        } catch (NumberFormatException e) {
          // Ignore
        }
      }
      lookAndFeelName = (String) props.get(prefix + LF_NAME_KEY);

      String serverPortString = (String) props.get(prefix + SERVER_PORT_KEY);
      if (serverPortString != null) {
        serverPort = Integer.parseInt(serverPortString);
      }
      String extRules = (String) props.get(prefix + EXTERNAL_RULE_DIRECTORY);
      if (extRules != null) {
        externalRuleDirectory = extRules;
      }

      String paraCheckString = (String) props.get(prefix + NO_DEFAULT_CHECK_KEY);
      if(paraCheckString != null && Boolean.parseBoolean(paraCheckString)) {
        paraCheckString = (String) props.get(prefix + PARA_CHECK_KEY);
        if (paraCheckString != null) {
          numParasToCheck = Integer.parseInt(paraCheckString);
        }
      }

      String resetCheckString = (String) props.get(prefix + RESET_CHECK_KEY);
      if (resetCheckString != null) {
        doResetCheck = Boolean.parseBoolean(resetCheckString);
      }

      String useTextLevelQueueString = (String) props.get(prefix + USE_QUEUE_KEY);
      if (useTextLevelQueueString != null) {
        useTextLevelQueue = Boolean.parseBoolean(useTextLevelQueueString);
      }

      String doFullCheckAtFirstString = (String) props.get(prefix + DO_FULL_CHECK_AT_FIRST_KEY);
      if (doFullCheckAtFirstString != null) {
        doFullCheckAtFirst = Boolean.parseBoolean(doFullCheckAtFirstString);
      }

      String switchOffString = (String) props.get(prefix + LT_SWITCHED_OFF_KEY);
      if (switchOffString != null) {
        switchOff = Boolean.parseBoolean(switchOffString);
      }

      String isMultiThreadString = (String) props.get(prefix + IS_MULTI_THREAD_LO_KEY);
      if (isMultiThreadString != null) {
        isMultiThreadLO = Boolean.parseBoolean(isMultiThreadString);
      }
      
      String doRemoteCheckString = (String) props.get(prefix + DO_REMOTE_CHECK_KEY);
      if (doRemoteCheckString != null) {
        doRemoteCheck = Boolean.parseBoolean(doRemoteCheckString);
      }
      
      String useOtherServerString = (String) props.get(prefix + USE_OTHER_SERVER_KEY);
      if (useOtherServerString != null) {
        useOtherServer = Boolean.parseBoolean(useOtherServerString);
      }
      
      otherServerUrl = (String) props.get(prefix + OTHER_SERVER_URL_KEY);
      
      String markSingleCharBoldString = (String) props.get(prefix + MARK_SINGLE_CHAR_BOLD_KEY);
      if (markSingleCharBoldString != null) {
        markSingleCharBold = Boolean.parseBoolean(markSingleCharBoldString);
      }
      
      String rulesValuesString = (String) props.get(prefix + CONFIGURABLE_RULE_VALUES_KEY + qualifier);
      if(rulesValuesString == null) {
        rulesValuesString = (String) props.get(prefix + CONFIGURABLE_RULE_VALUES_KEY);
      }
      parseConfigurableRuleValues(rulesValuesString);

      String colorsString = (String) props.get(prefix + ERROR_COLORS_KEY);
      parseErrorColors(colorsString);

      String underlineColorsString = (String) props.get(prefix + UNDERLINE_COLORS_KEY);
      parseUnderlineColors(underlineColorsString);

      String underlineTypesString = (String) props.get(prefix + UNDERLINE_TYPES_KEY);
      parseUnderlineTypes(underlineTypesString);

      //store config for other languages
      loadConfigForOtherLanguages(lang, props, prefix);

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

  private void parseUnderlineTypes(String typessString) {
    if (StringUtils.isNotEmpty(typessString)) {
      String[] categoryToTypesList = typessString.split(CONFIGURABLE_RULE_SPLITTER_REGEXP);
      for (String categoryToType : categoryToTypesList) {
        String[] categoryAndType = categoryToType.split(":");
        if (categoryAndType.length != 2) {
          throw new RuntimeException("Could not parse category and type, colon expected: '" + categoryToType + "'");
        }
        underlineTypes.put(categoryAndType[0], Short.parseShort(categoryAndType[1]));
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

  private void loadConfigForOtherLanguages(Language lang, Properties prop, String prefix) {
    for (Language otherLang : Languages.get()) {
      if (!otherLang.equals(lang)) {
        String languageSuffix = "." + otherLang.getShortCodeWithCountryAndVariant();
        storeConfigKeyFromProp(prop, prefix + DISABLED_RULES_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, prefix + ENABLED_RULES_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, prefix + DISABLED_CATEGORIES_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, prefix + ENABLED_CATEGORIES_KEY + languageSuffix);
        storeConfigKeyFromProp(prop, prefix + CONFIGURABLE_RULE_VALUES_KEY + languageSuffix);
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

    if(currentProfile != null && !currentProfile.isEmpty()) {
      props.setProperty(CURRENT_PROFILE_KEY, currentProfile);
    }
    
    if(!definedProfiles.isEmpty()) {
      props.setProperty(DEFINED_PROFILES_KEY, String.join(DELIMITER, definedProfiles));
    }
    
    if(logLevel != null) {
      props.setProperty(LOG_LEVEL_KEY, logLevel);
    }
    
    try (FileOutputStream fos = new FileOutputStream(configFile)) {
      props.store(fos, "LanguageTool configuration (" + JLanguageTool.VERSION + "/" + JLanguageTool.BUILD_DATE + ")");
    }

    List<String> prefixes = new ArrayList<String>();
    prefixes.add("");
    for(String profile : definedProfiles) {
      String prefix = profile;
      prefixes.add(prefix.replaceAll(BLANK, BLANK_REPLACE) + PROFILE_DELIMITER);
    }
    String currentPrefix;
    if (currentProfile == null) {
      currentPrefix = "";
    } else {
      currentPrefix = currentProfile;
    }
    if(!currentPrefix.isEmpty()) {
      currentPrefix = currentPrefix.replaceAll(BLANK, BLANK_REPLACE);
      currentPrefix += PROFILE_DELIMITER;
    }
    for(String prefix : prefixes) {
      props = new Properties();
      if(currentPrefix.equals(prefix)) {
        addListToProperties(props, prefix + DISABLED_RULES_KEY + qualifier, disabledRuleIds);
        addListToProperties(props, prefix + ENABLED_RULES_KEY + qualifier, enabledRuleIds);
        addListToProperties(props, prefix + DISABLED_CATEGORIES_KEY + qualifier, disabledCategoryNames);
        addListToProperties(props, prefix + ENABLED_CATEGORIES_KEY + qualifier, enabledCategoryNames);
        if (language != null && !language.isExternal()) {  // external languages won't be known at startup, so don't save them
          props.setProperty(prefix + LANGUAGE_KEY, language.getShortCodeWithCountryAndVariant());
        }
        if (motherTongue != null) {
          props.setProperty(prefix + MOTHER_TONGUE_KEY, motherTongue.getShortCode());
        }
        if (ngramDirectory != null) {
          props.setProperty(prefix + NGRAM_DIR_KEY, ngramDirectory.getAbsolutePath());
        }
        if (word2vecDirectory != null) {
          props.setProperty(prefix + WORD2VEC_DIR_KEY, word2vecDirectory.getAbsolutePath());
        }
        props.setProperty(prefix + AUTO_DETECT_KEY, Boolean.toString(autoDetect));
        props.setProperty(prefix + TAGGER_SHOWS_DISAMBIG_LOG_KEY, Boolean.toString(taggerShowsDisambigLog));
        props.setProperty(prefix + USE_GUI_KEY, Boolean.toString(guiConfig));
        props.setProperty(prefix + SERVER_RUN_KEY, Boolean.toString(runServer));
        props.setProperty(prefix + SERVER_PORT_KEY, Integer.toString(serverPort));
        if(numParasToCheck != DEFAULT_NUM_CHECK_PARAS) {
          props.setProperty(prefix + NO_DEFAULT_CHECK_KEY, Boolean.toString(true));
          props.setProperty(prefix + PARA_CHECK_KEY, Integer.toString(numParasToCheck));
        }
        if(doResetCheck != DEFAULT_DO_RESET) {
          props.setProperty(prefix + RESET_CHECK_KEY, Boolean.toString(doResetCheck));
        }
        if(useTextLevelQueue != DEFAULT_USE_QUEUE) {
          props.setProperty(prefix + USE_QUEUE_KEY, Boolean.toString(useTextLevelQueue));
        }
        if(doFullCheckAtFirst != DEFAULT_FULL_CHECK_FIRST) {
          props.setProperty(prefix + DO_FULL_CHECK_AT_FIRST_KEY, Boolean.toString(doFullCheckAtFirst));
        }
        if(useDocLanguage != DEFAULT_USE_DOC_LANGUAGE) {
          props.setProperty(prefix + USE_DOC_LANG_KEY, Boolean.toString(useDocLanguage));
        }
        if(isMultiThreadLO != DEFAULT_MULTI_THREAD) {
          props.setProperty(prefix + IS_MULTI_THREAD_LO_KEY, Boolean.toString(isMultiThreadLO));
        }
        if(doRemoteCheck != DEFAULT_DO_REMOTE_CHECK) {
          props.setProperty(prefix + DO_REMOTE_CHECK_KEY, Boolean.toString(doRemoteCheck));
        }
        if(useOtherServer != DEFAULT_USE_OTHER_SERVER) {
          props.setProperty(prefix + USE_OTHER_SERVER_KEY, Boolean.toString(useOtherServer));
        }
        if(markSingleCharBold != DEFAULT_MARK_SINGLE_CHAR_BOLD) {
          props.setProperty(prefix + MARK_SINGLE_CHAR_BOLD_KEY, Boolean.toString(markSingleCharBold));
        }
        if(switchOff) {
          props.setProperty(prefix + LT_SWITCHED_OFF_KEY, Boolean.toString(switchOff));
        }
        if (otherServerUrl != null) {
          props.setProperty(prefix + OTHER_SERVER_URL_KEY, otherServerUrl);
        }
        if (fontName != null) {
          props.setProperty(prefix + FONT_NAME_KEY, fontName);
        }
        if (fontStyle != FONT_STYLE_INVALID) {
          props.setProperty(prefix + FONT_STYLE_KEY, Integer.toString(fontStyle));
        }
        if (fontSize != FONT_SIZE_INVALID) {
          props.setProperty(prefix + FONT_SIZE_KEY, Integer.toString(fontSize));
        }
        if (this.lookAndFeelName != null) {
          props.setProperty(prefix + LF_NAME_KEY, lookAndFeelName);
        }
        if (externalRuleDirectory != null) {
          props.setProperty(prefix + EXTERNAL_RULE_DIRECTORY, externalRuleDirectory);
        }
        if(!configurableRuleValues.isEmpty()) {
          StringBuilder sbRV = new StringBuilder();
          for (Map.Entry<String, Integer> entry : configurableRuleValues.entrySet()) {
            sbRV.append(entry.getKey()).append(":").append(Integer.toString(entry.getValue())).append(", ");
          }
          props.setProperty(prefix + CONFIGURABLE_RULE_VALUES_KEY + qualifier, sbRV.toString());
        }
        if(!errorColors.isEmpty()) {
          StringBuilder sb = new StringBuilder();
          for (Map.Entry<ITSIssueType, Color> entry : errorColors.entrySet()) {
            String rgb = Integer.toHexString(entry.getValue().getRGB());
            rgb = rgb.substring(2);
            sb.append(entry.getKey()).append(":#").append(rgb).append(", ");
          }
          props.setProperty(prefix + ERROR_COLORS_KEY, sb.toString());
        }
        if(!underlineColors.isEmpty()) {
          StringBuilder sbUC = new StringBuilder();
          for (Map.Entry<String, Color> entry : underlineColors.entrySet()) {
            String rgb = Integer.toHexString(entry.getValue().getRGB());
            rgb = rgb.substring(2);
            sbUC.append(entry.getKey()).append(":#").append(rgb).append(", ");
          }
          props.setProperty(prefix + UNDERLINE_COLORS_KEY, sbUC.toString());
        }
        if(!underlineTypes.isEmpty()) {
          StringBuilder sbUT = new StringBuilder();
          for (Map.Entry<String, Short> entry : underlineTypes.entrySet()) {
            sbUT.append(entry.getKey()).append(":").append(Short.toString(entry.getValue())).append(", ");
          }
          props.setProperty(prefix + UNDERLINE_TYPES_KEY, sbUT.toString());
        }
        for (String key : configForOtherLanguages.keySet()) {
          props.setProperty(key, configForOtherLanguages.get(key));
        }
      } else {
        saveConfigforProfile(props, prefix);
      }

      try (FileOutputStream fos = new FileOutputStream(configFile, true)) {
        props.store(fos, "Profile: " + (prefix.isEmpty() ? "Default" : prefix.substring(0, prefix.length() - 2)));
      }
    }
    
    if(oldConfigFile != null && oldConfigFile.exists()) {
      oldConfigFile.delete();
    }
  }

  private void addListToProperties(Properties props, String key, Set<String> list) {
    if (list == null) {
      props.setProperty(key, "");
    } else {
      props.setProperty(key, String.join(DELIMITER, list));
    }
  }
  
  private void setAllProfileKeys() {
    allProfileKeys.add(LANGUAGE_KEY);
    allProfileKeys.add(MOTHER_TONGUE_KEY);
    allProfileKeys.add(NGRAM_DIR_KEY);
    allProfileKeys.add(WORD2VEC_DIR_KEY);
    allProfileKeys.add(AUTO_DETECT_KEY);
    allProfileKeys.add(TAGGER_SHOWS_DISAMBIG_LOG_KEY);
    allProfileKeys.add(SERVER_RUN_KEY);
    allProfileKeys.add(SERVER_PORT_KEY);
    allProfileKeys.add(NO_DEFAULT_CHECK_KEY);
    allProfileKeys.add(PARA_CHECK_KEY);
    allProfileKeys.add(RESET_CHECK_KEY);
    allProfileKeys.add(USE_QUEUE_KEY);
    allProfileKeys.add(DO_FULL_CHECK_AT_FIRST_KEY);
    allProfileKeys.add(USE_DOC_LANG_KEY);
    allProfileKeys.add(USE_GUI_KEY);
    allProfileKeys.add(FONT_NAME_KEY);
    allProfileKeys.add(FONT_STYLE_KEY);
    allProfileKeys.add(FONT_SIZE_KEY);
    allProfileKeys.add(LF_NAME_KEY);
    allProfileKeys.add(ERROR_COLORS_KEY);
    allProfileKeys.add(UNDERLINE_COLORS_KEY);
    allProfileKeys.add(UNDERLINE_TYPES_KEY);
    allProfileKeys.add(LT_SWITCHED_OFF_KEY);
    allProfileKeys.add(IS_MULTI_THREAD_LO_KEY);
    allProfileKeys.add(EXTERNAL_RULE_DIRECTORY);
    allProfileKeys.add(DO_REMOTE_CHECK_KEY);
    allProfileKeys.add(OTHER_SERVER_URL_KEY);
    allProfileKeys.add(USE_OTHER_SERVER_KEY);
    allProfileKeys.add(MARK_SINGLE_CHAR_BOLD_KEY);

    allProfileLangKeys.add(DISABLED_RULES_KEY);
    allProfileLangKeys.add(ENABLED_RULES_KEY);
    allProfileLangKeys.add(DISABLED_CATEGORIES_KEY);
    allProfileLangKeys.add(ENABLED_CATEGORIES_KEY);
    allProfileLangKeys.add(CONFIGURABLE_RULE_VALUES_KEY);
  }
  
  private void storeConfigforAllProfiles(Properties props) {
    List<String> prefix = new ArrayList<String>();
    prefix.add("");
    for(String profile : definedProfiles) {
      String sPrefix = profile;
      prefix.add(sPrefix.replaceAll(BLANK, BLANK_REPLACE) + PROFILE_DELIMITER);
    }
    for(String sPrefix : prefix) {
      for(String key : allProfileLangKeys) {
        for (Language lang : Languages.get()) {
          String preKey = sPrefix + key + "." + lang.getShortCodeWithCountryAndVariant();
          if (props.containsKey(preKey)) {
            configForOtherProfiles.put(preKey, props.getProperty(preKey));
          }
        }
      }
    }
    for(String sPrefix : prefix) {
      for(String key : allProfileKeys) {
        String preKey = sPrefix + key;
        if (props.containsKey(preKey)) {
          configForOtherProfiles.put(preKey, props.getProperty(preKey));
        }
      }
    }
  }

  private void saveConfigforProfile(Properties props, String prefix) {
    for(String key : allProfileLangKeys) {
      for (Language lang : Languages.get()) {
        String preKey = prefix + key + "." + lang.getShortCodeWithCountryAndVariant();
        if (configForOtherProfiles.containsKey(preKey)) {
          props.setProperty(preKey, configForOtherProfiles.get(preKey));
        }
      }
    }
    for(String key : allProfileKeys) {
      String preKey = prefix + key;
      if (configForOtherProfiles.containsKey(preKey)) {
        props.setProperty(preKey, configForOtherProfiles.get(preKey));
      }
    }
  }

}
