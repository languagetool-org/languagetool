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

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.languagetool.JLanguageTool;
import org.languagetool.Language;
import org.languagetool.Languages;
import org.languagetool.rules.ITSIssueType;
import org.languagetool.rules.Rule;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

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

  private static final String CONFIG_FILE = ".languagetool.cfg";

  private static final String DISABLED_RULES_KEY = "disabledRules";
  private static final String ENABLED_RULES_KEY = "enabledRules";
  private static final String DISABLED_CATEGORIES_KEY = "disabledCategories";
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
  private static final String STYLE_REPEAT_KEY = "distanceRepeatedWords";
  private static final String LONG_SENTENCES_KEY = "numberWordsLongSentences";
  private static final String USE_GUI_KEY = "useGUIConfig";
  private static final String FONT_NAME_KEY = "font.name";
  private static final String FONT_STYLE_KEY = "font.style";
  private static final String FONT_SIZE_KEY = "font.size";
  private static final String LF_NAME_KEY = "lookAndFeelName";
  private static final String ERROR_COLORS_KEY = "errorColors";

  private static final String DELIMITER = ",";
  private static final String EXTERNAL_RULE_DIRECTORY = "extRulesDirectory";

  private final Map<String, String> configForOtherLanguages = new HashMap<>();
  private final Map<ITSIssueType, Color> errorColors = new HashMap<>();

  private File configFile;
  private Set<String> disabledRuleIds = new HashSet<>();
  private Set<String> enabledRuleIds = new HashSet<>();
  private Set<String> disabledCategoryNames = new HashSet<>();
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
  private boolean doResetCheck = true;
  private int styleRepeatSentences = -1;
  private int longSentencesWords = -1;
  private String externalRuleDirectory;
  private String lookAndFeelName;

  /**
   * Uses the configuration file from the default location.
   * @param lang The language for the configuration, used to distinguish 
   * rules that are enabled or disabled per language.
   */
  public Configuration(Language lang) throws IOException {
    this(new File(System.getProperty("user.home")), CONFIG_FILE, lang);
  }

  public Configuration(File baseDir, Language lang) throws IOException {
    this(baseDir, CONFIG_FILE, lang);
  }

  public Configuration(File baseDir, String filename, Language lang) throws IOException {
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
    this.styleRepeatSentences = configuration.styleRepeatSentences;
    this.longSentencesWords = configuration.longSentencesWords;
    this.lookAndFeelName = configuration.lookAndFeelName;
    this.externalRuleDirectory = configuration.externalRuleDirectory;
    this.disabledRuleIds.clear();
    this.disabledRuleIds.addAll(configuration.disabledRuleIds);
    this.enabledRuleIds.clear();
    this.enabledRuleIds.addAll(configuration.enabledRuleIds);
    this.disabledCategoryNames.clear();
    this.disabledCategoryNames.addAll(configuration.disabledCategoryNames);
    this.configForOtherLanguages.clear();
    for (String key : configuration.configForOtherLanguages.keySet()) {
      this.configForOtherLanguages.put(key, configuration.configForOtherLanguages.get(key));
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

  public void setDisabledRuleIds(Set<String> ruleIDs) {
    disabledRuleIds = ruleIDs;
    enabledRuleIds.removeAll(ruleIDs);
  }

  public void setEnabledRuleIds(Set<String> ruleIDs) {
    enabledRuleIds = ruleIDs;
  }

  public void setDisabledCategoryNames(Set<String> categoryNames) {
    disabledCategoryNames = categoryNames;
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

  public boolean getAutoDetect() {
      return autoDetect;
  }

  public void setAutoDetect(boolean autoDetect) {
      this.autoDetect = autoDetect;
  }

  /**
   * Determines whether the tagger window will also print the disambiguation
   * log.
   *
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
   *
   * @param taggerShowsDisambigLog If true, the tagger window will print the
   * disambiguation log
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
   * get the maximal distance of two repeated words in number of sentences
   * @since 4.1
   */
  public int getStyleRepeatSentences() {
    return styleRepeatSentences;
  }

  /**
   * set the maximal distance of two repeated words in number of sentences
   * @since 4.1
   */
  public void setStyleRepeatSentences(int numSentences) {
    this.styleRepeatSentences = numSentences;
  }

  /**
   * get the number of words a sentence is marked as too long
   * @since 4.1
   */
  public int getLongSentencesWords() {
    return longSentencesWords;
  }

  /**
   * set the number of words a sentence is marked as too long
   * @since 4.1
   */
  public void setLongSentencesWords(int numWords) {
    this.longSentencesWords = numWords;
  }

  /**
   * Returns the name of the GUI's editing textarea font.
   * @return the name of the font.
   * @since 2.6
   * @see Font#getFamily()
   */
  public String getFontName() {
    return fontName;
  }

  /**
   * Sets the name of the GUI's editing textarea font.
   * @param fontName the name of the font.
   * @since 2.6
   * @see Font#getFamily()
   */
  public void setFontName(String fontName) {
    this.fontName = fontName;
  }

  /**
   * Returns the style of the GUI's editing textarea font.
   * @return the style of the font.
   * @since 2.6
   * @see Font#getStyle()
   */
  public int getFontStyle() {
    return fontStyle;
  }

  /**
   * Sets the style of the GUI's editing textarea font.
   * @param fontStyle the style of the font.
   * @since 2.6
   * @see Font#getStyle()
   */
  public void setFontStyle(int fontStyle) {
    this.fontStyle = fontStyle;
  }

  /**
   * Returns the size of the GUI's editing textarea font.
   * @return the size of the font.
   * @since 2.6
   * @see Font#getSize()
   */
  public int getFontSize() {
    return fontSize;
  }

  /**
   * Sets the size of the GUI's editing textarea font.
   * @param fontSize the size of the font.
   * @since 2.6
   * @see Font#getSize()
   */
  public void setFontSize(int fontSize) {
    this.fontSize = fontSize;
  }

  /**
   * Returns the name of the GUI's LaF.
   * @return the name of the LaF.
   * @since 2.6
   * @see javax.swing.UIManager.LookAndFeelInfo#getName()
   */
  public String getLookAndFeelName() {
    return this.lookAndFeelName;
  }

  /**
   * Sets the name of the GUI's LaF.
   * @param lookAndFeelName the name of the LaF.
   * @since 2.6 @see
   * @see javax.swing.UIManager.LookAndFeelInfo#getName()
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

  private void loadConfiguration(Language lang) throws IOException {

    String qualifier = getQualifier(lang);

    try (FileInputStream fis = new FileInputStream(configFile)) {

      Properties props = new Properties();
      props.load(fis);

      disabledRuleIds.addAll(getListFromProperties(props, DISABLED_RULES_KEY + qualifier));
      enabledRuleIds.addAll(getListFromProperties(props, ENABLED_RULES_KEY + qualifier));
      disabledCategoryNames.addAll(getListFromProperties(props, DISABLED_CATEGORIES_KEY + qualifier));
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
      
      String styleRepeatString = (String) props.get(STYLE_REPEAT_KEY);
      if (styleRepeatString != null) {
        styleRepeatSentences = Integer.parseInt(styleRepeatString);
        setValueToRule("STYLE_REPEATED_WORD_RULE", styleRepeatSentences, lang);
      }

      String longSentenceString = (String) props.get(LONG_SENTENCES_KEY);
      if (longSentenceString != null) {
        longSentencesWords = Integer.parseInt(longSentenceString);
        setValueToRule("TOO_LONG_SENTENCE", longSentencesWords, lang);
      }

      String colorsString = (String) props.get(ERROR_COLORS_KEY);
      parseErrorColors(colorsString);

      //store config for other languages
      loadConfigForOtherLanguages(lang, props);

    } catch (FileNotFoundException e) {
      // file not found: okay, leave disabledRuleIds empty
    }
  }

  private void parseErrorColors(String colorsString) {
    if (StringUtils.isNotEmpty(colorsString)) {
      String[] typeToColorList = colorsString.split(",\\s*");
      for (String typeToColor : typeToColorList) {
        String[] typeAndColor = typeToColor.split(":");
        if (typeAndColor.length != 2) {
          throw new RuntimeException("Could not parse type and color, colon expected: '" + typeToColor + "'");
        }
        ITSIssueType type = ITSIssueType.getIssueType(typeAndColor[0]);
        String hexColor = typeAndColor[1];
        errorColors.put(type, Color.decode(hexColor));
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
    if(styleRepeatSentences >= 0) {
      props.setProperty(STYLE_REPEAT_KEY, Integer.toString(styleRepeatSentences));
      setValueToRule ("STYLE_REPEATED_WORD_RULE", styleRepeatSentences, lang);
    }
    if(longSentencesWords >= 0) {
      props.setProperty(LONG_SENTENCES_KEY, Integer.toString(longSentencesWords));
      setValueToRule ("TOO_LONG_SENTENCE", longSentencesWords, lang);
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
    StringBuilder sb = new StringBuilder();
    for (Map.Entry<ITSIssueType, Color> entry : errorColors.entrySet()) {
      String rgb = Integer.toHexString(entry.getValue().getRGB());
      rgb = rgb.substring(2, rgb.length());
      sb.append(entry.getKey()).append(":").append("#").append(rgb).append(", ");
    }
    props.setProperty(ERROR_COLORS_KEY, sb.toString());

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
      props.setProperty(key, String.join(DELIMITER,  list));
    }
  }

  private void setValueToRule(String ruleID, int value, Language lang) {
    if (lang == null) {
      lang = language;
      if (lang == null) {
        return;
      }
    }
    JLanguageTool langTool = new JLanguageTool(lang, motherTongue);
    List<Rule> allRules = langTool.getAllRules();
    for (Rule rule : allRules) {
      if (rule.getId().startsWith(ruleID)) {
        rule.setDefaultValue(value);
        break;
      }
    }
  }
  
}
