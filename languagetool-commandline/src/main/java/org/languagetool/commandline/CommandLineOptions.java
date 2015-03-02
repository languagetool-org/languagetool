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

import java.io.File;

/**
 * Options that can be set via command line arguments.
 */
public class CommandLineOptions {

  private boolean printUsage = false;
  private boolean printVersion = false;
  private boolean printLanguages = false;
  private boolean verbose = false;
  private boolean recursive = false;
  private boolean taggerOnly = false;
  private boolean singleLineBreakMarksParagraph = false;
  private boolean apiFormat = false;
  private boolean listUnknown = false;
  private boolean applySuggestions = false;
  private boolean profile = false;
  private boolean bitext = false;
  private boolean autoDetect = false;
  private boolean xmlFiltering = false;
  private Language language = null;
  private Language motherTongue = null;
  private File languageModel = null;
  private String encoding = null;
  private String filename = null;
  private String[] disabledRules = new String[0];
  private String[] enabledRules = new String[0];
  private boolean useEnabledOnly = false;
  private String ruleFile = null;

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

  public boolean isApiFormat() {
    return apiFormat;
  }

  public void setApiFormat(boolean apiFormat) {
    this.apiFormat = apiFormat;
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

  public String getEncoding() {
    return encoding;
  }

  public void setEncoding(String encoding) {
    this.encoding = encoding;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

  public String[] getDisabledRules() {
    return disabledRules;
  }

  public void setDisabledRules(String[] disabledRules) {
    this.disabledRules = disabledRules;
  }

  public String[] getEnabledRules() {
    return enabledRules;
  }

  public void setEnabledRules(String[] enabledRules) {
    this.enabledRules = enabledRules;
  }

  /** @since 2.7 */
  public boolean getUseEnabledOnly() {
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
}
