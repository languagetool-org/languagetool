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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

/**
 * Configuration for checking a text with {@link RemoteLanguageTool}.
 * Use {@link CheckConfigurationBuilder} to create a configuration.
 * @since 3.4
 */
public class CheckConfiguration {

  private final String langCode;
  private final String motherTongueLangCode;
  private final boolean guessLanguage;
  private final List<String> enabledRuleIds;
  private final boolean enabledOnly;
  private final List<String> disabledRuleIds;
  private final String mode;
  private final String level;
  private final List<String> ruleValues;
  @Nullable
  private final String textSessionID;
  @Nullable
  private final String username;
  @Nullable
  private final String apiKey;
  
  CheckConfiguration(String langCode, String motherTongueLangCode, boolean guessLanguage, List<String> enabledRuleIds, boolean enabledOnly, 
                     List<String> disabledRuleIds, String mode, String level, List<String> ruleValues, String textSessionID,
                     String username, String apiKey) {
    if (langCode == null && !guessLanguage) {
      throw new IllegalArgumentException("No language was set but language guessing was not activated either");
    }
    if (langCode != null && guessLanguage) {
      throw new IllegalArgumentException("Language was set but language guessing was also activated");
    }
    this.langCode = langCode;
    this.motherTongueLangCode = motherTongueLangCode;
    this.guessLanguage = guessLanguage;
    this.enabledRuleIds = Objects.requireNonNull(enabledRuleIds);
    this.enabledOnly = enabledOnly;
    this.disabledRuleIds = Objects.requireNonNull(disabledRuleIds);
    this.mode = mode;
    this.level = level;
    this.ruleValues = Objects.requireNonNull(ruleValues);
    this.textSessionID = textSessionID;
    this.username = username;
    this.apiKey = apiKey;
  }

  public Optional<String> getLangCode() {
    return Optional.ofNullable(langCode);
  }

  public String getMotherTongueLangCode() {
    return motherTongueLangCode;
  }

  public boolean guessLanguage() {
    return guessLanguage;
  }

  public List<String> getEnabledRuleIds() {
    return enabledRuleIds;
  }

  public boolean enabledOnly() {
    return enabledOnly;
  }

  public List<String> getDisabledRuleIds() {
    return disabledRuleIds;
  }

  public String getMode() {
    return mode;
  }

  public String getLevel() {
    return level;
  }

  public List<String> getRuleValues() {
    return ruleValues;
  }

  @Nullable
  public String getTextSessionID() {
    return textSessionID;
  }

  @Nullable
  public String getUsername() {
    return username;
  }

  @Nullable
  public String getAPIKey() {
    return apiKey;
  }
}
