/* LanguageTool, a natural language style checker
 * Copyright (C) 2016 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool.remote;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Builder for a {@link CheckConfiguration}.
 * @since 3.4
 */
public class CheckConfigurationBuilder {

  private final String langCode;
  
  private String motherTongueLangCode;
  private boolean autoDetectLanguage;
  private boolean enabledOnly;
  private List<String> enabledRuleIds = new ArrayList<>();
  private List<String> disabledRuleIds = new ArrayList<>();

  /**
   * @param langCode a language code like {@code en} or {@code en-US}
   */
  public CheckConfigurationBuilder(String langCode) {
    this.langCode = Objects.requireNonNull(langCode);
  }

  /**
   * A configuration that causes the server to automatically detected the text language.
   * Note that this requires at least a few sentences of text to work reliably.
   */
  public CheckConfigurationBuilder() {
    this.langCode = null;
    this.autoDetectLanguage = true;
  }

  public CheckConfiguration build() {
    if (enabledOnly && enabledRuleIds.isEmpty()) {
      throw new IllegalStateException("You cannot use 'enabledOnly' when you haven't set rule ids to be enabled");
    }
    return new CheckConfiguration(langCode, motherTongueLangCode, autoDetectLanguage, enabledRuleIds, enabledOnly, disabledRuleIds);
  }

  public CheckConfigurationBuilder setMotherTongueLangCode(String motherTongueLangCode) {
    this.motherTongueLangCode = motherTongueLangCode;
    return this;
  }

  public CheckConfigurationBuilder enabledRuleIds(List<String> ruleIds) {
    this.enabledRuleIds = Objects.requireNonNull(ruleIds);
    return this;
  }

  public CheckConfigurationBuilder enabledRuleIds(String... ruleIds) {
    return enabledRuleIds(Arrays.asList(ruleIds));
  }

  public CheckConfigurationBuilder enabledOnly() {
    this.enabledOnly = true;
    return this;
  }

  public CheckConfigurationBuilder disabledRuleIds(List<String> ruleIds) {
    this.disabledRuleIds = Objects.requireNonNull(ruleIds);
    return this;
  }
  
  public CheckConfigurationBuilder disabledRuleIds(String... ruleIds) {
    return disabledRuleIds(Arrays.asList(ruleIds));
  }
  
}
