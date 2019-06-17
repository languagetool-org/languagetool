/* LanguageTool, a natural language style checker
 * Copyright (C) 2012 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.commandline;

import org.jetbrains.annotations.Nullable;
import org.languagetool.Language;
import org.languagetool.rules.CategoryId;

import java.io.File;
import java.util.*;

/**
 * Options that can be set via command line arguments.
 */
public class CommandLineOptions {

  /**
   * Constants for rule matches output in command-line.
   * @since 3.6
   */
  public enum OutputFormat {
    PLAINTEXT,
    JSON,
    XML
  }

  private final Set<CategoryId> enabledCategories = new HashSet<>();
  private final Set<CategoryId> disabledCategories = new HashSet<>();

  private boolean printUsage = false;
  private boolean printVersion = false;
  private boolean printLanguages = false;
  private boolean verbose = false;
  private boolean recursive = false;
  private boolean taggerOnly = false;
  private boolean singleLineBreakMarksParagraph = false;
  private OutputFormat outputFormat = OutputFormat.PLAINTEXT;
  private boolean listUnknown = false;
  private boolean applySuggestions = false;
  private boolean profile = false;
  private boolean bitext = false;
  private boolean autoDetect = false;
  private boolean xmlFiltering = false;
  private boolean lineByLine = false;
  @Nullable
  private Language language = null;
  @Nullable
  private Language motherTongue = null;
  @Nullable
  private File languageModel = null;
  @Nullable
  private File word2vecModel = null;
  @Nullable
  private File neuralNetworkModel = null;

  @Nullable
  private File fasttextModel = null;
  @Nullable
  private File fasttextBinary = null;
  @Nullable
  private String encoding = null;
  @Nullable
  private String filename = null;
  private List<String> disabledRules = new ArrayList<>();
  private List<String> enabledRules = new ArrayList<>();
  private boolean useEnabledOnly = false;
  @Nullable
  private String ruleFile = null;
  @Nullable
  private String falseFriendFile = null;
  @Nullable
  private String bitextRuleFile = null;

  public boolean isPrintUsage() {
    return printUsage;
  }

  public void setPrintUsage(boolean printUsage) {
    this.printUsage = printUsage;
  }

  public boolean isPrintVersion() {
    return printVersion;
  }

  public void setPrintVersion(boolean printVersion) {
    this.printVersion = printVersion;
  }

  public boolean isVerbose() {
    return verbose;
  }

  public void setVerbose(boolean verbose) {
    this.verbose = verbose;
  }

  public boolean isLineByLine() {
    return lineByLine;
  }

  public void setLineByLine (boolean lineByLine) {
    this.lineByLine = lineByLine;
  }

  public boolean isRecursive() {
    return recursive;
  }

  public void setRecursive(boolean recursive) {
    this.recursive = recursive;
  }

  public boolean isTaggerOnly() {
    return taggerOnly;
  }

  public void setTaggerOnly(boolean taggerOnly) {
    this.taggerOnly = taggerOnly;
  }

  public boolean isSingleLineBreakMarksParagraph() {
    return singleLineBreakMarksParagraph;
  }

  public void setSingleLineBreakMarksParagraph(boolean singleLineBreakMarksParagraph) {
    this.singleLineBreakMarksParagraph = singleLineBreakMarksParagraph;
  }

  /**
   * @since 3.6
   */
  public boolean isXmlFormat() {
    return this.outputFormat == OutputFormat.XML;
  }
  
  /**
   * @since 3.6
   */
  public void setXmlFormat() {
    this.outputFormat = OutputFormat.XML;
  }

  /**
   * @since 3.6
   */
  public boolean isJsonFormat() {
    return this.outputFormat == OutputFormat.JSON;
  }

  /**
   * @since 3.6
   */
  public void setJsonFormat() {
    this.outputFormat = OutputFormat.JSON;
  }

  public boolean isListUnknown() {
    return listUnknown;
  }

  public void setListUnknown(boolean listUnknown) {
    this.listUnknown = listUnknown;
  }

  public boolean isApplySuggestions() {
    return applySuggestions;
  }

  public void setApplySuggestions(boolean applySuggestions) {
    this.applySuggestions = applySuggestions;
  }

  public boolean isProfile() {
    return profile;
  }

  public void setProfile(boolean profile) {
    this.profile = profile;
  }

  public boolean isBitext() {
    return bitext;
  }

  public void setBitext(boolean bitext) {
    this.bitext = bitext;
  }

  public boolean isAutoDetect() {
    return autoDetect;
  }

  public void setAutoDetect(boolean autoDetect) {
    this.autoDetect = autoDetect;
  }

  @Nullable
  public Language getLanguage() {
    return language;
  }

  public void setLanguage(Language language) {
    this.language = language;
  }

  @Nullable
  public Language getMotherTongue() {
    return motherTongue;
  }

  public void setMotherTongue(Language motherTongue) {
    this.motherTongue = motherTongue;
  }

  /**
   * @return a directory with Lucene index sub directories like ({@code 3grams}), or {@code null}
   * @since 2.7
   */
  @Nullable
  public File getLanguageModel() {
    return languageModel;
  }

  /**
   * @since 2.7
   */
  public void setLanguageModel(File languageModel) {
    this.languageModel = languageModel;
  }

  /**
   * @return a directory with a word2vec language model for use with neural network rules in sub directories like ({@code en}), or {@code null}
   * @since 4.0
   */
  @Nullable
  public File getWord2VecModel() {
    return word2vecModel;
  }

  /**
   * @since 4.0
   */
  public void setWord2VecModel(File neuralNetworkLanguageModel) {
    this.word2vecModel = neuralNetworkLanguageModel;
  }


  /**
   * @since 4.4
   */
  @Nullable
  public File getNeuralNetworkModel() {
    return neuralNetworkModel;
  }

  /**
   * @since 4.4
   */
  public void setNeuralNetworkModel(File neuralNetworkModel) {
    this.neuralNetworkModel = neuralNetworkModel;
  }

  /**
   * @since 4.3
   */
  @Nullable
  public File getFasttextModel() {
    return fasttextModel;
  }

  /**
   * @since 4.3
   */
  public void setFasttextModel(File fasttextModel) {
    this.fasttextModel = fasttextModel;
  }

  /**
   * @since 4.3
   */
  @Nullable
  public File getFasttextBinary() {
    return fasttextBinary;
  }

  /**
   * @since 4.3
   */
  public void setFasttextBinary(File fasttextBinary) {
    this.fasttextBinary = fasttextBinary;
  }


  /**
   * @return an additional rule file name to use
   * @since 2.9
   */
  @Nullable
  public String getRuleFile() {
    return ruleFile;
  }

  /**
   * @param ruleFile absolute file name of the XML rule file
   * @since 2.9
   */
  public void setRuleFile(String ruleFile) {
    this.ruleFile = ruleFile;
  }

  @Nullable
  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  @Nullable
  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public List<String> getDisabledRules() {
    return disabledRules;
  }

  public void setDisabledRules(List<String> disabledRules) {
    this.disabledRules = Objects.requireNonNull(disabledRules);
  }

  public List<String> getEnabledRules() {
    return enabledRules;
  }

  public void setEnabledRules(List<String> enabledRules) {
    this.enabledRules = Objects.requireNonNull(enabledRules);
  }

  /** @since 3.3 */
  public void setEnabledCategories(List<String> categoryIds) {
    for (String categoryId : categoryIds) {
      enabledCategories.add(new CategoryId(categoryId));
    }
  }

  /** @since 3.3 */
  public Set<CategoryId> getEnabledCategories() {
    return Collections.unmodifiableSet(enabledCategories);
  }

  /** @since 3.3 */
  public void setDisabledCategories(List<String> categoryIds) {
    for (String categoryId : categoryIds) {
      disabledCategories.add(new CategoryId(categoryId));
    }
  }

  /** @since 3.3 */
  public Set<CategoryId> getDisabledCategories() {
    return Collections.unmodifiableSet(disabledCategories);
  }

  /** @since 2.9 */
  public boolean isUseEnabledOnly() {
    return useEnabledOnly;
  }

  /** @since 2.7 */
  public void setUseEnabledOnly() {
    this.useEnabledOnly = true;
  }

  public boolean isXmlFiltering() {
    return xmlFiltering;
  }

  public void setXmlFiltering(boolean xmlFiltering) {
    this.xmlFiltering = xmlFiltering;
  }

  public boolean isPrintLanguages() {
    return printLanguages;
  }

  public void setPrintLanguages(boolean printLanguages) {
    this.printLanguages = printLanguages;
  }

  /**
   * @param file False friends filename
   * @since 2.9
   */
  public void setFalseFriendFile(String file) {
    falseFriendFile = file;
  }

  /**
   * @return False friends file name or {@code null}
   * @since 2.9
   */
  @Nullable
  public String getFalseFriendFile() {
    return falseFriendFile;
  }

  /**
   * @return the bitext rule file name or {@code null}
   * @since 2.9
   */
  @Nullable
  public String getBitextRuleFile() {
    return bitextRuleFile;
  }

  /**
   * @param bitextRuleFile the bitext rule file name
   * @since 2.9
   */
  public void setBitextRuleFile(String bitextRuleFile) {
    this.bitextRuleFile = bitextRuleFile;
  }

}

