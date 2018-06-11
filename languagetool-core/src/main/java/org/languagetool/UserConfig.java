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
 * User-specific configuration. So far, this contains only a list of words.
 * @since 4.2
 */
@Experimental
public class UserConfig {

  private final List<String> userSpecificSpellerWords;
  private final Map<String, Integer> configurableRuleValues = new HashMap<>();

  public UserConfig() {
    userSpecificSpellerWords = new ArrayList<String>();
  }

  public UserConfig(List<String> userSpecificSpellerWords) {
    this.userSpecificSpellerWords = Objects.requireNonNull(userSpecificSpellerWords);
  }

  public UserConfig(Map<String, Integer> ruleValues) {
    for (Map.Entry<String, Integer> entry : ruleValues.entrySet()) {
      this.configurableRuleValues.put(entry.getKey(), entry.getValue());
    }
    userSpecificSpellerWords = new ArrayList<String>();
  }

  public UserConfig(List<String> userSpecificSpellerWords, Map<String, Integer> ruleValues) {
    this.userSpecificSpellerWords = Objects.requireNonNull(userSpecificSpellerWords);
    for (Map.Entry<String, Integer> entry : ruleValues.entrySet()) {
      this.configurableRuleValues.put(entry.getKey(), entry.getValue());
    }
  }

  public List<String> getAcceptedWords() {
    return userSpecificSpellerWords;
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
    if(configurableRuleValues.containsKey(ruleID)) {
      return configurableRuleValues.get(ruleID);
    }
    return -1;
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    UserConfig that = (UserConfig) o;
    return userSpecificSpellerWords.equals(that.userSpecificSpellerWords);
  }

  @Override
  public int hashCode() {
    return userSpecificSpellerWords.hashCode();
  }
  
}
