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
package org.languagetool;

import java.util.ArrayList;
import java.util.List;

/**
 * Store integer values to configuration of single rules in a List
 * and returns the values by rule-ID
 * @since 4.2
 * @author Fred Kruse
 */
public class ConfigValues {

  private final List<ValuePair> values;

  private class ValuePair {
    String ruleId;
    int value;
    
    ValuePair(String id, int v) {
      ruleId = id;
      value = v;
    }
  }
  
  /**
   * create a List with 0 entries
   * @since 4.2
   */
  public ConfigValues() {
    values = new ArrayList<>();
  }
  
  /**
   * add a pair of rule-ID and value
   * @since 4.2
   */
  public void addValue(String ruleId, int value) {
    for (ValuePair v : values) {
      if (ruleId.equals(v.ruleId)) {
        v.value = value;
        return;
      }
    }
    values.add( new ValuePair(ruleId, value) ); 
  }
  
  /**
   * Get a list of ValuePairs
   * @since 4.2
   */
  private List<ValuePair> getValuePairs() {
    return values; 
  }
  
  /**
   * Inserts the List of ConfigValues, overriding the existing list.
   * @since 4.2
   */
  void insertList(ConfigValues v) {
    values.clear();
    values.addAll(v.getValuePairs());
  }
  
  /**
   * returns the value for a rule-ID
   * returns -1, if the rule-ID wasn't found
   * @since 4.2
   */
  int getValueById(String id) {
    for (ValuePair v : values) {
      if (id.startsWith(v.ruleId)) {
        return v.value;
      }
    }
    return -1;
  }

}