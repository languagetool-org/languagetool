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
  private final Map<String, Integer> configurableRuleValues = new HashMap<>();
  private final LinguServices linguServices;

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
    this(new ArrayList<>(), Objects.requireNonNull(ruleValues), 0, linguServices);
  }

  public UserConfig(List<String> userSpecificSpellerWords, Map<String, Integer> ruleValues) {
    this(userSpecificSpellerWords, ruleValues, 0);
  }

  public UserConfig(List<String> userSpecificSpellerWords, Map<String, Integer> ruleValues, int maxSpellingSuggestions) {
    this(userSpecificSpellerWords, ruleValues, maxSpellingSuggestions, null);
  }
  
  public UserConfig(List<String> userSpecificSpellerWords, Map<String, Integer> ruleValues, 
        int maxSpellingSuggestions, LinguServices linguServices) {
    this.userSpecificSpellerWords = Objects.requireNonNull(userSpecificSpellerWords);
    for (Map.Entry<String, Integer> entry : ruleValues.entrySet()) {
      this.configurableRuleValues.put(entry.getKey(), entry.getValue());
    }
    this.maxSpellingSuggestions = maxSpellingSuggestions;
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
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserConfig that = (UserConfig) o;
    if (maxSpellingSuggestions != that.maxSpellingSuggestions) return false;
    if (!userSpecificSpellerWords.equals(that.userSpecificSpellerWords)) return false;
    return configurableRuleValues.equals(that.configurableRuleValues);
  }

  @Override
  public int hashCode() {
    int result = userSpecificSpellerWords.hashCode();
    result = 31 * result + maxSpellingSuggestions;
    result = 31 * result + configurableRuleValues.hashCode();
    return result;
  }
}
