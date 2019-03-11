/* LanguageTool, a natural language style checker
 * Copyright (C) 2018 Daniel Naber (http://www.danielnaber.de)
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
package org.languagetool;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * User-specific configuration. So far, this contains a list of words and a settings map.
 * @since 4.2
 */
@Experimental
public class UserConfig {

  private final List<String> userSpecificSpellerWords;
  private final int maxSpellingSuggestions;
  private final String userDictName;
  private final Map<String, Integer> configurableRuleValues = new HashMap<>();
  private final LinguServices linguServices;

  // indifferent for comparing UserConfigs (e.g. in PipelinePool)
  // provided to rules only for A/B tests ->
  private long textSessionId;
  private String abTest;

  public UserConfig() {
    this(new ArrayList<>(), new HashMap<>());
  }

  public UserConfig(List<String> userSpecificSpellerWords) {
    this(userSpecificSpellerWords, new HashMap<>());
  }

  public UserConfig(Map<String, Integer> ruleValues) {
    this(new ArrayList<>(), Objects.requireNonNull(ruleValues));
  }

  public UserConfig(Map<String, Integer> ruleValues, LinguServices linguServices) {
    this(new ArrayList<>(), Objects.requireNonNull(ruleValues), 0, null, linguServices);
  }

  public UserConfig(List<String> userSpecificSpellerWords, Map<String, Integer> ruleValues) {
    this(userSpecificSpellerWords, ruleValues, 0);
  }

  public UserConfig(List<String> userSpecificSpellerWords, Map<String, Integer> ruleValues, int maxSpellingSuggestions) {
    this(userSpecificSpellerWords, ruleValues, maxSpellingSuggestions, null, null);
  }
  
  public UserConfig(List<String> userSpecificSpellerWords, Map<String, Integer> ruleValues,
                    int maxSpellingSuggestions, String userDictName,
                    LinguServices linguServices) {
    this.userSpecificSpellerWords = Objects.requireNonNull(userSpecificSpellerWords);
    for (Map.Entry<String, Integer> entry : ruleValues.entrySet()) {
      this.configurableRuleValues.put(entry.getKey(), entry.getValue());
    }
    this.maxSpellingSuggestions = maxSpellingSuggestions;
    this.userDictName = userDictName == null ? "default" : userDictName;
    this.linguServices = linguServices;
  }

  public List<String> getAcceptedWords() {
    return userSpecificSpellerWords;
  }

  public int getMaxSpellingSuggestions() {
    return maxSpellingSuggestions;
  }

  public Map<String, Integer> getConfigValues() {
    return configurableRuleValues;
  }
  
  public void insertConfigValues(Map<String, Integer>  ruleValues) {
    for (Map.Entry<String, Integer> entry : ruleValues.entrySet()) {
      this.configurableRuleValues.put(entry.getKey(), entry.getValue());
    }
  }
  
  public int getConfigValueByID(String ruleID) {
    if (configurableRuleValues.containsKey(ruleID)) {
      return configurableRuleValues.get(ruleID);
    }
    return -1;
  }
  
  public boolean hasLinguServices() {
    return (linguServices != null ? true : false);
  }
  
  public LinguServices getLinguServices() {
    return linguServices;
  }

  /**
   * @since 4.4
   */
  public String getUserDictName() {
    return userDictName;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserConfig other = (UserConfig) o;
    // optimization: equals on userSpecificSpellerWords can be expensive with huge dictionaries
    // -> we use user id & dictionary names
    return new EqualsBuilder()
      .append(maxSpellingSuggestions, other.maxSpellingSuggestions)
      .append(configurableRuleValues, other.configurableRuleValues)
      .append(userDictName, other.userDictName)
      .append(userSpecificSpellerWords, other.userSpecificSpellerWords)
      // omitting these distorts A/B tests, as UserConfig is cached by the pipeline pool
      // -> (cached) textSessionId on server may say group A, but ID on client (relevant for saved correction) says B
      // only group must match; keeps hit rate of pipeline cache up
      .append(abTest, other.abTest)
      .append(textSessionId % 2, other.textSessionId % 2)
      .isEquals();
  }

  @Override
  public int hashCode() {
    // not calculating userSpecificSpellerWords.hashCode(), can be expensive; premiumId + userDictName is close enough
    return new HashCodeBuilder(3, 11)
      .append(maxSpellingSuggestions)
      .append(userDictName)
      .append(configurableRuleValues)
      // skipping abTest and textSessionId on purpose - not relevant for caching
      .toHashCode();
  }

  public void setTextSessionId(Long textSessionId) {
    this.textSessionId = textSessionId;
  }

  public Long getTextSessionId() {
    return textSessionId;
  }

  public String getAbTest() {
    return abTest;
  }

  public void setAbTest(String abTest) {
    this.abTest = abTest;
  }
}
